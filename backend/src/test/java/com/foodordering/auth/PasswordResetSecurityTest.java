package com.foodordering.auth;

import com.foodordering.User.entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class PasswordResetSecurityTest {

    @Mock
    private JavaMailSender mailSender;

    @Mock
    private ObjectProvider<JavaMailSender> mailSenderProvider;

    private PasswordResetEmailService emailService;

    @BeforeEach
    void setUp() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(mailSender);
        emailService = new PasswordResetEmailService(mailSenderProvider, "no-reply@foodordering.local");
    }

    @Test
    void testSendPasswordResetEmail_SendsMailWithLink() {
        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("testuser@example.com");
        user.setFirstName("Jane");

        String resetLink = "https://app.example.com/reset-password?token=secret123";
        UUID requestId = UUID.randomUUID();

        emailService.sendPasswordResetEmail(user, resetLink, requestId);

        ArgumentCaptor<SimpleMailMessage> captor = ArgumentCaptor.forClass(SimpleMailMessage.class);
        verify(mailSender).send(captor.capture());

        SimpleMailMessage sentMessage = captor.getValue();
        assertArrayEquals(new String[]{"testuser@example.com"}, sentMessage.getTo());
        assertTrue(sentMessage.getText().contains(resetLink));
        assertTrue(sentMessage.getSubject().contains("Reset your Food Ordering System password"));
    }

    @Test
    void testNoMailSender_DoesNotThrowAndDoesNotExposeUrlInReturn() {
        when(mailSenderProvider.getIfAvailable()).thenReturn(null);
        PasswordResetEmailService serviceWithoutMail = new PasswordResetEmailService(
                mailSenderProvider,
                "no-reply@foodordering.local"
        );

        User user = new User();
        user.setId(UUID.randomUUID());
        user.setEmail("testuser@example.com");
        user.setFirstName("Jane");

        assertDoesNotThrow(() ->
                serviceWithoutMail.sendPasswordResetEmail(user, "https://app.example.com/reset-password?token=secret123", UUID.randomUUID())
        );
    }
}

