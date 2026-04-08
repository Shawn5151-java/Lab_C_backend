package com.feirui.rental.service;

import com.feirui.rental.dto.contact.ContactRequest;
import com.feirui.rental.entity.Contact;
import com.feirui.rental.repository.ContactRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

/**
 * 聯絡表單服務
 * 負責將前台送出的聯絡表單儲存到資料庫
 */
@Service
@RequiredArgsConstructor
public class ContactService {

    private final ContactRepository contactRepository;

    /**
     * 儲存聯絡表單
     *
     * 將前端傳來的 ContactRequest DTO 轉換為 Contact Entity 並存入資料庫。
     * 管理員可以在後台查看所有聯絡紀錄。
     *
     * @param request 前端傳來的表單資料
     */
    @Transactional
    public void submitContact(ContactRequest request) {
        // 將 DTO 轉換為 Entity
        Contact contact = new Contact();
        contact.setName(request.getName());
        contact.setPhone(request.getPhone());
        contact.setEmail(request.getEmail());
        contact.setSubject(request.getSubject());
        contact.setMessage(request.getMessage());
        // isRead 預設 false（在 Entity 中已設定），不需要手動設定

        contactRepository.save(contact);
    }
}
