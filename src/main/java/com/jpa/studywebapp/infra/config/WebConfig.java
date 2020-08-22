package com.jpa.studywebapp.infra.config;

import com.jpa.studywebapp.modules.notification.NotificationInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.StaticResourceLocation;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final NotificationInterceptor notificationInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        //staticresourcelocation이 각각의 배열이라 그것을 문자열로 변환해서 패턴에 넣어주자
        List<String> staticResourceLocation = Arrays.stream(StaticResourceLocation.values())
                .flatMap(StaticResourceLocation::getPatterns)
                .collect(Collectors.toList());
        staticResourceLocation.add("/node_modules/**");

        registry.addInterceptor(notificationInterceptor)
                .excludePathPatterns(staticResourceLocation);
    }
}
