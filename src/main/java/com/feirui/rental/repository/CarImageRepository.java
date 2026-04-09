package com.feirui.rental.repository;

import com.feirui.rental.entity.CarImage;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * 車輛圖片 Repository
 */
public interface CarImageRepository extends JpaRepository<CarImage, Integer> {
    List<CarImage> findByCarIdOrderBySortOrderAsc(Integer carId);
}
