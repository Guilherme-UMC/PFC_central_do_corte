package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes de Exceções - UsuarioService")
class UsuarioServiceExceptionTest {

    @Mock private UsuarioRepository usuarioRepository;
    @Mock private PasswordEncoder passwordEncoder;
    @Mock private InativacaoService inativacaoService;
    @Mock private LogSistemaService logSistemaService;
    @InjectMocks private UsuarioService usuarioService;

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar usuário inativo por ID")
    void deveLancarExcecaoAoBuscarUsuarioInativo() {
        Usuario usuarioInativo = new Usuario();
        usuarioInativo.setId("user-123");
        usuarioInativo.setActive(false);

        when(usuarioRepository.findById("user-123")).thenReturn(Optional.of(usuarioInativo));

        assertThatThrownBy(() -> usuarioService.buscarPorId("user-123"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário não encontrado: user-123");
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar desativar último admin")
    void deveLancarExcecaoAoDesativarUltimoAdmin() {
        Usuario admin = new Usuario();
        admin.setId("admin-001");
        admin.setRole(UsuarioRole.ROLE_ADMIN);
        admin.setActive(true);

        when(usuarioRepository.findById("admin-001")).thenReturn(Optional.of(admin));
        when(usuarioRepository.countByRoleAndActiveTrue(UsuarioRole.ROLE_ADMIN)).thenReturn(1L);

        assertThatThrownBy(() -> usuarioService.alternarStatusDoUsuario("admin-001", null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Não é possível inativar o único administrador do sistema");
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar desativar usuário já removido")
    void deveLancarExcecaoAoDesativarUsuarioRemovido() {
        Usuario usuarioRemovido = new Usuario();
        usuarioRemovido.setId("user-removed");
        usuarioRemovido.setEmail("removido_abc@removido.com");
        usuarioRemovido.setActive(false);
        usuarioRemovido.setName("Removido");

        when(usuarioRepository.findById("user-removed")).thenReturn(Optional.of(usuarioRemovido));

        assertThatThrownBy(() -> usuarioService.alternarStatusDoUsuario("user-removed", null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Este usuário foi removido permanentemente e não pode ser reativado");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar usuário que não existe")
    void deveLancarExcecaoAoBuscarUsuarioInexistente() {
        when(usuarioRepository.findById("user-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> usuarioService.buscarPorId("user-inexistente"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Usuário não encontrado: user-inexistente");
    }

    @Test
    @DisplayName("Deve lançar BusinessException ao tentar remover usuário ativo")
    void deveLancarExcecaoAoRemoverUsuarioAtivo() {
        Usuario usuarioAtivo = new Usuario();
        usuarioAtivo.setId("user-123");
        usuarioAtivo.setActive(true);

        when(usuarioRepository.findById("user-123")).thenReturn(Optional.of(usuarioAtivo));

        assertThatThrownBy(() -> usuarioService.desativarUsuario("user-123", null))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Não é possível remover um usuário ativo. Primeiro inative o usuário usando a opção 'Inativar' e depois tente novamente.");
    }
}