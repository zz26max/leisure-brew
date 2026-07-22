package com.sky.config;

import com.sky.properties.AliOssProperties;
import com.sky.utils.AliOssUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
@Slf4j
public class Ossconfiguration {

    private final AliOssProperties aliOssProperties;

    public Ossconfiguration(AliOssProperties aliOssProperties) {
        this.aliOssProperties = aliOssProperties;
    }

    @Bean
    @ConditionalOnMissingBean
    public AliOssUtil aliOssUtil() {
        log.info("初始化对象存储客户端，endpoint={}，bucket={}",
                aliOssProperties.getEndpoint(), aliOssProperties.getBucketName());
        return new AliOssUtil(aliOssProperties.getEndpoint(),
                aliOssProperties.getAccessKeyId(),
                aliOssProperties.getAccessKeySecret(),
                aliOssProperties.getBucketName());
    }
}
