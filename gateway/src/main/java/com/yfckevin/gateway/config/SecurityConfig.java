package com.yfckevin.gateway.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.security.KeyPair;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.cert.Certificate;

@Configuration
public class SecurityConfig {
    private final JwtProperties jwtProperties;

    public SecurityConfig(JwtProperties jwtProperties) {
        this.jwtProperties = jwtProperties;
    }

    @Bean
    public KeyPair keyPair() throws Exception {
        // 載入 Keystore
        Resource keystoreResource = new org.springframework.core.io.ClassPathResource(jwtProperties.getLocation());
        try (InputStream is = keystoreResource.getInputStream()) {
            KeyStore keyStore = KeyStore.getInstance("JKS");
            keyStore.load(is, jwtProperties.getPassword().toCharArray());

            // 獲取私鑰
            PrivateKey privateKey = (PrivateKey) keyStore.getKey(jwtProperties.getAlias(), jwtProperties.getPassword().toCharArray());
            if (privateKey == null) {
                throw new RuntimeException("未找到指定 alias 的私鑰: " + jwtProperties.getAlias());
            }

            // 獲取公鑰
            Certificate cert = keyStore.getCertificate(jwtProperties.getAlias());
            if (cert == null) {
                throw new RuntimeException("未找到指定 alias 的證書: " + jwtProperties.getAlias());
            }
            PublicKey publicKey = cert.getPublicKey();

            return new KeyPair(publicKey, privateKey);
        }
    }

}
