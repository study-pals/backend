package com.studypals.domain.studyManage.dto;

import lombok.Builder;

/**
 * 클라이언트가 study category 를 요청하면, 해당 record에 대한 list를 반환합니다.
 *
 * @author jack8
 * @since 2025-04-12
 */
@Builder
public record GetCategoryRes(Long categoryId, String name, String color, Integer dayBelong, String description) {}
