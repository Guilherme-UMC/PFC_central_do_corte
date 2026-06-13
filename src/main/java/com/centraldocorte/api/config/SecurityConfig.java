package com.centraldocorte.api.config;

import com.centraldocorte.api.infra.FiltroDeAutenticacaoJwt;
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
    private FiltroDeAutenticacaoJwt filtroDeAutenticacaoJwt;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        return http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(authorize -> authorize

                        .requestMatchers("/swagger-ui/**", "/swagger-ui.html", "/api-docs/**", "/v3/api-docs/**").permitAll()

                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()


                        .requestMatchers(HttpMethod.GET, "/users").hasAnyRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/users/*").hasAnyRole("ADMIN", "BARBEARIA_ADM", "CLIENTE", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.GET, "/users/role/*").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.GET, "/users/search").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.PUT, "/users/*").hasAnyRole("ADMIN", "BARBEARIA_ADM", "CLIENTE", "FUNCIONARIO")
                        .requestMatchers(HttpMethod.DELETE, "/users/*").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PATCH, "/users/*/toggle-status").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.POST, "/users/*/change-password").hasAnyRole("ADMIN", "BARBEARIA_ADM", "CLIENTE", "FUNCIONARIO")


                        .requestMatchers("/admin/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/barbearia/owner/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/admin/logs/**").hasRole("ADMIN")



                        .requestMatchers(HttpMethod.GET, "/barbearia/minhas").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.POST, "/barbearia/**").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.PUT, "/barbearia/**").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.DELETE, "/barbearia/**").hasAnyRole("ADMIN", "BARBEARIA_ADM")
                        .requestMatchers(HttpMethod.PATCH, "/barbearia/**").hasAnyRole("ADMIN", "BARBEARIA_ADM")



                        .requestMatchers("/funcionario/**").hasAnyRole("FUNCIONARIO", "BARBEARIA_ADM", "ADMIN")


                        .requestMatchers("/cliente/**").hasAnyRole("CLIENTE", "ADMIN")


                        .requestMatchers(HttpMethod.GET, "/barbearia/buscar-por-cep/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/barbearia/buscar-cep/**").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        .requestMatchers("/api/produtos/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/barbearia/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/public/**").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicos/barbearia/{barbeariaId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/funcionarios/barbearia/{barbeariaId}").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/barbearias/{id}/horarios").permitAll()

                        .requestMatchers(HttpMethod.GET, "/api/servicos/categorias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicos/buscar-barbearias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicos/barbearia/{barbeariaId}/categorias").permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/servicos/barbearia/{barbeariaId}/categoria/{categoria}").permitAll()

                        .anyRequest().authenticated()
                )
                .addFilterBefore(filtroDeAutenticacaoJwt, UsernamePasswordAuthenticationFilter.class)
                .build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:5173",
                "http://localhost:3000",
                "http://127.0.0.1:5173",
                "http://127.0.0.1:3000"
        ));


        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "OPTIONS", "PATCH"
        ));


        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));


        configuration.setExposedHeaders(Arrays.asList(
                "Authorization"
        ));


        configuration.setAllowCredentials(true);


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