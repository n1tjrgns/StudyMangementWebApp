package com.jpa.studywebapp.infra.config;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.security.servlet.PathRequest;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.builders.WebSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.rememberme.JdbcTokenRepositoryImpl;
import org.springframework.security.web.authentication.rememberme.PersistentTokenRepository;

import javax.sql.DataSource;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig extends WebSecurityConfigurerAdapter {

    private final UserDetailsService userDetailsService;
    private final DataSource dataSource;

    //configure 메소드를 override 함으로써 http 요청에 대한 커스텀을 자유자재로 할 수 있다.
    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.authorizeRequests()
                .mvcMatchers("/","login","/sign-up", "/check-email-token", "/login-by-email",
                        "/email-login", "/check-email-login","/login-link").permitAll()
                .mvcMatchers(HttpMethod.GET,"/profile/*").permitAll() //Get 만 허용
                .anyRequest().authenticated(); //나머지는 인증을 타야한다.

        //로그인 커스텀 페이지에 대한 권한 허용
        http.formLogin()
                .loginPage("/login")
                .permitAll();

        //로그아웃이 성공했을 때
        http.logout()
                .logoutSuccessUrl("/");

        //안전한 쿠키 설정, rememberMe라는 메소드
        http.rememberMe()
                .userDetailsService(userDetailsService)
                .tokenRepository(tokenRepository());
    }

    //JDBC 기반의 토큰 구현체
    //jdbcTokenRepository가 사용하는 엔티티정보가 있어야 하기 때문에 해당 스키마가 생성될 수 있도록 엔티티를 만들어줘야한다.
    @Bean
    public PersistentTokenRepository tokenRepository(){
        JdbcTokenRepositoryImpl jdbcTokenRepository = new JdbcTokenRepositoryImpl();
        jdbcTokenRepository.setDataSource(dataSource);
        return jdbcTokenRepository;
    }

    //img를 허용해주기 위한 설정
    @Override
    public void configure(WebSecurity web) throws Exception {
        web.ignoring()
                .mvcMatchers("/node_modules/**","/img/**")
                .requestMatchers(PathRequest.toStaticResources().atCommonLocations()); //static resources들에 대한 허용
    }
}
