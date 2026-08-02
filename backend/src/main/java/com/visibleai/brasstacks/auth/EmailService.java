package com.visibleai.brasstacks.auth;

import io.micrometer.tracing.Span;
import io.micrometer.tracing.Tracer;
import jakarta.mail.*;
import jakarta.mail.internet.InternetAddress;
import jakarta.mail.internet.MimeMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.Properties;

@Service
public class EmailService {

    private static final Logger log = LoggerFactory.getLogger(EmailService.class);

    @Value("${contact.smtp.host}")
    private String smtpHost;

    @Value("${contact.smtp.port}")
    private int smtpPort;

    @Value("${contact.smtp.username}")
    private String smtpUsername;

    @Value("${contact.smtp.password}")
    private String smtpPassword;

    @Value("${app.base-url:https://brasstacks.visibleai.com}")
    private String baseUrl;

    private final Tracer tracer;

    public EmailService(Tracer tracer) {
        this.tracer = tracer;
    }

    public void sendVerificationEmail(String toEmail, String displayName, String token) {
        String verifyUrl = baseUrl + "/api/auth/verify?token=" + token;

        String html = """
                <div style="font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', Roboto, sans-serif; max-width: 480px; margin: 0 auto; padding: 32px; background: #0F172A; color: #E2E8F0; border-radius: 12px;">
                    <h1 style="color: #F8FAFC; font-size: 24px; margin-bottom: 8px;">Welcome to Brasstacks</h1>
                    <p style="color: #94A3B8; font-size: 15px; line-height: 1.6;">Hi %s,</p>
                    <p style="color: #CBD5E1; font-size: 15px; line-height: 1.6;">Thanks for signing up. Brasstacks shows you <strong style="color: #F8FAFC;">one task at a time</strong> &mdash; the right thing to focus on right now. No overwhelming lists, no guilt.</p>
                    <p style="color: #CBD5E1; font-size: 15px; line-height: 1.6;">To get started, please verify your email address:</p>
                    <a href="%s" style="display: inline-block; background: #6366F1; color: #F8FAFC; font-size: 16px; font-weight: 600; padding: 14px 32px; border-radius: 8px; text-decoration: none; margin: 24px 0;">Verify My Email</a>
                    <p style="color: #CBD5E1; font-size: 14px; line-height: 1.6; margin-top: 16px;"><strong style="color: #A5B4FC;">Quick tips to get the most out of Brasstacks:</strong></p>
                    <ul style="color: #94A3B8; font-size: 14px; line-height: 1.8; padding-left: 20px;">
                        <li>Capture tasks quickly &mdash; don't worry about organising them</li>
                        <li>Trust the Right Now screen &mdash; it picks the best task for you</li>
                        <li>Use micro-steps to break big tasks into tiny actions</li>
                        <li>Feeling overwhelmed? Tap "I'm overwhelmed" for rescue mode</li>
                        <li>Connect your own AI (Claude, OpenAI, or Gemini) in Settings to get a personal assistant that knows your tasks and can help you stay on track</li>
                    </ul>
                    <p style="color: #64748B; font-size: 13px; line-height: 1.5; margin-top: 24px;">This link expires in 24 hours. If you didn't create a Brasstacks account, you can ignore this email.</p>
                    <hr style="border: none; border-top: 1px solid #1E293B; margin: 24px 0;">
                    <p style="color: #475569; font-size: 12px;">Brasstacks &mdash; Your focus companion</p>
                </div>
                """.formatted(displayName, verifyUrl);

        Span span = tracer.nextSpan().name("email.send verification")
                .tag("email.type", "verification")
                .tag("email.recipient", toEmail)
                .tag("email.smtp.host", smtpHost)
                .start();

        try (Tracer.SpanInScope ws = tracer.withSpan(span)) {
            sendHtmlEmail(toEmail, "Verify your Brasstacks account", html);
            span.tag("email.status", "sent");
            log.info("Verification email sent to: {}", toEmail);
        } catch (Exception e) {
            span.error(e);
            span.tag("email.status", "failed");
            log.error("Failed to send verification email to: {}", toEmail, e);
        } finally {
            span.end();
        }
    }

    private void sendHtmlEmail(String to, String subject, String htmlBody) throws Exception {
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
        msg.setFrom(new InternetAddress(smtpUsername, "Brasstacks"));
        msg.setRecipient(Message.RecipientType.TO, new InternetAddress(to));
        msg.setSubject(subject);
        msg.setContent(htmlBody, "text/html; charset=UTF-8");

        Transport.send(msg);
    }
}
