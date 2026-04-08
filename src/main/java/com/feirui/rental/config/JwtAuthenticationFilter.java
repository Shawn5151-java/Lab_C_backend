package com.feirui.rental.config;

import com.feirui.rental.util.JwtUtil;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.List;

/**
 * JWT 驗證過濾器
 *
 * 每個 HTTP 請求進來時，這個過濾器會先執行：
 *   1. 從請求的 Header 中尋找 Authorization: Bearer <token>
 *   2. 若有帶 Token，就用 JwtUtil 驗證是否有效
 *   3. 若有效，就把用戶的身份資訊放入 Spring Security 的上下文中
 *   4. 放入後，後續的 Controller 就知道「這個請求是誰發的」
 *
 * OncePerRequestFilter = 確保每個請求只執行一次這個過濾器（不會重複執行）
 */
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        // 1. 從 HTTP Header 取出 Authorization 的值
        //    格式應為：Bearer eyJhbGciOiJ...
        String authHeader = request.getHeader("Authorization");

        // 2. 確認 Header 存在且以 "Bearer " 開頭
        if (authHeader != null && authHeader.startsWith("Bearer ")) {

            // 3. 截取 "Bearer " 後面的 Token 字串（從第 7 個字元開始）
            String token = authHeader.substring(7);

            // 4. 驗證 Token 是否有效（格式正確、簽名正確、未過期）
            if (jwtUtil.validateToken(token)) {

                // 5. 從 Token 取出 email 和 role
                String email = jwtUtil.getEmailFromToken(token);
                String role  = jwtUtil.getRoleFromToken(token);

                // 6. 建立 Spring Security 的認證物件
                //    SimpleGrantedAuthority 代表這個用戶有「ROLE_USER」或「ROLE_ADMIN」的權限
                UsernamePasswordAuthenticationToken authentication =
                        new UsernamePasswordAuthenticationToken(
                                email,                                                    // 主體（用戶識別）
                                null,                                                     // 密碼（已驗證過，不需要再放）
                                List.of(new SimpleGrantedAuthority("ROLE_" + role))       // 權限列表
                        );

                // 7. 將認證資訊存入 Security 上下文
                //    存入後，Controller 可以用 @AuthenticationPrincipal 或 SecurityContextHolder 取得用戶資訊
                SecurityContextHolder.getContext().setAuthentication(authentication);
            }
        }

        // 8. 不管有沒有 Token，都繼續往下傳給下一個過濾器或 Controller
        filterChain.doFilter(request, response);
    }
}
