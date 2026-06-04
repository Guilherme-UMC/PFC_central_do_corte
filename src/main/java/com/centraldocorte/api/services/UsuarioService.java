package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.dto.UsuarioResponseDTO;
import com.centraldocorte.api.dto.UsuarioRequestDTO;
import com.centraldocorte.api.dto.UsuarioUpdateDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.EmailAlreadyExistsException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class UsuarioService {
    private final UsuarioRepository usuarioRepository;
    private final PasswordEncoder passwordEncoder;
    private final InativacaoService inativacaoService;

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarTodos(Pageable pageable) {
        return usuarioRepository.findAll(pageable)
                .map(this::converterParaResponseDTO);
    }

    @Transactional(readOnly = true)
    public UsuarioResponseDTO buscarPorId(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(Usuario::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        return converterParaResponseDTO(usuario);
    }

    @Transactional(readOnly = true)
    public Usuario buscarPorEmail(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> buscarPorRole(UsuarioRole role, Pageable pageable) {
        return usuarioRepository.findByRole(role, pageable)
                .map(this::converterParaResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> buscarPorNome(String nome, Pageable pageable) {
        return usuarioRepository.findByNameContainingIgnoreCase(nome, pageable)
                .map(this::converterParaResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarPorRoleEStatus(UsuarioRole role, boolean active, Pageable pageable) {
        return usuarioRepository.findByRoleAndActive(role, active, pageable)
                .map(this::converterParaResponseDTO);
    }

    @Transactional(readOnly = true)
    public Page<UsuarioResponseDTO> listarPorStatus(boolean active, Pageable pageable) {
        return usuarioRepository.findByActive(active, pageable)
                .map(this::converterParaResponseDTO);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarTodosFuncionarios() {
        return usuarioRepository.findByRoleAndActiveTrue(UsuarioRole.ROLE_FUNCIONARIO)
                .stream()
                .map(this::converterParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorIdIncluindoInativos(String id) {
        return usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
    }

    @Transactional(readOnly = true)
    public Usuario buscarUsuarioPorEmailIncluindoInativos(String email) {
        return usuarioRepository.findByEmail(email)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + email));
    }

    @Transactional(readOnly = true)
    public boolean existsById(String id) {
        return usuarioRepository.existsById(id);
    }

    @Transactional
    public void setActiveStatus(String id, boolean active) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));
        usuario.setActive(active);
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO criarUsuario(UsuarioRequestDTO request) {
        validarEmailDisponivel(request.getEmail());

        Usuario novoUsuario = montarUsuarioAPartirDoRequest(request);
        usuarioRepository.save(novoUsuario);

        return converterParaResponseDTO(novoUsuario);
    }

    @Transactional
    public UsuarioResponseDTO atualizarUsuario(String id, UsuarioUpdateDTO request) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(Usuario::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(usuario.getEmail())) {
            validarEmailDisponivel(request.getEmail());
        }

        atualizarCamposDoUsuario(usuario, request);
        usuario = usuarioRepository.save(usuario);

        return converterParaResponseDTO(usuario);
    }

    @Transactional
    public void desativarUsuario(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (usuario.isActive()) {
            throw new BusinessException(
                    "Não é possível remover um usuário ativo. " +
                            "Primeiro inative o usuário usando a opção 'Inativar' e depois tente novamente."
            );
        }

        if (usuario.getRole() == UsuarioRole.ROLE_ADMIN) {
            long totalAdminsAtivos = usuarioRepository.countByRoleAndActiveTrue(UsuarioRole.ROLE_ADMIN);
            if (totalAdminsAtivos <= 1) {
                throw new BusinessException("Não é possível remover o único administrador do sistema");
            }
        }

        usuario.setName("Usuário Removido");
        usuario.setEmail("removido_"+ UUID.randomUUID() + "@removido.com");
        usuario.setTelefone(null);

        usuarioRepository.save(usuario);
        log.info("Usuário {} foi removido permanentemente", usuario.getEmail());
    }

    @Transactional
    public void alternarStatusDoUsuario(String id) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado"));

        if (usuario.getEmail() != null && usuario.getEmail().startsWith("removido_")) {
            throw new BusinessException("Este usuário foi removido permanentemente e não pode ser reativado");
        }

        if (usuario.getRole() == UsuarioRole.ROLE_ADMIN) {
            long totalAdminsAtivos = usuarioRepository.countByRoleAndActiveTrue(UsuarioRole.ROLE_ADMIN);
            if (totalAdminsAtivos <= 1) {
                throw new BusinessException("Não é possível inativar o único administrador do sistema");
            }
        }

        if (usuario.isActive()) {
            inativacaoService.processarInativacao(usuario);
        }

        usuario.setActive(!usuario.isActive());
        usuarioRepository.save(usuario);

        log.info("Status do usuário {} alterado para: {}", usuario.getEmail(), usuario.isActive());
    }

    @Transactional
    public void alterarSenha(String id, String senhaAtual, String novaSenha) {
        Usuario usuario = usuarioRepository.findById(id)
                .filter(Usuario::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (!passwordEncoder.matches(senhaAtual, usuario.getPassword())) {
            throw new BusinessException("Senha atual incorreta");
        }

        usuario.setPassword(passwordEncoder.encode(novaSenha));
        usuarioRepository.save(usuario);
    }

    @Transactional
    public UsuarioResponseDTO alterarRoleUsuario(String id, UsuarioRole novaRole) {
        Usuario usuario = usuarioRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        usuario.setRole(novaRole);
        usuario = usuarioRepository.save(usuario);

        return converterParaResponseDTO(usuario);
    }

    public boolean existeAdministrador() {
        return usuarioRepository.existsByRoleAndActiveTrue(UsuarioRole.ROLE_ADMIN);
    }

    private void validarEmailDisponivel(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new EmailAlreadyExistsException(email);
        }
    }

    private Usuario montarUsuarioAPartirDoRequest(UsuarioRequestDTO request) {
        Usuario usuario = new Usuario();
        usuario.setName(request.getName());
        usuario.setEmail(request.getEmail());
        usuario.setPassword(passwordEncoder.encode(request.getPassword()));
        usuario.setTelefone(request.getTelefone());
        usuario.setRole(request.getRole() != null ? request.getRole() : UsuarioRole.ROLE_CLIENTE);
        usuario.setActive(true);
        return usuario;
    }

    private void atualizarCamposDoUsuario(Usuario usuario, UsuarioUpdateDTO  request) {
        if (request.getName() != null) usuario.setName(request.getName());
        if (request.getEmail() != null) usuario.setEmail(request.getEmail());
        if (request.getTelefone() != null) usuario.setTelefone(request.getTelefone());
    }

    public UsuarioResponseDTO converterParaResponseDTO(Usuario usuario) {
        return UsuarioResponseDTO.builder()
                .id(usuario.getId())
                .name(usuario.getName())
                .email(usuario.getEmail())
                .telefone(usuario.getTelefone())
                .role(usuario.getRole())
                .active(usuario.isActive())
                .criadoEm(usuario.getCriadoEm())
                .atualizadoEm(usuario.getAtualizadoEm())
                .build();
    }
}