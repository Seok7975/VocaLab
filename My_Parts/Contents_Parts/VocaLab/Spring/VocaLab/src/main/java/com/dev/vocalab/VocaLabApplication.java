package com.dev.vocalab;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.annotation.ComponentScan;

@SpringBootApplication
public class VocaLabApplication {

    public static void main(String[] args) {

        ConfigurableApplicationContext context = SpringApplication.run(VocaLabApplication.class, args);
        System.out.println("등록된 빈 확인:");
        System.out.println("CustomOAuth2UsersService 빈 존재 여부: " +
                (context.containsBean("customOAuth2UsersService") ? "있음" : "없음"));
    }

}
