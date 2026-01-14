package com.studypals.domain.groupManage.dto;

import lombok.Builder;

/**
 *
 *
 * @author jack8
 * @since 2026-01-13
 */
@Builder
public record GroupSearchDto(String tag, String hashTag, String name, Boolean isOpen, Boolean isApprovalRequired) {
    public void validate() {
        int count = 0;

        if (hasText(tag)) count++;
        if (hasText(hashTag)) count++;
        if (hasText(name)) count++;

        if (count > 1) {
            throw new IllegalArgumentException("tag, hashTag, name 중 하나만 허용됩니다.");
        }
    }

    private static boolean hasText(String s) {
        return s != null && !s.isBlank();
    }
}
