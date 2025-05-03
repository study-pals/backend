package com.studypals.domain.groupManage.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;

/**
 * 그룹 가입 및 가입 요청 시 사용되는 DTO 입니다.
 *
 * <p>record 를 사용하였으며 Validaton 어노테이션을 통해 최소한의 검증이 이루어집니다.
 *
 * <pre>
 *     {@code groupId}는 NotNull
 *     {@code entryCode}의 길이는 무조건 6
 * </pre>
 *
 * @author s0o0bn
 * @since 2025-04-25
 */
public record GroupEntryReq(
        @NotNull Long groupId,
        @Size(min = 6, max = 6, message = "entry code must be exactly 6 characters long.") String entryCode) {}
