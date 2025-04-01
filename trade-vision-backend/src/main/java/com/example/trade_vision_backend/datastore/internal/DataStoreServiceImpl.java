package com.example.trade_vision_backend.datastore.internal;

import com.example.trade_vision_backend.datastore.DataStoreService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.annotation.Nonnull;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicReference;

@Service
@Slf4j
@RequiredArgsConstructor
public class DataStoreServiceImpl implements DataStoreService {
    private final AtomicReference<ConcurrentHashMap<String, byte[]>> store =
            new AtomicReference<>(new ConcurrentHashMap<>());
    private final ObjectMapper objectMapper = new ObjectMapper();

    @Override
    public <T> void set(@Nonnull String key, @Nonnull T value) {
        Objects.requireNonNull(key, "Invalid key, can't be null");
        Objects.requireNonNull(value, "Invalid value, can't be null");

        try {
            byte[] serializedValue = objectMapper.writeValueAsBytes(value);
            store.get().put(key, serializedValue);
            log.info("Stored serialized value for key: {}", key);
        } catch (JsonProcessingException e) {
            throw new RuntimeException("Failed to serialize value for key: " + key, e);
        }
    }

    @Override
    public <T> T get(@Nonnull String key, @Nonnull Class<T> type) {
        Objects.requireNonNull(key, "Invalid key, can't be null");
        Objects.requireNonNull(type, "Invalid type, can't be null");

        byte[] serializedValue = store.get().get(key);
        if (serializedValue == null) {
            return null;
        }

        try {
            T value = objectMapper.readValue(serializedValue, type);
            log.info("Retrieved and deserialized value for key: {}", key);
            return value;
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize value for key: " + key, e);
        }
    }

    @Override
    public <T> T get(@Nonnull String key, @Nonnull TypeReference<T> typeRef) {
        Objects.requireNonNull(key, "Invalid key, can't be null");
        Objects.requireNonNull(typeRef, "Invalid type reference, can't be null");

        byte[] serializedValue = store.get().get(key);
        if (serializedValue == null) {
            return null;
        }

        try {
            return objectMapper.readValue(serializedValue, typeRef);
        } catch (IOException e) {
            throw new RuntimeException("Failed to deserialize value for key: " + key, e);
        }
    }

    @Override
    public boolean delete(@Nonnull String key) {
        Objects.requireNonNull(key, "Invalid key, can't be null");

        byte[] removedValue = store.get().remove(key);
        boolean wasRemoved = (removedValue != null);
        log.debug("Delete operation for key {} {}", key, wasRemoved ? "succeeded" : "failed (key not found)");
        return wasRemoved;
    }

    @Override
    public boolean containsKey(@Nonnull String key) {
        Objects.requireNonNull(key, "Invalid key, can't be null");
        return store.get().containsKey(key);
    }

    @Override
    public void clear() {
        store.set(new ConcurrentHashMap<>());
        log.info("Store cleared");
    }

}
