package com.studypals.domain.groupManage.dto;

import jakarta.validation.constraints.AssertTrue;

/**
 * 초대 코드 만료 기한을 재설정하는 request dto 입니다. -1 인 경우 만료가 없는 무제한 초대 코드입니다.
 *
 * @author jack8
 * @see
 * @since 2026-01-08
 */
public record UpdateEntryCodeReq(Long day) {

    @AssertTrue(message = "day 는 -1 이거나 1 이상, 30 이하여야 합니다.")
    public boolean isDayMatch() {
        return (day == -1 || (day >= 1 && day <= 30));
    }
}
