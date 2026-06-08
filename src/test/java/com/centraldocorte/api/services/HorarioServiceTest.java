package com.centraldocorte.api.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.HorarioFuncionamento;
import com.centraldocorte.api.domain.models.enums.DiaSemana;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.HorarioFuncionamentoRepository;
import com.centraldocorte.api.dto.HorarioFuncionamentoDTO;
import com.centraldocorte.api.dto.HorarioDisponivelDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.*;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - HorarioService")
class HorarioServiceTest {

    @Mock private HorarioFuncionamentoRepository horarioRepository;
    @Mock private BarbeariaRepository barbeariaRepository;
    @Mock private DisponibilidadeService disponibilidadeService;
    @InjectMocks private HorarioService horarioService;

    private Barbearia barbearia;
    private HorarioFuncionamento horarioSegunda;
    private HorarioFuncionamento horarioDomingoFechado;

    @BeforeEach
    void setUp() {
        barbearia = new Barbearia();
        barbearia.setId("barb-123");
        barbearia.setNome("Barbearia Teste");

        horarioSegunda = new HorarioFuncionamento();
        horarioSegunda.setId(1L);
        horarioSegunda.setBarbearia(barbearia);
        horarioSegunda.setDia(DiaSemana.SEGUNDA);
        horarioSegunda.setHoraAbertura(LocalTime.of(9, 0));
        horarioSegunda.setHoraFechamento(LocalTime.of(18, 0));
        horarioSegunda.setFechado(false);

        horarioDomingoFechado = new HorarioFuncionamento();
        horarioDomingoFechado.setId(7L);
        horarioDomingoFechado.setBarbearia(barbearia);
        horarioDomingoFechado.setDia(DiaSemana.DOMINGO);
        horarioDomingoFechado.setFechado(true);
    }

