package test.java;

import main.java.Cache;
import main.java.LRUCache;
import main.java.LRUCacheEnhanced;
import org.junit.Before;
import org.junit.Test;

import java.util.Collections;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertEquals;

public class EnhancedLRUUnitTest {


    @Test
    public void runMultiThreadTask_WhenPutDataInConcurrentToCache_ThenNoDataLost() throws Exception {
        final int size = 10000000;
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        Map<Integer, String> enhancedLRU = Collections.synchronizedMap(new LRUCacheEnhanced<>(size));
        CountDownLatch countDownLatch = new CountDownLatch(size);
        try {
            IntStream.range(0, size).<Runnable>mapToObj(key -> () -> {
                enhancedLRU.put(key, "value" + key);
                countDownLatch.countDown();
            }).forEach(executorService::submit);
            countDownLatch.await();
        } finally {
            executorService.shutdown();
        }
        assertEquals(enhancedLRU.size(), size);
        IntStream.range(0, size).forEach(i -> assertEquals("value" + i, enhancedLRU.get(i)));
    }
}

