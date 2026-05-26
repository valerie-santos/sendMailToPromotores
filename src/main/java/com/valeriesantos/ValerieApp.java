package com.valeriesantos;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync
public class ValerieApp {
    public static void main(String[] args) {
        SpringApplication.run(ValerieApp.class, args);
    }
}
