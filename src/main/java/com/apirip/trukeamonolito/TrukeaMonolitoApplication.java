package com.apirip.trukeamonolito;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
public class TrukeaMonolitoApplication {

    public static void main(String[] args) {
        SpringApplication.run(TrukeaMonolitoApplication.class, args);
    }

}
