package com.example.crud.config;

import com.example.crud.infra.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        // Rotas públicas
                        .requestMatchers("/auth/**").permitAll()

                        // ADMIN
                        .requestMatchers("/admin/**").hasRole("ADMIN")
//                        .requestMatchers("/users/**").hasRole("ADMIN")

                        // BARBEARIA_ADM
                        .requestMatchers("/barbearia/**").hasAnyRole("BARBEARIA_ADM", "ADMIN")

                        // FUNCIONÁRIO
                        .requestMatchers("/funcionario/**").hasAnyRole("FUNCIONARIO", "BARBEARIA_ADM", "ADMIN")

                        // CLIENTE
                        .requestMatchers("/cliente/**").hasAnyRole("CLIENTE", "ADMIN")

                        // público
                        .requestMatchers(HttpMethod.GET, "/public/**").permitAll()

                        // Qualquer outra rota precisa de autenticação
                        .anyRequest().permitAll()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}