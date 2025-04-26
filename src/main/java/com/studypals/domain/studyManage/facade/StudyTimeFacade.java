package com.studypals.domain.studyManage.facade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.*;
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
        List<GetCategoryRes> categories = studyCategoryService.getUserCategoryByDate(userId, date);

        return toStudyResList(studies, categories);
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
        List<GetDailyStudyDto> studies = studyTimeService.getDailyStudyList(userId, period);
        List<GetDailyStudyInfoDto> infos = dailyStudyInfoService.getDailyStudyInfoList(userId, period);

        return toDailyStudyList(studies, infos);
    }

    /**
     * 카테고리 리스트와 공부 시간 리스트를 취합하여 하나의 리스트로 만드는 메서드.
     * 만약 카테고리에는 있으나 공부 시간에는 없다면, 해당 카테고리의 공부 시간은 0이다.
     * @param studies 공부 시간 리스트
     * @param categories 카테고리 리스트
     * @return 공부시간과 카테고리를 합한 리스트
     */
    private List<GetStudyRes> toStudyResList(List<GetStudyDto> studies, List<GetCategoryRes> categories) {

        // 공부 시간 리스트에서 카테고리에 대한 공부 시간을 추출
        Map<Long, Long> timeByCategory = studies.stream()
                .filter(s -> s.categoryId() != null)
                .collect(Collectors.toMap(GetStudyDto::categoryId, GetStudyDto::time));

        // 카테고리 리스트에서 방금 추출한 카테고리에 대한 공부 시간을 기반으로 매칭
        List<GetStudyRes> result = categories.stream()
                .map(category -> GetStudyRes.builder()
                        .categoryId(category.categoryId())
                        .name(category.name())
                        .color(category.color())
                        .description(category.description())
                        .time(timeByCategory.getOrDefault(category.categoryId(), 0L))
                        .build())
                .toList();

        // 공부 시간 리스트에서 카테고리가 아닌 임시 이름을 추출하여 저장
        List<GetStudyRes> temporary = studies.stream()
                .filter(s -> s.categoryId() == null && s.temporaryName() != null)
                .map(s -> GetStudyRes.builder()
                        .temporaryName(s.temporaryName())
                        .time(s.time())
                        .build())
                .toList();

        // 위 리스트를 합침
        List<GetStudyRes> combined = new ArrayList<>(result);
        combined.addAll(temporary);

        return combined;
    }

    /**
     * 두 리스트에서 값을 합쳐 반환한다.
     * studies 는 특정 기간 동안 모든 카테고리(임시토픽) 당 공부 시간에 대한 정보이고, infos 는 특정 날짜의 시작/종료 시간 및 메모
     * 에 대한 정보이다.
     * 따라서, studies 로부터 공부 날짜와 그에 따른 공부 리스트를 map 으로 구성하여, 반환 시 이를 기반으로 공부 날짜를 매칭하여 반환
     * @param studies 카테고리(임시토픽) 공부시간
     * @param infos 하루의 정보
     * @return 합친 것
     */
    private List<GetDailyStudyRes> toDailyStudyList(List<GetDailyStudyDto> studies, List<GetDailyStudyInfoDto> infos) {

        Map<LocalDate, List<StudyList>> studyMap =
                studies.stream().collect(Collectors.toMap(GetDailyStudyDto::studiedDate, GetDailyStudyDto::studyList));

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
