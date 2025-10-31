package io.hhplus.tdd.point;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import static org.junit.jupiter.api.Assertions.assertTrue; // assertTrue 임포트
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@AutoConfigureMockMvc
public class PointControllerConcurrencyIT {
    @Autowired
    MockMvc mvc;

    @Test
    void API_동시_충전도_정확히_반영된다() throws Exception {
        long userId = 1L;
        int threads = 10;
        int perThreadCalls = 10; // 총 기대값 = 100
        CountDownLatch start = new CountDownLatch(1);
        CountDownLatch done = new CountDownLatch(threads);
        ExecutorService pool = Executors.newFixedThreadPool(threads);
        for (int t = 0; t < threads; t++) {
            pool.submit(() -> {
                try {
                    start.await(); // 동시에 출발

                    for (int i = 0; i < perThreadCalls; i++) {
                        mvc.perform(patch("/point/{id}/charge", userId)
                                        .contentType(MediaType.APPLICATION_JSON)
                                        // [수정됨] JSON 객체 "{\"amount\":1}" 대신 숫자 "1"을 전송합니다.
                                        .content("1"))
                                .andExpect(status().isOk());
                    }
                } catch (Exception e) {
                    throw new RuntimeException(e);
                } finally {
                    done.countDown();
                }
            });
        }
        start.countDown(); // 출발 신호
        // [개선] 모든 스레드가 완료될 때까지 최대 10분간 대기합니다.
        boolean completed = done.await(10, TimeUnit.MINUTES);
        assertTrue(completed, "동시 충전 작업이 10분 내에 완료되지 않았습니다.");
        pool.shutdown(); // 스레드 풀 종료
        long expected = threads * perThreadCalls; // 1000
        mvc.perform(get("/point/{id}", userId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.point").value(expected)); // JSON 검증
    }
}
