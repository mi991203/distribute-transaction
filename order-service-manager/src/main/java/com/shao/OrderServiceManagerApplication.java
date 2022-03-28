package com.shao;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

@SpringBootApplication
public class OrderServiceManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceManagerApplication.class, args);
    }
}
