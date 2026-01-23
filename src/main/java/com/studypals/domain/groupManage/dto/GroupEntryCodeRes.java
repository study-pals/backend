package com.studypals.domain.groupManage.dto;

import java.time.LocalDateTime;

import lombok.Builder;

/**
 * 클라이언트가 그룹 초대 코드 생성을 요청하면, 해당 그룹 ID와 생성된 코드를 반환합니다.
 *
 * @author s0o0bn
 * @since 2025-04-15
 */
@Builder
public record GroupEntryCodeRes(Long groupId, String code, LocalDateTime expiredAt) {}
