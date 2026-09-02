package com.sakshi.brainforgeai.ai;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AiConfig {

    @Bean
    public RestTemplate aiRestTemplate() {
        return new RestTemplate();
    }
}
