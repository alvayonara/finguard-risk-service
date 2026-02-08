package com.alvayonara.finguardriskservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@SpringBootApplication
public class FinguardRiskServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(FinguardRiskServiceApplication.class, args);
    }

}
