package com.centraldocorte.api.services;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class EmailService {

    private final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    private String fromEmail;

    @Value("${app.frontend.url}")
    private String frontendUrl;

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
}