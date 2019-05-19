package com.kaijis.VowelExtraction.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:front.properties")
@Data
public class WebFrontConfig {

    @Value("${redirect.onetimetokenUrl}")
    private String oneTimeTokenUrl;

}
