package com.ckt.api.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConfigurationProperties(prefix = "app")
public class AppConfig {
    private Jwt jwt = new Jwt();
    private int rateLimitPerSec = 10;

    public static class Jwt {
        private String secret = "change_me";
        private int ttlHours = 24;
        public String getSecret() { return secret; }
        public void setSecret(String secret) { this.secret = secret; }
        public int getTtlHours() { return ttlHours; }
        public void setTtlHours(int ttlHours) { this.ttlHours = ttlHours; }
    }

    public Jwt getJwt() { return jwt; }
    public void setJwt(Jwt jwt) { this.jwt = jwt; }
    public int getRateLimitPerSec() { return rateLimitPerSec; }
    public void setRateLimitPerSec(int rateLimitPerSec) { this.rateLimitPerSec = rateLimitPerSec; }
}
