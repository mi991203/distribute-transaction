package com.shao.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.amqp.rabbit.support.CorrelationData;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Slf4j
@Component
public class PublisherConfirmConfig implements RabbitTemplate.ConfirmCallback {
    @Autowired
    private RabbitTemplate rabbitTemplate;

    @PostConstruct
    public void init() {
        rabbitTemplate.setConfirmCallback(this);
    }

    @Override
    public void confirm(CorrelationData correlationData, boolean ack, String cause) {
        if (ack) {
            log.info("发送端收到RabbitMQ的确认，消息已被RabbitMQ接收，correlationData={}", correlationData);
        } else {
            log.info("发送端收到RabbitMQ的确认，消息已被RabbitMQ拒收，correlationData={}, cause={}", correlationData, cause);
        }
    }
}
