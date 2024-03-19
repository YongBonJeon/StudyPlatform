package com.studyolle.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties("app") // properties 파일에서 app으로 시작하는 설정을 읽어옴
public class AppProperties {
    private String host;
}
