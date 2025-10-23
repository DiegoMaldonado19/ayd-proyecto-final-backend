package com.ayd.parkcontrol.infrastructure.cache;

import com.ayd.parkcontrol.application.port.cache.CacheService;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;
import java.util.Optional;

@Service
@RequiredArgsConstructor
@Slf4j
public class RedisCacheServiceImpl implements CacheService {

    private final RedisTemplate<String, Object> redisTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void save(String key, Object value, Duration ttl) {
        try {
            redisTemplate.opsForValue().set(key, value, ttl);
            log.debug("Saved to cache: {}", key);
        } catch (Exception e) {
            log.error("Error saving to cache: {}", key, e);
        }
    }

    @Override
    public <T> Optional<T> get(String key, Class<T> type) {
        try {
            Object value = redisTemplate.opsForValue().get(key);
            if (value == null) {
                log.debug("Cache miss: {}", key);
                return Optional.empty();
            }

            log.debug("Cache hit: {}", key);
            if (type.isInstance(value)) {
                return Optional.of(type.cast(value));
            }

            T convertedValue = objectMapper.convertValue(value, type);
            return Optional.of(convertedValue);
        } catch (Exception e) {
            log.error("Error retrieving from cache: {}", key, e);
            return Optional.empty();
        }
    }

    @Override
    public void delete(String key) {
        try {
            redisTemplate.delete(key);
            log.debug("Deleted from cache: {}", key);
        } catch (Exception e) {
            log.error("Error deleting from cache: {}", key, e);
        }
    }

    @Override
    public boolean exists(String key) {
        try {
            Boolean result = redisTemplate.hasKey(key);
            return result != null && result;
        } catch (Exception e) {
            log.error("Error checking cache existence: {}", key, e);
            return false;
        }
    }
}
