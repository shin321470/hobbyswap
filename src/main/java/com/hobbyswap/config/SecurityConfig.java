package com.hobbyswap.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.annotation.Order;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    // ▼▼▼ 第一條鏈：專門處理後台管理員 (優先級最高 Order=1) ▼▼▼
    @Bean
    @Order(1)
    public SecurityFilterChain adminFilterChain(HttpSecurity http) throws Exception {
        http
                // 1. 告訴 Spring Security：這條規則只管 /admin 開頭的網址
                .securityMatcher("/admin/**")

                .authorizeHttpRequests(auth -> auth
                        // 允許任何人訪問管理員登入頁面 (不然連登入都看不到)
                        .requestMatchers("/admin/login", "/css/**", "/images/**").permitAll()
                        // 其他所有 /admin/** 的頁面，都必須要有 ADMIN 角色
                        .requestMatchers("/admin/**").hasRole("ADMIN")
                )
                .formLogin(login -> login
                        .loginPage("/admin/login")        // ★ 設定管理員專用登入頁 URL
                        .loginProcessingUrl("/admin/login") // ★ 表單提交的 URL
                        .defaultSuccessUrl("/admin/dashboard", true) // ★ 登入成功後去後台儀表板
                        .failureUrl("/admin/login?error=true") // 登入失敗留在後台登入頁
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/admin/logout")       // ★ 管理員專用登出 URL
                        .logoutSuccessUrl("/admin/login?logout=true") // 登出後回到後台登入頁
                        .permitAll()
                );

        return http.build();
    }

    // ▼▼▼ 第二條鏈：處理前台一般使用者 (優先級較低 Order=2) ▼▼▼
    @Bean
    @Order(2)
    public SecurityFilterChain userFilterChain(HttpSecurity http) throws Exception {
        http
                // 這裡不用 securityMatcher，因為它會接住所有剩下沒被上面攔截的請求
                .authorizeHttpRequests(auth -> auth
                        // 公開頁面設定
                        .requestMatchers("/", "/register", "/login","/forum/**", "/css/**", "/images/**", "/uploads/**", "/h2-console/**", "/favicon.ico").permitAll()
                        // 其他都要登入
                        .anyRequest().authenticated()
                )
                .formLogin(login -> login
                        .loginPage("/login")              // ★ 一般使用者登入頁
                        .loginProcessingUrl("/login")
                        .defaultSuccessUrl("/", true)     // ★ 一般人登入後回首頁
                        .failureUrl("/login?error=true")
                        .permitAll()
                )
                .logout(logout -> logout
                        .logoutUrl("/logout")             // ★ 一般使用者登出
                        .logoutSuccessUrl("/")            // 登出後回首頁
                        .permitAll()
                );

        // H2 Console 的特殊設定 (放在一般通道即可)
        http.csrf(csrf -> csrf.ignoringRequestMatchers("/h2-console/**"));
        http.headers(headers -> headers.frameOptions(frame -> frame.disable()));

        return http.build();
    }
}