package org.uit.utimea.shared.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

@Configuration
public class NotificationRestClientConfig {

    @Value("${notification.service.url:http://localhost:8081}")
    private String notificationServiceUrl;

    @Bean
    public RestClient notificationRestClient() {
        return RestClient.builder()
                .baseUrl(notificationServiceUrl)
                .build();
    }
}
