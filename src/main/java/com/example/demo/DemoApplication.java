package com.example.demo;

import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

@SpringBootApplication
public class DemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(DemoApplication.class, args);
    }

    @Component
    public static class MessageListeners {
        @KafkaListener(topics = "first-topic", groupId = "first-group-id")
        public void firstListener(ConsumerRecord<Integer, String> record) {
            System.out.println("Received message: " + record);
        }

        @KafkaListener(topics = "second-topic", groupId = "second-group-id")
        public void secondListener(ConsumerRecord<Integer, String> record) {
            System.out.println("Received message: " + record);
        }
    }
}
