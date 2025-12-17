package com.studypals.domain.studyManage.api;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dto.EndStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.dto.StudyStatusRes;
import com.studypals.domain.studyManage.service.StudySessionService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 공부 시간 데이터 전반에 대한 컨트롤러입니다. 사용자는 해당 엔드포인트를 사용하여 공부를 시작하고 마칠 수 있습니다. <br>
 * 내부 구현 원리는 문서를 참조해 주시기 바랍니다. <br>
 * 담당하는 엔드포인트는 다음과 같습니다.
 * <pre>
 *     - GET  /studies/sessions/check        : 공부 상태 확인
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

    @GetMapping("/check")
    public ResponseEntity<Response<StudyStatusRes>> check(@AuthenticationPrincipal Long userId) {
        StudyStatusRes response = studySessionService.checkStudyStatus(userId);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_STATUS_CHECK, response, "success check"));
    }

    @PostMapping("/start")
    public ResponseEntity<Response<StartStudyRes>> start(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody StartStudyReq req) {

        StartStudyRes response = studySessionService.startStudy(userId, req);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_START, response, "success start"));
    }

    @PostMapping("/end")
    public ResponseEntity<Response<Long>> end(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody EndStudyReq req) {

        Long response = studySessionService.endStudy(userId, req.endTime());

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.STUDY_START, response, "success end"));
    }
}
