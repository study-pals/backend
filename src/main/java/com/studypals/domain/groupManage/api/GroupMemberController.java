package com.studypals.domain.groupManage.api;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.service.GroupMemberService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * 그룹 멤버 관리에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 *
 * <pre>
 *     - PUT /groups/{groupId}/promote/{nextLeaderId} : 그룹장 권한 넘기기
 * </pre>
 *
 * @author zjxlomin
 * @since 2026-01-13
 */
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupMemberController {
    private final GroupMemberService groupMemberService;

    @PutMapping("/{groupId}/promote/{nextLeaderId}")
    public ResponseEntity<Response<Long>> promoteLeader(
            @AuthenticationPrincipal Long userId, @PathVariable Long groupId, @PathVariable Long nextLeaderId
    ) {
        groupMemberService.promoteLeader(groupId, userId, nextLeaderId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.GROUP_LEADER, groupId));
    }
}
