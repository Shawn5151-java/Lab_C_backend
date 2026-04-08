package com.feirui.rental.service;

import com.feirui.rental.dto.booking.BookingResponse;
import com.feirui.rental.dto.booking.CreateBookingRequest;
import com.feirui.rental.entity.*;
import com.feirui.rental.repository.*;
import com.feirui.rental.util.AesEncryptionUtil;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 訂單服務
 * 負責處理租車訂單的建立、查詢等核心業務邏輯
 *
 * @Transactional 說明：
 *   加在 class 上代表「這個 class 所有 public 方法預設都在交易中執行」。
 *   若方法執行到一半發生例外，整個操作會自動 rollback（還原），
 *   不會留下半新增的資料（例如訂單建了但加購沒存進去）。
 */
@Service
@RequiredArgsConstructor
@Transactional
public class BookingService {

    private final BookingRepository bookingRepository;
    private final CarRepository carRepository;
    private final UserRepository userRepository;
    private final AddonRepository addonRepository;
    private final AesEncryptionUtil aesEncryptionUtil;

    /**
     * 建立新訂單
     *
     * 完整流程：
     *   1. 驗證車輛存在
     *   2. 計算租車天數與基本費用
     *   3. 計算加購費用，並預先建立 BookingAddon 物件（含價格快照）
     *   4. 設定付款期限（當下時間 + 30 分鐘）
     *   5. AES 加密承租人身分證號碼
     *   6. 若有登入（email 不為 null），關聯到會員帳號
     *   7. 將 BookingAddon 列表放入 Booking，一次儲存（JPA cascade 自動存入子表）
     *
     * @param request 前端傳來的訂單資料
     * @param email   登入用戶的 Email（從 JWT Token 取出），訪客傳 null
     * @return BookingResponse DTO
     */
    public BookingResponse createBooking(CreateBookingRequest request, String email) {
        // 1. 查詢車輛，確認存在
        Car car = carRepository.findById(request.getCarId())
                .orElseThrow(() -> new RuntimeException("找不到車輛，ID：" + request.getCarId()));

        // 2. 計算租車天數（例如 8/1 ~ 8/3 = 2 天）
        long totalDays = ChronoUnit.DAYS.between(request.getPickupDate(), request.getReturnDate());
        if (totalDays <= 0) {
            throw new RuntimeException("還車日期必須晚於取車日期");
        }

        // 3. 計算基本費用（車輛當下價格 × 天數）
        BigDecimal basePrice = car.getPricePerDay()
                .multiply(BigDecimal.valueOf(totalDays));

        // 4. 查詢加購項目，計算加購費用，並預先建立 BookingAddon 物件
        BigDecimal addonTotal = BigDecimal.ZERO;
        List<BookingAddon> bookingAddonList = new ArrayList<>(); // ← 存放要一起儲存的加購明細

        if (request.getAddonIds() != null && !request.getAddonIds().isEmpty()) {
            List<Addon> selectedAddons = addonRepository.findAllById(request.getAddonIds());

            // 驗證所有加購 ID 都存在，避免前端傳入不合法的 ID 被靜默忽略
            if (selectedAddons.size() != request.getAddonIds().size()) {
                throw new RuntimeException("部分加購項目不存在，請重新選擇");
            }

            for (Addon addon : selectedAddons) {
                // 累加加購費用：單日費用 × 租車天數
                addonTotal = addonTotal.add(
                        addon.getPricePerDay().multiply(BigDecimal.valueOf(totalDays))
                );

                // 建立加購明細物件，稍後透過 cascade 一起存入資料庫
                BookingAddon bookingAddon = new BookingAddon();
                bookingAddon.setAddon(addon);
                bookingAddon.setPriceSnapshot(addon.getPricePerDay()); // 價格快照
                // 注意：bookingAddon.setBooking() 在下方 booking 建立後才設定
                bookingAddonList.add(bookingAddon);
            }
        }

        // 5. 建立訂單主體
        Booking booking = new Booking();
        booking.setCar(car);
        booking.setPickupDate(request.getPickupDate());
        booking.setReturnDate(request.getReturnDate());
        booking.setTotalDays((int) totalDays);
        booking.setPickupLocation(request.getPickupLocation());
        booking.setRenterName(request.getRenterName());
        booking.setRenterPhone(request.getRenterPhone());

        // AES-256 加密承租人身分證號碼後存入
        booking.setRenterIdNumber(aesEncryptionUtil.encrypt(request.getRenterIdNumber()));

        // 儲存金額快照（當下的車輛售價，防止未來改價影響舊單）
        booking.setBasePrice(basePrice);
        booking.setAddonTotal(addonTotal);
        booking.setTotalPrice(basePrice.add(addonTotal));

        // 設定付款期限：當下時間 + 30 分鐘
        booking.setPaymentDeadline(LocalDateTime.now().plusMinutes(30));

        // 若有登入，關聯到會員帳號（訪客則 user 為 null）
        if (email != null) {
            userRepository.findByEmail(email).ifPresent(booking::setUser);
        }

        // 6. 將 BookingAddon 列表關聯到 Booking
        //    必須雙向設定：
        //      a. bookingAddon.setBooking(booking) → 讓 JPA 知道外鍵 booking_id 的值
        //      b. booking.setBookingAddons(list)   → 讓 cascade = ALL 在儲存 Booking 時自動儲存子表
        for (BookingAddon ba : bookingAddonList) {
            ba.setBooking(booking); // ← 設定外鍵關聯
        }
        booking.setBookingAddons(bookingAddonList); // ← 交給 cascade 一起儲存

        // 7. 儲存訂單（cascade = ALL 會自動一併儲存 booking_addons）
        Booking savedBooking = bookingRepository.save(booking);

        // 8. 取出加購項目名稱列表（用於組裝 DTO 回傳）
        List<Addon> savedAddons = bookingAddonList.stream()
                .map(BookingAddon::getAddon)
                .collect(Collectors.toList());

        return toResponse(savedBooking, savedAddons);
    }

