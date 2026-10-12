package com.hobbyswap;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.hobbyswap.model.ChatMessage;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import java.lang.reflect.Type;
import java.net.CookieManager;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.*;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.concurrent.*;
import java.util.regex.Pattern;

import static org.awaitility.Awaitility.await;
import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT,
        properties = "spring.datasource.url=jdbc:h2:mem:private_chat_delivery;DB_CLOSE_DELAY=-1")
@ActiveProfiles("test")
class PrivateChatDeliveryTest {
    @LocalServerPort int port;
    @Autowired ObjectMapper mapper;
    @Autowired SimpUserRegistry registry;

    @Test
    void deliversPrivateMessageToRecipientAndEchoesToSender() throws Exception {
        WebSocketStompClient client = new WebSocketStompClient(new StandardWebSocketClient());
        MappingJackson2MessageConverter converter = new MappingJackson2MessageConverter();
        converter.setObjectMapper(mapper);
        client.setMessageConverter(converter);
        StompSession alice = null;
        StompSession bob = null;
        try {
            alice = connect(client, "alice@test.com");
            bob = connect(client, "bob@test.com");
            BlockingQueue<ChatMessage> aliceMessages = new LinkedBlockingQueue<>();
            BlockingQueue<ChatMessage> bobMessages = new LinkedBlockingQueue<>();
            alice.subscribe("/user/queue/private", handler(aliceMessages));
            bob.subscribe("/user/queue/private", handler(bobMessages));
            await().atMost(Duration.ofSeconds(5)).until(() ->
                    registry.findSubscriptions(s -> "/user/queue/private".equals(s.getDestination())).size() == 2);

            ChatMessage message = new ChatMessage();
            message.setRecipient("bob@test.com");
            message.setContent("private-broker-delivery-marker");
            message.setType("CHAT");
            alice.send("/app/chat.sendPrivate", message);

            ChatMessage received = bobMessages.poll(5, TimeUnit.SECONDS);
            assertNotNull(received, "An online recipient must receive the private message without reloading");
            assertEquals("private-broker-delivery-marker", received.getContent());
            assertEquals("alice@test.com", received.getSender());
            ChatMessage echoed = aliceMessages.poll(5, TimeUnit.SECONDS);
            assertNotNull(echoed, "The sender must receive the persisted message echo");
            assertEquals(received.getContent(), echoed.getContent());
        } finally {
            if (alice != null && alice.isConnected()) alice.disconnect();
            if (bob != null && bob.isConnected()) bob.disconnect();
            client.stop();
        }
    }

    private StompSession connect(WebSocketStompClient client, String email) throws Exception {
        CookieManager cookies = new CookieManager();
        HttpClient http = HttpClient.newBuilder().cookieHandler(cookies).build();
        URI login = URI.create("http://localhost:" + port + "/login");
        String html = http.send(HttpRequest.newBuilder(login).GET().build(),
                HttpResponse.BodyHandlers.ofString()).body();
        var token = Pattern.compile("name=\"_csrf\"[^>]*value=\"([^\"]+)\"").matcher(html);
        assertTrue(token.find(), "The real login form must provide a CSRF token");
        String form = "username=" + encode(email) + "&password=" + encode("password")
                + "&_csrf=" + encode(token.group(1));
        var response = http.send(HttpRequest.newBuilder(login)
                        .header("Content-Type", "application/x-www-form-urlencoded")
                        .POST(HttpRequest.BodyPublishers.ofString(form)).build(),
                HttpResponse.BodyHandlers.discarding());
        assertEquals(302, response.statusCode());
        assertEquals("http://localhost:" + port + "/", response.headers().firstValue("Location").orElseThrow());
        WebSocketHttpHeaders headers = new WebSocketHttpHeaders();
        headers.set("Cookie", cookies.getCookieStore().getCookies().stream()
                .map(c -> c.getName() + "=" + c.getValue()).collect(java.util.stream.Collectors.joining("; ")));
        return client.connectAsync("ws://localhost:" + port + "/ws/websocket", headers,
                new StompSessionHandlerAdapter() {}).get(5, TimeUnit.SECONDS);
    }

    private static String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }

    private static StompFrameHandler handler(BlockingQueue<ChatMessage> messages) {
        return new StompFrameHandler() {
            @Override public Type getPayloadType(StompHeaders headers) { return ChatMessage.class; }
            @Override public void handleFrame(StompHeaders headers, Object payload) {
                messages.add((ChatMessage) payload);
            }
        };
    }
}
