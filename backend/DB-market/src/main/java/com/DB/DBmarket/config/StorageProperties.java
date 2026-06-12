package com.DB.DBmarket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "storage")
public class StorageProperties {
    private String provider = "local";
    private Local local = new Local();

    @Data
    public static class Local {
        private String baseDir = "./data/storage";
        private String publicUrlPrefix = "http://127.0.0.1:8080/storage";
    }
}
