package com.centraldocorte.api.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.dto.BarbeariaRequestDTO;
import com.centraldocorte.api.dto.BarbeariaResponseDTO;
import com.centraldocorte.api.dto.ViaCepResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;

import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - BarbeariaService")
class BarbeariaServiceTest {

    @Mock private BarbeariaRepository barbeariaRepository;
    @Mock private ViaCepService viaCepService;
    @InjectMocks private BarbeariaService barbeariaService;

    private Usuario proprietario;
    private Barbearia barbeariaAtiva;
    private Barbearia barbeariaInativa;
    private BarbeariaRequestDTO requestDTO;

    @BeforeEach
    void setUp() {
        proprietario = new Usuario();
        proprietario.setId("owner-123");
        proprietario.setName("Proprietário Teste");
        proprietario.setEmail("dono@barbearia.com");
        proprietario.setRole(UsuarioRole.ROLE_BARBEARIA_ADM);
        proprietario.setActive(true);

        barbeariaAtiva = new Barbearia();
        barbeariaAtiva.setId("barb-001");
        barbeariaAtiva.setNome("Barbearia do João");
        barbeariaAtiva.setDescricao("A melhor barbearia da região");
        barbeariaAtiva.setLogradouro("Rua das Flores");
        barbeariaAtiva.setNumero("123");
        barbeariaAtiva.setBairro("Centro");
        barbeariaAtiva.setCep("01001000");
        barbeariaAtiva.setCidade("São Paulo");
        barbeariaAtiva.setUf("SP");
        barbeariaAtiva.setTelefone("11999999999");
        barbeariaAtiva.setImgUrl("https://exemplo.com/logo.jpg");
        barbeariaAtiva.setAtivo(true);
        barbeariaAtiva.setOwner(proprietario);

        barbeariaInativa = new Barbearia();
        barbeariaInativa.setId("barb-002");
        barbeariaInativa.setNome("Barbearia Fechada");
        barbeariaInativa.setAtivo(false);
        barbeariaInativa.setOwner(proprietario);

        requestDTO = new BarbeariaRequestDTO();
        requestDTO.setNome("Nova Barbearia");
        requestDTO.setDescricao("Descrição da nova barbearia");
        requestDTO.setLogradouro("Av. Paulista");
        requestDTO.setNumero("1000");
        requestDTO.setBairro("Bela Vista");
        requestDTO.setCep("01310000");
        requestDTO.setCidade("São Paulo");
        requestDTO.setUf("SP");
        requestDTO.setTelefone("1133333333");
        requestDTO.setImgUrl("https://exemplo.com/nova.jpg");
    }


    @Test
    @DisplayName("Deve criar barbearia com sucesso")
    void deveCriarBarbeariaComSucesso() {
        Barbearia barbeariaSalva = new Barbearia();
        barbeariaSalva.setId("barb-nova-001");
        barbeariaSalva.setNome(requestDTO.getNome());
        barbeariaSalva.setDescricao(requestDTO.getDescricao());
        barbeariaSalva.setLogradouro(requestDTO.getLogradouro());
        barbeariaSalva.setNumero(requestDTO.getNumero());
        barbeariaSalva.setBairro(requestDTO.getBairro());
        barbeariaSalva.setCep(requestDTO.getCep());
        barbeariaSalva.setCidade(requestDTO.getCidade());
        barbeariaSalva.setUf(requestDTO.getUf());
        barbeariaSalva.setTelefone(requestDTO.getTelefone());
        barbeariaSalva.setImgUrl(requestDTO.getImgUrl());
        barbeariaSalva.setAtivo(true);
        barbeariaSalva.setOwner(proprietario);

        when(barbeariaRepository.save(any(Barbearia.class))).thenReturn(barbeariaSalva);

        BarbeariaResponseDTO response = barbeariaService.criarBarbearia(requestDTO, proprietario);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("barb-nova-001");
        assertThat(response.getNome()).isEqualTo("Nova Barbearia");
        assertThat(response.getOwnerId()).isEqualTo("owner-123");
        assertThat(response.getOwnerName()).isEqualTo("Proprietário Teste");
        assertThat(response.getAtivo()).isTrue();

        verify(barbeariaRepository).save(any(Barbearia.class));
    }

