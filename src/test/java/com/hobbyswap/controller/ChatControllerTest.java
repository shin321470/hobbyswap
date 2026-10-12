package com.hobbyswap.controller;

import com.hobbyswap.model.ChatMessage;
import com.hobbyswap.repository.ChatMessageRepository;
import com.hobbyswap.service.UserService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.messaging.simp.SimpMessagingTemplate;

import java.security.Principal;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ChatControllerTest {
    @Mock ChatMessageRepository messages;
    @Mock UserService users;
    @Mock SimpMessagingTemplate messaging;
    @InjectMocks ChatController controller;

    private final Principal alice = () -> "alice@example.com";

    @Test
    void publicMessageUsesAuthenticatedSenderAndIsSaved() {
        ChatMessage message = new ChatMessage();
        message.setSender("spoofed@example.com");
        message.setContent("Hello");
        when(messages.save(any(ChatMessage.class))).thenAnswer(invocation -> invocation.getArgument(0));

        ChatMessage result = controller.sendMessage(message, alice);

        assertEquals("alice@example.com", result.getSender());
        assertNotNull(result.getTimestamp());
        verify(messages).save(result);
    }

    @Test
    void privateMessageIsSavedAndDeliveredToBothParticipants() {
        ChatMessage message = new ChatMessage();
        message.setRecipient("bob@example.com");
        message.setContent("Private hello");

        controller.sendPrivateMessage(message, alice);

        assertEquals("alice@example.com", message.getSender());
        assertNotNull(message.getTimestamp());
        verify(messages).save(message);
        verify(messaging).convertAndSendToUser("bob@example.com", "/queue/private", message);
        verify(messaging).convertAndSendToUser("alice@example.com", "/queue/private", message);
    }
}
