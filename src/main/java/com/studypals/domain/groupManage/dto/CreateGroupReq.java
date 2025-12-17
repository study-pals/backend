package com.studypals.domain.groupManage.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;

/**
 * 그룹 생성 시 사용되는 DTO 입니다.
 *
 * <p>record 를 사용하였으며 Validaton 어노테이션을 통해 최소한의 검증이 이루어집니다.
 *
 * <pre>
 *     {@code name}, {@code tag}는 NotBlank
 *     {@code maxMember}는 10 < < 100
 * </pre>
 *
 * @author s0o0bn
 * @since 2025-04-12
 */
public record CreateGroupReq(
        @NotBlank String name,
        @NotBlank String tag,
        @Min(10) @Max(100) Integer maxMember,
        Boolean isOpen,
        Boolean isApprovalRequired,
        // since 12-05 sanghyeok
        String imageUrl) {}
