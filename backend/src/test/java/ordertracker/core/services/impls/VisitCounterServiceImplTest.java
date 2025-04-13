package ordertracker.core.services.impls;

import org.junit.jupiter.api.Test;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import static org.junit.jupiter.api.Assertions.assertEquals;

class VisitCounterServiceImplTest {

    @Test
    void testConcurrentIncrement() throws InterruptedException {
        VisitCounterServiceImpl service = new VisitCounterServiceImpl();
        int threadCount = 100;
        int incrementsPerThread = 1000;
        ExecutorService executor = Executors.newFixedThreadPool(threadCount);

        for (int i = 0; i < threadCount; i++) {
            executor.execute(() -> {
                for (int j = 0; j < incrementsPerThread; j++) {
                    service.incrementCount("/test-url");
                }
            });
        }

        executor.shutdown();
        executor.awaitTermination(1, TimeUnit.MINUTES);

        int expectedCount = threadCount * incrementsPerThread;
        assertEquals(expectedCount, service.getCount("/test-url"));
    }
}