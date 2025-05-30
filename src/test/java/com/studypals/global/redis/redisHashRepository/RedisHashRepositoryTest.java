package com.studypals.global.redis.redisHashRepository;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.StreamSupport;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import com.studypals.global.redis.redisHashRepository.annotations.EnableRedisHashRepositories;
import com.studypals.testModules.testComponent.TestRedisHashEntity;
import com.studypals.testModules.testComponent.TestRedisHashRepository;

/**
 * RedisHashREpository 에 대한 테스트코드
 *
 * @author jack8
 * @since 2025-05-27
 */
@SpringBootTest
@ActiveProfiles("test")
@EnableRedisHashRepositories(basePackageClasses = TestRedisHashRepository.class)
public class RedisHashRepositoryTest {
    @Autowired
    private TestRedisHashRepository repository;

    private final String key = "test:1";

    @BeforeEach
    void setup() {
        repository.delete(key);
    }

    @Test
    void save_success() {
        // given
        TestRedisHashEntity entity = new TestRedisHashEntity(key, "testUser", 25, Map.of("a", "1", "b", "2"));

        // when
        repository.save(entity);
        Optional<TestRedisHashEntity> result = repository.findById(key);

        // then
        assertThat(result).isPresent();
        assertThat(result.get().getName()).isEqualTo("testUser");
        assertThat(result.get().getAge()).isEqualTo(25);
        assertThat(result.get().getMetadata()).containsEntry("a", "1");
    }

    @Test
    void delete_success_keyMissing() {
        // given
        String missingKey = "nonexistent";

        // when
        repository.delete(missingKey);

        // then
        assertThat(repository.findById(missingKey)).isEmpty();
    }

    @Test
    void existById_success_detectsKeyCorrectly() {
        // given
        assertThat(repository.existById(key)).isFalse();
        repository.save(new TestRedisHashEntity(key, "k", 1, Map.of()));

        // when & then
        assertThat(repository.existById(key)).isTrue();
    }

    @Test
    void findAllById_success_returnsOnlyExisting() {
        // given
        String id1 = "test:id:1";
        String id2 = "test:id:2";
        repository.save(new TestRedisHashEntity(id1, "A", 10, Map.of()));
        repository.save(new TestRedisHashEntity(id2, "B", 20, Map.of()));

        // when
        Iterable<TestRedisHashEntity> result = repository.findAllById(List.of(id1, id2, "not-exist"));

        // then
        List<TestRedisHashEntity> list =
                StreamSupport.stream(result.spliterator(), false).toList();
        assertThat(list).hasSize(2);
        assertThat(list).extracting("id").contains(id1, id2);
    }

    @Test
    void findHashFieldsById_success_returnsSubsetOnly() {
        // given
        repository.save(new TestRedisHashEntity(key, "name", 10, Map.of("foo", "bar", "x", "y")));

        // when
        Map<String, String> result = repository.findHashFieldsById(key, List.of("foo", "x", "unknown"));

        // then
        assertThat(result).containsEntry("foo", "bar").containsEntry("x", "y");
        assertThat(result).doesNotContainKey("unknown");
    }

    @Test
    void findHashFieldsById_success_returnsEmptyIfKeyMissing() {
        // given
        String missingKey = "no-key";

        // when
        Map<String, String> result = repository.findHashFieldsById(missingKey, List.of("a", "b"));

        // then
        assertThat(result).isEmpty();
    }

    @Test
    void saveMapById_success_addsOrOverwrites() {
        // given & when
        repository.saveMapById(key, Map.of("fieldA", "val1"));
        Map<String, String> r1 = repository.findHashFieldsById(key, List.of("fieldA"));

        repository.saveMapById(key, Map.of("fieldA", "val2", "fieldB", "new"));
        Map<String, String> r2 = repository.findHashFieldsById(key, List.of("fieldA", "fieldB"));

        // then
        assertThat(r1).containsEntry("fieldA", "val1");
        assertThat(r2).containsEntry("fieldA", "val2").containsEntry("fieldB", "new");
    }

    @Test
    void deleteMapById_success_removesSpecifiedOnly() {
        // given
        repository.saveMapById(key, Map.of("k1", "v1", "k2", "v2", "k3", "v3"));

        // when
        repository.deleteMapById(key, List.of("k2", "k3"));
        Map<String, String> remain = repository.findHashFieldsById(key, List.of("k1", "k2", "k3"));

        // then
        assertThat(remain).containsKey("k1").doesNotContainKeys("k2", "k3");
    }

    @Test
    void deleteMapById_success_keyMissing() {
        // given
        String missingKey = "missing-key";

        // when
        repository.deleteMapById(missingKey, List.of("some", "fields"));

        // then: no exception
    }

    @Test
    void luaScript_deleteAndCountFields_success() {
        // given
        repository.save(new TestRedisHashEntity(key, "toDelete", 42, Map.of("a", "1", "b", "2", "c", "3")));

        // when
        Long deletedCount = repository.deleteAndReturn(key, List.of());

        // then
        assertThat(deletedCount).isEqualTo(5); // 일반필드 f:name, f:age, a, b, c

        assertThat(repository.findById(key)).isEmpty();
    }
}
