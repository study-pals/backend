package com.studypals.domain.studyManage.dto;

/**
 * 하루 동안 공부한 카테고리 혹은 임시 토픽 등의 데이터에 대한 리스트를 구성하기 위한 dto
 *
 * @author jack8
 * @see GetDailyStudyRes
 * @see GetDailyStudyDto
 * @since 2025-04-19
 */
public record StudyTimeInfo(Long categoryId, String name, Long time) {
    public StudyTimeInfo {
        if (time != null && time < 0) {
            throw new IllegalArgumentException("study time must be non-negative");
        }
    }
}
