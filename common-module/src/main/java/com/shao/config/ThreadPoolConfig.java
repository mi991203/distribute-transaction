package com.shao.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.task.TaskExecutor;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor;

/**
 * @author : SH35856
 * @Description: TODO
 * @date: 2022/3/31 16:11
 */
@Configuration
@EnableAsync
public class ThreadPoolConfig {
    /**
     * 每秒需要多少个线程处理,核心线程
     */
    private int corePoolSize = 3;

    /**
     * 最大处理线程数
     */
    private int maxPoolSize = 10;

    /**
     * 缓存队列
     */
    private int queueCapacity = 10;

    /**
     * 允许的空闲时间,
     * 默认为60
     */
    private int keepAlive = 100;

    @Bean
    public TaskExecutor taskExecutor() {
        ThreadPoolTaskExecutor executor = new ThreadPoolTaskExecutor();
        executor.setCorePoolSize(corePoolSize);
        executor.setMaxPoolSize(maxPoolSize);
        executor.setQueueCapacity(queueCapacity);
        executor.setKeepAliveSeconds(keepAlive);
        return executor;
    }
}
