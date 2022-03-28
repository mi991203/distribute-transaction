package com.shao.controller;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.shao.service.OrderService;
import com.shao.vo.OrderCreateVO;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@Slf4j
@RestController
@RequestMapping("api/v1")
public class OrderController {

    @Autowired
    OrderService orderService;

    @PostMapping("/orders")
    public void createOrder(@RequestBody OrderCreateVO orderCreateDTO) throws JsonProcessingException {
        log.info("createOrder:orderCreateDTO:{}",orderCreateDTO);
        orderService.createOrder(orderCreateDTO);
    }
}
