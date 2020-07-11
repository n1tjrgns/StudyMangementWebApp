package com.jpa.studywebapp.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    //configure 메소드를 override 함으로써 http 요청에 대한 커스텀을 자유자재로 할 수 있다.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/","login","/sign-up","/check-email","/check-email-token",
                        "/email-login", "/check-email-login","/login-link").permitAll()
                .mvcMatchers(HttpMethod.GET,"/profile/*").permitAll() //Get 만 허용
                .anyRequest().authenticated(); //나머지는 인증을 타야한다.

    }
}