    @Test
    @DisplayName("Deve buscar barbearia por ID com sucesso")
    void deveBuscarBarbeariaPorIdComSucesso() {
        when(barbeariaRepository.findByIdAndAtivoTrue("barb-001")).thenReturn(Optional.of(barbeariaAtiva));

        BarbeariaResponseDTO response = barbeariaService.buscarPorId("barb-001");

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo("barb-001");
        assertThat(response.getNome()).isEqualTo("Barbearia do João");
        assertThat(response.getCidade()).isEqualTo("São Paulo");
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException ao buscar barbearia inativa por ID")
    void deveLancarExcecaoAoBuscarBarbeariaInativa() {
        when(barbeariaRepository.findByIdAndAtivoTrue("barb-002")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> barbeariaService.buscarPorId("barb-002"))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Barbearia não encontrada com ID: barb-002");
    }

    @Test
    @DisplayName("Deve listar barbearias ativas paginadas")
    void deveListarBarbeariasAtivasPaginadas() {
        Pageable pageable = PageRequest.of(0, 10);
        List<Barbearia> barbearias = List.of(barbeariaAtiva);
        Page<Barbearia> page = new PageImpl<>(barbearias, pageable, 1);

        when(barbeariaRepository.findAllByAtivoTrue(pageable)).thenReturn(page);

        Page<BarbeariaResponseDTO> result = barbeariaService.listarBarbeariasAtivas(pageable);

        assertThat(result.getContent()).hasSize(1);
        assertThat(result.getContent().get(0).getNome()).isEqualTo("Barbearia do João");
    }

    @Test
    @DisplayName("Deve buscar barbearias por proprietário")
    void deveBuscarBarbeariasPorProprietario() {
        when(barbeariaRepository.findByOwnerAndAtivoTrue(proprietario))
                .thenReturn(List.of(barbeariaAtiva, barbeariaInativa));

        List<BarbeariaResponseDTO> result = barbeariaService.buscarPorProprietario(proprietario);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getNome()).isEqualTo("Barbearia do João");
    }

