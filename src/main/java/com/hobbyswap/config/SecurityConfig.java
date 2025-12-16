package com.hobbyswap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .authorizeHttpRequests(auth -> auth
                        // 設定哪些頁面是公開的 (首頁、登入頁、註冊頁、CSS樣式、圖片、H2控制台)
                        .requestMatchers("/", "/register", "/login", "/css/**", "/images/**", "/h2-console/**").permitAll()
                        .requestMatchers("/favicon.ico").permitAll()
                        // 其他所有頁面都需要登入才能存取 (例如刊登商品、購買商品)
                        .anyRequest().authenticated()
                )
                .formLogin(form -> form
                        .loginPage("/login") // 指定我們自定義的登入頁面路徑
                        .defaultSuccessUrl("/", true) // 登入成功後強制跳轉回首頁
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutSuccessUrl("/") // 登出後跳轉回首頁
                        .permitAll()
                );

        // 為了讓 H2 Console 能正常運作，必須關閉 CSRF 和 Frame 保護 (僅限開發環境使用)
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        // 使用 BCrypt 強雜湊演算法來加密密碼
        return new BCryptPasswordEncoder();
    }
}