package com.yh.stockmanagement.service;

import com.yh.stockmanagement.domain.Stock;
import com.yh.stockmanagement.repository.StockRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Service
public class StockService {

    private StockRepository stockRepository;

    public StockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public synchronized void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findById(id).orElseThrow(); // get stock
        stock.decrease(quantity); // 재고 감소
        stockRepository.saveAndFlush(stock); // 저장
    }
}
