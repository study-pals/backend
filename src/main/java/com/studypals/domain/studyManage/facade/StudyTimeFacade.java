package com.studypals.domain.studyManage.facade;


import com.studypals.domain.studyManage.dto.*;
import com.studypals.domain.studyManage.service.DailyStudyInfoService;
import com.studypals.domain.studyManage.service.StudyTimeService;
import com.studypals.global.annotations.Facade;
import lombok.RequiredArgsConstructor;

import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * 공부 시간을 반환하기 전, 읽어온 데이터를 하나의 데이터로 합치는 역할을 수행하는 파사드 객체입니다.
 * StudyTime 과 DailyStudyInfo 테이블의 정보를 읽어와, 요구 사항에 맞게 이를 병합하는 과정을 포함하고 있습니다.
 * @author jack8
 * @since 2025-09-05
 */

@Facade
@RequiredArgsConstructor
public class StudyTimeFacade {

    private final StudyTimeService studyTimeService;
    private final DailyStudyInfoService dailyStudyInfoService;

    /**
     * 사용자의 아이디와 period 를 기반으로 하여, 공부 시간 및 당일 날 시작/종료 시각을 불러와, 하나의 데이터로 합칩니다.
     * 읽어온 데이터에 대해, 동일한 날짜를 기반으로 하여 {@link GetDailyStudyRes} 로 변환하여 반환합니다.
     * @param userId 사용자 아이디
     * @param periodDto 기간
     * @return 날짜에 대한 카테고리 별 공부 시간 및 시작/종료 시각, description 등
     */

    public List<GetDailyStudyRes> readAndConcatStudyData(Long userId, PeriodDto periodDto) {
        List<GetDailyStudyDto> studyData = studyTimeService.getDailyStudyList(userId, periodDto);
        List<GetDailyStudyInfoDto> dailyData = dailyStudyInfoService.getDailyStudyInfoList(userId, periodDto);

        //읽어온 데이터를 날짜에 대한 map 으로 변환
        Map<LocalDate, GetDailyStudyDto> studyDataMap = studyData.stream().collect(Collectors.toMap(
                GetDailyStudyDto::studiedDate,
                x -> x,
                (a, b) -> a,
                LinkedHashMap::new
        ));

        Map<LocalDate, GetDailyStudyInfoDto> dailyDataMap = dailyData.stream().collect(Collectors.toMap(
                GetDailyStudyInfoDto::studiedDate,
                x -> x,
                (a, b) -> a,
                LinkedHashMap::new
        ));

        //읽어온 데이터를 Stream 을 통해 하나로 합침
        return Stream.concat(studyDataMap.keySet().stream(), dailyDataMap.keySet().stream()) // key set 에 대한 stream 생성
                .distinct() //중복 제거
                .sorted()   //날짜에 따른 정렬
                .map(date -> {  //객체 생성
                    GetDailyStudyDto studyTime = studyDataMap.get(date);
                    GetDailyStudyInfoDto info = dailyDataMap.get(date);

                    if(info == null) {
                        return GetDailyStudyRes.builder()
                                .studiedDate(date)
                                .studies(studyTime.studyTimeInfo())
                                .build();
                    }

                    return GetDailyStudyRes.builder()
                            .studiedDate(date)
                            .studies(studyTime.studyTimeInfo())
                            .startTime(info.startTime())
                            .endTime(info.endTime())
                            .description(info.description())
                            .build();
                }).toList(); // 리스트 변환
    }
}
