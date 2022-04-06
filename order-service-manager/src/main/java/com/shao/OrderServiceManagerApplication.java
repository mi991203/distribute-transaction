package com.shao;


import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class OrderServiceManagerApplication {

    public static void main(String[] args) {
        SpringApplication.run(OrderServiceManagerApplication.class, args);
    }
}
