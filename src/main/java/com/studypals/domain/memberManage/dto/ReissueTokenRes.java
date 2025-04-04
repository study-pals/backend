package com.studypals.domain.memberManage.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

/**
 * 토큰 재발급 시 반환됩니다.(response)
 * <p>
 * userId, accessToken, refreshToken이 들어갑니다. userId는 사실
 * service -> controller로 데이터를 넘길 때 필요해서 넣었는데, 클라이언트까지 보낼 필요가 없어서
 * userId에 @JsonIgnore 을 통하여 직렬화를 방지하였습니다.
 *
 * @author jack8
 * @since 2025-04-04
 */
@Builder
public record ReissueTokenRes (
        @JsonIgnore
        Long userId,
        String accessToken,
        String refreshToken
) { }
