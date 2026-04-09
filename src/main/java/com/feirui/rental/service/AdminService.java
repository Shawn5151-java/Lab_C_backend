package com.feirui.rental.service;

import com.feirui.rental.dto.admin.*;
import com.feirui.rental.entity.*;
import com.feirui.rental.enums.BookingStatus;
import com.feirui.rental.enums.PaymentStatus;
import com.feirui.rental.repository.*;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * 管理員後台服務
 * 提供所有後台管理功能：儀表板統計、訂單管理、車輛管理、加購項目管理、
 * 優惠活動管理、聯絡表單管理、付款紀錄管理
 */
@Service
@RequiredArgsConstructor
@Transactional
public class AdminService {

    private static final DateTimeFormatter DATE_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER =
            DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm");

    @Value("${app.upload.dir:uploads}")
    private String uploadDir;

    private final UserRepository userRepository;
    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final AddonRepository addonRepository;
    private final PromotionRepository promotionRepository;
    private final ContactRepository contactRepository;
    private final PaymentRepository paymentRepository;
    private final CarImageRepository carImageRepository;

    // ─────────────────────────────────────────────
    // 儀表板
    // ─────────────────────────────────────────────

    /**
     * 取得後台儀表板統計數據
     */
    @Transactional(readOnly = true)
    public AdminDashboardResponse getDashboard() {
        AdminDashboardResponse response = new AdminDashboardResponse();

        response.setTotalUsers(userRepository.count());
        response.setTotalBookings(bookingRepository.count());
        response.setPendingBookings(bookingRepository.countByStatus(BookingStatus.PENDING_PAYMENT));

        LocalDateTime todayStart = LocalDate.now().atStartOfDay();
        response.setTodayBookings(bookingRepository.countByCreatedAtAfter(todayStart));

        response.setUnreadContacts(contactRepository.countByIsReadFalse());

        // 計算已完成或已確認訂單的總營收
        BigDecimal revenue = bookingRepository.findAll().stream()
                .filter(b -> b.getStatus() == BookingStatus.COMPLETED
                        || b.getStatus() == BookingStatus.CONFIRMED)
                .map(Booking::getTotalPrice)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
        response.setTotalRevenue(revenue);

        return response;
    }

    // ─────────────────────────────────────────────
    // 會員管理
    // ─────────────────────────────────────────────

    /**
     * 取得所有會員列表
     */
    @Transactional(readOnly = true)
    public List<AdminUserResponse> getAllUsers() {
        return userRepository.findAll().stream()
                .map(this::toUserResponse)
                .collect(Collectors.toList());
    }

    // ─────────────────────────────────────────────
    // 訂單管理
    // ─────────────────────────────────────────────

