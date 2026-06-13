package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
public class AutenticacaoService implements UserDetailsService {

    @Autowired
    private UsuarioRepository usuarioRepository;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        String emailNormalizado = username != null ? username.toLowerCase().trim() : null;

        Usuario usuario = usuarioRepository.findByEmailIgnoreCase(emailNormalizado)
                .orElseThrow(() -> new UsernameNotFoundException("Usuário não encontrado: " + username));

        if (usuario.getRole() == UsuarioRole.ROLE_ADMIN) {
            if (!usuario.isActive()) {
                usuario.setActive(true);
            }
            if (!usuario.isEmailConfirmado()) {
                usuario.setEmailConfirmado(true);
            }
            usuarioRepository.save(usuario);
            return usuario;
        }

        if (!usuario.isActive() && !usuario.isEmailConfirmado()) {
            throw new UsernameNotFoundException("Conta não ativada. Verifique seu e-mail para confirmar o cadastro.");
        }

        if (!usuario.isActive()) {
            throw new UsernameNotFoundException("Usuário inativo. Entre em contato com o suporte.");
        }

        if (!usuario.isEmailConfirmado()) {
            throw new UsernameNotFoundException("E-mail não confirmado. Verifique sua caixa de entrada.");
        }

        return usuario;
    }
}