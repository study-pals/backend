package com.studypals.domain.groupManage.api;

import java.net.URI;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.GroupEntryInfo;
import com.studypals.domain.groupManage.service.GroupEntryService;

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
}
