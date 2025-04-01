package com.example.trade_vision_backend.datastore.internal;

import com.example.trade_vision_backend.datastore.DataStoreService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataStoreServiceImpl implements DataStoreService {
    private final AtomicReference<ConcurrentHashMap<String, Object>> store = new AtomicReference<>(new ConcurrentHashMap<>());

    @Override
    public <T> void set(String key, T value) {
        if (key == null) {
            throw new IllegalArgumentException("Invalid key has been passed on");
        }

        ConcurrentHashMap<String, Object> currentMap = store.get();
        currentMap.put(key, value);

        store.set(currentMap);
        log.info("Stored value for key: {}", key);
    }

    @Override
    public Object get(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Invalid key has been passed on");
        }

        log.info("Retrieved value for key: {}", key);
        return store.get().get(key);
    }

    @Override
    public boolean delete(String key) {
        if (key == null) {
            throw new IllegalArgumentException("Key cannot be null");
        }

        ConcurrentHashMap<String, Object> currentMap = store.get();
        Object removedValue = currentMap.remove(key);
        store.set(currentMap);

        boolean wasRemoved = (removedValue != null);
        log.debug("Delete operation for key {} {}", key, wasRemoved ? "succeeded" : "failed (key not found)");
        return wasRemoved;
    }

    @Override
    public boolean containsKey(String key) {
        return store.get().containsKey(key);
    }

    @Override
    public void clear() {
        store.set(new ConcurrentHashMap<>());
        log.info("Store cleared");
    }
}
