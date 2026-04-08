package com.feirui.rental.config;

import lombok.Getter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

/**
 * AES 加密設定類別
 * 從 application.properties 讀取加密金鑰，讓 AesEncryptionUtil 使用
 *
 * @Configuration           → 標示這是 Spring 設定類別，啟動時會被掃描載入
 * @ConfigurationProperties → 自動將 application.properties 中 "app.encryption" 開頭的設定
 *                            對應到這個類別的欄位
 *
 * application.properties 對應的設定：
 *   app.encryption.secret-key=FeiRui2025SecretKey!@#$%^&*ABCD  （32 個字元 = 256-bit）
 *   app.encryption.init-vector=FeiRuiIV12345678                （16 個字元 = 128-bit IV）
 */
@Getter
@Configuration
@ConfigurationProperties(prefix = "app.encryption")
public class AesEncryptionConfig {

    /**
     * AES-256 金鑰，必須是 32 個字元（32 bytes = 256 bits）
     * 對應 application.properties 的 app.encryption.secret-key
     */
    private String secretKey;

    /**
     * 初始化向量（IV），必須是 16 個字元（16 bytes = 128 bits）
     * AES-CBC 模式需要 IV 才能加密，每次加密結果會更難被破解
     * 對應 application.properties 的 app.encryption.init-vector
     */
    private String initVector;

    // Lombok @Getter 只產生 getter，這裡需要手動寫 setter
    // 因為 @ConfigurationProperties 需要 setter 才能注入值
    public void setSecretKey(String secretKey) {
        this.secretKey = secretKey;
    }

    public void setInitVector(String initVector) {
        this.initVector = initVector;
    }
}
