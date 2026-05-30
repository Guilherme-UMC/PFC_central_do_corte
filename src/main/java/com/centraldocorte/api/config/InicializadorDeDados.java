package com.centraldocorte.api.config;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InicializadorDeDados implements CommandLineRunner {
    @Value("${admin.email}")
    private String adminEmail;

    @Value("${admin.password}")
    private String adminPassword;

    @Value("${admin.telefone:11999999999}")
    private String adminTelefone;

    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        criarAdministradorPadrao();
    }

    private void criarAdministradorPadrao(){
        if (jaExisteAdministradorAtivo()) {
            return;
        }
        Usuario admin = montarAdministradorPadrao();
        usuarioRepository.save(admin);
        log.info("Admin criado com sucesso!");
    }

    private boolean jaExisteAdministradorAtivo(){
        return usuarioRepository.existsByRoleAndActiveTrue(UsuarioRole.ROLE_ADMIN);
    }

    private Usuario montarAdministradorPadrao() {
        Usuario admin = new Usuario();
        admin.setName("Administrador");
        admin.setEmail(adminEmail);
        admin.setPassword(passwordEncoder.encode(adminPassword));
        admin.setTelefone(adminTelefone);
        admin.setRole(UsuarioRole.ROLE_ADMIN);
        admin.setActive(true);
        return admin;
    }
}
