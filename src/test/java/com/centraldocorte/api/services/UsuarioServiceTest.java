//package com.centraldocorte.api.services;
//
//import com.centraldocorte.api.domain.models.Usuario;
//import com.centraldocorte.api.domain.models.enums.UsuarioRole;
//import com.centraldocorte.api.domain.repositories.FuncionarioBarbeariaRepository;
//import com.centraldocorte.api.domain.repositories.UsuarioRepository;
//import com.centraldocorte.api.dto.UsuarioRequestDTO;
//import com.centraldocorte.api.dto.UsuarioResponseDTO;
//import com.centraldocorte.api.exception.BusinessException;
//import com.centraldocorte.api.exception.ResourceNotFoundException;
//import org.junit.jupiter.api.BeforeEach;
//import org.junit.jupiter.api.DisplayName;
//import org.junit.jupiter.api.Test;
//import org.junit.jupiter.api.extension.ExtendWith;
//import org.mockito.InjectMocks;
//import org.mockito.Mock;
//import org.mockito.junit.jupiter.MockitoExtension;
//import org.springframework.security.crypto.password.PasswordEncoder;
//
//import java.util.List;
//import java.util.Optional;
//
//import static org.assertj.core.api.Assertions.*;
//import static org.mockito.ArgumentMatchers.*;
//import static org.mockito.Mockito.*;
//
//@ExtendWith(MockitoExtension.class)
//@DisplayName("UsuarioService - Testes Unitários")
//class UsuarioServiceTest {
//
//    @Mock
//    private UsuarioRepository usuarioRepository;
//
//    @Mock
//    private PasswordEncoder passwordEncoder;
//
//    @Mock
//    private FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
//
//    @InjectMocks
//    private UsuarioService usuarioService;
//
//    private Usuario usuarioPadrao;
//
//    @BeforeEach
//    void configurarCenarioBase() {
//        usuarioPadrao = new Usuario();
//        usuarioPadrao.setId("usuario-1");
//        usuarioPadrao.setName("Ana Costa");
//        usuarioPadrao.setEmail("ana@email.com");
//        usuarioPadrao.setPassword("senhaCodificada");
//        usuarioPadrao.setTelefone("11999998888");
//        usuarioPadrao.setRole(UsuarioRole.ROLE_CLIENTE);
//        usuarioPadrao.setActive(true);
//    }
//
//    @Test
//    @DisplayName("Deve retornar todos os usuários ativos")
//    void deveListarTodosUsuariosAtivos() {
//        when(usuarioRepository.findAllByActiveTrue()).thenReturn(List.of(usuarioPadrao));
//
//        List<UsuarioResponseDTO> resultado = usuarioService.listarTodosAtivos();
//
//        assertThat(resultado).hasSize(1);
//        assertThat(resultado.get(0).getName()).isEqualTo("Ana Costa");
//    }
//
//    @Test
//    @DisplayName("Deve retornar lista vazia quando não há usuários ativos")
//    void deveRetornarListaVaziaQuandoNaoHaUsuariosAtivos() {
//        when(usuarioRepository.findAllByActiveTrue()).thenReturn(List.of());
//
//        List<UsuarioResponseDTO> resultado = usuarioService.listarTodosAtivos();
//
//        assertThat(resultado).isEmpty();
//    }
//
//    @Test
//    @DisplayName("Deve buscar usuário ativo por ID com sucesso")
//    void deveBuscarUsuarioAtivoporId() {
//        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(usuarioPadrao));
//
//        UsuarioResponseDTO resultado = usuarioService.buscarPorId("usuario-1");
//
//        assertThat(resultado.getId()).isEqualTo("usuario-1");
//        assertThat(resultado.getEmail()).isEqualTo("ana@email.com");
//    }
//
//    @Test
//    @DisplayName("Deve lançar ResourceNotFoundException quando usuário não existe")
//    void deveLancarExcecaoQuandoUsuarioNaoExiste() {
//        when(usuarioRepository.findById("id-inexistente")).thenReturn(Optional.empty());
//
//        assertThatThrownBy(() -> usuarioService.buscarPorId("id-inexistente"))
//                .isInstanceOf(ResourceNotFoundException.class)
//                .hasMessageContaining("Usuário não encontrado");
//    }
//
//    @Test
//    @DisplayName("Deve lançar ResourceNotFoundException quando usuário existe mas está inativo")
//    void deveLancarExcecaoQuandoUsuarioEstaInativo() {
//        usuarioPadrao.setActive(false);
//        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(usuarioPadrao));
//
//        assertThatThrownBy(() -> usuarioService.buscarPorId("usuario-1"))
//                .isInstanceOf(ResourceNotFoundException.class);
//    }
//
//    @Test
//    @DisplayName("Deve criar novo usuário com sucesso")
//    void deveCriarUsuarioComSucesso() {
//        when(usuarioRepository.existsByEmail("novo@email.com")).thenReturn(false);
//        when(passwordEncoder.encode(anyString())).thenReturn("senhaCodificada");
//        when(usuarioRepository.save(any(Usuario.class))).thenAnswer(inv -> {
//            Usuario u = inv.getArgument(0);
//            u.setId("novo-id");
//            return u;
//        });
//
//        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
//                .name("Novo Usuario")
//                .email("novo@email.com")
//                .password("senha123")
//                .telefone("11988887777")
//                .role(UsuarioRole.ROLE_FUNCIONARIO)
//                .build();
//
//        UsuarioResponseDTO resultado = usuarioService.criarUsuario(request);
//
//        assertThat(resultado.getEmail()).isEqualTo("novo@email.com");
//        assertThat(resultado.getRole()).isEqualTo(UsuarioRole.ROLE_FUNCIONARIO);
//    }
//
//    @Test
//    @DisplayName("Deve lançar BusinessException ao criar usuário com email já existente")
//    void deveLancarExcecaoAoCriarComEmailExistente() {
//        when(usuarioRepository.existsByEmail("ana@email.com")).thenReturn(true);
//
//        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
//                .name("Outra Pessoa")
//                .email("ana@email.com")
//                .password("senha123")
//                .build();
//
//        assertThatThrownBy(() -> usuarioService.criarUsuario(request))
//                .isInstanceOf(BusinessException.class)
//                .hasMessageContaining("Email já cadastrado");
//    }
//
//    @Test
//    @DisplayName("Deve atualizar dados do usuário com sucesso")
//    void deveAtualizarUsuarioComSucesso() {
//        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(usuarioPadrao));
//        when(usuarioRepository.save(any())).thenReturn(usuarioPadrao);
//
//        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
//                .name("Ana Costa Atualizada")
//                .telefone("11977776666")
//                .build();
//
//        UsuarioResponseDTO resultado = usuarioService.atualizarUsuario("usuario-1", request);
//
//        assertThat(resultado).isNotNull();
//        verify(usuarioRepository, times(1)).save(any(Usuario.class));
//    }
//
//    @Test
//    @DisplayName("Deve lançar BusinessException ao atualizar com email que pertence a outro usuário")
//    void deveLancarExcecaoAoAtualizarComEmailDeOutroUsuario() {
//        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(usuarioPadrao));
//        when(usuarioRepository.existsByEmail("ocupado@email.com")).thenReturn(true);
//
//        UsuarioRequestDTO request = UsuarioRequestDTO.builder()
//                .email("ocupado@email.com")
//                .build();
//
//        assertThatThrownBy(() -> usuarioService.atualizarUsuario("usuario-1", request))
//                .isInstanceOf(BusinessException.class)
//                .hasMessageContaining("Email já cadastrado");
//    }
//
//    @Test
//    @DisplayName("Deve alternar status do usuário de ativo para inativo")
//    void deveAlternarStatusDeAtivoParaInativo() {
//        usuarioPadrao.setActive(true);
//        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(usuarioPadrao));
//        when(usuarioRepository.save(any())).thenReturn(usuarioPadrao);
//
//        usuarioService.alternarStatusDoUsuario("usuario-1");
//
//        assertThat(usuarioPadrao.isActive()).isFalse();
//        verify(usuarioRepository, times(1)).save(usuarioPadrao);
//    }
//
//    @Test
//    @DisplayName("Deve alternar status do usuário de inativo para ativo")
//    void deveAlternarStatusDeInativoParaAtivo() {
//        usuarioPadrao.setActive(false);
//        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(usuarioPadrao));
//        when(usuarioRepository.save(any())).thenReturn(usuarioPadrao);
//
//        usuarioService.alternarStatusDoUsuario("usuario-1");
//
//        assertThat(usuarioPadrao.isActive()).isTrue();
//    }
//
//    @Test
//    @DisplayName("Deve alterar senha com sucesso quando senha atual está correta")
//    void deveAlterarSenhaComSucesso() {
//        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(usuarioPadrao));
//        when(passwordEncoder.matches("senhaAtual", "senhaCodificada")).thenReturn(true);
//        when(passwordEncoder.encode("novaSenha")).thenReturn("novaSenhaCodificada");
//        when(usuarioRepository.save(any())).thenReturn(usuarioPadrao);
//
//        assertThatCode(() -> usuarioService.alterarSenha("usuario-1", "senhaAtual", "novaSenha"))
//                .doesNotThrowAnyException();
//
//        assertThat(usuarioPadrao.getPassword()).isEqualTo("novaSenhaCodificada");
//    }
//
//    @Test
//    @DisplayName("Deve lançar BusinessException quando senha atual informada é incorreta")
//    void deveLancarExcecaoQuandoSenhaAtualEstaIncorreta() {
//        when(usuarioRepository.findById("usuario-1")).thenReturn(Optional.of(usuarioPadrao));
//        when(passwordEncoder.matches("senhaErrada", "senhaCodificada")).thenReturn(false);
//
//        assertThatThrownBy(() -> usuarioService.alterarSenha("usuario-1", "senhaErrada", "novaSenha"))
//                .isInstanceOf(BusinessException.class)
//                .hasMessageContaining("Senha atual incorreta");
//    }
//
//    @Test
//    @DisplayName("Deve indicar que funcionário está vinculado quando há vínculo ativo")
//    void deveIndicarFuncionarioVinculado() {
//        when(funcionarioBarbeariaRepository.existsByFuncionarioIdAndAtivoTrue("func-1")).thenReturn(true);
//
//        boolean resultado = usuarioService.funcionarioEstaVinculado("func-1");
//
//        assertThat(resultado).isTrue();
//    }
//
//    @Test
//    @DisplayName("Deve indicar que funcionário não está vinculado quando não há vínculo ativo")
//    void deveIndicarFuncionarioNaoVinculado() {
//        when(funcionarioBarbeariaRepository.existsByFuncionarioIdAndAtivoTrue("func-2")).thenReturn(false);
//
//        boolean resultado = usuarioService.funcionarioEstaVinculado("func-2");
//
//        assertThat(resultado).isFalse();
//    }
//}
