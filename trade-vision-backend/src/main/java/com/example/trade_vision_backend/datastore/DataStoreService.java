package com.example.trade_vision_backend.datastore;

public interface DataStoreService {
    <T> void set(String key, T value);
    Object get(String key);
    boolean delete(String key);
    boolean containsKey(String key);
    void clear();
}
