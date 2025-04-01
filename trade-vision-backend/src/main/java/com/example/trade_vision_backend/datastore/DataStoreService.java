package com.example.trade_vision_backend.datastore;

import com.fasterxml.jackson.core.type.TypeReference;

public interface DataStoreService {
    <T> void set(String key, T value);
    <T> T get(String key, Class<T> type);
    <T> T get(String key, TypeReference<T> typeRef);
    boolean delete(String key);
    boolean containsKey(String key);
    void clear();
}
