package com.studypals.domain.groupManage.dto;

import lombok.Builder;

/**
 * 그룹 검색 조건을 전달하기 위한 DTO입니다.
 *
 * <p>
 * 그룹 목록 조회 시 사용자가 입력한 검색 조건을 담으며,
 * 키워드 기반 검색은 {@code tag}, {@code hashTag}, {@code name} 중
 * 하나만 허용하도록 제한합니다.
 * </p>
 *
 * <p>
 * 상태 필터 조건으로 공개 여부({@code isOpen})와
 * 승인 필요 여부({@code isApprovalRequired})를 함께 전달할 수 있습니다.
 * </p>
 *
 * <p>
 * {@link #validate()} 메서드는 키워드 조건이 동시에 여러 개 지정되는 것을
 * 방지하기 위한 검증 로직을 담당합니다.
 * </p>
 *
 * @author jack8
 * @since 2026-01-13
 */
@Builder
public record GroupSearchDto(String tag, String hashTag, String name, Boolean isOpen, Boolean isApprovalRequired) {
    /**
     * 키워드 검색 조건의 유효성을 검증합니다.
     *
     * <p>
     * {@code tag}, {@code hashTag}, {@code name} 중
     * 두 개 이상이 동시에 지정되면 예외를 발생시킵니다.
     * </p>
     *
     * @throws IllegalArgumentException 키워드 조건이 둘 이상 지정된 경우
     */
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