    @Test
    @DisplayName("Deve buscar barbearias por nome (case insensitive)")
    void deveBuscarBarbeariasPorNome() {
        when(barbeariaRepository.findByNomeContainingIgnoreCaseAndAtivoTrue("joão"))
                .thenReturn(List.of(barbeariaAtiva));

        List<BarbeariaResponseDTO> result = barbeariaService.buscarPorNome("joão");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getNome()).isEqualTo("Barbearia do João");
    }

    @Test
    @DisplayName("Deve buscar barbearias por cidade e UF")
    void deveBuscarBarbeariasPorCidadeEUf() {
        when(barbeariaRepository.findByCidadeAndUfAndAtivoTrue("São Paulo", "SP"))
                .thenReturn(List.of(barbeariaAtiva));

        List<BarbeariaResponseDTO> result = barbeariaService.buscarPorCidadeUf("São Paulo", "SP");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCidade()).isEqualTo("São Paulo");
        assertThat(result.get(0).getUf()).isEqualTo("SP");
    }

    @Test
    @DisplayName("Deve buscar barbearias por CEP")
    void deveBuscarBarbeariasPorCep() {
        when(barbeariaRepository.findByCepAndAtivoTrue("01001000"))
                .thenReturn(List.of(barbeariaAtiva));

        List<BarbeariaResponseDTO> result = barbeariaService.buscarPorCep("01001-000");

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getCep()).isEqualTo("01001000");
    }

    @Test
    @DisplayName("Deve atualizar barbearia com sucesso")
    void deveAtualizarBarbeariaComSucesso() {
        BarbeariaRequestDTO updateDTO = new BarbeariaRequestDTO();
        updateDTO.setNome("Barbearia do João - Unidade 2");
        updateDTO.setDescricao("Nova descrição atualizada");
        updateDTO.setLogradouro("Rua Nova");
        updateDTO.setNumero("456");
        updateDTO.setBairro("Jardins");
        updateDTO.setCep("01414000");
        updateDTO.setCidade("São Paulo");
        updateDTO.setUf("SP");
        updateDTO.setTelefone("1144444444");
        updateDTO.setImgUrl("https://exemplo.com/atualizada.jpg");

        when(barbeariaRepository.findByIdAndAtivoTrue("barb-001")).thenReturn(Optional.of(barbeariaAtiva));
        when(barbeariaRepository.save(any(Barbearia.class))).thenAnswer(inv -> inv.getArgument(0));

        BarbeariaResponseDTO response = barbeariaService.atualizarBarbearia("barb-001", updateDTO);

        assertThat(response.getNome()).isEqualTo("Barbearia do João - Unidade 2");
        assertThat(response.getDescricao()).isEqualTo("Nova descrição atualizada");
        assertThat(response.getLogradouro()).isEqualTo("Rua Nova");

        verify(barbeariaRepository).save(barbeariaAtiva);
    }

    @Test
    @DisplayName("Deve desativar barbearia (soft delete)")
    void deveDesativarBarbearia() {
        when(barbeariaRepository.findByIdAndAtivoTrue("barb-001")).thenReturn(Optional.of(barbeariaAtiva));
        when(barbeariaRepository.save(any(Barbearia.class))).thenAnswer(inv -> inv.getArgument(0));

        barbeariaService.desativarBarbearia("barb-001");

        assertThat(barbeariaAtiva.getAtivo()).isFalse();
        verify(barbeariaRepository).save(barbeariaAtiva);
    }

    @Test
    @DisplayName("Deve alternar status da barbearia (ativar/desativar)")
    void deveAlternarStatusBarbearia() {
        when(barbeariaRepository.findById("barb-001")).thenReturn(Optional.of(barbeariaAtiva));
        when(barbeariaRepository.save(any(Barbearia.class))).thenAnswer(inv -> inv.getArgument(0));

        barbeariaService.alternarStatusBarbearia("barb-001");

        assertThat(barbeariaAtiva.getAtivo()).isFalse();

        when(barbeariaRepository.findById("barb-001")).thenReturn(Optional.of(barbeariaAtiva));

        barbeariaService.alternarStatusBarbearia("barb-001");

        assertThat(barbeariaAtiva.getAtivo()).isTrue();
    }

    @Test
    @DisplayName("Deve buscar barbearias por CEP e retornar resultados exatos quando existem")
    void deveBuscarBarbeariasPorCepComFallback_EncontraExato() {
        String cep = "01001000";
        when(barbeariaRepository.findByCepAndAtivoTrue(cep)).thenReturn(List.of(barbeariaAtiva));

        Map<String, Object> result = barbeariaService.buscarBarbeariasPorCepComFallback(cep, viaCepService);

        assertThat(result.get("tipo_busca")).isEqualTo("CEP_EXATO");
        assertThat(result.get("total")).isEqualTo(1);
        assertThat(result.containsKey("barbearias")).isTrue();
        verify(viaCepService, never()).buscarEnderecoPorCep(anyString());
    }

    @Test
    @DisplayName("Deve buscar por cidade quando CEP não tem barbearias cadastradas")
    void deveBuscarPorCidadeQuandoCepNaoTemBarbearias() {
        String cep = "99999999";
        ViaCepResponseDTO endereco = new ViaCepResponseDTO(
                "99999999", "Rua Exemplo", "", "Centro",
                "Cidade Exemplo", "CE", "Estado Exemplo", "Região Exemplo",
                "1234567", "", "85", "", false
        );

        when(barbeariaRepository.findByCepAndAtivoTrue(cep)).thenReturn(List.of());
        when(viaCepService.buscarEnderecoPorCep(cep)).thenReturn(endereco);
        when(barbeariaRepository.findByCidadeAndUfAndAtivoTrue("Cidade Exemplo", "CE"))
                .thenReturn(List.of(barbeariaAtiva));

        Map<String, Object> result = barbeariaService.buscarBarbeariasPorCepComFallback(cep, viaCepService);

        assertThat(result.get("tipo_busca")).isEqualTo("CIDADE_PROXIMA");
        assertThat(result.get("total")).isEqualTo(1);
        assertThat(result.containsKey("cep_consultado")).isTrue();
        assertThat(result.containsKey("endereco_do_cep")).isTrue();
    }

    @Test
    @DisplayName("Deve retornar mensagem de erro quando CEP é inválido")
    void deveRetornarErroQuandoCepInvalido() {
        String cep = "00000000";
        when(barbeariaRepository.findByCepAndAtivoTrue(cep)).thenReturn(List.of());
        when(viaCepService.buscarEnderecoPorCep(cep)).thenThrow(new BusinessException("CEP não encontrado"));

        Map<String, Object> result = barbeariaService.buscarBarbeariasPorCepComFallback(cep, viaCepService);

        assertThat(result.get("erro")).isEqualTo("CEP_INVALIDO");
        assertThat(result.get("mensagem")).isEqualTo("CEP não encontrado");
    }

    @Test
    @DisplayName("Deve retornar sem resultados quando CEP válido mas nenhuma barbearia na cidade")
    void deveRetornarSemResultadosQuandoNenhumaBarbeariaNaCidade() {
        String cep = "01001000";
        ViaCepResponseDTO endereco = new ViaCepResponseDTO(
                "01001000", "Rua Exemplo", "", "Centro",
                "Cidade Sem Barbearia", "SP", "Estado Exemplo", "Região Exemplo",
                "1234567", "", "11", "", false
        );

        when(barbeariaRepository.findByCepAndAtivoTrue(cep)).thenReturn(List.of());
        when(viaCepService.buscarEnderecoPorCep(cep)).thenReturn(endereco);
        when(barbeariaRepository.findByCidadeAndUfAndAtivoTrue("Cidade Sem Barbearia", "SP"))
                .thenReturn(List.of());

        Map<String, Object> result = barbeariaService.buscarBarbeariasPorCepComFallback(cep, viaCepService);

        assertThat(result.get("tipo_busca")).isEqualTo("SEM_RESULTADOS");
        assertThat(result.get("mensagem")).isEqualTo("Nenhuma barbearia encontrada neste CEP ou na cidade correspondente");
    }
}