package com.visibleai.brasstacks.config;

import java.util.Map;
import java.util.Properties;

import jakarta.mail.Authenticator;
import jakarta.mail.Message;
import jakarta.mail.PasswordAuthentication;
import jakarta.mail.Session;
import jakarta.mail.Transport;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class ContactController {

    private static final Logger log = LoggerFactory.getLogger(ContactController.class);

    @Value("${contact.smtp.host}")
    private String smtpHost;

    @Value("${contact.smtp.port}")
    private int smtpPort;

    @Value("${contact.smtp.username}")
    private String smtpUsername;

    @Value("${contact.smtp.password}")
    private String smtpPassword;

    @Value("${contact.recipient}")
    private String recipientEmail;

    @PostMapping("/api/contact")
    public ResponseEntity<Map<String, String>> handleContactForm(@RequestBody ContactRequest request) {
        if (request.name() == null || request.name().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Name is required."));
        }
        if (request.email() == null || request.email().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Email is required."));
        }
        if (request.message() == null || request.message().isBlank()) {
            return ResponseEntity.badRequest().body(Map.of("error", "Message is required."));
        }

        try {
            sendEmail(request);
            return ResponseEntity.ok(Map.of("status", "success", "message", "Your message has been sent."));
        } catch (Exception e) {
            log.error("Failed to send contact form email", e);
            return ResponseEntity.internalServerError()
                    .body(Map.of("error", "Failed to send your message. Please try again later."));
        }
    }

    private void sendEmail(ContactRequest request) throws Exception {
        Properties props = new Properties();
        props.put("mail.smtp.host", smtpHost);
        props.put("mail.smtp.port", String.valueOf(smtpPort));
        props.put("mail.smtp.auth", "true");
        props.put("mail.smtp.ssl.enable", "true");
        props.put("mail.smtp.socketFactory.port", String.valueOf(smtpPort));
        props.put("mail.smtp.socketFactory.class", "javax.net.ssl.SSLSocketFactory");

        Session session = Session.getInstance(props, new Authenticator() {
            @Override
            protected PasswordAuthentication getPasswordAuthentication() {
                return new PasswordAuthentication(smtpUsername, smtpPassword);
            }
        });

        MimeMessage msg = new MimeMessage(session);
        msg.setFrom(new InternetAddress(smtpUsername, "Brasstacks Contact Form"));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(recipientEmail));
        msg.setReplyTo(new InternetAddress[]{new InternetAddress(request.email(), request.name())});
        msg.setSubject("Brasstacks Contact: " + request.name());
        msg.setText(
                "Name: " + request.name() + "\n" +
                "Email: " + request.email() + "\n\n" +
                "Message:\n" + request.message()
        );

        Transport.send(msg);
        log.info("Contact form email sent from: {} <{}>", request.name(), request.email());
    }

    public record ContactRequest(String name, String email, String message) {}
}
