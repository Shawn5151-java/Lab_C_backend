package com.feirui.rental.service;

import com.feirui.rental.dto.car.CarResponse;
import com.feirui.rental.entity.Car;
import com.feirui.rental.entity.CarImage;
import com.feirui.rental.repository.CarRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;

/**
 * 車輛服務
 * 負責處理車輛查詢相關邏輯
 *
 * 為什麼需要 @Transactional(readOnly = true)？
 *   Car.images 和 Car.category 都是 LAZY 載入（fetch = FetchType.LAZY）。
 *   LAZY 表示「需要時才去資料庫查」，但「需要時」必須 JPA Session 還開著。
 *   若沒有 @Transactional，方法結束後 Session 就關閉，
 *   再呼叫 car.getImages() 時 Session 已關閉，就會拋出 LazyInitializationException。
 *   加上 @Transactional(readOnly = true) 讓整個方法在同一個 Session 內完成。
 */
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) // 所有方法預設為唯讀交易，需要寫入的方法再個別覆寫
public class CarService {

    private final CarRepository carRepository;

    /**
     * 查詢所有上架中的車輛
     *
     * @return 車輛列表（CarResponse DTO）
     */
    public List<CarResponse> getAllCars() {
        return carRepository.findByIsAvailableTrue()
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 依 ID 查詢單台車輛詳情
     *
     * @param id 車輛 ID
     * @return CarResponse DTO
     */
    public CarResponse getCarById(Integer id) {
        Car car = carRepository.findById(id)
                .orElseThrow(() -> new RuntimeException("找不到車輛，ID：" + id));
        return toResponse(car);
    }

    /**
     * 查詢指定日期區間內可用的車輛
     * 用於首頁或 Car_model.html 的日期篩選功能
     *
     * @param pickupDate 取車日期
     * @param returnDate 還車日期
     * @return 可用車輛列表（排除已有訂單衝突的車輛）
     */
    public List<CarResponse> getAvailableCars(LocalDate pickupDate, LocalDate returnDate) {
        // 基本日期驗證
        if (pickupDate.isAfter(returnDate)) {
            throw new RuntimeException("取車日期不可晚於還車日期");
        }
        if (pickupDate.isBefore(LocalDate.now())) {
            throw new RuntimeException("取車日期不可早於今天");
        }

        return carRepository.findAvailableCars(pickupDate, returnDate, LocalDateTime.now())
                .stream()
                .map(this::toResponse)
                .collect(Collectors.toList());
    }

    /**
     * 將 Car Entity 轉換為 CarResponse DTO
     * private 方法，只在這個 Service 內使用
     *
     * 轉換重點：
     *   - 從關聯的 CarCategory 取出分類名稱（LAZY，需在 Transaction 內存取）
     *   - 從圖片列表找出 sort_order 最小的主圖
     *   - 將所有圖片路徑整理成字串列表
     *
     * @param car Car Entity 物件
     * @return CarResponse DTO 物件
     */
    private CarResponse toResponse(Car car) {
        CarResponse response = new CarResponse();
        response.setId(car.getId());
        response.setName(car.getName());
        response.setBrand(car.getBrand());
        response.setPricePerDay(car.getPricePerDay());
        response.setSeats(car.getSeats());
        response.setLuggageDesc(car.getLuggageDesc());
        response.setDescription(car.getDescription());

        // 從關聯的分類物件取出名稱（LAZY，在 @Transactional 內才能安全存取）
        if (car.getCategory() != null) {
            response.setCategoryName(car.getCategory().getName());
        }

        // 處理圖片（LAZY，在 @Transactional 內才能安全存取）
        if (car.getImages() != null && !car.getImages().isEmpty()) {
            List<String> imagePaths = car.getImages().stream()
                    .sorted((a, b) -> Integer.compare(a.getSortOrder(), b.getSortOrder()))
                    .map(CarImage::getImagePath)
                    .collect(Collectors.toList());

            response.setImages(imagePaths);
            response.setMainImage(imagePaths.get(0)); // sort_order 最小的為主圖
        }

        return response;
    }
}
