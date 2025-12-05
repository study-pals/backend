package com.studypals.domain.studyManage.dto;

import java.time.LocalDateTime;

public record StudyingStatusRes(
        boolean studying, LocalDateTime startTime, Long studyTime, Long categoryId, String name, Long goal)
        implements StudyStatusRes {}
