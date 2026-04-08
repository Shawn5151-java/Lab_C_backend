package com.feirui.rental.entity;

import com.feirui.rental.enums.UserRole;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDate;
import java.time.LocalDateTime;

/**
 * 會員 Entity，對應資料庫 users 表
 * 儲存會員的帳號、密碼（BCrypt加密）、基本資料
 */
@Getter
@Setter
@Entity
@Table(name = "users")
public class User {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, unique = true, length = 100)
    private String email; // 登入帳號，全系統唯一

    /**
     * 密碼使用 BCrypt 雜湊後再存入資料庫
     * BCrypt 的特性：同一個密碼每次加密結果都不同，但都能驗證正確
     * 長度設 255 是因為 BCrypt 雜湊後的字串長度為 60 字元，給多一點空間備用
     */
    @Column(nullable = false, length = 255)
    private String password;

    @Column(nullable = false, length = 50)
    private String name; // 真實姓名

    @Column(length = 20)
    private String phone; // 手機號碼

    /**
     * 身分證號碼使用 AES-256-CBC 加密後存入資料庫
     * 加密後的字串比原本長，所以長度設為 255
     * 解密邏輯在 AesEncryptionUtil 工具類別中
     */
    @Column(name = "id_number", length = 255)
    private String idNumber;

    private LocalDate birthday; // 生日

    /**
     * @Enumerated(EnumType.STRING)：將 enum 以字串形式存入資料庫
     * 例如存 "USER" 或 "ADMIN"，而不是存數字 0、1
     * 用字串存的好處是資料庫看得懂，不會因為 enum 順序改變而出錯
     */
    @Enumerated(EnumType.STRING)
    @Column(length = 10)
    private UserRole role = UserRole.USER; // 預設為一般會員

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 註冊時間
}
