package com.foodordering.auth;

import com.foodordering.User.entity.User;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.springframework.beans.factory.ObjectProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.MailException;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;

@Service
public class PasswordResetEmailService {

    private static final Logger log =
            LoggerFactory.getLogger(
                    PasswordResetEmailService.class
            );

    private final JavaMailSender mailSender;

    private final String fromAddress;

    public PasswordResetEmailService(
            ObjectProvider<JavaMailSender> mailSender,
            @Value("${app.mail.from:no-reply@foodordering.local}")
            String fromAddress
    ) {

        this.mailSender =
                mailSender.getIfAvailable();

        this.fromAddress =
                fromAddress;
    }

    public void sendPasswordResetEmail(
            User user,
            String resetLink
    ) {

        if (mailSender == null) {
            log.warn(
                    "No mail sender is configured. Password reset link for {}: {}",
                    user.getEmail(),
                    resetLink
            );

            return;
        }

        SimpleMailMessage message =
                new SimpleMailMessage();

        message.setFrom(
                fromAddress
        );

        message.setTo(
                user.getEmail()
        );

        message.setSubject(
                "Reset your Food Ordering System password"
        );

        message.setText(
                "Hello "
                        + user.getFirstName()
                        + ",\n\nUse this link to reset your password:\n"
                        + resetLink
                        + "\n\nThis link expires in 30 minutes. If you did not request it, you can ignore this email."
        );

        try {
            mailSender.send(
                    message
            );
        } catch (MailException exception) {
            log.warn(
                    "Could not send password reset email to {}. Development reset link: {}",
                    user.getEmail(),
                    resetLink,
                    exception
            );
        }
    }
}
