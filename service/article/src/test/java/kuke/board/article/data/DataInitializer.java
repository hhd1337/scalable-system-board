package kuke.board.article.data;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import kuke.board.article.entity.Article;
import kuke.board.common.snowflake.Snowflake;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.support.TransactionTemplate;

@SpringBootTest
public class DataInitializer {
    static final int BULK_INSERT_SIZE = 2000; // 한번 bulkInsert()로 저장할 게시글 개수
    static final int EXECUTE_COUNT = 6000; // bulkInsert() 실행 횟수 -> 총 2000개*6000번 = 1200만개 게시글 생성됨

    @PersistenceContext
    EntityManager entityManager;

    @Autowired
    TransactionTemplate transactionTemplate; // 메서드에 붙이는 @Transactional 보다 더 세밀하게 트랜잭션을 사용하고 싶을 때

    Snowflake snowflake = new Snowflake();

    // 모든 스레드의 작업이 끝날 때 까지 기다리도록 해줄 동시성 도구 (latch: 자물쇠)
    CountDownLatch latch = new CountDownLatch(EXECUTE_COUNT);

    @Test
    void initialize() throws InterruptedException {
        // 동시에 최대 10개 스레드가 작업하도록 스레드풀 생성
        ExecutorService executorService = Executors.newFixedThreadPool(10);

        // bulkInsert 작업을 총 6000개 생성
        for (int i = 0; i < EXECUTE_COUNT; i++) {

            //스레드풀에 (2000개 저장하는) 작업 제출
            executorService.submit(() -> {
                bulkInsert(); // 게시글 6000개 저장
                latch.countDown(); // 작업 하나 완료
                System.out.println("latch.getCount() = " + latch.getCount());
            });
        }
        latch.await();
        executorService.shutdown();
    }

    void bulkInsert() {
        // 하나의 bulkInsert()는 하나의 트랜잭션으로 수행된다.
        transactionTemplate.executeWithoutResult(status -> {
            for (int i = 0; i < BULK_INSERT_SIZE; i++) {
                Article article = Article.create(
                        snowflake.nextId(), //snowflake로 계속 다른 articleId 뽑아냄
                        "title" + i,
                        "content" + i,
                        1L,
                        1L
                );
                entityManager.persist(article);
            }
        });
    }
}
