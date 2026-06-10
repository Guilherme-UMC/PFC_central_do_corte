package com.centraldocorte.api.services;

import org.junit.jupiter.api.*;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.*;
import org.mockito.junit.jupiter.MockitoExtension;

import com.centraldocorte.api.domain.repositories.AgendamentoRepository;

import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Testes Unitários - DisponibilidadeService")
class DisponibilidadeServiceTest {

    @Mock private AgendamentoRepository agendamentoRepository;
    @InjectMocks private DisponibilidadeService disponibilidadeService;

    @Test
    @DisplayName("Deve retornar true quando horário está disponível na barbearia")
    void deveRetornarTrueQuandoHorarioDisponivelNaBarbearia() {
        when(agendamentoRepository.countAgendamentosConfirmadosNoHorario(eq("barb-123"), any(LocalDateTime.class)))
                .thenReturn(0L);

        boolean disponivel = disponibilidadeService.isHorarioDisponivel("barb-123", LocalDateTime.now().plusDays(1));

        assertThat(disponivel).isTrue();
    }

    @Test
    @DisplayName("Deve retornar false quando horário está ocupado na barbearia")
    void deveRetornarFalseQuandoHorarioOcupadoNaBarbearia() {
        when(agendamentoRepository.countAgendamentosConfirmadosNoHorario(eq("barb-123"), any(LocalDateTime.class)))
                .thenReturn(1L);

        boolean disponivel = disponibilidadeService.isHorarioDisponivel("barb-123", LocalDateTime.now().plusDays(1));

        assertThat(disponivel).isFalse();
    }
}