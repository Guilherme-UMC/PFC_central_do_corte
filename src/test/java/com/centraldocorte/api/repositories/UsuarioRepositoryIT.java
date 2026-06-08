package com.centraldocorte.api.repositories;

import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("Testes de Integração - UsuarioRepository")
class UsuarioRepositoryIT {

    @Autowired
    private UsuarioRepository usuarioRepository;

    private Usuario clienteAtivo;
    private Usuario funcionarioAtivo;
    private Usuario adminAtivo;
    private Usuario clienteInativo;

    @BeforeEach
    void setUp() {
        usuarioRepository.deleteAll();

        clienteAtivo = new Usuario();
        clienteAtivo.setName("Cliente Ativo");
        clienteAtivo.setEmail("cliente@teste.com");
        clienteAtivo.setPassword("senha123");
        clienteAtivo.setRole(UsuarioRole.ROLE_CLIENTE);
        clienteAtivo.setActive(true);
        clienteAtivo = usuarioRepository.save(clienteAtivo);

        funcionarioAtivo = new Usuario();
        funcionarioAtivo.setName("Funcionário Ativo");
        funcionarioAtivo.setEmail("funcionario@teste.com");
        funcionarioAtivo.setPassword("senha123");
        funcionarioAtivo.setRole(UsuarioRole.ROLE_FUNCIONARIO);
        funcionarioAtivo.setActive(true);
        funcionarioAtivo = usuarioRepository.save(funcionarioAtivo);

        adminAtivo = new Usuario();
        adminAtivo.setName("Admin Ativo");
        adminAtivo.setEmail("admin@teste.com");
        adminAtivo.setPassword("senha123");
        adminAtivo.setRole(UsuarioRole.ROLE_ADMIN);
        adminAtivo.setActive(true);
        adminAtivo = usuarioRepository.save(adminAtivo);

        clienteInativo = new Usuario();
        clienteInativo.setName("Cliente Inativo");
        clienteInativo.setEmail("inativo@teste.com");
        clienteInativo.setPassword("senha123");
        clienteInativo.setRole(UsuarioRole.ROLE_CLIENTE);
        clienteInativo.setActive(false);
        clienteInativo = usuarioRepository.save(clienteInativo);
    }

