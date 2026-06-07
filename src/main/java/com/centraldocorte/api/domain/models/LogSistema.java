package com.centraldocorte.api.domain.models;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

@Entity
@Table(name = "logs_sistema", indexes = {
        @Index(name = "idx_logs_tipo", columnList = "tipo"),
        @Index(name = "idx_logs_usuario", columnList = "usuario_id"),
        @Index(name = "idx_logs_data", columnList = "data_hora"),
        @Index(name = "idx_logs_entidade", columnList = "entidade")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class LogSistema {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String tipo; // AGENDAMENTO, USUARIO, BARBEARIA, SERVICO, FUNCIONARIO, LOGIN, LOGOUT

    @Column(nullable = false)
    private String acao; // CRIADO, ATUALIZADO, CANCELADO, CONFIRMADO, CONCLUIDO, DELETADO, LOGIN, LOGOUT

    @Column(name = "usuario_id", nullable = false)
    private String usuarioId;

    @Column(name = "usuario_email")
    private String usuarioEmail;

    @Column(name = "usuario_nome")
    private String usuarioNome;

    @Column(name = "usuario_role")
    private String usuarioRole;

    @Column(name = "entidade")
    private String entidade; // Nome da entidade afetada

    @Column(name = "entidade_id")
    private String entidadeId;

    @Column(name = "descricao", length = 1000)
    private String descricao;

    @Column(name = "detalhes", length = 2000)
    private String detalhes; // JSON com detalhes adicionais

    @Column(name = "ip_origem")
    private String ipOrigem;

    @Column(name = "user_agent")
    private String userAgent;

    @CreationTimestamp
    @Column(name = "data_hora", updatable = false)
    private LocalDateTime dataHora;
}