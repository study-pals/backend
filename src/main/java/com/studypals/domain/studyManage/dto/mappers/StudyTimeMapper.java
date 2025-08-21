package com.studypals.domain.studyManage.dto.mappers;

import java.time.LocalDateTime;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.studypals.domain.studyManage.dto.*;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 *
 *
 * @author jack8
 * @since 2025-04-13
 */
@Mapper(componentModel = "spring")
public interface StudyTimeMapper {

    StartStudyRes toDto(StudyStatus entity);

    @Mapping(target = "categoryId", source = "studyCategory.id")
    GetStudyDto toDto(StudyTime entity);

    @Mapping(target = "categoryId", source = "studyCategory.id")
    StudyList toStudyDto(StudyTime entity);

    @Mapping(target = "startDateTime", source = "startDateTime")
    StartStudyDto toDto(StartStudyReq req, LocalDateTime startDateTime);
}
