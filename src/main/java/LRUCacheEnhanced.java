package main.java;

import java.util.LinkedHashMap;
import java.util.Map;

public class LRUCacheEnhanced<K,V> extends LinkedHashMap<K,V>{
    private final int allocSize;

    public LRUCacheEnhanced(int initialCapacity) {
      super(initialCapacity,0.75f,true);
      this.allocSize = initialCapacity;
    }

    @Override
    protected boolean removeEldestEntry(Map.Entry<K, V> eldest) {
        return this.size() > allocSize;
    }
}
