package com.studypals.domain.groupManage.dto;

import jakarta.validation.constraints.NotBlank;

import com.studypals.domain.groupManage.entity.Group;

/**
 * 그룹 생성 시 사용되는 DTO 입니다.
 *
 * <p>record 를 사용하였으며 Validaton 어노테이션을 통해 최소한의 검증이 이루어집니다.
 *
 * @author s0o0bn
 * @since 2025-04-12
 */
public record CreateGroupReq(
        @NotBlank String name, @NotBlank String tag, Integer maxMember, Boolean isOpen, Boolean isApprovalRequired) {

    public Group toEntity() {
        return Group.builder()
                .name(name)
                .tag(tag)
                .maxMember(maxMember)
                .isOpen(isOpen)
                .isApprovalRequired(isApprovalRequired)
                .build();
    }
}
