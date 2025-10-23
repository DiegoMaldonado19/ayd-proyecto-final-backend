package com.ayd.parkcontrol.application.port.cache;

import java.time.Duration;
import java.util.Optional;

public interface CacheService {
    void save(String key, Object value, Duration ttl);

    <T> Optional<T> get(String key, Class<T> type);

    void delete(String key);

    boolean exists(String key);
}
