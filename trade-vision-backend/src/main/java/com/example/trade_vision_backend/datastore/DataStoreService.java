package com.example.trade_vision_backend.datastore;

import java.util.concurrent.ConcurrentHashMap;

public interface DataStoreService <T> {
    void set(String key, T value);
    ConcurrentHashMap<String, T> get(String key);
}
