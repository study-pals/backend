package com.studypals.global.redis;

import com.fasterxml.jackson.core.JacksonException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Repository;

import java.util.Set;

/**
 * redis에 대한 기본적인 입출력 메서드를 정의하였습니다.
 * <p>
 * save, get, delete, isExist 메서드를 구현하였습니다.
 *
 * <p><b>빈 관리:</b><br>
 * repository
 *
 * <p><b>외부 모듈:</b><br>
 * REDIS 에 대한 레포지토리입니다.
 *
 * @author jack8
 * @since 2025-04-03
 */
@Repository
@RequiredArgsConstructor
public class RedisRepository {

    private final StringRedisTemplate stringRedisTemplate;
    private final ObjectMapper objectMapper;

    /**
     * 단순히 String 타입의 key-value 쌍의 저장을 지원합니다.
     * @param key 저장될 key
     * @param value 저장될 value
     */
    public void save(String key, String value) {
        stringRedisTemplate.opsForValue().set(key,value);
    }

    /**
     * Builder 패턴 기반의, save 메서드입니다. 다음과 같이 사용될 수 있습니다 <br>
     * <pre>
     * {@code
     * redisRepository.save()
     *      .key(key)
     *      .object(member)  //단, value("string") 도 가능
     *      .timeout(10)    //기본값 : -1(영구 저장)
     *      .timeUnit(TimeUnit.HOURS)   //기본값: TimeUnit.MINUTES(분 단위)
     *      .save()
     * }
     * </pre>
     * @return RedisSaveBuilder 를 반환하나, 내부적으로 builder 패턴에 의한 체이닝 메커니즘으로 실행됩니다.
     * @see RedisSaveBuilder
     */
    public RedisSaveBuilder save() {
        return new RedisSaveBuilder(stringRedisTemplate);
    }

    /**
     * key 에 따른 값을 반환받습니다.
     * @param key 검색할 key
     * @return 존재할 시 string 타입의 값, 혹은 null
     */
    public String get(String key) {
        return stringRedisTemplate.opsForValue().get(key);
    }

    /**
     * key 에 따른 값을 반환받습니다. 단, 저장된 데이터를 객체로 역직렬화를 수행하여 반환합니다. <br>
     * 다음과 같이 사용됩니다. {@code redisTemplate.get(key, Member.class)} <br>
     * @param key 검색할 key
     * @param clazz 반환받을 객체의 타입
     * @return 만약 존재할 시, 해당하는 객체. 단, 객체 타입이 일치하지 않으면 예외를 뱉는다. 존재하지 않는 경우 null
     * @throws IllegalArgumentException 객체 타입 불일치 혹은 역직렬화 문제가 발생한느 경우
     */
    public <T> T get(String key, Class<T> clazz) {
        String value = stringRedisTemplate.opsForValue().get(key);
        if (value == null) return null;
        try {
            return objectMapper.readValue(value, clazz);
        } catch (JacksonException e) {
            throw new IllegalArgumentException("can't parse value to object");
        }
    }

    /**
     * key 값에 따라 데이터를 삭제합니다.
     * @param key 검색할 key
     */
    public void delete(String key) {
        stringRedisTemplate.delete(key);
    }

    /**
     * key 값에 일치하는 값이 존재하는지 반환합니다.
     * @param key 검색할 key
     * @return 존재하면 true, 아니면  false
     */
    public boolean isExist(String key) {
        return Boolean.TRUE.equals(stringRedisTemplate.hasKey(key));
    }

    /**
     * 특정 패턴의 key를 전부 set으로 반환합니다.
     * @param pattern 검색할 pattern
     * @return key 들에 대한 set
     */
    public Set<String> getKeysByPatern(String pattern) {
        return stringRedisTemplate.keys(pattern);
    }

}
