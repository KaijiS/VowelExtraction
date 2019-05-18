package com.kaijis.VowelExtraction.config;

import lombok.Data;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource(value = "classpath:oauth.properties")
@Data
public class OAuthConfig {

    /*
    *
    * githubのOAuth設定
    *
    * */

    @Value("${github.clientId}")
    private String githubClientId;

    @Value("${github.clientSecret}")
    private String githubClientSecret;

    @Value("${github.callbackURL}")
    private String githubCallbackURL;



    /*
     *
     * Twitterなどの他の設定を以下にOAuth設定
     *
     * */
}
