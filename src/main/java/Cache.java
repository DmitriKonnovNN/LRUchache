package main.java;

import java.util.Optional;

public interface Cache <K,V> {
    boolean put(K key, V value);

    Optional<V> get(K key);

    int currentSize();
    default int getMaxSize(){return -1;}

    boolean isEmpty();

    void clear();
}
