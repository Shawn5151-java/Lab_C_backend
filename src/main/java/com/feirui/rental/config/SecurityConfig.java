package com.feirui.rental.config;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security 設定類別
 *
 * 負責設定：
 *   1. 哪些 API 需要登入才能存取（路徑權限規則）
 *   2. 使用 JWT 無狀態驗證（不使用 Session）
 *   3. BCrypt 密碼加密器
 *   4. CORS 設定（允許前端頁面跨網域呼叫 API）
 *
 * @EnableWebSecurity → 啟用 Spring Security 的 Web 安全功能
 */
@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 密碼加密器 Bean
     *
     * BCryptPasswordEncoder 使用 BCrypt 演算法：
     *   - 同一個密碼每次加密結果都不同（加鹽）
     *   - 無法反向解密，只能用 matches() 比對
     *   - 強度參數預設為 10（越高越安全，但越慢）
     *
     * 使用方式（在 Service 中注入）：
     *   String hashed = passwordEncoder.encode("原始密碼");
     *   boolean match = passwordEncoder.matches("原始密碼", hashed);
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    /**
     * AuthenticationManager Bean
     * 用於登入驗證時，手動觸發帳號密碼的驗證流程
     * AuthService 的 login() 方法會用到它
     */
    @Bean
    public AuthenticationManager authenticationManager(
            AuthenticationConfiguration authenticationConfiguration) throws Exception {
        return authenticationConfiguration.getAuthenticationManager();
    }

    /**
     * 主要安全規則設定
     * Spring Security 6.x 改用 SecurityFilterChain Bean，不再繼承 WebSecurityConfigurerAdapter
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            // ── CSRF 設定 ──────────────────────────────────────────────
            // 關閉 CSRF 保護
            // CSRF 攻擊是針對「Session + Cookie」的，我們使用 JWT 所以不需要
            .csrf(csrf -> csrf.disable())

            // ── CORS 設定 ──────────────────────────────────────────────
            // 啟用跨網域存取，設定詳情在下方的 corsConfigurationSource()
            .cors(cors -> cors.configurationSource(corsConfigurationSource()))

            // ── Session 設定 ───────────────────────────────────────────
            // 使用 JWT 就不需要 Server 端的 Session，設為 STATELESS
            // 每次請求都靠 JWT Token 來驗證身份，不依賴 Session
            .sessionManagement(session ->
                    session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))

            // ── API 路徑權限規則 ────────────────────────────────────────
            .authorizeHttpRequests(auth -> auth

                // ✅ 公開 API（不需要登入）
                .requestMatchers("/api/auth/**").permitAll()          // 登入、註冊
                .requestMatchers(HttpMethod.GET, "/api/cars/**").permitAll()       // 瀏覽車輛
                .requestMatchers(HttpMethod.GET, "/api/promotions/**").permitAll() // 瀏覽優惠
                .requestMatchers(HttpMethod.GET, "/api/addons").permitAll()        // 前台加購項目列表
                .requestMatchers(HttpMethod.POST, "/api/contacts").permitAll()     // 送出聯絡表單
                .requestMatchers(HttpMethod.POST, "/api/bookings").permitAll()     // 訪客也可以建立訂單

                // 🔒 管理員專用 API（必須登入且角色為 ADMIN）
                .requestMatchers("/api/admin/**").hasRole("ADMIN")

                // 🔒 其他所有 API 都需要登入（例如查詢自己的訂單）
                .anyRequest().authenticated()
            )

            // ── JWT 過濾器 ─────────────────────────────────────────────
            .addFilterBefore(jwtAuthenticationFilter,
                    UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    /**
     * CORS（跨來源資源共享）設定
     *
     * 前端是純 HTML 開在不同的 port 或網域（例如 file:// 或 localhost:5500），
     * 要呼叫後端 API（localhost:8080）就會被瀏覽器的 CORS 政策擋住。
     * 這裡設定允許哪些來源、方法、Header 可以跨網域存取。
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();

        // 允許的前端來源（開發階段允許所有，上線後改為正式網域）
        config.setAllowedOriginPatterns(List.of("*"));

        // 允許的 HTTP 方法
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 允許的請求 Header（Authorization 用來帶 JWT Token）
        config.setAllowedHeaders(List.of("*"));

        // 允許前端帶上 Cookie 或 Authorization Header
        config.setAllowCredentials(true);

        // 套用到所有 API 路徑
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }
}
