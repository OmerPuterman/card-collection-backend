package com.cardcollection;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class CardCollectionApplication {

    public static void main(String[] args) {
        SpringApplication.run(CardCollectionApplication.class, args);
        System.out.println("🚀 Card Collection API is running on http://localhost:8080");
    }
}