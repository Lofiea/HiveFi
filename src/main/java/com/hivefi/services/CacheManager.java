package com.hivefi.services;

import java.util.concurrent.ConcurrentHashMap;

public class CacheManager<K, V> {
    private static class Entry<V> {
        final V value;
        final long expiresAt;
        Entry(V value, long expiresAt) { this.value = value; this.expiresAt = expiresAt; }
    }

    private final ConcurrentHashMap<K, Entry<V>> map = new ConcurrentHashMap<>();

    public V getIfFresh(K key) {
        Entry<V> e = map.get(key);
        if (e == null) return null;
        if (System.currentTimeMillis() > e.expiresAt) {
            map.remove(key);
            return null;
        }
        return e.value;
    }

    public void put(K key, V value, long ttlMillis) {
        map.put(key, new Entry<>(value, System.currentTimeMillis() + ttlMillis));
    }

    public void clear() { map.clear(); }
}