    @Test
    @DisplayName("Deve encontrar usuário por email quando existe")
    void deveEncontrarUsuarioPorEmail() {
        Optional<Usuario> encontrado = usuarioRepository.findByEmail("cliente@teste.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().getName()).isEqualTo("Cliente Ativo");
        assertThat(encontrado.get().getEmail()).isEqualTo("cliente@teste.com");
        assertThat(encontrado.get().getRole()).isEqualTo(UsuarioRole.ROLE_CLIENTE);
    }

    @Test
    @DisplayName("Deve retornar empty quando email não existe")
    void deveRetornarEmptyQuandoEmailNaoExiste() {
        Optional<Usuario> encontrado = usuarioRepository.findByEmail("naoexiste@teste.com");

        assertThat(encontrado).isEmpty();
    }

    @Test
    @DisplayName("Deve encontrar email mesmo quando usuário está inativo")
    void deveEncontrarEmailQuandoUsuarioInativo() {
        Optional<Usuario> encontrado = usuarioRepository.findByEmail("inativo@teste.com");

        assertThat(encontrado).isPresent();
        assertThat(encontrado.get().isActive()).isFalse();
    }

    @Test
    @DisplayName("existsByEmail deve retornar true quando email existe")
    void existsByEmailDeveRetornarTrueQuandoEmailExiste() {
        boolean exists = usuarioRepository.existsByEmail("cliente@teste.com");

        assertThat(exists).isTrue();
    }

    @Test
    @DisplayName("existsByEmail deve retornar false quando email não existe")
    void existsByEmailDeveRetornarFalseQuandoEmailNaoExiste() {
        boolean exists = usuarioRepository.existsByEmail("naoexiste@teste.com");

        assertThat(exists).isFalse();
    }

    @Test
    @DisplayName("findAll paginado deve retornar todos os usuários")
    void findAllPaginadoDeveRetornarTodosUsuarios() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> page = usuarioRepository.findAll(pageable);

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent()).hasSize(4);
    }

    @Test
    @DisplayName("findAll paginado com page maior que total deve retornar vazio")
    void findAllPaginadoComPageMaiorQueTotalDeveRetornarVazio() {
        Pageable pageable = PageRequest.of(10, 10);
        Page<Usuario> page = usuarioRepository.findAll(pageable);

        assertThat(page.getTotalElements()).isEqualTo(4);
        assertThat(page.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByRole deve retornar usuários com role específica")
    void findByRoleDeveRetornarUsuariosComRoleEspecifica() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> clientes = usuarioRepository.findByRole(UsuarioRole.ROLE_CLIENTE, pageable);

        assertThat(clientes.getTotalElements()).isEqualTo(2);
        assertThat(clientes.getContent()).allMatch(u -> u.getRole() == UsuarioRole.ROLE_CLIENTE);
    }

    @Test
    @DisplayName("findByRole com role sem usuários deve retornar página vazia")
    void findByRoleComRoleSemUsuariosDeveRetornarVazio() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> barbeariaAdms = usuarioRepository.findByRole(UsuarioRole.ROLE_BARBEARIA_ADM, pageable);

        assertThat(barbeariaAdms.getTotalElements()).isZero();
        assertThat(barbeariaAdms.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByActive deve retornar apenas usuários ativos")
    void findByActiveDeveRetornarApenasUsuariosAtivos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> ativos = usuarioRepository.findByActive(true, pageable);

        assertThat(ativos.getTotalElements()).isEqualTo(3);
        assertThat(ativos.getContent()).allMatch(Usuario::isActive);
    }

    @Test
    @DisplayName("findByActive deve retornar apenas usuários inativos")
    void findByActiveDeveRetornarApenasUsuariosInativos() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> inativos = usuarioRepository.findByActive(false, pageable);

        assertThat(inativos.getTotalElements()).isEqualTo(1);
        assertThat(inativos.getContent()).allMatch(u -> !u.isActive());
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase deve buscar por nome case insensitive")
    void findByNameContainingIgnoreCaseDeveBuscarPorNome() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> resultado = usuarioRepository.findByNameContainingIgnoreCase("cliente", pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(2);
        assertThat(resultado.getContent()).allMatch(u -> u.getName().toLowerCase().contains("cliente"));
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase deve buscar nome parcial")
    void findByNameContainingIgnoreCaseDeveBuscarNomeParcial() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> resultado = usuarioRepository.findByNameContainingIgnoreCase("func", pageable);

        assertThat(resultado.getTotalElements()).isEqualTo(1);
        assertThat(resultado.getContent().get(0).getName()).isEqualTo("Funcionário Ativo");
    }

    @Test
    @DisplayName("findByNameContainingIgnoreCase deve retornar vazio quando nome não encontrado")
    void findByNameContainingIgnoreCaseDeveRetornarVazioQuandoNaoEncontrado() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> resultado = usuarioRepository.findByNameContainingIgnoreCase("inexistente", pageable);

        assertThat(resultado.getTotalElements()).isZero();
        assertThat(resultado.getContent()).isEmpty();
    }

    @Test
    @DisplayName("findByRoleAndActive deve filtrar por role e status")
    void findByRoleAndActiveDeveFiltrarPorRoleEStatus() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Usuario> clientesAtivos = usuarioRepository.findByRoleAndActive(UsuarioRole.ROLE_CLIENTE, true, pageable);

        assertThat(clientesAtivos.getTotalElements()).isEqualTo(1);
        assertThat(clientesAtivos.getContent().get(0).getEmail()).isEqualTo("cliente@teste.com");
        assertThat(clientesAtivos.getContent().get(0).isActive()).isTrue();
    }

    @Test
    @DisplayName("countByRoleAndActiveTrue deve contar usuários ativos por role")
    void countByRoleAndActiveTrueDeveContarUsuariosAtivosPorRole() {
        long clientesAtivos = usuarioRepository.countByRoleAndActiveTrue(UsuarioRole.ROLE_CLIENTE);
        long funcionariosAtivos = usuarioRepository.countByRoleAndActiveTrue(UsuarioRole.ROLE_FUNCIONARIO);
        long adminsAtivos = usuarioRepository.countByRoleAndActiveTrue(UsuarioRole.ROLE_ADMIN);

        assertThat(clientesAtivos).isEqualTo(1);
        assertThat(funcionariosAtivos).isEqualTo(1);
        assertThat(adminsAtivos).isEqualTo(1);
    }

    @Test
    @DisplayName("existsByRoleAndActiveTrue deve verificar se existe usuário ativo com role")
    void existsByRoleAndActiveTrueDeveVerificarExistencia() {
        boolean existeClienteAtivo = usuarioRepository.existsByRoleAndActiveTrue(UsuarioRole.ROLE_CLIENTE);
        boolean existeBarbeariaAdmAtivo = usuarioRepository.existsByRoleAndActiveTrue(UsuarioRole.ROLE_BARBEARIA_ADM);

        assertThat(existeClienteAtivo).isTrue();
        assertThat(existeBarbeariaAdmAtivo).isFalse();
    }

    @Test
    @DisplayName("findByRoleAndActiveTrue deve listar usuários ativos por role")
    void findByRoleAndActiveTrueDeveListarUsuariosAtivos() {
        List<Usuario> funcionariosAtivos = usuarioRepository.findByRoleAndActiveTrue(UsuarioRole.ROLE_FUNCIONARIO);
        List<Usuario> clientesAtivos = usuarioRepository.findByRoleAndActiveTrue(UsuarioRole.ROLE_CLIENTE);

        assertThat(funcionariosAtivos).hasSize(1);
        assertThat(funcionariosAtivos.get(0).getEmail()).isEqualTo("funcionario@teste.com");

        assertThat(clientesAtivos).hasSize(1);
        assertThat(clientesAtivos.get(0).getEmail()).isEqualTo("cliente@teste.com");
    }

    @Test
    @DisplayName("Deve salvar usuário com UUID gerado automaticamente")
    void deveSalvarUsuarioComUuidGerado() {
        Usuario novoUsuario = new Usuario();
        novoUsuario.setName("Novo Usuário");
        novoUsuario.setEmail("novo@teste.com");
        novoUsuario.setPassword("senha123");
        novoUsuario.setRole(UsuarioRole.ROLE_CLIENTE);
        novoUsuario.setActive(true);

        Usuario salvo = usuarioRepository.save(novoUsuario);

        assertThat(salvo.getId()).isNotBlank();
        assertThat(salvo.getCriadoEm()).isNotNull();
        assertThat(salvo.getAtualizadoEm()).isNotNull();
    }

    @Test
    @DisplayName("Deve atualizar data de atualização ao modificar usuário")
    void deveAtualizarDataAtualizacaoAoModificar() {
        Usuario usuario = usuarioRepository.findByEmail("cliente@teste.com").orElseThrow();
        usuario.setName("Nome Atualizado");

        Usuario atualizado = usuarioRepository.save(usuario);

        assertThat(atualizado.getAtualizadoEm()).isNotNull();
        assertThat(atualizado.getAtualizadoEm()).isAfterOrEqualTo(atualizado.getCriadoEm());
    }
}