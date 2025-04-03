package com.studypals.domain.membersManage.api;

import com.studypals.domain.membersManage.dto.CreateMemberReq;
import com.studypals.domain.membersManage.dto.SignInReq;
import com.studypals.domain.membersManage.service.MemberService;
import com.studypals.domain.membersManage.service.SignInService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.global.security.jwt.JwtToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

/**
 * 회원가입/로그인 및 기타 권한 , 인증 등에 대한 컨트롤러입니다.
 * 담당하는 엔드포인트는 다음과 같습니다.
 * <pre>
 *     - POST /register : 회원가입({@link CreateMemberReq})
 *     - POST /sign-in : 로그인({@link SignInReq})
 * </pre>
 * @author jack8
 * @since 2025-04-02
 */

@RestController
@RequiredArgsConstructor
public class AuthController {
    private final MemberService memberService;
    private final SignInService signInService;

    @PostMapping("/register")
    public ResponseEntity<Response<Long>> register(@Valid @RequestBody CreateMemberReq dto) {
        Long id = memberService.createMember(dto);
        Response<Long> response = CommonResponse.success(ResponseCode.USER_CREATE, id, "success create user");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Response<JwtToken>> signIn(@Valid @RequestBody SignInReq dto) {
        String username = dto.username();
        String password = dto.password();

        JwtToken jwtToken = signInService.signInByUsernameAndPassword(username, password);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.USER_LOGIN, jwtToken, "success login"));
    }
}
