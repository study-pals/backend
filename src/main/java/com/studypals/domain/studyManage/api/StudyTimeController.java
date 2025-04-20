package com.studypals.domain.studyManage.api;

import java.time.LocalDate;
import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.GetDailyStudyRes;
import com.studypals.domain.studyManage.dto.GetStudyRes;
import com.studypals.domain.studyManage.dto.PeriodDto;
import com.studypals.domain.studyManage.facade.StudyTimeFacade;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 공부 시간 데이터 전반에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 * <pre>
 *     - GET /studies/{date}        : 해당 날짜의 카테고리 및 공부 시간 반환(쿼리 파라미터, date)
 *     - GET /studies/stat          : 특정 기간 간 통계를 받아옵니다(쿼리 파라미터, start/end)
 * </pre>
 *
 * @author jack8
 * @since 2025-04-14
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/stat")
public class StudyTimeController {

    private final StudyTimeFacade studyTimeFacade;

    @GetMapping(params = "date")
    public ResponseEntity<Response<List<GetStudyRes>>> studiesDate(
            @AuthenticationPrincipal Long userId, @RequestParam LocalDate date) {

        List<GetStudyRes> response = studyTimeFacade.getStudyTimeByDate(userId, date);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_TIME_PARTIAL, response, "data of date"));
    }

    @GetMapping(params = {"start", "end"})
    public ResponseEntity<Response<List<GetDailyStudyRes>>> studiesDateByPeriod(
            @AuthenticationPrincipal Long userId, @RequestParam LocalDate start, @RequestParam LocalDate end) {
        PeriodDto periodDto = new PeriodDto(start, end);
        List<GetDailyStudyRes> response = studyTimeFacade.getDailyStudyTimeByPeriod(userId, periodDto);

        return ResponseEntity.ok(
                CommonResponse.success(ResponseCode.STUDY_TIME_ALL, response, "data of study time by period"));
    }
}
