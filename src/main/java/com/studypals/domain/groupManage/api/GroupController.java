package com.studypals.domain.groupManage.api;

import java.net.URI;
import java.util.List;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.dto.GetGroupDetailRes;
import com.studypals.domain.groupManage.dto.GetGroupTagRes;
import com.studypals.domain.groupManage.dto.GetGroupsRes;
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
 *     - GET /groups : 유저가 속한 그룹 조회
 *     - GET /groups/{groupId} : 그룹 정보 조회
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

    @GetMapping
    public ResponseEntity<Response<List<GetGroupsRes>>> getGroups(@AuthenticationPrincipal Long userId) {
        List<GetGroupsRes> response = groupService.getGroups(userId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.GROUP_LIST, response));
    }

    // 오늘의 목표 정보도 포함시켜야함
    @GetMapping("/{groupId}")
    public ResponseEntity<Response<GetGroupDetailRes>> getGroupDetail(
            @AuthenticationPrincipal Long userId, @PathVariable Long groupId) {
        GetGroupDetailRes response = groupService.getGroupDetails(userId, groupId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.GROUP_DETAIL, response));
    }
}
