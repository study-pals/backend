package com.studypals.domain.studyManage.api;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.EndStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.service.StudySessionService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 공부 시간 데이터 전반에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 * <pre>
 *     - POST /studies/sessions/start        : 공부 시작
 *     - POST /studies/sessions/end          : 공부 끝
 * </pre>
 *
 * @author jack8
 * @since 2025-04-14
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/studies/sessions/")
public class StudySessionController {

    private final StudySessionService studySessionService;

    @PostMapping("/start")
    public ResponseEntity<Response<StartStudyRes>> start(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody StartStudyReq req) {

        StartStudyRes response = studySessionService.startStudy(userId, req);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_START, response, "success start"));
    }

    @PostMapping("/end")
    public ResponseEntity<Response<Long>> end(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody EndStudyReq req) {

        Long response = studySessionService.endStudy(userId, req.endedAt());

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_START, response, "success end"));
    }
}
