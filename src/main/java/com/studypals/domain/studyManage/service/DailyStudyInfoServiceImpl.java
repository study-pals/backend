package com.studypals.domain.studyManage.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.GetDailyStudyInfoDto;
import com.studypals.domain.studyManage.dto.PeriodDto;
import com.studypals.domain.studyManage.dto.mappers.DailyStudyInfoMapper;
import com.studypals.domain.studyManage.worker.DailyInfoReader;

/**
 * {@link DailyStudyInfoService} 의 구현 클래스입니다.
 * 오버라이드된 메서드가 존재합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@code DailyStudyInfoService} 의 구현 클래스
 *
 *
 * <p><b>빈 관리:</b><br>
 * private final DailyStudyInfoMapper mapper;
 * private final DailyInfoReader dailyInfoReader;
 *
 * @author jack8
 * @since 2025-04-19
 */
@Service
@RequiredArgsConstructor
public class DailyStudyInfoServiceImpl implements DailyStudyInfoService {

    private final DailyStudyInfoMapper mapper;
    private final DailyInfoReader dailyInfoReader;

    @Override
    @Transactional(readOnly = true)
    public List<GetDailyStudyInfoDto> getDailyStudyInfoList(Long userId, PeriodDto period) {
        return dailyInfoReader.getDailyInfoListByPeriod(userId, period).stream()
                .map(mapper::toDto)
                .toList();
    }
}
