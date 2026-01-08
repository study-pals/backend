package com.studypals.domain.groupManage.entity;

import java.util.concurrent.TimeUnit;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;
import org.springframework.data.redis.core.index.Indexed;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

/**
 * redis에 저장되는 groupEntryCode에 대한 정보입니다.
 *
 * <p>추후 GroupEntryCodeRedisRepository에 사용되며, 그룹 초대 코드가 저장되는 하나의 단위입니다. id, code 가 들어가 있습니다.
 *
 * @author s0o0bn
 * @since 2025-04-15
 */
@RedisHash("entryCode")
@AllArgsConstructor
@Builder
@Getter
public class GroupEntryCode {
    @Id
    private String code;

    @Indexed
    private Long groupId;

    @TimeToLive(unit = TimeUnit.DAYS)
    @Builder.Default
    @Setter
    private Long ttl = 1L;
}
