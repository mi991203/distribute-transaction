package com.shao.task;

import com.shao.po.TransactionMsgPO;
import com.shao.service.TransactionMsgService;
import lombok.extern.slf4j.Slf4j;
import org.redisson.api.RLock;
import org.redisson.api.RedissonClient;
import org.springframework.amqp.AmqpException;
import org.springframework.amqp.core.Message;
import org.springframework.amqp.core.MessageProperties;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.scheduling.annotation.Scheduled;

import java.util.List;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/31 16:01
 */
@EnableScheduling
@Configuration
@Slf4j
public class ResendTask {
    @Autowired
    private TransactionMsgService transactionMsgService;

    @Autowired
    private RabbitTemplate rabbitTemplate;

    @Value("#{new Integer('${rabbitmq.resendTimes}')}")
    private Integer resendTimes;

    @Autowired
    private RedissonClient redisson;

    @Scheduled(cron = "${rabbitmq.msg-cron}")
    public void resendMsg() {
        log.info("触发重发消息任务");
        RLock lock = redisson.getLock("retry-msg-lock");
        // 需要分布式锁去定时触发
        try {
            if (lock.tryLock()) {
                log.info("当前服务获取锁成功");
                final List<TransactionMsgPO> readyMsgList = transactionMsgService.listReadyMessages();
                for (TransactionMsgPO transactionMsgPO : readyMsgList) {
                    log.info("准备重发消息，message={}", transactionMsgPO);
                    // 判断是否超过重发次数
                    if (transactionMsgPO.getSequence() > resendTimes) {
                        log.info("消息重发失败超过阈值={}，message={}", resendTimes, transactionMsgPO);
                        transactionMsgService.messageDead(transactionMsgPO.getId());
                        continue;
                    }
                    // 设置消息属性
                    final MessageProperties messageProperties = new MessageProperties();
                    messageProperties.setContentType("application/json");
                    final Message message = new Message(transactionMsgPO.getPayload().getBytes(), messageProperties);
                    message.getMessageProperties().setMessageId(transactionMsgPO.getId());
                    rabbitTemplate.convertAndSend(transactionMsgPO.getExchange(), transactionMsgPO.getRoutingKey(), message, new CorrelationData(transactionMsgPO.getId()));
                    transactionMsgService.messageResend(transactionMsgPO.getId());
                }
            } else {
                log.info("当前服务获取锁失败");
            }
        } catch (AmqpException e) {
            log.error(e.getMessage(), e);
        } finally {
            log.info("处理完毕，释放redis锁");
            lock.unlock();
        }
    }
}
