package com.centraldocorte.api.config;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.crypto.password.PasswordEncoder;

@Slf4j
@Configuration
@RequiredArgsConstructor
public class InicializadorDeDados implements CommandLineRunner {
    private static final String EMAIL_ADMIN_PADRAO = "admin@sistema.com";
    private static final String SENHA_ADMIN_PADRAO = "admin123";
    private static final String TELEFONE_ADMIN_PADRAO = "(11) 99999-9999";

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
        registrarCriacaoDoAdmin();
    }

    private boolean jaExisteAdministradorAtivo(){
        return usuarioRepository.existsByRoleAndActiveTrue(UsuarioRole.ROLE_ADMIN);
    }

    private Usuario montarAdministradorPadrao() {
        Usuario admin = new Usuario();
        admin.setName("Administrador");
        admin.setEmail(EMAIL_ADMIN_PADRAO);
        admin.setPassword(passwordEncoder.encode(SENHA_ADMIN_PADRAO));
        admin.setTelefone(TELEFONE_ADMIN_PADRAO);
        admin.setRole(UsuarioRole.ROLE_ADMIN);
        admin.setActive(true);
        return admin;
    }

    private void registrarCriacaoDoAdmin(){
        log.info("=".repeat(50));
        log.info("ADMIN PADRÃO CRIADO");
        log.info("Email: {}", EMAIL_ADMIN_PADRAO);
        log.info("Senha: {}", SENHA_ADMIN_PADRAO);
        log.info("=".repeat(50));
    }

}
