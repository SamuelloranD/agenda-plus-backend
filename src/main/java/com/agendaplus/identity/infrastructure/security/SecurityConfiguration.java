package com.agendaplus.identity.infrastructure.security;

import com.agendaplus.identity.domain.repository.UsuarioRepository;

import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
public class SecurityConfiguration {
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http, JwtService tokens,
                                            UsuarioRepository usuarios, ObjectMapper json) throws Exception {
        return http.csrf(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .httpBasic(AbstractHttpConfigurer::disable)
                .logout(AbstractHttpConfigurer::disable)
                .requestCache(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(routes -> routes.requestMatchers("/auth/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(errors -> errors.authenticationEntryPoint((request, response, exception) -> {
                    response.setStatus(401);
                    response.setHeader("WWW-Authenticate", "Bearer");
                    response.setContentType("application/problem+json");
                    json.writeValue(response.getOutputStream(), ProblemDetail.forStatusAndDetail(
                            HttpStatus.UNAUTHORIZED, "Autenticação necessária."));
                }))
                .addFilterBefore(new JwtAuthenticationFilter(tokens, usuarios), UsernamePasswordAuthenticationFilter.class)
                .build();
    }
}
