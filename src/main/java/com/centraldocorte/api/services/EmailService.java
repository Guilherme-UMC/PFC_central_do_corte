package com.centraldocorte.api.services;

import com.centraldocorte.api.domain.models.Agendamento;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

    private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    public void enviarEmailRecuperacaoSenha(String destinatario, String token) {
        String linkRecuperacao = frontendUrl + "/redefinir-senha?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinatario);
        message.setSubject("Redefinição de Senha - Central do Corte");
        message.setText(String.format("""
            Olá!
            
            Recebemos uma solicitação para redefinir sua senha no Central do Corte.
            
            Clique no link abaixo para criar uma nova senha:
            %s
            
            Este link é válido por 1 hora.
            
            Se você não solicitou essa alteração, ignore este email.
            
            Atenciosamente,
            Equipe Central do Corte
            """, linkRecuperacao));

        mailSender.send(message);
        log.info("Email de recuperação enviado para: {}", destinatario);
    }

    public void enviarEmailConfirmacaoCadastro(String destinatario, String token) {
        String linkConfirmacao = frontendUrl + "/confirmar-email?token=" + token;

        SimpleMailMessage message = new SimpleMailMessage();
        message.setFrom(fromEmail);
        message.setTo(destinatario);
        message.setSubject("Confirme seu cadastro - Central do Corte");
        message.setText(String.format("""
            Olá!
            
            Bem-vindo ao Central do Corte!
            
            Para ativar sua conta e começar a usar nossos serviços, clique no link abaixo:
            %s
            
            Este link é válido por 24 horas.
            
            Se você não se cadastrou em nosso sistema, ignore este e-mail.
            
            Atenciosamente,
            Equipe Central do Corte
            """, linkConfirmacao));

        mailSender.send(message);
        log.info("E-mail de confirmação de cadastro enviado para: {}", destinatario);
    }

    public void enviarEmailAgendamentoCriado(Agendamento agendamento) {
        String destinatario = agendamento.getCliente().getEmail();
        String dataHoraFormatada = agendamento.getDataHora().format(dateFormatter);

        String subject = "Agendamento Criado - Central do Corte";
        String body = String.format("""
            Olá %s!
            
            Seu agendamento foi criado com sucesso e está aguardando confirmação da barbearia.
            
            Barbearia: %s
            Serviço: %s
            Data e Hora: %s
            Profissional: %s
            
            Status: AGUARDANDO CONFIRMAÇÃO
            
            Assim que a barbearia confirmar, você receberá outra notificação.
            
            Acesse sua conta para acompanhar: %s/meus-agendamentos
            
            Atenciosamente,
            Equipe Central do Corte
            """,
                agendamento.getCliente().getName(),
                agendamento.getBarbearia().getNome(),
                agendamento.getServico().getNome(),
                dataHoraFormatada,
                agendamento.getFuncionario() != null ? agendamento.getFuncionario().getName() : "A definir",
                frontendUrl
        );

        enviarEmail(destinatario, subject, body);
    }

    public void enviarEmailAgendamentoConfirmado(Agendamento agendamento) {
        String destinatario = agendamento.getCliente().getEmail();
        String dataHoraFormatada = agendamento.getDataHora().format(dateFormatter);

        String subject = "Agendamento Confirmado - Central do Corte";
        String body = String.format("""
            Olá %s!
            
            Seu agendamento foi CONFIRMADO pela barbearia!
            
            Barbearia: %s
            Serviço: %s
            Data e Hora: %s
            Profissional: %s
            
            Status: CONFIRMADO
            
            Por favor, chegue no horário agendado. Caso precise cancelar, faça com pelo menos 24 horas de antecedência.
            
            Acesse sua conta: %s/meus-agendamentos
            
            Atenciosamente,
            Equipe Central do Corte
            """,
                agendamento.getCliente().getName(),
                agendamento.getBarbearia().getNome(),
                agendamento.getServico().getNome(),
                dataHoraFormatada,
                agendamento.getFuncionario() != null ? agendamento.getFuncionario().getName() : "A definir",
                frontendUrl
        );

        enviarEmail(destinatario, subject, body);
    }

    public void enviarEmailCancelamentoPelaBarbearia(Agendamento agendamento, String motivo) {
        String destinatario = agendamento.getCliente().getEmail();
        String dataHoraFormatada = agendamento.getDataHora().format(dateFormatter);

        String subject = "Agendamento Cancelado - Central do Corte";
        String body = String.format("""
            Olá %s!
            
            Infelizmente, a barbearia cancelou seu agendamento.
            
            Barbearia: %s
            Serviço: %s
            Data e Hora: %s
            
            Motivo do cancelamento: %s
            
            Pedimos desculpas pelo inconveniente. Você pode agendar um novo horário acessando nossa plataforma.
            
            Acesse: %s
            
            Atenciosamente,
            Equipe Central do Corte
            """,
                agendamento.getCliente().getName(),
                agendamento.getBarbearia().getNome(),
                agendamento.getServico().getNome(),
                dataHoraFormatada,
                motivo != null ? motivo : "Não informado",
                frontendUrl
        );

        enviarEmail(destinatario, subject, body);
    }

    public void enviarEmailCancelamentoPeloCliente(Agendamento agendamento, String motivo) {
        String dataHoraFormatada = agendamento.getDataHora().format(dateFormatter);

        String subject = "Cliente Cancelou Agendamento - Central do Corte";
        String bodyCliente = String.format("""
            Olá %s,
            
            Um agendamento foi CANCELADO pelo cliente.
            
            Barbearia: %s
            Cliente: %s
            Telefone: %s
            Serviço: %s
            Data e Hora: %s
            Profissional: %s
            
            Motivo do cancelamento: %s
            
            Acesse o painel para mais detalhes: %s/page/barbearia
            
            Atenciosamente,
            Equipe Central do Corte
            """,
                null,
                agendamento.getBarbearia().getNome(),
                agendamento.getCliente().getName(),
                agendamento.getCliente().getTelefone() != null ? agendamento.getCliente().getTelefone() : "Não informado",
                agendamento.getServico().getNome(),
                dataHoraFormatada,
                agendamento.getFuncionario() != null ? agendamento.getFuncionario().getName() : "Não atribuído",
                motivo != null ? motivo : "Não informado",
                frontendUrl
        );

        if (agendamento.getBarbearia().getOwner() != null) {
            String subjectOwner = "Cliente Cancelou Agendamento - " + agendamento.getBarbearia().getNome();
            String bodyOwner = String.format("""
                Olá %s,
                
                Um agendamento foi CANCELADO pelo cliente.
                
                Barbearia: %s
                Cliente: %s
                Telefone: %s
                Serviço: %s
                Data e Hora: %s
                Profissional: %s
                
                Motivo do cancelamento: %s
                
                Acesse o painel para mais detalhes: %s/page/barbearia
                
                Atenciosamente,
                Equipe Central do Corte
                """,
                    agendamento.getBarbearia().getOwner().getName(),
                    agendamento.getBarbearia().getNome(),
                    agendamento.getCliente().getName(),
                    agendamento.getCliente().getTelefone() != null ? agendamento.getCliente().getTelefone() : "Não informado",
                    agendamento.getServico().getNome(),
                    dataHoraFormatada,
                    agendamento.getFuncionario() != null ? agendamento.getFuncionario().getName() : "Não atribuído",
                    motivo != null ? motivo : "Não informado",
                    frontendUrl
            );
            enviarEmail(agendamento.getBarbearia().getOwner().getEmail(), subjectOwner, bodyOwner);
        }

        if (agendamento.getFuncionario() != null) {
            String subjectFunc = "Cliente Cancelou Agendamento - " + agendamento.getBarbearia().getNome();
            String bodyFunc = String.format("""
                Olá %s,
                
                Um agendamento foi CANCELADO pelo cliente.
                
                Barbearia: %s
                Cliente: %s
                Telefone: %s
                Serviço: %s
                Data e Hora: %s
                
                Motivo do cancelamento: %s
                
                Acesse o painel para mais detalhes: %s/page/funcionario
                
                Atenciosamente,
                Equipe Central do Corte
                """,
                    agendamento.getFuncionario().getName(),
                    agendamento.getBarbearia().getNome(),
                    agendamento.getCliente().getName(),
                    agendamento.getCliente().getTelefone() != null ? agendamento.getCliente().getTelefone() : "Não informado",
                    agendamento.getServico().getNome(),
                    dataHoraFormatada,
                    motivo != null ? motivo : "Não informado",
                    frontendUrl
            );
            enviarEmail(agendamento.getFuncionario().getEmail(), subjectFunc, bodyFunc);
        }
    }

    public void enviarEmailAgendamentoConcluido(Agendamento agendamento) {
        String destinatario = agendamento.getCliente().getEmail();
        String dataHoraFormatada = agendamento.getDataHora().format(dateFormatter);

        String subject = "Atendimento Concluído - Central do Corte";
        String body = String.format("""
            Olá %s!
            
            Seu atendimento foi concluído com sucesso!
            
            Barbearia: %s
            Serviço: %s
            Data e Hora: %s
            Profissional: %s
            
            Esperamos que tenha gostado da experiência!
            
            Avalie o atendimento: %s/avaliar/%s
            
            Atenciosamente,
            Equipe Central do Corte
            """,
                agendamento.getCliente().getName(),
                agendamento.getBarbearia().getNome(),
                agendamento.getServico().getNome(),
                dataHoraFormatada,
                agendamento.getFuncionario() != null ? agendamento.getFuncionario().getName() : "A definir",
                frontendUrl,
                agendamento.getId()
        );

        enviarEmail(destinatario, subject, body);
    }

    private void enviarEmail(String destinatario, String subject, String body) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(destinatario);
            message.setSubject(subject);
            message.setText(body);
            mailSender.send(message);
            log.info("E-mail enviado para: {} - Assunto: {}", destinatario, subject);
        } catch (Exception e) {
            log.error("Erro ao enviar e-mail para {}: {}", destinatario, e.getMessage());
        }
    }
}
