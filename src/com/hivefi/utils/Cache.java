package com.hivefi.utils;

import java.util.HashMap;
import java.util.Map;

public class Cache {
    private final Map<String, Object> map = new HashMap<>();
    public void put(String key, Object value) { map.put(key, value); }
    @SuppressWarnings("unchecked")
    public <T> T get(String key) { return (T) map.get(key); }
}
    
