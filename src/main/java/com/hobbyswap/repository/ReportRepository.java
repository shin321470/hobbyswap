package com.hobbyswap.repository;

import com.hobbyswap.model.Report;
import org.springframework.data.jpa.repository.JpaRepository;
import java.util.List;

public interface ReportRepository extends JpaRepository<Report, Long> {
    // 找出所有「待處理」的檢舉 (最新的在上面)
    List<Report> findByStatusOrderByCreatedAtDesc(String status);
}