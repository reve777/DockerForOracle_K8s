package com.portfolio.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class JacksonConfig {
    @Bean
    public com.fasterxml.jackson.databind.Module hibernate6Module() {
        return new com.fasterxml.jackson.datatype.hibernate6.Hibernate6Module();
    }
}
