package com.redmind;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class RedMindApplication {

    public static void main(String[] args) {
        SpringApplication.run(RedMindApplication.class, args);
    }
}
