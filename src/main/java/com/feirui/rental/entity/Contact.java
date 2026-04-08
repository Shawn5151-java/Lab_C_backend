package com.feirui.rental.entity;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.time.LocalDateTime;

/**
 * 聯絡表單 Entity，對應資料庫 contacts 表
 * 對應前台「聯絡我們」頁面（Contact_us.html）送出的表單
 * 客人填寫後資料會存在這裡，管理員從後台查看並標記已讀
 */
@Getter
@Setter
@Entity
@Table(name = "contacts")
public class Contact {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Integer id;

    @Column(nullable = false, length = 50)
    private String name; // 聯絡人姓名

    @Column(nullable = false, length = 20)
    private String phone; // 聯絡人電話

    @Column(length = 100)
    private String email; // 聯絡人 Email（選填）

    @Column(length = 50)
    private String subject; // 詢問主題，例如「租車詢問」、「投訴建議」

    @Column(nullable = false, columnDefinition = "TEXT")
    private String message; // 詢問內容

    @Column(name = "is_read")
    private Boolean isRead = false; // 是否已讀，預設為 false（未讀），管理員讀過後改為 true

    @CreationTimestamp
    @Column(name = "created_at", updatable = false)
    private LocalDateTime createdAt; // 表單送出時間
}
