package com.DB.DBmarket.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;


@Data
@Component
@ConfigurationProperties(prefix = "alipay")
public class AliPayConfig {
    private String appId;
    private String appPrivateKey;
    private String alipayPublicKey;
    private String gatewayUrl = "https://openapi-sandbox.dl.alipaydev.com/gateway.do";
    private String notifyUrl;
    private String returnUrl;

    public boolean isConfigured() {
        return hasText(appId)
                && hasText(appPrivateKey)
                && hasText(alipayPublicKey)
                && !appId.contains("your_")
                && !appPrivateKey.contains("your_")
                && !alipayPublicKey.contains("your_");
    }

    private boolean hasText(String value) {
        return value != null && !value.trim().isEmpty();
    }
}
