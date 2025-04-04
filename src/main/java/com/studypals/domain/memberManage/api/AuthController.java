package com.studypals.domain.memberManage.api;

import com.studypals.domain.memberManage.dto.*;
import com.studypals.domain.memberManage.service.MemberService;
import com.studypals.domain.memberManage.service.TokenService;
import com.studypals.domain.memberManage.service.SignInService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;
import com.studypals.global.security.jwt.JwtToken;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
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
    private final TokenService tokenService;

    @PostMapping("/register")
    public ResponseEntity<Response<Long>> register(@Valid @RequestBody CreateMemberReq req) {
        Long id = memberService.createMember(req);
        Response<Long> response = CommonResponse.success(ResponseCode.USER_CREATE, id, "success create user");

        return ResponseEntity.ok(response);
    }

    @PostMapping("/sign-in")
    public ResponseEntity<Response<JwtToken>> signIn(@Valid @RequestBody SignInReq req) {
        String username = req.username();
        String password = req.password();

        JwtToken jwtToken = signInService.signInByUsernameAndPassword(username, password);
        Long userId = memberService.getMemberIdByUsername(username);

        CreateRefreshTokenDto createDto = CreateRefreshTokenDto.builder()
                .token(jwtToken.getRefreshToken())
                .userId(userId)
                .build();
        tokenService.saveRefreshToken(createDto);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.USER_LOGIN, jwtToken, "success login"));
    }

    @PostMapping("/refresh")
    public ResponseEntity<Response<ReissueTokenRes>> refreshToken(@Valid @RequestBody TokenReissueReq req,
                                                           @RequestHeader("Authorization") String accessToken) {
        JwtToken token = JwtToken.builder()
                .accessToken(accessToken)
                .refreshToken(req.refreshToken())
                .build();

        ReissueTokenRes response = tokenService.reissueJwtToken(token);

        CreateRefreshTokenDto createDto = CreateRefreshTokenDto.builder()
                .token(response.refreshToken())
                .userId(response.userId())
                .build();
        tokenService.saveRefreshToken(createDto);

        return ResponseEntity.ok(CommonResponse.success(ResponseCode.USER_REISSUE_TOKEN, response,
                "success reissue token"));
    }
}
