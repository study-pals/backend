package com.studypals.domain.groupManage.entity;

import com.studypals.domain.groupManage.service.GroupEntryService;
import lombok.Getter;

/**
 * group 그룹 관련 상수를 담는 enum 클래스입니다.
 *
 *
 * @author sleepyhoon
 * @since 2025-12-22
 */
@Getter
public enum GroupConst {
    /** 그룹 요약 정보 조회 시 포함되는 그룹 멤버 수 */
    GROUP_SUMMARY_MEMBER_COUNT(4),
    ;

    private final int value;
    GroupConst(int value) {
        this.value = value;
    }
}
