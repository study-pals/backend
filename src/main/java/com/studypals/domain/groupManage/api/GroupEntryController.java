package com.studypals.domain.groupManage.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupEntryInfo;
import com.studypals.domain.groupManage.service.GroupEntryService;

/**
 * 그룹 가입 관리에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 *
 * <pre>
 *     - POST /groups/{groupId}/join : 공개 그룹에 가입
 *     - POST /groups/{groupId}/request-entry : 비공개 그룹 가입 요청
 * </pre>
 *
 * @author s0o0bn
 * @since 2025-04-25
 */
@RestController
@RequestMapping("/groups")
@RequiredArgsConstructor
public class GroupEntryController {
    private GroupEntryService groupEntryService;

    @PostMapping("/{groupId}/join")
    public ResponseEntity<Void> joinGroup(
            @AuthenticationPrincipal Long userId, @PathVariable Long groupId, @RequestParam String entryCode) {
        Long joinId = groupEntryService.joinGroup(userId, new GroupEntryInfo(groupId, entryCode));

        return ResponseEntity.created(URI.create("/groups/" + groupId + "/members/" + joinId))
                .build();
    }

    @PostMapping("/{groupId}/request-entry")
    public ResponseEntity<Void> requestGroupParticipant(
            @AuthenticationPrincipal Long userId, @PathVariable Long groupId, @RequestParam String entryCode) {
        Long requestId = groupEntryService.requestParticipant(userId, new GroupEntryInfo(groupId, entryCode));

        return ResponseEntity.created(URI.create("/groups/" + groupId + "/requests/" + requestId))
                .build();
    }
}
