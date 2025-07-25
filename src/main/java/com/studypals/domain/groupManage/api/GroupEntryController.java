package com.studypals.domain.groupManage.api;

import java.net.URI;

import jakarta.validation.Valid;
import jakarta.validation.constraints.Size;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.*;
import com.studypals.domain.groupManage.service.GroupEntryService;
import com.studypals.global.annotations.CursorDefault;
import com.studypals.global.request.Cursor;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.CursorResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 그룹 가입 관리에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 *
 * <pre>
 *     - POST /groups/{groupId}/entry-code : 그룹 초대 코드 생성
 *     - GET /groups/summary : 그룹 대표 정보 조회
 *     - POST /groups/join : 공개 그룹에 가입
 *     - POST /groups/entry-requests : 비공개 그룹 가입 요청
 *     - GET /groups/{groupId}/entry-requests : 그룹 가입 요청 목록 조회
 *     - POST /groups/entry-requests/{requestId}/accept : 그룹 가입 요청 승인
 *     - DELETE /groups/entry-requests/{requestId} : 그룹 가입 요청 거절
 * </pre>
 *
 * @author s0o0bn
 * @since 2025-04-25
 */
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupEntryController {
    private final GroupEntryService groupEntryService;

    @PostMapping("/{groupId}/entry-code")
    public ResponseEntity<Response<GroupEntryCodeRes>> generateEntryCode(
            @AuthenticationPrincipal Long userId, @PathVariable Long groupId) {
        GroupEntryCodeRes codeResponse = groupEntryService.generateEntryCode(userId, groupId);
        Response<GroupEntryCodeRes> response = CommonResponse.success(ResponseCode.GROUP_ENTRY_CODE, codeResponse);

        return ResponseEntity.created(URI.create("/groups/" + groupId + "/entry-code/" + codeResponse.code()))
                .body(response);
    }

    @GetMapping("/summary")
    public ResponseEntity<Response<GroupSummaryRes>> getGroupSummary(
            @RequestParam(required = true)
                    @Size(min = 6, max = 6, message = "entry code must be exactly 6 characters long.")
                    String entryCode) {
        GroupSummaryRes response = groupEntryService.getGroupSummary(entryCode);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.GROUP_SUMMARY, response));
    }

    @PostMapping("/join")
    public ResponseEntity<Void> joinGroup(@AuthenticationPrincipal Long userId, @Valid @RequestBody GroupEntryReq req) {
        Long joinId = groupEntryService.joinGroup(userId, req);

        return ResponseEntity.created(URI.create(String.format("/groups/%d/members/%d", req.groupId(), joinId)))
                .build();
    }

    @PostMapping("/entry-requests")
    public ResponseEntity<Void> requestGroupParticipant(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody GroupEntryReq req) {
        Long requestId = groupEntryService.requestParticipant(userId, req);

        return ResponseEntity.created(URI.create(String.format("/groups/%d/requests/%d", req.groupId(), requestId)))
                .build();
    }

    @GetMapping("/{groupId}/entry-requests")
    public ResponseEntity<CursorResponse<GroupEntryRequestDto>> getEntryRequests(
            @AuthenticationPrincipal Long userId, @PathVariable Long groupId, @CursorDefault Cursor cursor) {
        CursorResponse.Content<GroupEntryRequestDto> response =
                groupEntryService.getEntryRequests(userId, groupId, cursor);
        return ResponseEntity.ok(CursorResponse.success(ResponseCode.GROUP_ENTRY_REQUEST_LIST, response));
    }

    @PostMapping("/entry-requests/{requestId}/accept")
    public ResponseEntity<Void> acceptEntryRequest(@AuthenticationPrincipal Long userId, @PathVariable Long requestId) {
        AcceptEntryRes response = groupEntryService.acceptEntryRequest(userId, requestId);

        return ResponseEntity.created(
                        URI.create(String.format("/groups/%d/members/%d", response.groupId(), response.memberId())))
                .build();
    }

    @DeleteMapping("/entry-requests/{requestId}")
    public ResponseEntity<Void> refuseEntryRequest(@AuthenticationPrincipal Long userId, @PathVariable Long requestId) {
        groupEntryService.refuseEntryRequest(userId, requestId);

        return ResponseEntity.noContent().build();
    }
}
