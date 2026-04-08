package com.feirui.rental.service;

import com.feirui.rental.dto.auth.AuthResponse;
import com.feirui.rental.dto.auth.LoginRequest;
import com.feirui.rental.dto.auth.RegisterRequest;
import com.feirui.rental.entity.User;
import com.feirui.rental.repository.UserRepository;
import com.feirui.rental.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 認證服務
 * 負責處理會員的「註冊」和「登入」邏輯
 *
 * @Service → 標示這是服務層，Spring 會管理這個類別的生命週期
 */
@Service
@RequiredArgsConstructor
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;       // BCrypt 加密器（來自 SecurityConfig）
    private final JwtUtil jwtUtil;                       // JWT 工具
    private final AuthenticationManager authenticationManager; // Spring Security 認證管理器

    /**
     * 會員註冊
     *
     * 流程：
     *   1. 檢查 Email 是否已被使用
     *   2. 建立 User Entity，密碼用 BCrypt 加密後存入
     *   3. 儲存到資料庫
     *   4. 產生 JWT Token 並回傳（註冊後直接登入狀態）
     *
     * @param request 包含 email、password、name、phone 的註冊資料
     * @return AuthResponse（含 JWT Token 和用戶基本資訊）
     */
    public AuthResponse register(RegisterRequest request) {
        // 1. 檢查 Email 是否重複
        if (userRepository.existsByEmail(request.getEmail())) {
            throw new RuntimeException("此 Email 已被註冊：" + request.getEmail());
        }

        // 2. 建立新用戶
        User user = new User();
        user.setEmail(request.getEmail());
        user.setPassword(passwordEncoder.encode(request.getPassword())); // BCrypt 加密
        user.setName(request.getName());
        user.setPhone(request.getPhone());
        // role 預設為 USER（在 User Entity 中已設定預設值）

        // 3. 儲存到資料庫
        userRepository.save(user);

        // 4. 產生 JWT Token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // 5. 組裝並回傳 AuthResponse
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }

    /**
     * 會員登入
     *
     * 流程：
     *   1. 透過 AuthenticationManager 驗證帳號密碼
     *      （內部會呼叫 UserDetailsServiceImpl.loadUserByUsername() 查詢用戶，
     *       再用 BCrypt 比對密碼）
     *   2. 驗證成功後從資料庫查出用戶資訊
     *   3. 產生 JWT Token 並回傳
     *
     * @param request 包含 email、password 的登入資料
     * @return AuthResponse（含 JWT Token 和用戶基本資訊）
     */
    /**
     * 更改密碼
     *
     * 流程：
     *   1. 用 email 查出會員
     *   2. 用 BCrypt 比對目前密碼是否正確
     *   3. 將新密碼加密後儲存
     *
     * @param email           目前登入的會員 email（從 JWT 取出）
     * @param currentPassword 目前密碼（明文）
     * @param newPassword     新密碼（明文）
     */
    @Transactional
    public void changePassword(String email, String currentPassword, String newPassword) {
        // 1. 查出會員
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("找不到會員"));

        // 2. 驗證目前密碼
        if (!passwordEncoder.matches(currentPassword, user.getPassword())) {
            throw new RuntimeException("目前密碼錯誤");
        }

        // 3. 加密並儲存新密碼
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepository.save(user);
    }

    public AuthResponse login(LoginRequest request) {
        // 1. 讓 Spring Security 驗證帳號密碼
        //    若帳號不存在或密碼錯誤，會自動拋出 BadCredentialsException
        authenticationManager.authenticate(
                new UsernamePasswordAuthenticationToken(
                        request.getEmail(),
                        request.getPassword()
                )
        );

        // 2. 驗證通過，從資料庫查出完整用戶資訊
        User user = userRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new RuntimeException("用戶不存在"));

        // 3. 產生 JWT Token
        String token = jwtUtil.generateToken(user.getEmail(), user.getRole().name());

        // 4. 組裝並回傳 AuthResponse
        return new AuthResponse(token, user.getName(), user.getEmail(), user.getRole().name());
    }
}
