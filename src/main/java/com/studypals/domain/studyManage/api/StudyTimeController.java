package com.studypals.domain.studyManage.api;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.GetCategoryRes;
import com.studypals.domain.studyManage.dto.GetStudyDto;
import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.service.StudyCategoryService;
import com.studypals.domain.studyManage.service.StudyTimeService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 공부 시간 데이터 전반에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 * <pre>
 *     - GET /studies/{date}        : 해당 날짜의 카테고리 및 공부 시간 반환
 * </pre>
 *
 * @author jack8
 * @since 2025-04-14
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/studies")
public class StudyTimeController {

    private final StudyTimeService studyTimeService;
    private final StudyCategoryService studyCategoryService;

    @GetMapping("/{date}")
    public ResponseEntity<Response<List<GetStudyRes>>> getStudyTimeByDate(
            @AuthenticationPrincipal Long userId, @PathVariable LocalDate date) {

        List<GetStudyDto> studies = studyTimeService.getStudyList(userId, date);
        List<GetCategoryRes> categories = studyCategoryService.getUserCategoryByDate(userId, date);

        List<GetStudyRes> response = toStudyResList(studies, categories);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_TIME_PARTIAL, response, "data of date"));
    }

    private List<GetStudyRes> toStudyResList(List<GetStudyDto> studies, List<GetCategoryRes> categories) {

        Map<Long, Long> timeByCategory = studies.stream()
                .filter(s -> s.categoryId() != null)
                .collect(Collectors.toMap(GetStudyDto::categoryId, GetStudyDto::time));

        List<GetStudyRes> result = categories.stream()
                .map(category -> GetStudyRes.builder()
                        .categoryId(category.categoryId())
                        .name(category.name())
                        .color(category.color())
                        .description(category.description())
                        .time(timeByCategory.getOrDefault(category.categoryId(), 0L))
                        .build())
                .toList();

        List<GetStudyRes> temporary = studies.stream()
                .filter(s -> s.categoryId() == null && s.temporaryName() != null)
                .map(s -> GetStudyRes.builder()
                        .temporaryName(s.temporaryName())
                        .time(s.time())
                        .build())
                .toList();

        List<GetStudyRes> combined = new ArrayList<>(result);
        combined.addAll(temporary);

        return combined;
    }
}
