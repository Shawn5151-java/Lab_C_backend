package com.feirui.rental.service;

import com.feirui.rental.entity.User;
import com.feirui.rental.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

/**
 * Spring Security 的用戶載入服務
 *
 * UserDetailsService 是 Spring Security 的標準介面，
 * 當 AuthenticationManager 需要驗證帳號密碼時，
 * 會呼叫這裡的 loadUserByUsername() 去資料庫查詢用戶。
 *
 * 實作步驟：
 *   1. 用 Email 查詢資料庫
 *   2. 若找不到 → 拋出例外
 *   3. 若找到 → 將 User Entity 包裝成 Spring Security 認識的 UserDetails 物件回傳
 */
@Service
@RequiredArgsConstructor
public class UserDetailsServiceImpl implements UserDetailsService {

    private final UserRepository userRepository;

    /**
     * 依 Email（username）載入用戶資料
     *
     * @param email 登入時輸入的 Email
     * @return Spring Security 的 UserDetails 物件（包含帳號、密碼、權限）
     * @throws UsernameNotFoundException 若 Email 不存在則拋出此例外
     */
    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        // 從資料庫查詢用戶，若找不到就拋出例外
        User user = userRepository.findByEmail(email)
                .orElseThrow(() -> new UsernameNotFoundException("找不到用戶：" + email));

        // 將我們自己的 User Entity 轉換為 Spring Security 的 UserDetails
        // org.springframework.security.core.userdetails.User 是 Spring Security 提供的實作類別
        return new org.springframework.security.core.userdetails.User(
                user.getEmail(),   // 帳號（username）
                user.getPassword(), // 已 BCrypt 加密的密碼
                List.of(new SimpleGrantedAuthority("ROLE_" + user.getRole().name()))
                // 權限列表，格式必須是 "ROLE_xxx"，例如 "ROLE_USER" 或 "ROLE_ADMIN"
        );
    }
}
