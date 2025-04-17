package com.studypals.domain.studyManage.dto;

import java.time.LocalTime;

/**
 * 공부 종료 시 사용되는 데이터. 언제 종료 했는지에 대한 데이터가 포함되어 있다.
 *
 * @author jack8
 * @since 2025-04-14
 */
public record EndStudyReq(LocalTime endedAt) {}
