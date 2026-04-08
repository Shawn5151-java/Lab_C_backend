package com.feirui.rental.util;

import com.feirui.rental.config.AesEncryptionConfig;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import javax.crypto.Cipher;
import javax.crypto.spec.IvParameterSpec;
import javax.crypto.spec.SecretKeySpec;
import java.util.Base64;

/**
 * AES-256-CBC 加解密工具
 *
 * 用途：
 *   - 加密會員身分證號碼（users.id_number）
 *   - 加密訂單中承租人身分證號碼（bookings.renter_id_number）
 *
 * 為什麼用 AES 而不是 BCrypt？
 *   - BCrypt 是「單向雜湊」，加密後無法還原，適合密碼
 *   - AES 是「雙向加密」，可以解密還原原始資料，適合需要看到原文的資料（如身分證）
 *
 * AES-256-CBC 說明：
 *   - AES-256 = 使用 256-bit（32 bytes）金鑰，安全強度最高
 *   - CBC（Cipher Block Chaining）= 每個區塊加密都依賴前一個區塊，
 *     搭配 IV（初始化向量）讓相同明文每次加密結果不同
 *
 * @Component → 讓 Spring 管理這個類別的生命週期，可以在其他地方用 @Autowired 注入
 * @RequiredArgsConstructor → Lombok：自動產生包含 final 欄位的建構子（用於依賴注入）
 */
@Component
@RequiredArgsConstructor
public class AesEncryptionUtil {

    // 從 AesEncryptionConfig 讀取 application.properties 裡的金鑰設定
    private final AesEncryptionConfig config;

    // 加密演算法規格：AES / CBC 模式 / PKCS5Padding 填充
    private static final String ALGORITHM = "AES/CBC/PKCS5Padding";

    /**
     * 加密方法：將明文字串加密為 Base64 字串
     *
     * 流程：
     *   1. 將金鑰字串轉為 SecretKeySpec（AES 金鑰物件）
     *   2. 將 IV 字串轉為 IvParameterSpec（初始化向量物件）
     *   3. 初始化 Cipher 為加密模式
     *   4. 執行加密，得到 byte 陣列
     *   5. 將 byte 陣列轉為 Base64 字串後存入資料庫
     *
     * @param plainText 原始明文，例如身分證號碼「A123456789」
     * @return Base64 編碼的加密字串，例如「xW3k9...」
     */
    public String encrypt(String plainText) {
        try {
            // 建立 AES 金鑰物件，金鑰來自 application.properties
            SecretKeySpec keySpec = new SecretKeySpec(
                    config.getSecretKey().getBytes("UTF-8"), "AES"
            );

            // 建立初始化向量物件
            IvParameterSpec ivSpec = new IvParameterSpec(
                    config.getInitVector().getBytes("UTF-8")
            );

            // 初始化加密器
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.ENCRYPT_MODE, keySpec, ivSpec);

            // 執行加密並轉為 Base64 字串（Base64 才能安全存入資料庫文字欄位）
            byte[] encrypted = cipher.doFinal(plainText.getBytes("UTF-8"));
            return Base64.getEncoder().encodeToString(encrypted);

        } catch (Exception e) {
            throw new RuntimeException("加密失敗：" + e.getMessage(), e);
        }
    }

    /**
     * 解密方法：將 Base64 加密字串還原為明文
     *
     * @param encryptedText 加密後的 Base64 字串（從資料庫讀出的值）
     * @return 原始明文，例如身分證號碼「A123456789」
     */
    public String decrypt(String encryptedText) {
        try {
            SecretKeySpec keySpec = new SecretKeySpec(
                    config.getSecretKey().getBytes("UTF-8"), "AES"
            );

            IvParameterSpec ivSpec = new IvParameterSpec(
                    config.getInitVector().getBytes("UTF-8")
            );

            // 初始化解密器（改為 DECRYPT_MODE）
            Cipher cipher = Cipher.getInstance(ALGORITHM);
            cipher.init(Cipher.DECRYPT_MODE, keySpec, ivSpec);

            // 將 Base64 字串轉回 byte 陣列，再解密還原為原始字串
            byte[] original = cipher.doFinal(Base64.getDecoder().decode(encryptedText));
            return new String(original, "UTF-8");

        } catch (Exception e) {
            throw new RuntimeException("解密失敗：" + e.getMessage(), e);
        }
    }
}
