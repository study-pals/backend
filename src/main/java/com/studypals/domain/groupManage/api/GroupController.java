package com.studypals.domain.groupManage.api;

import java.net.URI;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dto.CreateGroupReq;
import com.studypals.domain.groupManage.service.GroupService;

/**
 * 그룹 관리에 대한 컨트롤러입니다. 담당하는 엔드포인트는 다음과 같습니다.
 *
 * <pre>
 *     - POST /groups : 그룹 생성({@link CreateGroupReq})
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

    @PostMapping
    public ResponseEntity<Void> createGroup(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody CreateGroupReq request) {
        Long groupId = groupService.createGroup(userId, request);

        return ResponseEntity.created(URI.create("/groups/" + groupId)).build();
    }
}
