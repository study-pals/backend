package com.studypals.domain.memberManage.api;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.memberManage.dto.*;
import com.studypals.domain.memberManage.service.MemberService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 회원가입/프로필 관련 API 입니다.
 *
 * <pre>
 *     - POST /register : 회원가입({@link CreateMemberReq})
 *     - GET /profile : 프로필 조회
 *     - PUT /profile : 프로필 수정 ({@link UpdateProfileReq})
 *
 * </pre>
 *
 * @author jack8
 * @since 2025-12-16
 */
@RestController
@RequiredArgsConstructor
public class MemberController {
    private final MemberService memberService;

    @PostMapping("/register")
    public ResponseEntity<Response<Long>> register(@Valid @RequestBody CreateMemberReq req) {
        Long id = memberService.createMember(req);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.USER_CREATE, id, "회원가입을 성공하였습니다."));
    }

    @GetMapping("/profile")
    public ResponseEntity<Response<MemberDetailsRes>> getProfile(@AuthenticationPrincipal Long userId) {
        MemberDetailsRes res = memberService.getProfile(userId);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.USER_UPDATE, res, ""));
    }

    @PutMapping("/profile")
    public ResponseEntity<Response<Long>> updateProfile(
            @AuthenticationPrincipal Long userId, @Valid @RequestBody UpdateProfileReq req) {
        Long id = memberService.updateProfile(userId, req);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.USER_UPDATE, id, "프로필 갱신을 성공하였습니다."));
    }
}
