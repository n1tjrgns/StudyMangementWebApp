package com.jpa.studywebapp.infra.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app") //app을 공통값으로
public class AppProperties {

    private String host;
}
