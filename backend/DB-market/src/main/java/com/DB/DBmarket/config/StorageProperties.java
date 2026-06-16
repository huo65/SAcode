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
        private String baseDir = "../../front/public/img/uploads";
        private String publicUrlPrefix = "/img/uploads";
    }
}
