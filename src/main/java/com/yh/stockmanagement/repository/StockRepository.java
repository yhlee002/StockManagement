package com.yh.stockmanagement.repository;

import com.yh.stockmanagement.domain.Stock;
import org.springframework.data.jpa.repository.JpaRepository;

public interface StockRepository extends JpaRepository<Stock, Long> {

}
