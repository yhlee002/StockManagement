package com.yh.stockmanagement.service;

import com.yh.stockmanagement.domain.Stock;
import com.yh.stockmanagement.repository.StockRepository;
import org.springframework.stereotype.Service;

import javax.transaction.Transactional;

@Service
public class OptimisticLockStockService {
    private StockRepository stockRepository;

    public OptimisticLockStockService(StockRepository stockRepository) {
        this.stockRepository = stockRepository;
    }

    @Transactional
    public void decrease(Long id, Long quantity) {
        Stock stock = stockRepository.findByIdWidthOptimisticLock(id);
        stock.decrease(quantity);
        stockRepository.saveAndFlush(stock);

    }
}
