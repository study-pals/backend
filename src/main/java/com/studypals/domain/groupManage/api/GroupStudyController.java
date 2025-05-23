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
