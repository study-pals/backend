package com.studypals.domain.groupManage.api;

import java.util.List;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupMemberRankingDto;
import com.studypals.domain.groupManage.entity.GroupRankingPeriod;
import com.studypals.domain.groupManage.service.GroupRankingService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 그룹 랭킹 조회 엔드포인트입니다.
 * <pre>
 *     - GET /groups/rank/{groupId}/{period} : 특정 그룹에 대한 일간/주간/월간 랭킹을 조회합니다. (최대 유저는 99명)
 * </pre>
 *
 * @author sleepyhoon
 * @since 2025-12-27
 */
@RestController
@RequiredArgsConstructor
@RequestMapping("/groups")
public class GroupRankingController {
    private final GroupRankingService groupRankingService;

    @GetMapping("/rank/{groupId}/{period}")
    public ResponseEntity<Response<List<GroupMemberRankingDto>>> getGroupRanking(
            @PathVariable Long groupId, @PathVariable GroupRankingPeriod period, @AuthenticationPrincipal Long userId) {
        List<GroupMemberRankingDto> response = groupRankingService.getGroupRanking(userId, groupId, period);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.GROUP_RANKING, response));
    }
}
