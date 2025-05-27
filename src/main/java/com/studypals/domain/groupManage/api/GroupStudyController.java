package com.studypals.domain.groupManage.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.DailySuccessRateRes;
import com.studypals.domain.groupManage.service.GroupStudyCategoryService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 그룹 루틴 관리에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 *
 * <pre>
 *     - GET /groups/{groupId}/routines/daily-goal : 오늘의 목표 조회
 * </pre>
 *
 * @author s0o0bn
 * @since 2025-05-11
 */
@RestController
@RequestMapping("/groups/{groupId}/routines")
@RequiredArgsConstructor
public class GroupStudyController {
    private final GroupStudyCategoryService groupStudyCategoryService;

    @GetMapping("/daily-goal")
    public ResponseEntity<Response<DailySuccessRateRes>> getGroupDailyGoal(@PathVariable Long groupId) {
        DailySuccessRateRes response = groupStudyCategoryService.getGroupDailyGoal(groupId);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.GROUP_DAILY_GOAL, response));
    }
}