    /**
     * 取得所有訂單列表，依建立時間倒序排列
     */
    @Transactional(readOnly = true)
    public List<AdminBookingResponse> getAllBookings() {
        return bookingRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toBookingResponse)
                .collect(Collectors.toList());
    }

    /**
     * 更新訂單狀態
     *
     * @param id     訂單 ID
     * @param status 新狀態字串（必須符合 BookingStatus 枚舉值）
     * @return 更新後的訂單資料
     */
    public AdminBookingResponse updateBookingStatus(Integer id, String status) {
        Booking booking = bookingRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到訂單 ID: " + id));

        BookingStatus newStatus;
        try {
            newStatus = BookingStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("無效的訂單狀態: " + status
                    + "，有效值為: PENDING_PAYMENT, CONFIRMED, ACTIVE, COMPLETED, CANCELLED, EXPIRED");
        }

        booking.setStatus(newStatus);
        Booking saved = bookingRepository.save(booking);
        return toBookingResponse(saved);
    }

    // ─────────────────────────────────────────────
    // 車輛管理
    // ─────────────────────────────────────────────

    /**
     * 取得所有車輛列表
     */
    @Transactional(readOnly = true)
    public List<AdminCarResponse> getAllCars() {
        List<Booking> allBookings = bookingRepository.findAll();
        return carRepository.findAll().stream()
                .map(car -> toCarResponse(car, allBookings))
                .collect(Collectors.toList());
    }

    /**
     * 切換車輛上架/下架狀態
     *
     * @param id 車輛 ID
     * @return 更新後的車輛資料
     */
    public AdminCarResponse toggleCarAvailability(Integer id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到車輛 ID: " + id));

        Boolean current = car.getIsAvailable();
        car.setIsAvailable(current == null || !current);
        Car saved = carRepository.save(car);

        List<Booking> allBookings = bookingRepository.findAll();
        return toCarResponse(saved, allBookings);
    }

    // ─────────────────────────────────────────────
    // 車輛更新
    // ─────────────────────────────────────────────

    /**
     * 更新車輛基本資料（名稱、品牌、日租金、座位數、行李描述、介紹）
     */
    public AdminCarResponse updateCar(Integer id, AdminCarUpdateRequest request) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到車輛 ID: " + id));

        car.setName(request.getName());
        car.setBrand(request.getBrand());
        car.setPricePerDay(request.getPricePerDay());
        car.setSeats(request.getSeats());
        car.setLuggageDesc(request.getLuggageDesc());
        car.setDescription(request.getDescription());

        Car saved = carRepository.save(car);
        List<Booking> allBookings = bookingRepository.findAll();
        return toCarResponse(saved, allBookings);
    }

    /**
     * 取得某台車的所有圖片列表
     */
    @Transactional(readOnly = true)
    public List<AdminCarImageResponse> getCarImages(Integer carId) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("找不到車輛 ID: " + carId));

        return car.getImages() == null ? Collections.emptyList() :
                car.getImages().stream()
                        .sorted((a, b) -> {
                            int sa = a.getSortOrder() == null ? 999 : a.getSortOrder();
                            int sb = b.getSortOrder() == null ? 999 : b.getSortOrder();
                            return Integer.compare(sa, sb);
                        })
                        .map(img -> {
                            AdminCarImageResponse r = new AdminCarImageResponse();
                            r.setId(img.getId());
                            r.setImagePath(img.getImagePath());
                            r.setSortOrder(img.getSortOrder());
                            return r;
                        })
                        .collect(Collectors.toList());
    }

    /**
     * 上傳車輛圖片
     * - 儲存到 uploads/cars/ 資料夾
     * - 在 car_images 表新增記錄，sortOrder = 當前最大值 + 1
     */
    public AdminCarImageResponse uploadCarImage(Integer carId, MultipartFile file) {
        Car car = carRepository.findById(carId)
                .orElseThrow(() -> new RuntimeException("找不到車輛 ID: " + carId));

        if (file.isEmpty()) {
            throw new RuntimeException("上傳的檔案不可為空");
        }

        // 驗證檔案類型
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.matches(".*\\.(jpg|jpeg|png|webp|gif)$")) {
            throw new RuntimeException("僅支援 jpg、jpeg、png、webp、gif 格式");
        }

        // 產生唯一檔名：時間戳_UUID_原檔名
        String ext = originalFilename.substring(originalFilename.lastIndexOf('.'));
        String filename = System.currentTimeMillis() + "_" + UUID.randomUUID().toString().replace("-", "") + ext;

        // 確保目錄存在
        Path uploadPath = Paths.get(uploadDir, "cars");
        try {
            Files.createDirectories(uploadPath);
            Path filePath = uploadPath.resolve(filename);
            Files.copy(file.getInputStream(), filePath);
        } catch (IOException e) {
            throw new RuntimeException("圖片儲存失敗: " + e.getMessage());
        }

        // 計算 sortOrder（最大值 + 1）
        int maxSort = 0;
        if (car.getImages() != null) {
            maxSort = car.getImages().stream()
                    .mapToInt(img -> img.getSortOrder() == null ? 0 : img.getSortOrder())
                    .max().orElse(-1) + 1;
        }

        // 儲存到資料庫
        CarImage carImage = new CarImage();
        carImage.setCar(car);
        carImage.setImagePath("/uploads/cars/" + filename);
        carImage.setSortOrder(maxSort);
        CarImage saved = carImageRepository.save(carImage);

        AdminCarImageResponse response = new AdminCarImageResponse();
        response.setId(saved.getId());
        response.setImagePath(saved.getImagePath());
        response.setSortOrder(saved.getSortOrder());
        return response;
    }

    /**
     * 刪除車輛圖片
     * - 從資料庫刪除記錄
     * - 若圖片是上傳的（路徑以 /uploads/ 開頭），也從檔案系統刪除
     */
    public void deleteCarImage(Integer carId, Integer imageId) {
        CarImage image = carImageRepository.findById(imageId)
                .orElseThrow(() -> new RuntimeException("找不到圖片 ID: " + imageId));

        // 確認圖片屬於該車輛
        if (!image.getCar().getId().equals(carId)) {
            throw new RuntimeException("此圖片不屬於車輛 ID: " + carId);
        }

        // 若是上傳的圖片，從磁碟刪除
        String path = image.getImagePath();
        if (path != null && path.startsWith("/uploads/")) {
            try {
                Path filePath = Paths.get(uploadDir, path.substring("/uploads/".length()));
                Files.deleteIfExists(filePath);
            } catch (IOException e) {
                // 檔案刪除失敗不影響資料庫操作，只記錄
                System.err.println("警告：無法刪除圖片檔案 " + path + ": " + e.getMessage());
            }
        }

        carImageRepository.delete(image);
    }

    // ─────────────────────────────────────────────
    // 加購項目管理
    // ─────────────────────────────────────────────

    /**
     * 取得所有加購項目列表
     */
    @Transactional(readOnly = true)
    public List<AdminAddonResponse> getAllAddons() {
        return addonRepository.findAll().stream()
                .map(this::toAddonResponse)
                .collect(Collectors.toList());
    }

    /**
     * 切換加購項目啟用/停用狀態
     *
     * @param id 加購項目 ID
     * @return 更新後的加購項目資料
     */
    public AdminAddonResponse toggleAddonActive(Integer id) {
        Addon addon = addonRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到加購項目 ID: " + id));

        Boolean current = addon.getIsActive();
        addon.setIsActive(current == null || !current);
        Addon saved = addonRepository.save(addon);
        return toAddonResponse(saved);
    }

    // ─────────────────────────────────────────────
    // 優惠活動管理
    // ─────────────────────────────────────────────

    /**
     * 取得所有優惠活動列表
     */
    @Transactional(readOnly = true)
    public List<AdminPromotionResponse> getAllPromotions() {
        return promotionRepository.findAll().stream()
                .map(this::toPromotionResponse)
                .collect(Collectors.toList());
    }

    /**
     * 切換優惠活動上架/下架狀態
     *
     * @param id 優惠活動 ID
     * @return 更新後的優惠活動資料
     */
    public AdminPromotionResponse togglePromotionActive(Integer id) {
        Promotion promotion = promotionRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到優惠活動 ID: " + id));

        Boolean current = promotion.getIsActive();
        promotion.setIsActive(current == null || !current);
        Promotion saved = promotionRepository.save(promotion);
        return toPromotionResponse(saved);
    }

    // ─────────────────────────────────────────────
    // 聯絡表單管理
    // ─────────────────────────────────────────────

    /**
     * 取得所有聯絡表單，依建立時間倒序排列
     */
    @Transactional(readOnly = true)
    public List<AdminContactResponse> getAllContacts() {
        return contactRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toContactResponse)
                .collect(Collectors.toList());
    }

    /**
     * 將聯絡表單標記為已讀
     *
     * @param id 聯絡表單 ID
     * @return 更新後的聯絡表單資料
     */
    public AdminContactResponse markContactRead(Integer id) {
        Contact contact = contactRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到聯絡表單 ID: " + id));

        contact.setIsRead(true);
        Contact saved = contactRepository.save(contact);
        return toContactResponse(saved);
    }

    // ─────────────────────────────────────────────
    // 付款紀錄管理
    // ─────────────────────────────────────────────

    /**
     * 取得所有付款紀錄，依建立時間倒序排列
     */
    @Transactional(readOnly = true)
    public List<AdminPaymentResponse> getAllPayments() {
        return paymentRepository.findAllByOrderByCreatedAtDesc().stream()
                .map(this::toPaymentResponse)
                .collect(Collectors.toList());
    }

    /**
     * 更新付款狀態
     *
     * @param id     付款紀錄 ID
     * @param status 新狀態字串（必須符合 PaymentStatus 枚舉值）
     * @return 更新後的付款紀錄資料
     */
    public AdminPaymentResponse updatePaymentStatus(Integer id, String status) {
        Payment payment = paymentRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到付款紀錄 ID: " + id));

        PaymentStatus newStatus;
        try {
            newStatus = PaymentStatus.valueOf(status.toUpperCase());
        } catch (IllegalArgumentException e) {
            throw new RuntimeException("無效的付款狀態: " + status
                    + "，有效值為: PENDING, SUCCESS, FAILED, REFUNDED");
        }

        payment.setStatus(newStatus);
        // 若狀態改為 SUCCESS 且 paidAt 尚未設定，自動記錄付款時間
        if (newStatus == PaymentStatus.SUCCESS && payment.getPaidAt() == null) {
            payment.setPaidAt(LocalDateTime.now());
        }
        Payment saved = paymentRepository.save(payment);
        return toPaymentResponse(saved);
    }

    // ─────────────────────────────────────────────
    // 私有轉換方法
    // ─────────────────────────────────────────────

    private AdminUserResponse toUserResponse(User user) {
        AdminUserResponse dto = new AdminUserResponse();
        dto.setId(user.getId());
        dto.setName(user.getName());
        dto.setEmail(user.getEmail());
        dto.setPhone(user.getPhone());
        dto.setRole(user.getRole() != null ? user.getRole().name() : null);
        dto.setCreatedAt(formatDateTime(user.getCreatedAt()));
        return dto;
    }

    private AdminBookingResponse toBookingResponse(Booking booking) {
        AdminBookingResponse dto = new AdminBookingResponse();
        dto.setId(booking.getId());
        dto.setRenterName(booking.getRenterName());
        dto.setRenterPhone(booking.getRenterPhone());
        dto.setPickupDate(formatDate(booking.getPickupDate()));
        dto.setReturnDate(formatDate(booking.getReturnDate()));
        dto.setTotalDays(booking.getTotalDays());
        dto.setPickupLocation(booking.getPickupLocation());
        dto.setTotalPrice(booking.getTotalPrice());
        dto.setStatus(booking.getStatus() != null ? booking.getStatus().name() : null);
        dto.setPaymentDeadline(formatDateTime(booking.getPaymentDeadline()));
        dto.setCreatedAt(formatDateTime(booking.getCreatedAt()));

        // 車輛資訊
        Car car = booking.getCar();
        if (car != null) {
            dto.setCarId(car.getId());
            dto.setCarName(car.getName());
            // 取第一張圖片（sort_order = 0）作為主圖
            String mainImage = null;
            if (car.getImages() != null && !car.getImages().isEmpty()) {
                mainImage = car.getImages().stream()
                        .filter(img -> img.getSortOrder() != null && img.getSortOrder() == 0)
                        .map(CarImage::getImagePath)
                        .findFirst()
                        .orElse(car.getImages().get(0).getImagePath());
            }
            dto.setCarMainImage(mainImage);
        }

        // 會員資訊（訪客訂單 user 可能為 null）
        User user = booking.getUser();
        if (user != null) {
            dto.setUserName(user.getName());
            dto.setUserEmail(user.getEmail());
        }

        // 加購項目名稱列表
        List<BookingAddon> bookingAddons = booking.getBookingAddons();
        if (bookingAddons != null && !bookingAddons.isEmpty()) {
            List<String> addonNames = bookingAddons.stream()
                    .filter(ba -> ba.getAddon() != null)
                    .map(ba -> ba.getAddon().getName())
                    .collect(Collectors.toList());
            dto.setAddonNames(addonNames);
        } else {
            dto.setAddonNames(Collections.emptyList());
        }

        return dto;
    }

    private AdminCarResponse toCarResponse(Car car, List<Booking> allBookings) {
        AdminCarResponse dto = new AdminCarResponse();
        dto.setId(car.getId());
        dto.setName(car.getName());
        dto.setBrand(car.getBrand());
        dto.setPricePerDay(car.getPricePerDay());
        dto.setSeats(car.getSeats());
        dto.setLuggageDesc(car.getLuggageDesc());
        dto.setDescription(car.getDescription());
        dto.setIsAvailable(car.getIsAvailable());

        // 分類名稱（null 安全）
        CarCategory category = car.getCategory();
        dto.setCategoryName(category != null ? category.getName() : null);

        // 主圖（sort_order = 0 的圖片）
        String mainImage = null;
        if (car.getImages() != null && !car.getImages().isEmpty()) {
            mainImage = car.getImages().stream()
                    .filter(img -> img.getSortOrder() != null && img.getSortOrder() == 0)
                    .map(CarImage::getImagePath)
                    .findFirst()
                    .orElse(car.getImages().get(0).getImagePath());
        }
        dto.setMainImage(mainImage);

        // 統計這台車的訂單總數
        long totalBookings = allBookings.stream()
                .filter(b -> b.getCar() != null && b.getCar().getId().equals(car.getId()))
                .count();
        dto.setTotalBookings((int) totalBookings);

        return dto;
    }

    private AdminAddonResponse toAddonResponse(Addon addon) {
        AdminAddonResponse dto = new AdminAddonResponse();
        dto.setId(addon.getId());
        dto.setName(addon.getName());
        dto.setPricePerDay(addon.getPricePerDay());
        dto.setDescription(addon.getDescription());
        dto.setIsActive(addon.getIsActive());
        return dto;
    }

    private AdminPromotionResponse toPromotionResponse(Promotion promotion) {
        AdminPromotionResponse dto = new AdminPromotionResponse();
        dto.setId(promotion.getId());
        dto.setSlug(promotion.getSlug());
        dto.setTitle(promotion.getTitle());
        dto.setTag(promotion.getTag());
        dto.setIntro(promotion.getIntro());
        dto.setPeriodStart(formatDate(promotion.getPeriodStart()));
        dto.setPeriodEnd(formatDate(promotion.getPeriodEnd()));
        dto.setIsActive(promotion.getIsActive());
        dto.setCreatedAt(formatDateTime(promotion.getCreatedAt()));
        return dto;
    }

    private AdminContactResponse toContactResponse(Contact contact) {
        AdminContactResponse dto = new AdminContactResponse();
        dto.setId(contact.getId());
        dto.setName(contact.getName());
        dto.setPhone(contact.getPhone());
        dto.setEmail(contact.getEmail());
        dto.setSubject(contact.getSubject());
        dto.setMessage(contact.getMessage());
        dto.setIsRead(contact.getIsRead());
        dto.setCreatedAt(formatDateTime(contact.getCreatedAt()));
        return dto;
    }

    private AdminPaymentResponse toPaymentResponse(Payment payment) {
        AdminPaymentResponse dto = new AdminPaymentResponse();
        dto.setId(payment.getId());
        dto.setAmount(payment.getAmount());
        dto.setPaymentMethod(payment.getPaymentMethod());
        dto.setStatus(payment.getStatus() != null ? payment.getStatus().name() : null);
        dto.setPaidAt(formatDateTime(payment.getPaidAt()));
        dto.setCreatedAt(formatDateTime(payment.getCreatedAt()));

        Booking booking = payment.getBooking();
        if (booking != null) {
            dto.setBookingId(booking.getId());
            dto.setRenterName(booking.getRenterName());
            Car car = booking.getCar();
            dto.setCarName(car != null ? car.getName() : null);
        }

        return dto;
    }

    // ─────────────────────────────────────────────
    // 格式化工具方法
    // ─────────────────────────────────────────────

    private String formatDate(LocalDate date) {
        if (date == null) return null;
        return date.format(DATE_FORMATTER);
    }

    private String formatDateTime(LocalDateTime dateTime) {
        if (dateTime == null) return null;
        return dateTime.format(DATETIME_FORMATTER);
    }
}
