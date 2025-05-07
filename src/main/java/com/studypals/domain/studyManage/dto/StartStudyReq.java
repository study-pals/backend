package com.studypals.domain.studyManage.dto;

import java.time.LocalTime;

import jakarta.validation.constraints.AssertTrue;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * 공부 시작 시 시의 데이터입니다. typeId 와 temporaryName은 하나만 존재할 수 있습니다.
 *
 * @author jack8
 * @since 2025-04-13
 */
public record StartStudyReq(StudyType studyType, Long typeId, String temporaryName, LocalTime startTime) {
    @AssertTrue(message = "typeId 와 temporaryName 중 하나만 존재해야 합니다.")
    @JsonIgnore
    public boolean isValidExclusive() {
        return (typeId != null && temporaryName == null) || (typeId == null && temporaryName != null);
    }
}
