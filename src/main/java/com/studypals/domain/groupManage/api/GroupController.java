package com.studypals.domain.groupManage.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.GetGroupTagRes;
import com.studypals.domain.groupManage.dto.GroupEntryCodeRes;
import com.studypals.domain.groupManage.dto.GroupSummaryRes;
import com.studypals.domain.groupManage.service.GroupService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 그룹 관리에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 *
 * <pre>
 *     - GET /groups/tags : 그룹 태그 조회
 *     - POST /groups : 그룹 생성({@link CreateGroupReq})
 *     - POST /groups/{groupId}/entry-code : 그룹 초대 코드 생성
 *     - GET /groups/summary : 그룹 대표 정보 조회
 * </pre>
 *
 * @author s0o0bn
 * @since 2025-04-12
 */
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupController {
    private final GroupService groupService;

    @GetMapping("/tags")
    public ResponseEntity<Response<List<GetGroupTagRes>>> getGroupTags() {
        List<GetGroupTagRes> tags = groupService.getGroupTags();

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.GROUP_TAG_LIST, tags));
    }

    @PostMapping
    public ResponseEntity<Void> createGroup(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody CreateGroupReq request) {
        Long groupId = groupService.createGroup(userId, request);

        return ResponseEntity.created(URI.create("/groups/" + groupId)).build();
    }

    @PostMapping("/{groupId}/entry-code")
    public ResponseEntity<Response<GroupEntryCodeRes>> generateEntryCode(
            @AuthenticationPrincipal Long userId, @PathVariable Long groupId) {
        GroupEntryCodeRes codeResponse = groupService.generateEntryCode(userId, groupId);
        Response<GroupEntryCodeRes> response = CommonResponse.success(ResponseCode.GROUP_ENTRY_CODE, codeResponse);

        return ResponseEntity.created(URI.create("/groups/" + groupId + "/entry-code/" + codeResponse.code()))
                .body(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<Response<GroupSummaryRes>> getGroupSummary(@RequestParam String entryCode) {
        GroupSummaryRes response = groupService.getGroupSummary(entryCode);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.GROUP_SUMMARY, response));
    }
}
