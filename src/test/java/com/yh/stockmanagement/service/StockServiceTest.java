package com.yh.stockmanagement.service;

import com.yh.stockmanagement.domain.Stock;
import com.yh.stockmanagement.repository.StockRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@DisplayNameGeneration(DisplayNameGenerator.ReplaceUnderscores.class)
class StockServiceTest {

    @Autowired
    private StockService stockService;

    @Autowired
    private StockRepository stockRepository;

    @BeforeEach
    public void before() {
        Stock stock = new Stock(1L, 100L);

        stockRepository.save(stock);
    }

    @AfterEach
    public void after() {
        stockRepository.deleteAll();
    }

    @Test
    public void stock_decrease() {
        stockService.decrease(1L, 1L);
        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(stock.getQuantity(), 99);
        // 100 - 1 = 99 (expect) -> Success
    }

    @Test
    public void send_multi_request() throws InterruptedException {
        int threadCnt = 10;
        // CountDownLatch : 다른 스레드에서 수행되스 작업이 끝날 때까지 대기할 수 있게 해주는 클래스
        CountDownLatch latch = new CountDownLatch(threadCnt);

        // ExecutorService : 비동기로 이루어지는 작업을 단순화시켜주는 Java API
        ExecutorService executorService = Executors.newFixedThreadPool(32);
        for(int i = 0; i < threadCnt; i++) {
            executorService.submit(() -> {
                try {
                    stockService.decrease(1L, 1L);
                } finally {
                    latch.countDown();
                }
            });
        }
        latch.await();

        Stock stock = stockRepository.findById(1L).orElseThrow();
        assertEquals(0L, stock.getQuantity());
        // 100 - (1 * 100) = 0 (expect) -> 99
        /*
        결과가 예상과 다른 이유는 Race Condition이 일어났기 때문
        Race Condition이란 여러 개의 스레드가 동시에 공유 자원에 접근할 수 있고, 이를 동시에 변경하려할 때 발생할 수 있는 문제이다.
        */
    }
}