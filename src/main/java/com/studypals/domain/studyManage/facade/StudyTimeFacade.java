package com.studypals.domain.studyManage.facade;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyRes;
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
}
