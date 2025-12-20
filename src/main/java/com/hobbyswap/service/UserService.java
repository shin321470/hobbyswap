package com.hobbyswap.service;

import java.util.ArrayList;
import java.util.List;
import com.hobbyswap.model.User;
import com.hobbyswap.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

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

        // ▼ 檢查帳號是否被封鎖
        if (!user.isEnabled()) {
            throw new UsernameNotFoundException("此帳號已被封鎖 (Account Banned)");
        }

        // ▼ 設定角色權限 (Authority)
        List<GrantedAuthority> authorities = new ArrayList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole()));


        // 回傳 Spring Security 需要的使用者物件
        return user;
    }

    public User findById(Long id) {
        return userRepository.findById(id).orElse(null);
    }

    @Transactional// 2. 儲存使用者 (用於封鎖/解鎖後的存檔)
    public void save(User user) {
        userRepository.save(user);
    }

    // 輔助方法：根據 Email 查找使用者實體 (給 ItemController 刊登商品時用的)
    public User findByEmail(String email) {
        return userRepository.findByEmail(email).orElse(null);
    }


}