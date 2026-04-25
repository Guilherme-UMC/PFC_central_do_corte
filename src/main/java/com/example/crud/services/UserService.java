package com.example.crud.services;

import com.example.crud.domain.user.*;
import com.example.crud.dto.UserResponseDTO;
import com.example.crud.dto.UserRequestDTO;
import com.example.crud.exception.ResourceNotFoundException;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class UserService {
    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findAll( ) {
        return userRepository.findAllByActiveTrue()
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public UserResponseDTO findById(String id){
        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(()-> new ResourceNotFoundException("Usuário não encontrado" + id));
        return toResponseDTO(user);
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findByRole(UserRole role) {
        return userRepository.findByRoleAndActiveTrue(role)
                .stream()
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional(readOnly = true)
    public List<UserResponseDTO> findByName(String name) {
        return userRepository.findByNameContainingIgnoreCase(name)
                .stream()
                .filter(User::isActive)
                .map(this::toResponseDTO)
                .collect(Collectors.toList());
    }

    @Transactional
    public UserResponseDTO update(String id, UserRequestDTO request) {
        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (request.getEmail() != null && !request.getEmail().equals(user.getEmail())) {
            if (userRepository.existsByEmail(request.getEmail())) {
                throw new RuntimeException("Email já cadastrado");
            }
        }

        updateEntityFromRequest(user, request);
        user = userRepository.save(user);

        return toResponseDTO(user);
    }

    @Transactional
    public void delete(String id) {
        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        user.setActive(false);
        userRepository.save(user);
    }

    @Transactional
    public void toggleStatus(String id) {
        User user = userRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        user.setActive(!user.isActive());
        userRepository.save(user);
    }

    @Transactional
    public void changePassword(String id, String oldPassword, String newPassword) {
        User user = userRepository.findById(id)
                .filter(User::isActive)
                .orElseThrow(() -> new ResourceNotFoundException("Usuário não encontrado: " + id));

        if (!passwordEncoder.matches(oldPassword, user.getPassword())) {
            throw new RuntimeException("Senha atual incorreta");
        }

        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public boolean existsAnyAdmin() {
        return userRepository.existsByRoleAndActiveTrue(UserRole.ROLE_ADMIN);
    }

    @Transactional
    public UserResponseDTO createUser(UserRequestDTO request) {
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("Email já cadastrado");
        }

        User newUser = new User();
        newUser.setName(request.getName());
        newUser.setEmail(request.getEmail());
        newUser.setPassword(passwordEncoder.encode(request.getPassword()));
        newUser.setTelefone(request.getTelefone());
        newUser.setRole(request.getRole() != null ? request.getRole() : UserRole.ROLE_CLIENTE);
        newUser.setActive(true);

        userRepository.save(newUser);

        return toResponseDTO(newUser);
    }

    private void updateEntityFromRequest(User user, UserRequestDTO request) {
        if (request.getName() != null) user.setName(request.getName());
        if (request.getEmail() != null) user.setEmail(request.getEmail());
        if (request.getTelefone() != null) user.setTelefone(request.getTelefone());
        if (request.getRole() != null) user.setRole(request.getRole());
    }

    private UserResponseDTO toResponseDTO(User user) {
        return UserResponseDTO.builder()
                .id(user.getId())
                .name(user.getName())
                .email(user.getEmail())
                .telefone(user.getTelefone())
                .role(user.getRole())
                .active(user.isActive())
                .build();
    }
}