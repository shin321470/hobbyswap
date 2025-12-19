package com.hobbyswap.service;

import java.util.List;
import com.hobbyswap.model.User;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.Collections;

@Service
public class UserService implements UserDetailsService {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    public List<User> findAllUsers() {
        return userRepository.findAll();
    }

    // 1. 註冊功能：將使用者密碼加密後存入資料庫
    public void register(User user) {
        // 密碼加密 (這是安全性的關鍵)
        user.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepository.save(user);
    }

    // 2. 登入驗證功能 (Spring Security 會自動呼叫這個方法)
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("找不到使用者: " + email));

        // 回傳 Spring Security 需要的使用者物件
        return user;
    }

    // 3. [重要] 這就是您之前報錯缺少的程式碼！
    // 輔助方法：根據 Email 查找使用者實體 (給 ItemController 刊登商品時用的)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }
}