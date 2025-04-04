package com.studypals.domain.memberManage.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Builder;

/**
 * 토큰 재발급 시 반환됩니다.(response)
 * <p>

 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
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
 * @since 2025-04-04
 */
@Builder
public record ReissueTokenRes (
        @JsonIgnore
        Long userId,
        String accessToken,
        String refreshToken
) { }