    @Test
    @DisplayName("Deve buscar horários por barbearia ordenados por dia")
    void deveBuscarHorariosPorBarbearia() {
        List<HorarioFuncionamento> horarios = Arrays.asList(horarioSegunda, horarioDomingoFechado);
        when(horarioRepository.findByBarbeariaIdOrderByDia("barb-123")).thenReturn(horarios);

        List<HorarioFuncionamento> result = horarioService.getHorariosByBarbearia("barb-123");

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getDia()).isEqualTo(DiaSemana.SEGUNDA);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando barbearia não tem horários cadastrados")
    void deveRetornarListaVaziaQuandoNaoHaHorarios() {
        when(horarioRepository.findByBarbeariaIdOrderByDia("barb-123")).thenReturn(List.of());

        List<HorarioFuncionamento> result = horarioService.getHorariosByBarbearia("barb-123");

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve salvar horários de funcionamento com sucesso")
    void deveSalvarHorariosComSucesso() {
        when(barbeariaRepository.findById("barb-123")).thenReturn(Optional.of(barbearia));

        List<HorarioFuncionamentoDTO> horariosDTO = new ArrayList<>();
        HorarioFuncionamentoDTO dtoSegunda = new HorarioFuncionamentoDTO();
        dtoSegunda.setDia("SEGUNDA");
        dtoSegunda.setHoraAbertura("09:00");
        dtoSegunda.setHoraFechamento("18:00");
        dtoSegunda.setFechado(false);
        horariosDTO.add(dtoSegunda);

        HorarioFuncionamentoDTO dtoTerca = new HorarioFuncionamentoDTO();
        dtoTerca.setDia("TERCA");
        dtoTerca.setHoraAbertura("09:00");
        dtoTerca.setHoraFechamento("18:00");
        dtoTerca.setFechado(false);
        horariosDTO.add(dtoTerca);

        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), any(DiaSemana.class)))
                .thenReturn(Optional.empty());
        when(horarioRepository.save(any(HorarioFuncionamento.class))).thenAnswer(inv -> inv.getArgument(0));

        List<HorarioFuncionamento> result = horarioService.saveHorarios("barb-123", horariosDTO);

        assertThat(result).hasSize(2);
        verify(horarioRepository, times(2)).save(any(HorarioFuncionamento.class));
    }

    @Test
    @DisplayName("Deve atualizar horário existente ao invés de criar novo")
    void deveAtualizarHorarioExistente() {
        when(barbeariaRepository.findById("barb-123")).thenReturn(Optional.of(barbearia));
        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.SEGUNDA)))
                .thenReturn(Optional.of(horarioSegunda));

        HorarioFuncionamentoDTO updateDTO = new HorarioFuncionamentoDTO();
        updateDTO.setDia("SEGUNDA");
        updateDTO.setHoraAbertura("10:00");
        updateDTO.setHoraFechamento("19:00");
        updateDTO.setFechado(false);

        when(horarioRepository.save(any(HorarioFuncionamento.class))).thenAnswer(inv -> inv.getArgument(0));

        List<HorarioFuncionamento> result = horarioService.saveHorarios("barb-123", List.of(updateDTO));

        assertThat(result).hasSize(1);
        assertThat(horarioSegunda.getHoraAbertura()).isEqualTo(LocalTime.of(10, 0));
        assertThat(horarioSegunda.getHoraFechamento()).isEqualTo(LocalTime.of(19, 0));
    }

    @Test
    @DisplayName("Deve lançar exceção quando hora abertura é após hora fechamento")
    void deveLancarExcecaoQuandoHoraAberturaAposFechamento() {
        when(barbeariaRepository.findById("barb-123")).thenReturn(Optional.of(barbearia));

        HorarioFuncionamentoDTO dtoInvalido = new HorarioFuncionamentoDTO();
        dtoInvalido.setDia("SEGUNDA");
        dtoInvalido.setHoraAbertura("18:00");
        dtoInvalido.setHoraFechamento("09:00");
        dtoInvalido.setFechado(false);

        assertThatThrownBy(() -> horarioService.saveHorarios("barb-123", List.of(dtoInvalido)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Horário de abertura deve ser anterior ao fechamento");
    }

    @Test
    @DisplayName("Deve lançar exceção quando horário não fechado não tem abertura/fechamento")
    void deveLancarExcecaoQuandoHorarioAbertoSemHorarios() {
        when(barbeariaRepository.findById("barb-123")).thenReturn(Optional.of(barbearia));

        HorarioFuncionamentoDTO dtoIncompleto = new HorarioFuncionamentoDTO();
        dtoIncompleto.setDia("SEGUNDA");
        dtoIncompleto.setHoraAbertura(null);
        dtoIncompleto.setHoraFechamento(null);
        dtoIncompleto.setFechado(false);

        assertThatThrownBy(() -> horarioService.saveHorarios("barb-123", List.of(dtoIncompleto)))
                .isInstanceOf(BusinessException.class)
                .hasMessage("Horário de abertura e fechamento são obrigatórios quando não está fechado");
    }

    @Test
    @DisplayName("Deve permitir salvar dia como fechado sem horários")
    void devePermitirSalvarDiaFechado() {
        when(barbeariaRepository.findById("barb-123")).thenReturn(Optional.of(barbearia));

        HorarioFuncionamentoDTO dtoFechado = new HorarioFuncionamentoDTO();
        dtoFechado.setDia("DOMINGO");
        dtoFechado.setFechado(true);

        when(horarioRepository.save(any(HorarioFuncionamento.class))).thenAnswer(inv -> inv.getArgument(0));

        List<HorarioFuncionamento> result = horarioService.saveHorarios("barb-123", List.of(dtoFechado));

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getFechado()).isTrue();
        assertThat(result.get(0).getHoraAbertura()).isNull();
        assertThat(result.get(0).getHoraFechamento()).isNull();
    }

    @Test
    @DisplayName("Deve lançar ResourceNotFoundException quando barbearia não existe")
    void deveLancarExcecaoQuandoBarbeariaNaoExiste() {
        when(barbeariaRepository.findById("barb-inexistente")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> horarioService.saveHorarios("barb-inexistente", List.of()))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessage("Barbearia não encontrada");
    }

    @Test
    @DisplayName("Deve retornar true quando barbearia está aberta no horário")
    void deveRetornarTrueQuandoBarbeariaAberta() {
        LocalDateTime segundaFeira10h = LocalDateTime.of(2024, 1, 8, 10, 0); // Segunda-feira 10h
        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.SEGUNDA)))
                .thenReturn(Optional.of(horarioSegunda));

        boolean aberta = horarioService.isBarbeariaAberta("barb-123", segundaFeira10h);

        assertThat(aberta).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando barbearia está fechada no horário (antes da abertura)")
    void deveRetornarFalseQuandoAntesDaAbertura() {
        LocalDateTime segundaFeira8h = LocalDateTime.of(2024, 1, 8, 8, 0); // Antes das 9h
        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.SEGUNDA)))
                .thenReturn(Optional.of(horarioSegunda));

        boolean aberta = horarioService.isBarbeariaAberta("barb-123", segundaFeira8h);

        assertThat(aberta).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando barbearia está fechada no horário (depois do fechamento)")
    void deveRetornarFalseQuandoDepoisDoFechamento() {
        LocalDateTime segundaFeira19h = LocalDateTime.of(2024, 1, 8, 19, 0); // Depois das 18h
        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.SEGUNDA)))
                .thenReturn(Optional.of(horarioSegunda));

        boolean aberta = horarioService.isBarbeariaAberta("barb-123", segundaFeira19h);

        assertThat(aberta).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando barbearia está fechada no dia (Domingo)")
    void deveRetornarFalseQuandoDiaFechado() {
        LocalDateTime domingo15h = LocalDateTime.of(2024, 1, 14, 15, 0); // Domingo
        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.DOMINGO)))
                .thenReturn(Optional.of(horarioDomingoFechado));

        boolean aberta = horarioService.isBarbeariaAberta("barb-123", domingo15h);

        assertThat(aberta).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando não há horário cadastrado para o dia")
    void deveRetornarFalseQuandoNaoHaHorarioCadastrado() {
        LocalDateTime tercaFeira10h = LocalDateTime.of(2024, 1, 9, 10, 0); // Terça-feira
        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.TERCA)))
                .thenReturn(Optional.empty());

        boolean aberta = horarioService.isBarbeariaAberta("barb-123", tercaFeira10h);

        assertThat(aberta).isFalse();
    }

    @Test
    @DisplayName("Deve retornar false quando dataHora é nula")
    void deveRetornarFalseQuandoDataHoraNula() {
        boolean aberta = horarioService.isBarbeariaAberta("barb-123", null);

        assertThat(aberta).isFalse();
    }

    @Test
    @DisplayName("Deve retornar horários disponíveis para uma data específica")
    void deveRetornarHorariosDisponiveisParaData() {
        LocalDate data = LocalDate.of(2024, 1, 8); // Segunda-feira

        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.SEGUNDA)))
                .thenReturn(Optional.of(horarioSegunda));

        LocalTime[] horarios = {LocalTime.of(9, 0), LocalTime.of(9, 30), LocalTime.of(10, 0)};
        for (LocalTime hora : horarios) {
            LocalDateTime dateTime = LocalDateTime.of(data, hora);
            when(disponibilidadeService.isHorarioDisponivel("barb-123", dateTime))
                    .thenReturn(true);
        }

        List<HorarioDisponivelDTO> result = horarioService.getHorariosDisponiveisParaAgendamento(
                "barb-123", data, 30);

        assertThat(result).isNotEmpty();
        assertThat(result.size()).isEqualTo(18);
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando barbearia está fechada no dia")
    void deveRetornarListaVaziaQuandoBarbeariaFechada() {
        LocalDate data = LocalDate.of(2024, 1, 14); // Domingo
        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.DOMINGO)))
                .thenReturn(Optional.of(horarioDomingoFechado));

        List<HorarioDisponivelDTO> result = horarioService.getHorariosDisponiveisParaAgendamento(
                "barb-123", data, 30);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve retornar lista vazia quando não há horário cadastrado")
    void deveRetornarListaVaziaQuandoSemHorarioCadastrado() {
        LocalDate data = LocalDate.of(2024, 1, 9); // Terça-feira
        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.TERCA)))
                .thenReturn(Optional.empty());

        List<HorarioDisponivelDTO> result = horarioService.getHorariosDisponiveisParaAgendamento(
                "barb-123", data, 30);

        assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Deve marcar horários como indisponíveis quando já ocupados")
    void deveMarcarHorariosComoIndisponiveis() {
        LocalDate data = LocalDate.of(2024, 1, 8); // Segunda-feira

        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.SEGUNDA)))
                .thenReturn(Optional.of(horarioSegunda));

        LocalDateTime horarioOcupado = LocalDateTime.of(data, LocalTime.of(10, 0));
        when(disponibilidadeService.isHorarioDisponivel("barb-123", horarioOcupado))
                .thenReturn(false);

        when(disponibilidadeService.isHorarioDisponivel(eq("barb-123"), argThat(t -> !t.equals(horarioOcupado))))
                .thenReturn(true);

        List<HorarioDisponivelDTO> result = horarioService.getHorariosDisponiveisParaAgendamento(
                "barb-123", data, 30);

        HorarioDisponivelDTO horario10h = result.stream()
                .filter(h -> h.getHorario().equals(horarioOcupado))
                .findFirst()
                .orElse(null);

        assertThat(horario10h).isNotNull();
        assertThat(horario10h.getDisponivel()).isFalse();

        long disponiveis = result.stream().filter(HorarioDisponivelDTO::getDisponivel).count();
        assertThat(disponiveis).isEqualTo(17);
    }

    @Test
    @DisplayName("Deve usar duração do serviço para calcular horários (quando fornecido)")
    void deveUsarDuracaoServicoParaCalcularHorarios() {
        LocalDate data = LocalDate.of(2024, 1, 8);

        when(horarioRepository.findByBarbeariaIdAndDia(eq("barb-123"), eq(DiaSemana.SEGUNDA)))
                .thenReturn(Optional.of(horarioSegunda));

        List<HorarioDisponivelDTO> resultPadrao = horarioService.getHorariosDisponiveisParaAgendamento(
                "barb-123", data, null);

        List<HorarioDisponivelDTO> result60min = horarioService.getHorariosDisponiveisParaAgendamento(
                "barb-123", data, 60);

        assertThat(resultPadrao.size()).isEqualTo(result60min.size());
    }
}