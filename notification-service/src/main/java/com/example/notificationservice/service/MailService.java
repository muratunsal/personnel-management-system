package com.example.notificationservice.service;

import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Value;
import org.thymeleaf.TemplateEngine;
import org.thymeleaf.context.Context;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import java.util.List;

@Service
public class MailService {

        private final JavaMailSender mailSender;
    private final TemplateEngine templateEngine;
    private final String username;
    
    public MailService(JavaMailSender mailSender, TemplateEngine templateEngine, 
                      @Value("${spring.mail.username}") String username) {
        this.mailSender = mailSender;
        this.templateEngine = templateEngine;
        this.username = username;
    }

    public void sendSimpleMail(String to, String subject, String text) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(username);
            message.setTo(to);
            message.setSubject(subject);
            message.setText(text);
            mailSender.send(message);
        } catch (Exception e) {
            
        }
    }

    public void sendHtmlMail(String to, String subject, String templateName, Context context) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");
            
            helper.setFrom(username);
            helper.setTo(to);
            helper.setSubject(subject);
            
            String htmlContent = templateEngine.process(templateName, context);
            helper.setText(htmlContent, true);
            
            mailSender.send(message);
        } catch (MessagingException e) {
            
        }
    }

    public void sendBulkHtmlMail(List<String> toEmails, String subject, String templateName, Context context) {
        for (String email : toEmails) {
            sendHtmlMail(email, subject, templateName, context);
        }
    }
}
