package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.*;
import com.centraldocorte.api.domain.models.enums.UsuarioRole;
import com.centraldocorte.api.domain.repositories.AgendamentoRepository;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.FuncionarioBarbeariaRepository;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.dto.*;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class FuncionarioService {

    private final AuthService authService;
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
    private final BarbeariaRepository barbeariaRepository;
    private final AgendamentoRepository agendamentoRepository;

    @Transactional
    public UsuarioResponseDTO criarFuncionario(String barbeariaId, RegisterRequestDTO request) {
        log.info("Criando novo funcionário para barbearia: {}", barbeariaId);

        Barbearia barbearia = buscarBarbeariaPorId(barbeariaId);
        authService.registrarUsuario(request, UsuarioRole.ROLE_FUNCIONARIO);
        Usuario funcionario = usuarioService.buscarPorEmail(request.email());

        criarVinculo(funcionario, barbearia);

        return usuarioService.converterParaResponseDTO(funcionario);
    }

    @Transactional
    public void vincularFuncionarioExistente(String barbeariaId, FuncionarioVinculoDTO dto) {
        log.info("Vinculando funcionário existente à barbearia: {} - Email: {}", barbeariaId, dto.getFuncionarioEmail());

        Usuario funcionario = usuarioService.buscarUsuarioPorEmailIncluindoInativos(dto.getFuncionarioEmail());
        Barbearia barbearia = buscarBarbeariaPorId(barbeariaId);

        validarFuncionarioParaVinculo(funcionario);
        validarVinculoAtivo(funcionario.getId(), barbeariaId);

        var vinculoInativo = funcionarioBarbeariaRepository
                .findByFuncionarioIdAndBarbeariaId(funcionario.getId(), barbeariaId);

        if (vinculoInativo.isPresent() && !vinculoInativo.get().getAtivo()) {
            reativarVinculo(vinculoInativo.get());

            if (!funcionario.isActive()) {
                usuarioService.setActiveStatus(funcionario.getId(), true);
                log.info("Usuário reativado durante vinculação");
            }
            log.info("Vínculo anterior reativado para funcionário: {}", funcionario.getId());
        } else {
            criarVinculo(funcionario, barbearia);
            log.info("Novo vínculo criado para funcionário: {}", funcionario.getId());
        }
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarFuncionariosPorBarbearia(String barbeariaId) {
        log.debug("Listando funcionários da barbearia: {}", barbeariaId);

        validarBarbeariaExistente(barbeariaId);

        return funcionarioBarbeariaRepository.findByBarbeariaIdAndAtivoTrue(barbeariaId)
                .stream()
                .map(FuncionarioBarbearia::getFuncionario)
                .filter(Usuario::isActive)
                .map(usuarioService::converterParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarFuncionariosDisponiveisParaAgendamento(String barbeariaId) {
        log.debug("Listando funcionários disponíveis para agendamento na barbearia: {}", barbeariaId);

        validarBarbeariaExistente(barbeariaId);

        return funcionarioBarbeariaRepository.findFuncionariosDisponiveisPorBarbearia(barbeariaId)
                .stream()
                .map(FuncionarioBarbearia::getFuncionario)
                .filter(Usuario::isActive)
                .map(usuarioService::converterParaResponseDTO)
                .toList();
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarFuncionariosDisponiveisParaContratacao() {
        log.debug("Listando funcionários disponíveis para contratação");

        return usuarioRepository.findByRoleAndActiveTrue(UsuarioRole.ROLE_FUNCIONARIO)
                .stream()
                .filter(funcionario -> !possuiVinculoAtivo(funcionario.getId()))
                .map(usuarioService::converterParaResponseDTO)
                .toList();
    }

    @Transactional
    public void desvincularFuncionario(String barbeariaId, String funcionarioId) {
        log.info("Desvinculando funcionário {} da barbearia {}", funcionarioId, barbeariaId);

        validarFuncionarioExiste(funcionarioId);
        validarBarbeariaExistente(barbeariaId);

        LocalDateTime agora = LocalDateTime.now();
        List<Agendamento> agendamentosFuturos = agendamentoRepository
                .findFutureAgendamentosByFuncionarioId(funcionarioId, agora);

        if (!agendamentosFuturos.isEmpty()) {
            transferirAgendamentos(funcionarioId, barbeariaId, agendamentosFuturos);
        }

        FuncionarioBarbearia vinculo = funcionarioBarbeariaRepository
                .findByFuncionarioIdAndBarbeariaIdAndAtivoTrue(funcionarioId, barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo ativo não encontrado"));

        vinculo.setAtivo(false);
        funcionarioBarbeariaRepository.save(vinculo);

        log.info("Funcionário desvinculado com sucesso");
    }

    private void transferirAgendamentos(String funcionarioId,
                                        String barbeariaId,
                                        List<Agendamento> agendamentos) {
        String nomeFuncionarioOriginal = buscarNomeFuncionarioPorId(funcionarioId);

        for (Agendamento agendamento : agendamentos) {
            Usuario substituto = buscarFuncionarioSubstitutoDisponivel(
                    barbeariaId,
                    funcionarioId,
                    agendamento.getDataHora()
            );

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
    }

    private String buscarNomeFuncionarioPorId(String funcionarioId) {
        return usuarioRepository.findById(funcionarioId)
                .map(Usuario::getName)
                .orElse("Funcionário desconhecido (ID: " + funcionarioId + ")");
    }

    private Usuario buscarFuncionarioSubstitutoDisponivel(String barbeariaId,
                                                          String funcionarioExcluidoId,
                                                          LocalDateTime dataHora) {
        List<FuncionarioBarbearia> vinculos = funcionarioBarbeariaRepository
                .findFuncionariosDisponiveisPorBarbearia(barbeariaId);

        for (FuncionarioBarbearia vinculo : vinculos) {
            Usuario funcionario = vinculo.getFuncionario();

            if (funcionario.getId().equals(funcionarioExcluidoId)) {
                continue;
            }

            if (!funcionario.isActive()) {
                log.debug("Funcionário {} está inativo no sistema, ignorando", funcionario.getId());
                continue;
            }

            if (!vinculo.getAtivo()) {
                log.debug("Vínculo do funcionário {} está inativo, ignorando", funcionario.getId());
                continue;
            }

            long conflitos = agendamentoRepository.countAgendamentosPorFuncionarioNoHorario(
                    funcionario.getId(), dataHora);

            if (conflitos == 0) {
                log.info("Substituto encontrado: {} (ID: {})", funcionario.getName(), funcionario.getId());
                return funcionario;
            }
        }

        log.warn("Nenhum substituto disponível encontrado para barbearia {} no horário {}", barbeariaId, dataHora);
        return null;
    }

    @Transactional
    public void desativarFuncionario(String barbeariaId, String funcionarioId) {
        log.info("Desativando funcionário {} da barbearia {}", funcionarioId, barbeariaId);

        validarFuncionarioExiste(funcionarioId);
        validarBarbeariaExistente(barbeariaId);

        FuncionarioBarbearia vinculo = funcionarioBarbeariaRepository
                .findByFuncionarioIdAndBarbeariaIdAndAtivoTrue(funcionarioId, barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo ativo não encontrado"));

        vinculo.setAtivo(false);
        funcionarioBarbeariaRepository.save(vinculo);

        usuarioService.setActiveStatus(funcionarioId, false);
        log.info("Funcionário desativado com sucesso");
    }

    @Transactional
    public void reativarFuncionario(String barbeariaId, String funcionarioId) {
        log.info("Reativando funcionário {} na barbearia {}", funcionarioId, barbeariaId);

        Usuario funcionario = usuarioService.buscarUsuarioPorIdIncluindoInativos(funcionarioId);

        if (!funcionario.isActive()) {
            usuarioService.setActiveStatus(funcionarioId, true);
            log.info("Conta do usuário reativada");
        }

        var vinculoOpt = funcionarioBarbeariaRepository
                .findByFuncionarioIdAndBarbeariaId(funcionarioId, barbeariaId);

        if (vinculoOpt.isPresent()) {
            FuncionarioBarbearia vinculo = vinculoOpt.get();
            if (!vinculo.getAtivo()) {
                reativarVinculo(vinculo);
                log.info("Vínculo reativado");
            }
        } else {
            Barbearia barbearia = buscarBarbeariaPorId(barbeariaId);
            criarVinculo(funcionario, barbearia);
            log.info("Novo vínculo criado durante reativação");
        }
    }

    @Transactional
    public void alternarDisponibilidadeFuncionario(String barbeariaId, String funcionarioId) {
        log.info("Alternando disponibilidade do funcionário {} na barbearia {}", funcionarioId, barbeariaId);

        Usuario funcionario = usuarioService.buscarUsuarioPorIdIncluindoInativos(funcionarioId);

        if (!funcionario.isActive()) {
            throw new BusinessException(
                    String.format("Não é possível alterar disponibilidade de funcionário inativo: %s", funcionarioId));
        }

        FuncionarioBarbearia vinculo = funcionarioBarbeariaRepository
                .findByFuncionarioIdAndBarbeariaIdAndAtivoTrue(funcionarioId, barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo ativo não encontrado"));

        vinculo.setDisponivel(!vinculo.getDisponivel());
        funcionarioBarbeariaRepository.save(vinculo);

        String status = vinculo.getDisponivel() ? "disponível" : "indisponível";
        log.info("Funcionário agora está {} para agendamentos", status);
    }

    private Barbearia buscarBarbeariaPorId(String id) {
        return barbeariaRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada: " + id));
    }

    private void validarBarbeariaExistente(String id) {
        if (!barbeariaRepository.existsById(id)) {
            throw new ResourceNotFoundException("Barbearia não encontrada: " + id);
        }
    }

    private void validarFuncionarioExiste(String id) {
        if (!usuarioRepository.existsById(id)) {
            throw new ResourceNotFoundException("Funcionário não encontrado: " + id);
        }
    }

    private void validarFuncionarioParaVinculo(Usuario funcionario) {
        if (funcionario.getRole() != UsuarioRole.ROLE_FUNCIONARIO) {
            throw new BusinessException(
                    String.format("Usuário %s não possui papel de funcionário", funcionario.getEmail()));
        }
    }

    private void validarVinculoAtivo(String funcionarioId, String barbeariaId) {
        boolean jaVinculado = funcionarioBarbeariaRepository
                .existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(funcionarioId, barbeariaId);

        if (jaVinculado) {
            throw new BusinessException("Funcionário já está vinculado ativamente a esta barbearia");
        }
    }

    private FuncionarioBarbearia criarVinculo(Usuario funcionario, Barbearia barbearia) {
        FuncionarioBarbearia vinculo = FuncionarioBarbearia.builder()
                .funcionario(funcionario)
                .barbearia(barbearia)
                .ativo(true)
                .disponivel(true)
                .build();

        return funcionarioBarbeariaRepository.save(vinculo);
    }

    private void reativarVinculo(FuncionarioBarbearia vinculo) {
        vinculo.setAtivo(true);
        vinculo.setDisponivel(true);
        funcionarioBarbeariaRepository.save(vinculo);
    }

    private boolean isFuncionarioAtivoEDisponivel(String funcionarioId, String barbeariaId) {
        if (!usuarioService.existsById(funcionarioId)) {
            return false;
        }

        Usuario funcionario = usuarioService.buscarUsuarioPorIdIncluindoInativos(funcionarioId);
        if (!funcionario.isActive()) {
            return false;
        }

        return funcionarioBarbeariaRepository
                .existsByFuncionarioIdAndBarbeariaIdAndAtivoTrueAndDisponivelTrue(
                        funcionarioId, barbeariaId);
    }

    private boolean possuiVinculoAtivo(String funcionarioId) {
        return funcionarioBarbeariaRepository.existsByFuncionarioIdAndAtivoTrue(funcionarioId);
    }

    public boolean verificarDisponibilidadeDoFuncionario(String barbeariaId, String funcionarioId, LocalDateTime dataHora) {
        boolean pertenceABarbearia = funcionarioBarbeariaRepository
                .existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(funcionarioId, barbeariaId);

        if (!pertenceABarbearia) {
            return false;
        }

        LocalDateTime horaFim = dataHora.plusHours(1);

        List<Agendamento> agendamentosConflitantes = agendamentoRepository
                .findByFuncionarioIdAndDataHoraBetween(funcionarioId, dataHora, horaFim);

        return agendamentosConflitantes.isEmpty();
    }

    @Transactional(readOnly = true)
    public List<BarbeariaResponseDTO> listarBarbeariasPorFuncionario(String funcionarioId) {
        log.debug("Listando barbearias do funcionário: {}", funcionarioId);

        validarFuncionarioExiste(funcionarioId);

        return funcionarioBarbeariaRepository.findByFuncionarioIdAndAtivoTrue(funcionarioId)
                .stream()
                .map(FuncionarioBarbearia::getBarbearia)
                .map(this::converterBarbeariaParaResponseDTO)
                .collect(Collectors.toList());
    }

    private BarbeariaResponseDTO converterBarbeariaParaResponseDTO(Barbearia barbearia) {
        return BarbeariaResponseDTO.builder()
                .id(barbearia.getId())
                .ownerId(barbearia.getOwner() != null ? barbearia.getOwner().getId() : null)
                .ownerName(barbearia.getOwner() != null ? barbearia.getOwner().getName() : null)
                .nome(barbearia.getNome())
                .descricao(barbearia.getDescricao())
                .logradouro(barbearia.getLogradouro())
                .numero(barbearia.getNumero())
                .bairro(barbearia.getBairro())
                .cep(barbearia.getCep())
                .cidade(barbearia.getCidade())
                .uf(barbearia.getUf())
                .imgUrl(barbearia.getImgUrl())
                .telefone(barbearia.getTelefone())
                .criadoEm(barbearia.getCriadoEm())
                .atualizadoEm(barbearia.getAtualizadoEm())
                .ativo(barbearia.getAtivo())
                .build();
    }
}