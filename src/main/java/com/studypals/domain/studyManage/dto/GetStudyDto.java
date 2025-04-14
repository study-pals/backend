package com.studypals.domain.studyManage.dto;

/**
 * 공부한 시간에 대한 정보를 가져올 때 사용합니다. 공부한 카테고리/이름에 대해서만 반환하도록 설계되었습니다.
 *
 * @author jack8
 * @since 2025-04-14
 */
public record GetStudyDto(Long categoryId, String temporaryName, Long time) {}
