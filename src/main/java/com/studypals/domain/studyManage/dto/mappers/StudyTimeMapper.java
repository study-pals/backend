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

    @Mapping(target = "studyTime", source = "studyTime")
    StartStudyRes toDto(StudyStatus entity, Long studyTime);

    @Mapping(target = "categoryId", source = "studyCategory.id")
    GetStudyDto toDto(StudyTime entity);

    @Mapping(target = "categoryId", source = "studyCategory.id")
    StudyTimeInfo toStudyDto(StudyTime entity);

    @Mapping(target = "startDateTime", source = "startDateTime")
    StartStudyDto toDto(StartStudyReq req, LocalDateTime startDateTime);

    StudyStatusRes toStudyStatusDto(Boolean studying);

    @Mapping(target = "studyTime", source = "studyTime")
    StudyStatusRes toStudyStatusDto(StudyStatus entity, Long studyTime);
}