    /**
     * 查詢單筆訂單詳情
     *
     * @param bookingId 訂單 ID
     * @return BookingResponse DTO
     */
    @Transactional(readOnly = true)
    public BookingResponse getBookingById(Integer bookingId) {
        Booking booking = bookingRepository.findById(bookingId)
                .orElseThrow(() -> new RuntimeException("找不到訂單，ID：" + bookingId));

        List<Addon> addons = booking.getBookingAddons() == null ? List.of() :
                booking.getBookingAddons().stream()
                        .map(BookingAddon::getAddon)
                        .collect(Collectors.toList());

        return toResponse(booking, addons);
    }

    /**
     * 依 Email 查詢該會員的所有訂單（給 Controller 使用）
     * Controller 從 JWT Token 取得 email 後呼叫此方法，
     * 先查出 userId 再轉發給 getUserBookings()
     *
     * @param email 會員的 Email（從 JWT Token 取出）
     * @return 訂單列表
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookingsByEmail(String email) {
        Integer userId = userRepository.findByEmail(email)
                .orElseThrow(() -> new RuntimeException("找不到會員：" + email))
                .getId();
        return getUserBookings(userId);
    }

    /**
     * 查詢某位會員的所有訂單
     *
     * @param userId 會員 ID
     * @return 訂單列表
     */
    @Transactional(readOnly = true)
    public List<BookingResponse> getUserBookings(Integer userId) {
        return bookingRepository.findByUserId(userId)
                .stream()
                .map(booking -> {
                    List<Addon> addons = booking.getBookingAddons() == null ? List.of() :
                            booking.getBookingAddons().stream()
                                    .map(BookingAddon::getAddon)
                                    .collect(Collectors.toList());
                    return toResponse(booking, addons);
                })
                .collect(Collectors.toList());
    }

    /**
     * 將 Booking Entity 轉換為 BookingResponse DTO
     *
     * @param booking 訂單 Entity
     * @param addons  此訂單的加購項目列表
     * @return BookingResponse DTO
     */
    private BookingResponse toResponse(Booking booking, List<Addon> addons) {
        BookingResponse response = new BookingResponse();
        response.setId(booking.getId());
        response.setPickupDate(booking.getPickupDate());
        response.setReturnDate(booking.getReturnDate());
        response.setTotalDays(booking.getTotalDays());
        response.setPickupLocation(booking.getPickupLocation());
        response.setBasePrice(booking.getBasePrice());
        response.setAddonTotal(booking.getAddonTotal());
        response.setTotalPrice(booking.getTotalPrice());
        response.setStatus(booking.getStatus());
        response.setPaymentDeadline(booking.getPaymentDeadline());
        response.setCreatedAt(booking.getCreatedAt());

        // 從關聯的車輛取出名稱和主圖
        if (booking.getCar() != null) {
            response.setCarName(booking.getCar().getName());
            if (booking.getCar().getImages() != null && !booking.getCar().getImages().isEmpty()) {
                booking.getCar().getImages().stream()
                        .min((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                        .ifPresent(img -> response.setCarMainImage(img.getImagePath()));
            }
        }

        // 加購項目名稱列表
        response.setAddonNames(
                addons.stream().map(Addon::getName).collect(Collectors.toList())
        );

        return response;
    }
}
