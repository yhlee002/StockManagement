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

🔥 문제점 🔥 : synchronized 키워드를 통해 스레드의 동시성 처리가 가능하나, 이는 단일 서버일 때에 국한된다. 서버 인스턴스가 여러 대가 되게 되면 `synchronized`를 사용하지 않을 때와 같은 결과가 나오게 된다.


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

### 2) DB(MySQL 기준)을 이용
#### Optimistic Lock(낙관적 락)
Lock을 걸지 않고 문제가 발생할 때 처리한다. 대표적으로 `version` 등의 버전 관리 컬럼을 만들어서 업데이트시마다 버전을 올리고, 버전이 안맞는 업데이트는 수행하지 않게끔하는 방법이 있다.

트랜잭션을 사용하지 않고 문제가 생기면 **애플리케이션 단계에서 처리**한다.

애플리케이션 단계에서 처리한다는 것은 아래와 같은 특징들을 야기한다.

- 버전이 안맞아 처리가 되지 않을 때 이를 다시 처리해주어야 한다. 
- 두 개 이상의 데이터 처리가 하나의 메서드에서 수행된다고 했을 때 하나의 데이터 처리가 오류가 났을 경우 이를 롤백해주어야 하기 때문에 결국에는 또 다른 업데이트를 수행하는 것으로 이러한 오류가 잦은 경우에는 성능이 떨어질 수 있다.
- 오류가 자주 발생하지 않는 상황에 쓰이는 것이 효율적이다.
#### Pessimistic Lock(exclusive lock, 비관적 락)
**DB 차원에서 사용**하는 락 기능이다.
다른 트랜잭션이 특정 row 의 Lock 을 얻는 것을 방지한다.
- 다른 트랜잭션이 특정 row 의 Lock 을 얻는것을 방지한다.
  - A 트랜잭션이 끝날때까지 기다렸다가 B 트랜잭션이 Lock 을 획득한다.
- 특정 row 를 update 하거나 delete 할 수 있다.
- 일반 select 는 별다른 Lock 이 없기때문에 조회는 가능하다.


#### named Lock
이름과 함께 Lock(상태)를 획득한다. 해당 Lock은 다른 세션에서 획득 및 해제가 불가능하다.