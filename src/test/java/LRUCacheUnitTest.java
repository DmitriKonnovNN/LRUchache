package test.java;

import main.java.Cache;
import main.java.LRUCache;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.IntStream;

public class LRUCacheUnitTest {
    LRUCache<String, String> lruCache = new LRUCache<>(3);

    @Before
    public void addSomeDataToCache(){
        lruCache.put("1", "test1");
        lruCache.put("2", "test2");
        lruCache.put("3", "test3");
        System.out.println(16%16);
    }

    @Test
    public void addSomeDataToCache_WhenPutNewValueToExistingKey_ThenValueIsUpdated(){


        lruCache.put("2", "Value to be Updated");
        assertEquals("Value to be Updated", lruCache.get("2").get());
    }

    @Test
    public void WhenGetSomeValueManyTimes_ThenElementGetsMostRecentlyUsed (){
        lruCache.get("2");
        assertEquals(lruCache.getMostRecentElement().get().getKey(), "2");
    }

    @Test
    public void WhenGetSomeValuesManyTimes_ThenUntouchedElementGetsLeastRecentlyUsed (){
        lruCache.get("3");
        lruCache.get("2");
        assertEquals(lruCache.getLeastRecentElement().get().getKey(), "1");
    }


    @Test
    public void addSomeDataToCache_WhenGetData_ThenIsEqualWithCacheElement() {


        assertEquals("test1", lruCache.get("1").get());
        assertEquals("test2", lruCache.get("2").get());
        assertEquals("test3", lruCache.get("3").get());
    }

    @Test
    public void addDataToCacheToTheNumberOfSize_WhenAddOneMoreData_ThenLeastRecentlyDataWillEvict() {

        lruCache.put("4", "test4");
        assertFalse(lruCache.get("1").isPresent());
    }

    @Test
    public void runMultiThreadTask_WhenPutDataInConcurrentToCache_ThenNoDataLost() throws Exception {
        final int size = 50;
        final ExecutorService executorService = Executors.newFixedThreadPool(5);
        Cache<Integer, String> cache = new LRUCache<>(size);
        CountDownLatch countDownLatch = new CountDownLatch(size);
        try {
            IntStream.range(0, size).<Runnable>mapToObj(key -> () -> {
                cache.put(key, "value" + key);
                countDownLatch.countDown();
            }).forEach(executorService::submit);
            countDownLatch.await();
        } finally {
            executorService.shutdown();
        }
        assertEquals(cache.currentSize(), size);
        IntStream.range(0, size).forEach(i -> assertEquals("value" + i, cache.get(i).get()));
    }
}
