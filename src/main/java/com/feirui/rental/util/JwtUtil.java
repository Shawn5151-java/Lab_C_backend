package com.feirui.rental.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.util.Date;

/**
 * JWT（JSON Web Token）工具類別
 *
 * JWT 是什麼？
 *   一種在前後端之間傳遞身份資訊的標準格式。
 *   結構分三段：Header.Payload.Signature（用 . 分隔）
 *     - Header   = 演算法資訊
 *     - Payload  = 帶有用戶資料（email、role 等），但不加密，只是 Base64 編碼
 *     - Signature = 用金鑰簽名，防止被竄改
 *
 * 登入流程：
 *   1. 用戶輸入帳號密碼 POST /api/auth/login
 *   2. 後端驗證成功後，用此工具產生 JWT Token
 *   3. 前端收到 Token 存在 localStorage
 *   4. 之後每個需要登入的 API 請求，前端都在 Header 帶上：
 *      Authorization: Bearer <token>
 *   5. 後端的 JwtAuthenticationFilter 攔截請求，驗證 Token 是否有效
 *
 * @Value → 從 application.properties 讀取設定值，注入到這個欄位
 */
@Component
public class JwtUtil {

    /**
     * JWT 簽名金鑰，從 application.properties 讀取
     * 對應：app.jwt.secret=...
     * 建議至少 64 個字元以上（512-bit），越長越安全
     */
    @Value("${app.jwt.secret}")
    private String jwtSecret;

    /**
     * Token 有效時間（毫秒），從 application.properties 讀取
     * 對應：app.jwt.expiration-ms=86400000
     * 86400000 ms = 24 小時
     */
    @Value("${app.jwt.expiration-ms}")
    private long jwtExpirationMs;

    /**
     * 根據用戶 email 和 role 產生 JWT Token
     *
     * @param email 用戶的 email（作為 JWT 的 subject 主題）
     * @param role  用戶角色（存入 JWT Payload，供後端判斷權限）
     * @return 完整的 JWT 字串，例如 "eyJhbGciOiJ..."
     */
    public String generateToken(String email, String role) {
        return Jwts.builder()
                .subject(email)                                            // JWT 主題，通常放用戶識別資訊
                .claim("role", role)                                       // 自訂欄位：用戶角色
                .issuedAt(new Date())                                      // Token 發行時間
                .expiration(new Date(System.currentTimeMillis() + jwtExpirationMs)) // Token 到期時間
                .signWith(getSignKey())                                    // 用金鑰簽名，防止被竄改
                .compact();                                                // 產生最終的 JWT 字串
    }

    /**
     * 從 JWT Token 中取出 email（subject）
     *
     * @param token JWT 字串
     * @return 用戶的 email
     */
    public String getEmailFromToken(String token) {
        return getClaims(token).getSubject();
    }

    /**
     * 從 JWT Token 中取出 role（用戶角色）
     *
     * @param token JWT 字串
     * @return 角色字串，例如 "USER" 或 "ADMIN"
     */
    public String getRoleFromToken(String token) {
        return getClaims(token).get("role", String.class);
    }

    /**
     * 驗證 Token 是否有效
     * 有效條件：Token 格式正確 + 簽名正確 + 尚未過期
     *
     * @param token JWT 字串
     * @return true = 有效，false = 無效（過期或被竄改）
     */
    public boolean validateToken(String token) {
        try {
            getClaims(token); // 能成功解析就代表有效
            return true;
        } catch (Exception e) {
            // Token 過期、格式錯誤、簽名不符等情況都會拋出例外
            return false;
        }
    }

    /**
     * 解析 JWT Token，取出所有的 Payload 資料（Claims）
     * 這是內部輔助方法，其他 public 方法都使用它
     *
     * @param token JWT 字串
     * @return Claims 物件，包含 subject、role、issuedAt、expiration 等
     */
    private Claims getClaims(String token) {
        return Jwts.parser()
                .verifyWith(getSignKey()) // 用同一把金鑰驗證簽名
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    /**
     * 將 application.properties 中的金鑰字串轉為 SecretKey 物件
     * HMAC-SHA 演算法需要此格式
     */
    private SecretKey getSignKey() {
        return Keys.hmacShaKeyFor(jwtSecret.getBytes());
    }
}
