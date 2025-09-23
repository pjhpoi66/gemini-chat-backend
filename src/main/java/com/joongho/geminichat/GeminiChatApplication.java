package com.joongho.geminichat;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

import java.time.LocalDateTime;

@SpringBootApplication
@Slf4j
@EnableAsync
public class GeminiChatApplication {

    public static void main(String[] args) {
        SpringApplication.run(GeminiChatApplication.class, args);

        log.info("=========================================================");
        log.info("=========================================================");
        log.info("Build Instance : {}", SpringApplication.class.getName());
        log.info("Build Time : {}", LocalDateTime.now());
        log.info("=========================================================");
    }

}
