package com.example.aimusicdispatcher;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableAsync;

@SpringBootApplication
@EnableAsync // 启用Spring的异步方法执行功能
public class AiMusicDispatcherApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiMusicDispatcherApplication.class, args);
    }

}
