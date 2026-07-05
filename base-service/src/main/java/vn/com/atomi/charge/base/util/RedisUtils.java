package vn.com.atomi.charge.base.util;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class RedisUtils {

    private final RedisTemplate<String, String> redisTemplate;

    //co thoi gian het han
    public void pushDataToCache(String key, String value, long ttlInSeconds) {
        redisTemplate.opsForValue().set(key, value);
        // Chỉ set TTL nếu chưa có
        Long currentTtl = redisTemplate.getExpire(key);
        if (currentTtl == -1) {
            redisTemplate.expire(key, ttlInSeconds, TimeUnit.SECONDS);
        }
    }

    //co thoi gian het han
    public void pushDataToCache(String key, String value, long ttl, TimeUnit timeUnit) {
        redisTemplate.opsForValue().set(key, value);
        // Chỉ set TTL nếu chưa có
        Long currentTtl = redisTemplate.getExpire(key);
        if (currentTtl == -1) {
            redisTemplate.expire(key, ttl, timeUnit);
        }
    }

    //khong co thoi gian het han
    public void pushDataToCache(String key, String value) {
        redisTemplate.opsForValue().set(key, value);
    }

    //push list data
    public void pushDataToCache(String key, List<?> value) {
        String jsonValue = JsonUtil.convertObjectToJson(value);
        if (StringUtils.isNotEmpty(jsonValue) && jsonValue.length() < 100) {
            log.info("pushDataToCache: {}", jsonValue);
        } else {
            log.info("pushDataToCache: ***");
        }
        redisTemplate.opsForSet().add(key, jsonValue);
    }

    public void pushDataToCache(String key, List<?> value, long ttlInSeconds) {
        String jsonValue = JsonUtil.convertObjectToJson(value);
        if (StringUtils.isNotEmpty(jsonValue) && jsonValue.length() < 100) {
            log.info("pushDataToCache: {}", jsonValue);
        } else {
            log.info("pushDataToCache: ***");
        }
        redisTemplate.opsForSet().add(key, jsonValue);

        Long currentTtl = redisTemplate.getExpire(key);
        if (currentTtl == -1) {
            redisTemplate.expire(key, ttlInSeconds, TimeUnit.SECONDS);
        }
    }

    public boolean isKeyInCache(String key) {
        return Boolean.TRUE.equals(redisTemplate.hasKey(key));
    }

    //get string data
    public String getDataFromCache(String key) {
        return redisTemplate.opsForValue().get(key);
    }

    public String getDataAndDelFromCache(String key) {
        String data = getDataFromCache(key);
        this.deleteByKey(key);
        return data;
    }

    //get list data
    public <T> List<T> getDataFromCache(String key, Class<T> clazz) {
        try {
            Set<String> values = redisTemplate.opsForSet().members(key);

            if (values == null || values.isEmpty()) {
                return new ArrayList<>();
            }

            String json = values.iterator().next();

            return JsonUtil.convertJsonToList(json, clazz);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    //get list data
    public <T> List<T> getListDataFromCache(String key, Class<T> clazz) {
        try {
            String values = redisTemplate.opsForValue().get(key);

            if (values == null || values.isEmpty()) {
                return new ArrayList<>();
            }

            return JsonUtil.convertJsonToList(values, clazz);
        } catch (Exception e) {
            log.error(e.getMessage());
            return new ArrayList<>();
        }
    }

    public <T> List<T> getDataAndDelFromCache(String key, Class<T> clazz) {
        List<T> data = getDataFromCache(key, clazz);
        this.deleteByKey(key);
        return data;
    }

    //get object
    public <T> T getSingleDataFromCache(String key, Class<T> clazz) {
        try {
            String value = getDataFromCache(key);

            if (StringUtils.isEmpty(value)) {
                return null;
            }

            return JsonUtil.convertJsonToObject(value, clazz);
        } catch (Exception e) {
            log.error(e.getMessage());
            return null;
        }
    }

    //get object and delete
    public <T> T getSingleDataFromCacheAndDelete(String key, Class<T> clazz) {
        T T = getSingleDataFromCache(key, clazz);
        deleteByKey(key);
        return T;
    }

    public void deleteByKey(String key) {
        if (isKeyInCache(key)) {
            redisTemplate.delete(key);
        }
    }
}
