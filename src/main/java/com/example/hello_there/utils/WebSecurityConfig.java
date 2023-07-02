package com.example.hello_there.utils;

import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityConfigurerAdapter;

@Configuration
@EnableWebSecurity // SpringSecurity 사용을 위한 어노테이션, 기본적으로 CSRF 활성화
// SpringSecurity란, Spring기반의 애플리케이션의 보안(인증, 권한, 인가 등)을 담당하는 Spring 하위 프레임워크이다.
public class WebSecurityConfig extends WebSecurityConfigurerAdapter {

    @Override
    protected void configure(HttpSecurity http) throws Exception {
        http.csrf().disable(); // CSRF(사용자의 권한을 가지고 특정 동작을 수행하도록 유도하는 공격) 비활성화,
        // REST API 서버는 stateless하게 개발하기 때문에 사용자 정보를 Session에 저장 안함
        // jwt 토큰을 Cookie에 저장하지 않는다면, CSRF에 어느정도는 안전.
    }

    /**
     * 세션 기반 인증을 위해 사용하는 스프링 Ec2빈이므로 토큰 기반 인증 방식을 사용한다면 안 써도 됨.
     */
//    @Bean
//    public BCryptPasswordEncoder bCryptPasswordEncoder() {
//        // 단방향 해시 함수이므로 디코딩이 불가. 입력받은 패스워드를 인코딩해서 결과를 비교
//        return new BCryptPasswordEncoder();
//    }
}