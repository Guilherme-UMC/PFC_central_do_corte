package com.example.crud.config;

import com.example.crud.domain.user.User;
import com.example.crud.domain.user.UserRepository;
import com.example.crud.domain.user.UserRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class DataInitializer implements CommandLineRunner {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        createDefaultAdmin();
    }

    private void createDefaultAdmin(){
        if (!userRepository.existsByRoleAndActiveTrue(UserRole.ROLE_ADMIN)){
            User admin = new User();
            admin.setName("Administrador");
            admin.setEmail("admin@sistema.com");
            admin.setPassword(passwordEncoder.encode("admin123"));
            admin.setTelefone("(11) 99999-9999");
            admin.setRole(UserRole.ROLE_ADMIN);
            admin.setActive(true);

            userRepository.save(admin);

            log.info("=".repeat(50));
            log.info("ADMIN PADRÃO CRIADO!");
            log.info("Email: admin@sistema.com");
            log.info("Senha: admin123");
            log.info("=".repeat(50));
        }
    }

}
