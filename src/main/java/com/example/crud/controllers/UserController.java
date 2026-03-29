package com.example.crud.controllers;

import com.example.crud.domain.user.User;
import com.example.crud.domain.user.UserRepository;
import com.example.crud.domain.user.RequestUser;
import com.example.crud.domain.user.UserRole;
import jakarta.persistence.EntityNotFoundException;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.bind.annotation.*;

import java.util.Optional;

@RestController
@RequestMapping("/users")
public class UserController {
    @Autowired
    private UserRepository repository;

    @GetMapping
    public ResponseEntity getAllUsers(){
        var allUser = repository.findAllByActiveTrue();
        return ResponseEntity.ok(allUser);
    }

    @PostMapping
    public ResponseEntity registerUser(@RequestBody @Valid RequestUser data){
        User newUser = new User(data);
        repository.save(newUser);
        return ResponseEntity.ok(newUser);
    }

    @PutMapping("/{id}")
    @Transactional
    public ResponseEntity updateUser(@PathVariable String id, @RequestBody @Valid RequestUser data){
        Optional<User> optionalUser = repository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();

            if (data.name() != null) {
                user.setName(data.name());
            }
            if (data.email() != null) {
                user.setEmail(data.email());
            }
            if (data.password() != null && !data.password().isEmpty()) {
                user.setPassword(data.password());
            }
            if (data.telefone() != null) {
                user.setTelefone(data.telefone());
            }
            if (data.role() != null) {
                user.setRole(data.role());
            }

            return ResponseEntity.ok(user);
        } else {
            throw new EntityNotFoundException("Usuário não encontrado com id: " + id);
        }
    }

    @DeleteMapping("/{id}")
    @Transactional
    public ResponseEntity deleteUser(@PathVariable String id){
        Optional<User> optionalUser = repository.findById(id);
        if (optionalUser.isPresent()) {
            User user = optionalUser.get();
            user.setActive(false);
            return ResponseEntity.noContent().build();
        } else {
            throw new EntityNotFoundException("Usuário não encontrado com id: " + id);
        }
    }

    @GetMapping("/role/{role}")
    public ResponseEntity getUsersByRole(@PathVariable UserRole role){
        var users = repository.findByRole(role);
        return ResponseEntity.ok(users);
    }

    @GetMapping("/{id}")
    public ResponseEntity getUserById(@PathVariable String id){
        Optional<User> optionalUser = repository.findById(id);
        if (optionalUser.isPresent()) {
            return ResponseEntity.ok(optionalUser.get());
        } else {
            throw new EntityNotFoundException("Usuário não encontrado com id: " + id);
        }
    }
}