package com.studypals.domain.studyManage.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.annotation.JsonInclude;

@JsonInclude(JsonInclude.Include.NON_NULL)
public record StudyStatusRes(
        boolean studying, LocalDateTime startTime, Long studyTime, Long categoryId, String name, Long goal) {}
