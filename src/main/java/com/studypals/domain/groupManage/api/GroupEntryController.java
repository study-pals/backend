package com.studypals.domain.groupManage.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupEntryReq;
import com.studypals.domain.groupManage.service.GroupEntryService;

/**
 * 그룹 가입 관리에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 *
 * <pre>
 *     - POST /groups/join : 공개 그룹에 가입
 *     - POST /groups/request-entry : 비공개 그룹 가입 요청
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

    @PostMapping("/join")
    public ResponseEntity<Void> joinGroup(@AuthenticationPrincipal Long userId, @RequestBody GroupEntryReq req) {
        Long joinId = groupEntryService.joinGroup(userId, req);

        return ResponseEntity.created(URI.create(String.format("/groups/%d/members/%d", req.groupId(), joinId)))
                .build();
    }

    @PostMapping("/request-entry")
    public ResponseEntity<Void> requestGroupParticipant(
            @AuthenticationPrincipal Long userId, @RequestBody GroupEntryReq req) {
        Long requestId = groupEntryService.requestParticipant(userId, req);

        return ResponseEntity.created(URI.create(String.format("/groups/%d/requests/%d", req.groupId(), requestId)))
                .build();
    }
}
