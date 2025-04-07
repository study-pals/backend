package com.studypals.global.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 *
 * <p>코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)} <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-04-03
 */
public class RedisSaveBuilder {

    private final StringRedisTemplate stringRedisTemplate;
    private String key;
    private String value;
    private Object object;
    private long timeout = -1;
    private TimeUnit timeUnit = TimeUnit.MINUTES;
    private boolean isObject = false;

    public RedisSaveBuilder(StringRedisTemplate stringRedisTemplate) {
        this.stringRedisTemplate = stringRedisTemplate;
    }

    public RedisSaveBuilder key(String key) {
        this.key = key;
        return this;
    }

    public RedisSaveBuilder value(String value) {
        this.value = value;
        return this;
    }

    public RedisSaveBuilder Object(Object object) {
        this.object = object;
        this.isObject = true;
        return this;
    }

    public RedisSaveBuilder timeout(long timeout) {
        this.timeout = timeout;
        return this;
    }

    public RedisSaveBuilder timeUnit(TimeUnit timeUnit) {
        this.timeUnit = timeUnit;
        return this;
    }

    public void save() {

        if (isObject) {
            saveObject();
        } else {
            saveKeyValue();
        }
    }

    private void saveObject() {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            String json = objectMapper.writeValueAsString(object);

            if (timeout == -1) {
                stringRedisTemplate.opsForValue().set(key, json);
            } else {
                stringRedisTemplate.opsForValue().set(key, json, timeout, timeUnit);
            }
        } catch (JsonProcessingException e) {
            throw new RuntimeException("failed to serialize object to json", e);
        }
    }

    private void saveKeyValue() {

        if (timeout == -1) {
            stringRedisTemplate.opsForValue().set(key, value);
        } else {
            stringRedisTemplate.opsForValue().set(key, value, timeout, timeUnit);
        }
    }
}
