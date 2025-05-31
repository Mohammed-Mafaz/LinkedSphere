package com.quad.linkedin.posts_service.config;

import org.apache.kafka.clients.admin.NewTopic;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.stereotype.Service;

@Configuration
public class KafkaTopicConfig {

    @Value("${kafka.topic.post-created}")
    private String PostCreatedTopic;

    @Value("${kafka.topic.post-liked}")
    private String PostLikedTopic;

    @Bean
    public NewTopic PostCreatedTopic(){
        return new NewTopic(PostCreatedTopic,3, (short) 1);
    }

    @Bean
    public NewTopic PostLikedTopic(){
        return new NewTopic(PostLikedTopic,3,(short) 1);
    }
}