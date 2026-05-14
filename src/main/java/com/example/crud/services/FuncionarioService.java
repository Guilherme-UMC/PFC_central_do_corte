package com.example.crud.services;

import com.example.crud.domain.models.*;
import com.example.crud.domain.repositories.*;
import com.example.crud.dto.FuncionarioVinculoDTO;
import com.example.crud.dto.UserResponseDTO;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FuncionarioService {
    private final UsuarioService usuarioService;
    private final UserRepository userRepository;
    private final FuncionarioBarbeariaRepository funcionarioBarbeariaRepository;
    private final BarbeariaRepository barbeariaRepository;
    private final AgendamentoRepository agendamentoRepository;

    // Criar novo funcionário
    @Transactional
    public User criarFuncionario(String barbeariaId, User funcionario) {
        Barbearia barbearia = barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new RuntimeException("Barbearia não encontrada: " + barbeariaId));

        if (userRepository.existsByEmail(funcionario.getEmail())) {
            throw new RuntimeException("Email já cadastrado: " + funcionario.getEmail());
        }

        User savedFuncionario = userRepository.save(funcionario);

        FuncionarioBarbearia vinculo = new FuncionarioBarbearia();
        vinculo.setFuncionario(savedFuncionario);
        vinculo.setBarbearia(barbearia);
        vinculo.setAtivo(true);
        funcionarioBarbeariaRepository.save(vinculo);

        return savedFuncionario;
    }

    // Vincular funcionário existente
    @Transactional
    public void vincularFuncionarioExistente(String barbeariaId, FuncionarioVinculoDTO dto) {
        User funcionario = usuarioService.findUserEntityByEmail(dto.getFuncionarioEmail());

        if (funcionario.getRole() != UserRole.ROLE_FUNCIONARIO) {
            throw new RuntimeException("Usuário não é um funcionário");
        }

        Barbearia barbearia = barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new RuntimeException("Barbearia não encontrada"));

        if (funcionarioBarbeariaRepository.existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(
                funcionario.getId(), barbearia.getId())) {
            throw new RuntimeException("Funcionário já vinculado a esta barbearia");
        }

        FuncionarioBarbearia vinculo = new FuncionarioBarbearia();
        vinculo.setFuncionario(funcionario);
        vinculo.setBarbearia(barbearia);
        funcionarioBarbeariaRepository.save(vinculo);
    }

    // Listar funcionários de uma barbearia
    @Transactional(readOnly = true)
    public List<User> listarFuncionariosPorBarbearia(String barbeariaId) {
        barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new RuntimeException("Barbearia não encontrada: " + barbeariaId));

        List<FuncionarioBarbearia> vinculos = funcionarioBarbeariaRepository
                .findByBarbeariaIdAndAtivoTrue(barbeariaId);

        return vinculos.stream()
                .map(FuncionarioBarbearia::getFuncionario)
                .filter(User::isActive)
                .toList();
    }

    // Desvincular funcionário
    @Transactional
    public void desvincularFuncionario(String barbeariaId, String funcionarioId) {
        barbeariaRepository.findById(barbeariaId)
                .orElseThrow(() -> new RuntimeException("Barbearia não encontrada: " + barbeariaId));

        userRepository.findById(funcionarioId)
                .orElseThrow(() -> new RuntimeException("Funcionário não encontrado: " + funcionarioId));

        FuncionarioBarbearia vinculo = funcionarioBarbeariaRepository
                .findByFuncionarioIdAndBarbeariaId(funcionarioId, barbeariaId)
                .orElseThrow(() -> new RuntimeException("Vínculo não encontrado"));

        vinculo.setAtivo(false);
        funcionarioBarbeariaRepository.save(vinculo);
    }

    // Listar funcionários disponíveis
    public List<UserResponseDTO> listarFuncionariosDisponiveis() {
        return usuarioService.findFuncionariosDisponiveis();
    }

    // Verificar disponibilidade
    public boolean verificarDisponibilidadeFuncionario(String barbeariaId, String funcionarioId, LocalDateTime dataHora) {
        boolean pertence = funcionarioBarbeariaRepository
                .existsByFuncionarioIdAndBarbeariaIdAndAtivoTrue(funcionarioId, barbeariaId);

        if (!pertence) {
            return false;
        }

        LocalDateTime horaFim = dataHora.plusHours(1);
        List<Agendamento> conflitos = agendamentoRepository
                .findByFuncionarioIdAndDataHoraBetween(funcionarioId, dataHora, horaFim);

        return conflitos.isEmpty();
    }
}