package com.order.config;

import org.springframework.cloud.client.loadbalancer.LoadBalanced;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
public class WebClientConfig {

    //When ever bean annotation annotated with the method
    // then the bean was created at the name of method name.
    @Bean
    public WebClient webClient() {
        return WebClient.builder()
                .build();
    }

    //To manage the client side load-balance we have to create the webclient builder with @loadbalance annotation
    @Bean
    @LoadBalanced
    public WebClient.Builder webClientBuilder() {
        return WebClient.builder();
    }
}
