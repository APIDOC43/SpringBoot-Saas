package com.hocs.server.saas_platform.common.config;


import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
@Configuration
@EnableWebSecurity
public class SecurityConfig {

	@Bean
	public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
		http
			// CSRF 설정 (필요 시)
			.csrf(AbstractHttpConfigurer::disable)
			// 요청에 대한 보안 설정
			.authorizeHttpRequests(authorize -> authorize
				// '/hello' 엔드포인트는 인증 없이 접근 가능
				.requestMatchers("/**","/hello", "/webhook", "/api/v1/oas","/demo/**").permitAll()
				// 그 외의 엔드포인트는 인증 필요
				.anyRequest().authenticated()
			).oauth2Login(oauth2Login -> oauth2Login
				.loginPage("/oauth2/authorization/github")
			);

		return http.build();
	}
}