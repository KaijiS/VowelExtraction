package com.kaijis.VowelExtraction.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class PropertiesConfiguration {

    @Bean
    public OAuthConfig oauthConfig() {
        return new OAuthConfig();
    }

    @Bean
    public WebFrontConfig webFrontConfig() {
        return new WebFrontConfig();
    }
}
