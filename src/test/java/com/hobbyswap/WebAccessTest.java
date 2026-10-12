package com.hobbyswap;

import com.hobbyswap.model.Item;
import com.hobbyswap.model.ItemStatus;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.ItemRepository;
import com.hobbyswap.repository.OrderRepository;
import com.hobbyswap.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockHttpServletRequest;
import org.springframework.mock.web.MockHttpSession;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.LocaleResolver;

import java.util.Locale;
import java.util.UUID;

import static org.hamcrest.Matchers.containsString;
import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.csrf;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class WebAccessTest {
    @Autowired MockMvc mvc;
    @Autowired ItemRepository items;
    @Autowired OrderRepository orders;
    @Autowired UserRepository users;
    @Autowired PasswordEncoder passwordEncoder;
    @Autowired LocaleResolver localeResolver;

    @Test
    void visitorCanBrowseHomeAndForum() throws Exception {
        mvc.perform(get("/")).andExpect(status().isOk());
        mvc.perform(get("/forum")).andExpect(status().isOk());
    }

    @Test
    void visitorCanBrowseItemDetail() throws Exception {
        Item item = items.findAll().getFirst();

        mvc.perform(get("/items/{id}", item.getId())).andExpect(status().isOk());
    }

    @Test
    void visitorMustLogInBeforePostingToForum() throws Exception {
        mvc.perform(post("/forum/new").with(csrf())
                        .param("title", "Guest post")
                        .param("content", "Should be rejected"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrlPattern("**/login"));
    }

    @Test
    @WithMockUser(roles = "USER")
    void regularUserCannotOpenAdminDashboard() throws Exception {
        mvc.perform(get("/admin/dashboard")).andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(roles = "ADMIN")
    void adminMutationRequiresCsrfToken() throws Exception {
        mvc.perform(post("/admin/users/1/toggle-ban")).andExpect(status().isForbidden());
    }

    @Test
    void invalidRegistrationReturnsFieldErrors() throws Exception {
        mvc.perform(post("/register").with(csrf())
                        .param("email", "not-an-email")
                        .param("password", "123")
                        .param("name", ""))
                .andExpect(status().isOk())
                .andExpect(model().attributeHasFieldErrors("user", "email", "password", "name"));
    }

    @Test
    void validRegistrationStoresEncodedPassword() throws Exception {
        String email = "registered-" + UUID.randomUUID() + "@example.com";

        mvc.perform(post("/register").with(csrf())
                        .param("email", email)
                        .param("password", "secret")
                        .param("name", "New member"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/login"));

        User saved = users.findByEmail(email).orElseThrow();
        assertTrue(passwordEncoder.matches("secret", saved.getPassword()));
    }

    @Test
    @WithMockUser(username = "alice@test.com")
    void directCheckoutRejectsZeroQuantity() throws Exception {
        Item item = items.findAll().getFirst();
        long ordersBefore = orders.count();
        int stockBefore = item.getStockQuantity();

        mvc.perform(post("/checkout/direct/confirm").with(csrf())
                        .param("itemId", item.getId().toString())
                        .param("quantity", "0")
                        .param("receiverName", "Alice")
                        .param("phone", "0912345678")
                        .param("address", "Taipei"));

        assertEquals(ordersBefore, orders.count(), "Zero quantity must not create an order");
        assertEquals(stockBefore, items.findById(item.getId()).orElseThrow().getStockQuantity());
    }

    @Test
    @WithMockUser(username = "alice@test.com")
    void directCheckoutCreatesOrderAndMarksLastItemSold() throws Exception {
        Item item = items.findAll().getFirst();
        User alice = users.findByEmail("alice@test.com").orElseThrow();
        item.setStockQuantity(1);
        item.setStatus(ItemStatus.ON_SALE);
        items.save(item);

        mvc.perform(post("/checkout/direct/confirm").with(csrf())
                        .param("itemId", item.getId().toString())
                        .param("quantity", "1")
                        .param("receiverName", "Alice")
                        .param("phone", "0912345678")
                        .param("address", "Taipei"))
                .andExpect(status().is3xxRedirection())
                .andExpect(redirectedUrl("/mypage"));

        var buyerOrders = orders.findByUserOrderByCreatedAtDesc(alice);
        assertEquals(1, buyerOrders.size());
        assertEquals(item.getPrice(), buyerOrders.getFirst().getTotalPrice());
        assertEquals(0, items.findById(item.getId()).orElseThrow().getStockQuantity());
        assertEquals(ItemStatus.SOLD, items.findById(item.getId()).orElseThrow().getStatus());
    }

    @Test
    void localeDefaultsToTraditionalChineseAndCanSwitchToEnglishAndJapanese() throws Exception {
        assertEquals(Locale.TRADITIONAL_CHINESE,
                localeResolver.resolveLocale(new MockHttpServletRequest()));
        MockHttpSession session = new MockHttpSession();

        mvc.perform(get("/login").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("登入 HobbySwap")));
        var english = mvc.perform(get("/login?lang=en").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("LOGIN SYSTEM")))
                .andReturn();
        assertEquals(Locale.ENGLISH, localeResolver.resolveLocale(english.getRequest()));

        var japanese = mvc.perform(get("/login?lang=ja").session(session))
                .andExpect(status().isOk())
                .andExpect(content().string(containsString("<title>ログイン</title>")))
                .andReturn();
        assertEquals(Locale.JAPANESE, localeResolver.resolveLocale(japanese.getRequest()));
    }
}
