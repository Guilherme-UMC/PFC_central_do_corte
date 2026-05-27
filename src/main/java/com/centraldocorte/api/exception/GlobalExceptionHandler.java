package com.centraldocorte.api.exception;

import com.centraldocorte.api.exception.BusinessException;
import com.centraldocorte.api.exception.ResourceNotFoundException;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.validation.FieldError;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;

@RestControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(ResourceNotFoundException.class)
    public ResponseEntity<RespostaDeErro> tratarRecursoNaoEncontrado(ResourceNotFoundException ex) {
        RespostaDeErro erro = new RespostaDeErro(
                HttpStatus.NOT_FOUND.value(),
                "Recurso não encontrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.NOT_FOUND).body(erro);
    }

    @ExceptionHandler(ScheduleConflictException.class)
    public ResponseEntity<RespostaDeErro> tratarConflitoDeAgendamento(ScheduleConflictException ex) {
        RespostaDeErro erro = new RespostaDeErro(
                HttpStatus.CONFLICT.value(),
                "Conflito de horário",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(EmailAlreadyExistsException.class)
    public ResponseEntity<RespostaDeErro> tratarEmailJaCadastrado(EmailAlreadyExistsException ex) {
        RespostaDeErro erro = new RespostaDeErro(
                HttpStatus.CONFLICT.value(),
                "Email já cadastrado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.CONFLICT).body(erro);
    }

    @ExceptionHandler(BusinessException.class)
    public ResponseEntity<RespostaDeErro> tratarRegraDeNegocio(BusinessException ex) {
        RespostaDeErro erro = new RespostaDeErro(
                HttpStatus.UNPROCESSABLE_ENTITY.value(),
                "Erro de negócio",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNPROCESSABLE_ENTITY).body(erro);
    }

    // ✅ ADICIONAR ESTE MÉTODO
    @ExceptionHandler(UnauthorizedException.class)
    public ResponseEntity<RespostaDeErro> tratarNaoAutorizado(UnauthorizedException ex) {
        RespostaDeErro erro = new RespostaDeErro(
                HttpStatus.UNAUTHORIZED.value(),
                "Não autorizado",
                ex.getMessage(),
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
    }

    @ExceptionHandler(MethodArgumentNotValidException.class)
    public ResponseEntity<RespostaDeErroDeValidacao> tratarErroDeValidacao(MethodArgumentNotValidException ex) {
        Map<String, String> errosPorCampo = new HashMap<>();

        ex.getBindingResult().getAllErrors().forEach(erro -> {
            String nomeDoCampo = ((FieldError) erro).getField();
            String mensagemDeErro = erro.getDefaultMessage();
            errosPorCampo.put(nomeDoCampo, mensagemDeErro);
        });

        RespostaDeErroDeValidacao resposta = new RespostaDeErroDeValidacao(
                HttpStatus.BAD_REQUEST.value(),
                "Erro de validação nos campos enviados",
                errosPorCampo,
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.BAD_REQUEST).body(resposta);
    }

    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<RespostaDeErro> tratarErroDeAutenticacao(AuthenticationException ex) {
        RespostaDeErro erro = new RespostaDeErro(
                HttpStatus.UNAUTHORIZED.value(),
                "Não autenticado",
                "Credenciais inválidas ou token ausente",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(erro);
    }

    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<RespostaDeErro> tratarAcessoNegado(AccessDeniedException ex) {
        RespostaDeErro erro = new RespostaDeErro(
                HttpStatus.FORBIDDEN.value(),
                "Acesso negado",
                "Você não tem permissão para realizar esta operação",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.FORBIDDEN).body(erro);
    }

    @ExceptionHandler(Exception.class)
    public ResponseEntity<RespostaDeErro> tratarErroGenerico(Exception ex) {
        RespostaDeErro erro = new RespostaDeErro(
                HttpStatus.INTERNAL_SERVER_ERROR.value(),
                "Erro interno do servidor",
                "Ocorreu um erro inesperado. Tente novamente mais tarde.",
                LocalDateTime.now()
        );
        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(erro);
    }

    record RespostaDeErro(
            int status,
            String titulo,
            String mensagem,
            LocalDateTime timestamp
    ) {
    }

    record RespostaDeErroDeValidacao(
            int status,
            String titulo,
            Map<String, String> errosPorCampo,
            LocalDateTime timestamp
    ) {
    }
}