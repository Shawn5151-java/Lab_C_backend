package com.feirui.rental.repository;

import com.feirui.rental.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

/**
 * 會員 Repository
 */
public interface UserRepository extends JpaRepository<User, Integer> {

    /**
     * 依 Email 查詢會員
     * 用於登入驗證：用戶輸入 Email 後，先查出這筆資料，再比對密碼
     *
     * Optional<User> 的意思：查詢結果「可能有值、也可能是空的」
     * 比直接回傳 User 安全，可以避免 NullPointerException
     * 使用方式：userRepository.findByEmail(email).orElseThrow(...)
     */
    Optional<User> findByEmail(String email);

    /**
     * 確認某個 Email 是否已被註冊
     * 用於「註冊」時的重複 Email 檢查
     * 回傳 true = 已存在，false = 可以使用
     */
    boolean existsByEmail(String email);
}
