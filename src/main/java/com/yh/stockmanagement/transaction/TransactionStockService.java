package com.yh.stockmanagement.transaction;

import com.yh.stockmanagement.service.StockService;

public class TransactionStockService {

    private StockService stockService;

    public TransactionStockService(StockService stockService) {
        this.stockService = stockService;
    }

    public void decrease(Long id, Long quantity) {
        startTransaction();
        stockService.decrease(id, quantity);
        endTransaction();
    }

    public void startTransaction() {

    }

    public void endTransaction() {

    }
}
