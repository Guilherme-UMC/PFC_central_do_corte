package com.example.crud.config;

import com.example.crud.infra.SecurityFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Autowired
    private SecurityFilter securityFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource())) // HABILITA CORS
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize
                        //Swagger
                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()
                        // Rotas públicas
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/barbearia/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/**").permitAll()

                        //Rotas protegidas
                        //Users
                        .requestMatchers(HttpMethod.GET, "/users").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/*").hasAnyRole("ADMIN", "BARBEARIA_ADM", "CLIENTE", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/users/role/*").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.GET, "/users/search").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.PUT, "/users/*").hasAnyRole("ADMIN", "BARBEARIA_ADM", "CLIENTE", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.DELETE, "/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/*/toggle-status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users/*/change-password").hasAnyRole("ADMIN", "BARBEARIA_ADM", "CLIENTE", "FUNCIONARIO")

                        //Admin
                        .requestMatchers("/admin/**").hasRole("ADMIN")

                        //Barbearia
                        .requestMatchers(HttpMethod.POST, "/barbearia/**").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.PUT, "/barbearia/**").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.DELETE, "/barbearia/**").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.PATCH, "/barbearia/**").hasAnyRole("ADMIN", "BARBEARIA_ADM")

                        //Funcionario
                        .requestMatchers("/funcionario/**").hasAnyRole("FUNCIONARIO", "BARBEARIA_ADM", "ADMIN")

                        //Cliente
                        .requestMatchers("/cliente/**").hasAnyRole("CLIENTE", "ADMIN")

                        // Qualquer outra rota precisa de autenticação
                        .anyRequest().authenticated()
                )
                .addFilterBefore(securityFilter, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // URLs do seu frontend
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",  // Vite padrão
                "http://localhost:3000",   // React padrão
                "http://127.0.0.1:5173",
                "http://127.0.0.1:3000"
        ));

        // Métodos HTTP permitidos
        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));

        // Headers permitidos
        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        // Headers expostos
        configuration.setExposedHeaders(Arrays.asList(
                "Authorization"
        ));

        // Permitir credenciais (cookies, auth headers)
        configuration.setAllowCredentials(true);

        // Tempo de cache da configuração CORS (em segundos)
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
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