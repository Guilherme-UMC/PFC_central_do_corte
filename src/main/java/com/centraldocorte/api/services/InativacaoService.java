package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Agendamento;
import com.centraldocorte.api.domain.models.Barbearia;
import com.centraldocorte.api.domain.models.FuncionarioBarbearia;
import com.centraldocorte.api.domain.models.Usuario;
import com.centraldocorte.api.domain.models.enums.StatusAgendamento;
import com.centraldocorte.api.domain.repositories.AgendamentoRepository;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.FuncionarioBarbeariaRepository;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class InativacaoService {

    private final AgendamentoRepository agendamentoRepository;
    private final BarbeariaRepository barbeariaRepository;
    private final FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
    private final UsuarioRepository usuarioRepository;

    public void processarInativacao(Usuario usuario) {
        switch (usuario.getRole()) {
            case ROLE_CLIENTE:
                cancelarAgendamentosCliente(usuario.getId());
                break;

            case ROLE_BARBEARIA_ADM:
                desativarBarbeariasDoProprietario(usuario.getId());
                break;

            case ROLE_FUNCIONARIO:
                desativarFuncionarioDeTodasBarbearias(usuario.getId());
                break;

            default:
                break;
        }
    }

    private void cancelarAgendamentosCliente(String clienteId) {
        LocalDateTime agora = LocalDateTime.now();
        List<StatusAgendamento> statusesIgnorados = List.of(
                StatusAgendamento.CONCLUIDO,
                StatusAgendamento.CANCELADO_PELO_CLIENTE,
                StatusAgendamento.CANCELADO_PELA_BARBEARIA
        );

        List<Agendamento> agendamentos = agendamentoRepository
                .findByClienteIdAndDataHoraAfterAndStatusNotIn(clienteId, agora, statusesIgnorados);

        for (Agendamento ag : agendamentos) {
            ag.setStatus(StatusAgendamento.CANCELADO_PELO_CLIENTE);
            ag.setObservacao((ag.getObservacao() != null ? ag.getObservacao() + " | " : "") +
                    "Cancelado automaticamente - Cliente inativado em " + LocalDateTime.now());
            agendamentoRepository.save(ag);
        }

        log.info("{} agendamentos cancelados para cliente inativo {}", agendamentos.size(), clienteId);
    }

    private void desativarBarbeariasDoProprietario(String ownerId) {
        List<Barbearia> barbearias = barbeariaRepository.findByOwnerIdAndAtivoTrue(ownerId);

        for (Barbearia barbearia : barbearias) {
            barbearia.setAtivo(false);
            barbeariaRepository.save(barbearia);

            List<FuncionarioBarbearia> vinculos = funcionarioBarbeariaRepository
                    .findByBarbeariaIdAndAtivoTrue(barbearia.getId());

            for (FuncionarioBarbearia vinculo : vinculos) {
                vinculo.setAtivo(false);
                funcionarioBarbeariaRepository.save(vinculo);
            }

            LocalDateTime agora = LocalDateTime.now();
            List<Agendamento> agendamentos = agendamentoRepository
                    .findByBarbeariaIdAndDataHoraAfterAndStatusNotIn(
                            barbearia.getId(),
                            agora,
                            List.of(
                                    StatusAgendamento.CONCLUIDO,
                                    StatusAgendamento.CANCELADO_PELO_CLIENTE,
                                    StatusAgendamento.CANCELADO_PELA_BARBEARIA
                            )
                    );

            for (Agendamento ag : agendamentos) {
                ag.setStatus(StatusAgendamento.CANCELADO_PELA_BARBEARIA);
                ag.setObservacao((ag.getObservacao() != null ? ag.getObservacao() + " | " : "") +
                        "Cancelado automaticamente - Barbearia desativada");
                agendamentoRepository.save(ag);
            }

            log.info("Barbearia {} desativada. {} funcionários desvinculados. {} agendamentos cancelados.",
                    barbearia.getId(), vinculos.size(), agendamentos.size());
        }
    }

    private void desativarFuncionarioDeTodasBarbearias(String funcionarioId) {
        List<FuncionarioBarbearia> vinculos = funcionarioBarbeariaRepository
                .findByFuncionarioIdAndAtivoTrue(funcionarioId);

        if (vinculos.isEmpty()) {
            log.info("Funcionário {} não possui vínculos ativos", funcionarioId);
            return;
        }

        for (FuncionarioBarbearia vinculo : vinculos) {
            String barbeariaId = vinculo.getBarbearia().getId();

            transferirAgendamentosDoFuncionario(barbeariaId, funcionarioId);

            vinculo.setAtivo(false);
            funcionarioBarbeariaRepository.save(vinculo);

            log.info("Funcionário {} desativado da barbearia {}", funcionarioId, barbeariaId);
        }

        Usuario funcionario = usuarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado"));
        funcionario.setActive(false);
        usuarioRepository.save(funcionario);

        log.info("Funcionário {} inativado do sistema", funcionarioId);
    }


    private void transferirAgendamentosDoFuncionario(String barbeariaId, String funcionarioId) {
        LocalDateTime agora = LocalDateTime.now();
        List<Agendamento> agendamentosFuturos = agendamentoRepository
                .findFutureAgendamentosByFuncionarioId(funcionarioId, agora);

        if (agendamentosFuturos.isEmpty()) {
            log.info("Funcionário {} não possui agendamentos futuros", funcionarioId);
            return;
        }

        String nomeFuncionarioOriginal = buscarNomeFuncionarioPorId(funcionarioId);

        for (Agendamento agendamento : agendamentosFuturos) {
            Usuario substituto = buscarFuncionarioSubstitutoDisponivel(
                    barbeariaId, funcionarioId, agendamento.getDataHora());

            if (substituto == null) {
                throw new BusinessException(
                        String.format("Não foi possível transferir agendamento do dia %s. Nenhum substituto disponível.",
                                agendamento.getDataHora().toLocalDate())
                );
            }

            String observacao = String.format(
                    "Transferido do funcionário %s para %s em %s. %s",
                    nomeFuncionarioOriginal,
                    substituto.getName(),
                    LocalDateTime.now(),
                    agendamento.getObservacao() != null ? agendamento.getObservacao() : ""
            );

            agendamento.setFuncionario(substituto);
            agendamento.setObservacao(observacao);
            agendamentoRepository.save(agendamento);
        }

        log.info("{} agendamentos transferidos do funcionário {}", agendamentosFuturos.size(), funcionarioId);
    }

    private String buscarNomeFuncionarioPorId(String funcionarioId) {
        return usuarioRepository.findById(funcionarioId)
                .map(Usuario::getName)
                .orElse("Funcionário desconhecido (ID: " + funcionarioId + ")");
    }

    private Usuario buscarFuncionarioSubstitutoDisponivel(String barbeariaId, String funcionarioExcluidoId, LocalDateTime dataHora) {
        List<FuncionarioBarbearia> vinculos = funcionarioBarbeariaRepository
                .findFuncionariosDisponiveisPorBarbearia(barbeariaId);

        for (FuncionarioBarbearia vinculo : vinculos) {
            Usuario funcionario = vinculo.getFuncionario();

            if (funcionario.getId().equals(funcionarioExcluidoId)) {
                continue;
            }

            if (!funcionario.isActive()) {
                continue;
            }

            if (!vinculo.getAtivo()) {
                continue;
            }

            long conflitos = agendamentoRepository.countAgendamentosPorFuncionarioNoHorario(
                    funcionario.getId(), dataHora);

            if (conflitos == 0) {
                return funcionario;
            }
        }
        return null;
    }
}