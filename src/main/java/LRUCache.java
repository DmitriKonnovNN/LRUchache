package main.java;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.locks.ReentrantReadWriteLock;

public class LRUCache<K, V> implements Cache<K, V> {
    private final int maxSize;
    private final Map<K, LinkedListNode<CacheElement<K, V>>> keyValueNodeMap;
    private final DoublyLinkedList<CacheElement<K, V>> listOfMostRecent;
    private final ReentrantReadWriteLock lock = new ReentrantReadWriteLock();

    public LRUCache(int size) {
        this.maxSize = size;
        this.keyValueNodeMap = new ConcurrentHashMap<>(size);
        this.listOfMostRecent = new DoublyLinkedList<>();
    }

    public Optional <CacheElement<K,V>> getMostRecentElement () {
        if (listOfMostRecent.isEmpty()) return Optional.empty();
        return Optional.of(listOfMostRecent.getHead().getElement());
    }

    public Optional <CacheElement<K,V>> getLeastRecentElement () {
        if (listOfMostRecent.isEmpty()) return Optional.empty();
        return Optional.of(listOfMostRecent.getTail().getElement());
    }

    @Override
    public int getMaxSize() {
        return maxSize;
    }

    @Override
    public boolean put(K key, V value) {
        this.lock.writeLock().lock();
        try {
            CacheElement<K, V> item = new CacheElement<>(key, value);
            LinkedListNode<CacheElement<K, V>> newNode;
            if (this.keyValueNodeMap.containsKey(key)) {
                LinkedListNode<CacheElement<K, V>> node = this.keyValueNodeMap.get(key);
                newNode = listOfMostRecent.updateAndMoveToFront(node, item);
            } else {
                if (this.currentSize() >= this.maxSize) {
                    this.evictElement();
                }
                newNode = this.listOfMostRecent.add(item);
            }
            if (newNode.isEmpty()) {
                return false;
            }
            this.keyValueNodeMap.put(key, newNode);
            return true;
        } finally {
            this.lock.writeLock().unlock();
        }
    }

    @Override
    public Optional<V> get(K key) {
        this.lock.readLock().lock();
        try {
            LinkedListNode<CacheElement<K, V>> linkedListNode = this.keyValueNodeMap.get(key);
            if (linkedListNode != null && !linkedListNode.isEmpty()) {
                keyValueNodeMap.put(key, this.listOfMostRecent.moveToFront(linkedListNode));
                return Optional.of(linkedListNode.getElement().getValue());
            }
            return Optional.empty();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public int currentSize() {
        this.lock.readLock().lock();
        try {
            return listOfMostRecent.size();
        } finally {
            this.lock.readLock().unlock();
        }
    }

    @Override
    public boolean isEmpty() {
        return currentSize() == 0;
    }

    @Override
    public void clear() {
        this.lock.writeLock().lock();
        try {
            keyValueNodeMap.clear();
            listOfMostRecent.clear();
        } finally {
            this.lock.writeLock().unlock();
        }
    }


    private void evictElement() {
        this.lock.writeLock().lock();
        try {
            LinkedListNode<CacheElement<K, V>> linkedListNode = listOfMostRecent.removeTail();
            if (linkedListNode.isEmpty()) {
                return;
            }
            keyValueNodeMap.remove(linkedListNode.getElement().getKey());
        } finally {
            this.lock.writeLock().unlock();
        }
    }
}