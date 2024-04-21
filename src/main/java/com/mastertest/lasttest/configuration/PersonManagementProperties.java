package com.mastertest.lasttest.configuration;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;


@Getter
@Setter
@Configuration
@ConfigurationProperties(prefix = "person-management")
public class PersonManagementProperties {

    private int defaultPageSize;
    private int batchSize;
    private int queueCapacity;
    private String threadNamePrefix;
}
