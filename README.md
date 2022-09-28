# 개발 환경
- JDK : Java 11
- DB : Docker를 통해 Mysql 서버를 구축해 사용
----

# 동시성 처리
## 동시성이란
동시에 다수의 스레드가 공유 자원에 접근해 이를 변경하려하면 Race Condition(경쟁 상태)이 발생하게 된다.
## 동시성 처리 방안
### 1) Java의 synchronized 키워드 사용
한 번에 하나의 스레드만이 한 공유 자원에 접근할 수 있게 하는 키워드이다.
```java
@Service
public class StockService {
    public synchronized void decrease(Long id, Long quantity) {
        // 해당 id의 물건 재고(Stock) 수량(quantity)을 주어진 수량만큼 감소시킨다.
    }
}
```
#### Cf. 주의할 점
`@Transactional`과 함께 사용할 경우 제대로 이루어지지 않는 것을 볼 수 있는데, 이는 트랜잭션의 동작 원리 때문이다. 

Spring에서는 `@Transactional`을 붙이면 해당 메서드에서 수행하는 데이터 CRUD 기능을 하나의 트랜잭션으로 묶어 처리할 수 있게 해준다. 트랜잭션은 기본적으로 전체가 성공적으로 종료됐을 때만 그 결과가 적용된다.

트랜잭션을 사용하게 되면 트랜잭션 기능을 포함한 프록시 객체가 새로 생성되어 이 것이 실행된다. (만들어지는 형태를 임의로 구현한 것이 transaction.TransactionStockService이다.)

> 프록시 객체란?
> 원래 객체를 감싸고 있는 객체로, 기존의 객체를 감싸서 클라이언트의 요청을 처리해준다.

```java
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
```
#### Spring에서의 Transaction 동작 원리
위의 코드와 같이 새로운 클래스가 생성되어서 이 인스턴스의 `decrease(Long id, Long quantity)`가 호출이된다.

`decrease` 메서드가 호출이되면, 트랜잭션을 시작하고(`startTransaction()`) `stockService`의 `decrease` 메서드을 호출하는데 이 것이 성공적으로 종료되면 트랜잭션이 종료된다. (`endTransaction()`)

이러한 클래스의 형태로 실행되는 트랜잭션의 문제는 `stockService.decrease(id, quantity);`가 실행되고 나서 트랜잭션을 종료하는 시점에 실제 DB의 테이블에 해당 값을 업데이트하는데, 작업을 성공적으로 마친 후 트랜잭션을 종류하기 전에 또 다른 스레드가 여기에 접근할 수 있다는 것이다.
