package com.hobbyswap.repository;

import com.hobbyswap.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.Optional;

public interface UserRepository extends JpaRepository<User, Long> {
    // 透過 Email 尋找使用者
    Optional<User> findByEmail(String email);
}