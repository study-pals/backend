package com.studypals.domain.studyManage.dto.mappers;

import org.mapstruct.Mapper;

import com.studypals.domain.studyManage.dto.StartStudyDto;
import com.studypals.domain.studyManage.entity.StudyStatus;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 *
 *
 * @author jack8
 * @since 2025-04-13
 */
@Mapper(componentModel = "spring")
public interface StudyTimeMapper {

    StartStudyDto toDto(StudyStatus entity);
}
