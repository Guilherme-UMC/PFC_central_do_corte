package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.*;
import com.centraldocorte.api.domain.repositories.AgendamentoRepository;
import com.centraldocorte.api.domain.repositories.BarbeariaRepository;
import com.centraldocorte.api.domain.repositories.FuncionarioBarbeariaRepository;
import com.centraldocorte.api.domain.repositories.UsuarioRepository;
import com.centraldocorte.api.domain.models.*;
import com.centraldocorte.api.domain.repositories.*;
import com.centraldocorte.api.dto.FuncionarioVinculoDTO;
import com.centraldocorte.api.dto.UsuarioResponseDTO;
import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioService {
    private final UsuarioService usuarioService;
    private final UsuarioRepository usuarioRepository;
    private final FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
    private final BarbeariaRepository barbeariaRepository;
    private final AgendamentoRepository agendamentoRepository;

    @Transactional
    public Usuario criarFuncionario(String barbeariaId, Usuario funcionario) {
        Barbearia barbearia = buscarBarbeariaPorId(barbeariaId);
        validarEmailDisponivel(funcionario.getEmail());

        Usuario funcionarioSalvo = usuarioRepository.save(funcionario);
        vincularFuncionarioABarbearia(funcionarioSalvo, barbearia);

        return funcionarioSalvo;
    }

    @Transactional
    public void vincularFuncionarioExistente(String barbeariaId, FuncionarioVinculoDTO dto) {
        Usuario funcionario = usuarioService.buscarPorEmail(dto.getFuncionarioEmail());
        validarRoleFuncionario(funcionario);

        Barbearia barbearia = buscarBarbeariaPorId(barbeariaId);
        validarFuncionarioNaoVinculadoABarbearia(funcionario.getId(), barbearia.getId());

        criarVinculo(funcionario, barbearia);
    }

    @Transactional(readOnly = true)
    public List<Usuario> listarFuncionariosBarbearia(String barbeariaId) {
        buscarBarbeariaPorId(barbeariaId);

        return funcionarioBarbeariaRepository.findByBarbeariaIdAndAtivoTrue(barbeariaId)
                .stream()
                .map(FuncionarioBarbearia::getFuncionario)
                .filter(Usuario::isActive)
                .toList();
    }

    @Transactional
    public void desvincularFuncionario(String barbeariaId, String funcionarioId) {
        buscarBarbeariaPorId(barbeariaId);
        buscarFuncionarioPorId(funcionarioId);

        FuncionarioBarbearia vinculo = funcionarioBarbeariaRepository
                .findByFuncionarioIdAndBarbeariaId(funcionarioId, barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Vínculo não encontrado"));

        vinculo.setAtivo(false);
        funcionarioBarbeariaRepository.save(vinculo);
    }

    @Transactional(readOnly = true)
    public List<UsuarioResponseDTO> listarFuncionariosDisponiveis() {
        return usuarioService.listarFuncionariosDisponiveis();
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

    private Barbearia buscarBarbeariaPorId(String barbeariaId) {
        return barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new ResourceNotFoundException("Barbearia não encontrada: " + barbeariaId));
    }

    private void buscarFuncionarioPorId(String funcionarioId) {
        usuarioRepository.findById(funcionarioId)
                .orElseThrow(() -> new ResourceNotFoundException("Funcionário não encontrado: " + funcionarioId));
    }

    private void validarEmailDisponivel(String email) {
        if (usuarioRepository.existsByEmail(email)) {
            throw new BusinessException("Email já cadastrado: " + email);
        }
    }

    private void validarRoleFuncionario(Usuario usuario) {
        if (usuario.getRole() != UsuarioRole.ROLE_FUNCIONARIO) {
            throw new BusinessException("Usuário não possui o papel de funcionário");
        }
    }

    private void validarFuncionarioNaoVinculadoABarbearia(String funcionarioId, String barbeariaId) {
        boolean jaVinculado = funcionarioBarbeariaRepository
                .existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(funcionarioId, barbeariaId);
        if (jaVinculado) {
            throw new BusinessException("Funcionário já está vinculado a esta barbearia");
        }
    }

    private void vincularFuncionarioABarbearia(Usuario funcionario, Barbearia barbearia) {
        FuncionarioBarbearia vinculo = new FuncionarioBarbearia();
        vinculo.setFuncionario(funcionario);
        vinculo.setBarbearia(barbearia);
        vinculo.setAtivo(true);
        funcionarioBarbeariaRepository.save(vinculo);
    }

    private void criarVinculo(Usuario funcionario, Barbearia barbearia) {
        FuncionarioBarbearia vinculo = new FuncionarioBarbearia();
        vinculo.setFuncionario(funcionario);
        vinculo.setBarbearia(barbearia);
        funcionarioBarbeariaRepository.save(vinculo);
    }
}