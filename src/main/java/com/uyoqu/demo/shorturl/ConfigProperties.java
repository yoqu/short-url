package com.uyoqu.demo.shorturl;

import lombok.Data;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@ConfigurationProperties(prefix = "config")
@Configuration
@Data
public class ConfigProperties implements InitializingBean {

    private String baseUrl;
    private Integer customerMaxLength;
    private Integer length;
    private String charsetData;

    @Override
    public void afterPropertiesSet() throws Exception {
        if (length > 32) {
            throw new RuntimeException("短连接的长度过长");
        }
    }
}
