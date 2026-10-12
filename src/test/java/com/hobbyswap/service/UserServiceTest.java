package com.hobbyswap.service;

import com.hobbyswap.model.User;
import com.hobbyswap.repository.UserRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class UserServiceTest {
    @Mock UserRepository users;
    @Mock PasswordEncoder passwordEncoder;
    @InjectMocks UserService service;

    @Test
    void registrationStoresEncodedPasswordAndEnabledUser() {
        User user = new User();
        user.setEmail("alice@example.com");
        user.setName("Alice");
        user.setPassword("secret");
        when(passwordEncoder.encode("secret")).thenReturn("encoded-secret");

        service.register(user);

        ArgumentCaptor<User> saved = ArgumentCaptor.forClass(User.class);
        verify(users).save(saved.capture());
        assertEquals("encoded-secret", saved.getValue().getPassword());
        assertTrue(saved.getValue().isEnabled());
    }

    @Test
    void bannedUserCannotLogIn() {
        User user = new User();
        user.setEmail("banned@example.com");
        user.setEnabled(false);
        when(users.findByEmail(user.getEmail())).thenReturn(Optional.of(user));

        assertThrows(UsernameNotFoundException.class,
                () -> service.loadUserByUsername("banned@example.com"));
    }
}
