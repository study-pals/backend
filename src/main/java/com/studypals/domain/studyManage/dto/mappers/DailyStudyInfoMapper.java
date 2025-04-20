package com.studypals.domain.studyManage.dto.mappers;

import org.mapstruct.Mapper;

import com.studypals.domain.studyManage.dto.GetDailyStudyInfoDto;
import com.studypals.domain.studyManage.entity.DailyStudyInfo;

/**
 * DailyStudyInfo 에 대한 mapper 클래스
 *
 * @author jack8
 * @since 2025-04-19
 */
@Mapper(componentModel = "spring")
public interface DailyStudyInfoMapper {

    GetDailyStudyInfoDto toDto(DailyStudyInfo entity);
}
