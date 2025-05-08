package com.studypals.domain.studyManage.facade;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.*;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.domain.studyManage.facade.strategy.StudyRenderStrategyFactory;
import com.studypals.domain.studyManage.service.DailyStudyInfoService;
import com.studypals.domain.studyManage.service.StudyCategoryService;
import com.studypals.domain.studyManage.service.StudyTimeService;
import com.studypals.global.annotations.Facade;

/**
 * StudyTime 에 대하 facade 레이어 객체이다. 공부 시간 및 카테고리 데이터를 정제할 때 사용한다.
 * <p>
 * studyTimeSerivce 및 studyCategoryService 의 의존성을 주입받아, 해당 하는 데이터를 받아 취합한다.
 *
 * <p><b>빈 관리:</b><br>
 * custom component {@code @Facade}
 *
 * @author jack8
 * @since 2025-04-15
 */
@RequiredArgsConstructor
@Facade
public class StudyTimeFacade {

    private final StudyTimeService studyTimeService;
    private final StudyCategoryService studyCategoryService;
    private final DailyStudyInfoService dailyStudyInfoService;
    private final StudyRenderStrategyFactory strategyFactory;

    /**
     * 특정 날짜의 공부 시간을 반환한다. studyTimeService 로부터 해당 날짜에 공부한 기록 및,
     * studyCateogrySerivce 로 부터 해당 날짜의 카테고리 리스트를 받아온 다음, 이 내용을
     * 취합하여 반환한다.
     * @param userId 검색하고자 하는 유저의 id
     * @param date 검색하고 하는 날짜
     * @return 해당 날짜의 공부 기록(카테고리, 혹은 임시 이름 기반 공부 시간 등)
     */
    public List<GetStudyRes> getStudyTimeByDate(Long userId, LocalDate date) {
        List<GetStudyDto> studies = studyTimeService.getStudyList(userId, date);

        // 고민 중... 해당 부분도 전략 패턴으로 옮기는게 맞을까?
        // 유지보수성이냐(전략 패턴에 옮기기), 객체의 책임의 분리냐(현재 위치 유지)
        List<GetCategoryRes> personalCategories = studyCategoryService.getUserCategory(userId);

        return strategyFactory.compose(studies, Map.of(StudyType.PERSONAL, personalCategories));
    }

    /**
     * 특정 기간에 대하여, 해당 기간 동안의 공부 정보를 받아온다.
     * studyTimeService 로부터 특정 기간 동안의 모든 기록(카테고리/임시 토픽 - 시간)을 받아오고
     * dailyStudyInfoService 로부터 해당 날짜의 시작/종료 시간 및 메모를 받아와 합쳐서 반환한다.
     * @param userId 검색하고자 하는 user 의 id
     * @param period 검색하고자 하는 기간
     * @return 특정 날짜 구간에 대한 공부 정보 리스트
     */
    public List<GetDailyStudyRes> getDailyStudyTimeByPeriod(Long userId, PeriodDto period) {
        List<GetDailyStudyDto> dailyStudies = studyTimeService.getDailyStudyList(userId, period);
        List<GetDailyStudyInfoDto> infos = dailyStudyInfoService.getDailyStudyInfoList(userId, period);

        Map<LocalDate, List<StudyList>> studyMap = dailyStudies.stream()
                .collect(Collectors.toMap(GetDailyStudyDto::studiedDate, GetDailyStudyDto::studyList));

        return infos.stream()
                .map(info -> GetDailyStudyRes.builder()
                        .studiedDate(info.studiedDate())
                        .startTime(info.startTime())
                        .endTime(info.endTime())
                        .memo(info.memo())
                        .studies(studyMap.getOrDefault(info.studiedDate(), List.of()))
                        .build())
                .toList();
    }
}
