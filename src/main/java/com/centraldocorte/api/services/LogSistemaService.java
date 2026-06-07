package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.LogSistema;
import com.centraldocorte.api.domain.repositories.LogSistemaRepository;
import com.centraldocorte.api.dto.LogSistemaDTO;
import com.centraldocorte.api.dto.LogSistemaFiltroDTO;
import com.centraldocorte.api.dto.LogSistemaResponseDTO;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
public class LogSistemaService {

    private final LogSistemaRepository logSistemaRepository;
    private final ObjectMapper objectMapper;

    @Transactional
    public void registrarLog(
            String tipo,
            String acao,
            String entidade,
            String entidadeId,
            String descricao,
            Object detalhes,
            HttpServletRequest request) {

        try {
            String email = null;
            String usuarioId = "SISTEMA";
            String usuarioNome = "Sistema";
            String usuarioRole = "SYSTEM";

            try {
                if (SecurityContextHolder.getContext().getAuthentication() != null) {
                    email = SecurityContextHolder.getContext().getAuthentication().getName();
                    if (email != null && !email.equals("anonymousUser")) {
                        usuarioId = email;
                        usuarioNome = email;
                    }
                }
            } catch (Exception e) {
                log.warn("Não foi possível obter usuário autenticado: {}", e.getMessage());
            }

            String detalhesJson = detalhes != null ? objectMapper.writeValueAsString(detalhes) : null;
            String ipOrigem = request != null ? getClientIp(request) : null;
            String userAgent = request != null ? request.getHeader("User-Agent") : null;

            LogSistema logSistema = LogSistema.builder()
                    .tipo(tipo)
                    .acao(acao)
                    .usuarioId(usuarioId)
                    .usuarioEmail(email)
                    .usuarioNome(usuarioNome)
                    .usuarioRole(usuarioRole)
                    .entidade(entidade)
                    .entidadeId(entidadeId)
                    .descricao(descricao)
                    .detalhes(detalhesJson)
                    .ipOrigem(ipOrigem)
                    .userAgent(userAgent)
                    .build();

            logSistemaRepository.save(logSistema);
            log.info("Log registrado: {} - {}", tipo, acao);

        } catch (Exception e) {
            log.error("Erro ao registrar log: {}", e.getMessage(), e);
        }
    }

    @Transactional(readOnly = true)
    public LogSistemaResponseDTO buscarLogs(LogSistemaFiltroDTO filtro, int page, int size) {
        try {
            Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "dataHora"));
            Page<LogSistema> logsPage = null;

            String tipo = filtro.getTipo();
            String acao = filtro.getAcao();
            String usuarioId = filtro.getUsuarioId();
            LocalDateTime dataInicio = filtro.getDataInicio();
            LocalDateTime dataFim = filtro.getDataFim();

            // Aplicar filtros
            if (tipo != null && !tipo.isEmpty() && acao != null && !acao.isEmpty()) {
                logsPage = logSistemaRepository.findByTipoContainingIgnoreCaseAndAcaoContainingIgnoreCaseOrderByDataHoraDesc(
                        tipo, acao, pageable);
            }
            else if (tipo != null && !tipo.isEmpty()) {
                logsPage = logSistemaRepository.findByTipoContainingIgnoreCaseOrderByDataHoraDesc(tipo, pageable);
            }
            else if (acao != null && !acao.isEmpty()) {
                logsPage = logSistemaRepository.findByAcaoContainingIgnoreCaseOrderByDataHoraDesc(acao, pageable);
            }
            else if (usuarioId != null && !usuarioId.isEmpty()) {
                logsPage = logSistemaRepository.findByUsuarioIdOrderByDataHoraDesc(usuarioId, pageable);
            }
            else if (dataInicio != null && dataFim != null) {
                logsPage = logSistemaRepository.findByDataHoraBetweenOrderByDataHoraDesc(dataInicio, dataFim, pageable);
            }
            else {
                logsPage = logSistemaRepository.findAllByOrderByDataHoraDesc(pageable);
            }

            // Se não encontrou nada, retorna página vazia
            if (logsPage == null) {
                logsPage = Page.empty(pageable);
            }

            List<LogSistemaDTO> logs = logsPage.getContent().stream()
                    .map(this::converterParaDTO)
                    .collect(Collectors.toList());

            return LogSistemaResponseDTO.builder()
                    .logs(logs)
                    .totalElements(logsPage.getTotalElements())
                    .totalPages(logsPage.getTotalPages())
                    .currentPage(page)
                    .pageSize(size)
                    .build();

        } catch (Exception e) {
            log.error("Erro ao buscar logs: {}", e.getMessage(), e);
            // Retornar página vazia em caso de erro
            return LogSistemaResponseDTO.builder()
                    .logs(List.of())
                    .totalElements(0L)
                    .totalPages(0)
                    .currentPage(page)
                    .pageSize(size)
                    .build();
        }
    }

    @Transactional(readOnly = true)
    public List<String> getTiposDisponiveis() {
        try {
            return logSistemaRepository.findDistinctTipos();
        } catch (Exception e) {
            log.error("Erro ao buscar tipos: {}", e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public List<String> getAcoesDisponiveis() {
        try {
            return logSistemaRepository.findDistinctAcoes();
        } catch (Exception e) {
            log.error("Erro ao buscar ações: {}", e.getMessage());
            return List.of();
        }
    }

    @Transactional(readOnly = true)
    public Map<String, Long> getEstatisticas() {
        try {
            List<Object[]> resultados = logSistemaRepository.countByTipo();
            Map<String, Long> estatisticas = new HashMap<>();
            for (Object[] resultado : resultados) {
                estatisticas.put((String) resultado[0], (Long) resultado[1]);
            }
            return estatisticas;
        } catch (Exception e) {
            log.error("Erro ao buscar estatísticas: {}", e.getMessage());
            return new HashMap<>();
        }
    }

    private LogSistemaDTO converterParaDTO(LogSistema logSistema) {
        return LogSistemaDTO.builder()
                .id(logSistema.getId())
                .tipo(logSistema.getTipo())
                .acao(logSistema.getAcao())
                .usuarioId(logSistema.getUsuarioId())
                .usuarioEmail(logSistema.getUsuarioEmail())
                .usuarioNome(logSistema.getUsuarioNome())
                .usuarioRole(logSistema.getUsuarioRole())
                .entidade(logSistema.getEntidade())
                .entidadeId(logSistema.getEntidadeId())
                .descricao(logSistema.getDescricao())
                .detalhes(logSistema.getDetalhes())
                .ipOrigem(logSistema.getIpOrigem())
                .dataHora(logSistema.getDataHora())
                .build();
    }

    private String getClientIp(HttpServletRequest request) {
        if (request == null) return null;
        String xForwardedFor = request.getHeader("X-Forwarded-For");
        if (xForwardedFor != null && !xForwardedFor.isEmpty()) {
            return xForwardedFor.split(",")[0].trim();
        }
        String xRealIp = request.getHeader("X-Real-IP");
        if (xRealIp != null && !xRealIp.isEmpty()) {
            return xRealIp;
        }
        return request.getRemoteAddr();
    }
}