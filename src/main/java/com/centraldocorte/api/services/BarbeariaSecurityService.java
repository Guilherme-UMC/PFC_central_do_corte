package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Component;

@Component("barbeariaSecurity")
@RequiredArgsConstructor
public class BarbeariaSecurityService {

    private final BarbeariaRepository barbeariaRepository;

    public boolean isOwner(String barbeariaId, Authentication authentication) {
        if (authentication == null || !authentication.isAuthenticated()) {
            return false;
        }

        Usuario usuario = (Usuario) authentication.getPrincipal();

        if (usuario.getAuthorities().stream()
                .anyMatch(auth -> auth.getAuthority().equals("ROLE_ADMIN"))) {
            return true;
        }

        return barbeariaRepository.findById(barbeariaId)
                .map(barbearia -> barbearia.getOwner() != null &&
                        barbearia.getOwner().getId().equals(usuario.getId()))
                .orElse(false);
    }
}