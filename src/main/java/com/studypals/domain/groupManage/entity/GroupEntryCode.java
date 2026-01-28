package com.studypals.domain.groupManage.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.Id;
import org.springframework.data.redis.core.RedisHash;
import org.springframework.data.redis.core.TimeToLive;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

import com.studypals.domain.groupManage.dto.GroupEntryCodeConstant;
import com.studypals.global.utils.TimeUtils;

/**
 * redis에 저장되는 groupEntryCode에 대한 정보입니다.
 *
 * <p>추후 GroupEntryCodeRedisRepository에 사용되며, 그룹 초대 코드가 저장되는 하나의 단위입니다. id, code 가 들어가 있습니다.
 *
 * @author s0o0bn
 * @since 2025-04-15
 */
@RedisHash(GroupEntryCodeConstant.IDENTIFIER)
@AllArgsConstructor
@Builder
@Getter
public class GroupEntryCode {
    @Id
    private String code;

    private Long groupId;

    @Setter
    private LocalDateTime expireAt;

    @TimeToLive
    @Builder.Default
    @Setter
    private Long ttl = TimeUtils.getSecondOfDay(1L);
}
