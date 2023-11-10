package com.valueconnect;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class ValueConnectApp {
    public static void main(String[] args) {
        SpringApplication app = new SpringApplication(ValueConnectApp.class);
        app.run();
    }
}