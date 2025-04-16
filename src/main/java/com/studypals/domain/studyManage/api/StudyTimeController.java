package com.studypals.domain.studyManage.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.facade.StudyTimeFacade;
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

    private final StudyTimeFacade studyTimeFacade;

    @GetMapping("/{date}")
    public ResponseEntity<Response<List<GetStudyRes>>> studiesDate(
            @AuthenticationPrincipal Long userId, @PathVariable LocalDate date) {

        List<GetStudyRes> response = studyTimeFacade.getStudyTimeByDate(userId, date);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_TIME_PARTIAL, response, "data of date"));
    }
}
