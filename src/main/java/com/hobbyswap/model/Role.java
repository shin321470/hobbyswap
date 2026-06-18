package com.hobbyswap.model;

/**
 * 使用者角色。
 *
 * <p>用 enum 取代原本的字串 ("ROLE_USER" / "ROLE_ADMIN")。
 * 名稱刻意維持 {@code ROLE_} 前綴，以符合 Spring Security 的權限命名慣例
 * (SecurityConfig 中的 {@code hasRole("ADMIN")} 對應 {@code ROLE_ADMIN})。</p>
 *
 * <p>以 {@code @Enumerated(EnumType.STRING)} 存入資料庫，與舊資料相容。</p>
 */
public enum Role {
    ROLE_USER,   // 一般使用者
    ROLE_ADMIN   // 管理員
}
