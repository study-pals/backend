package com.studypals.global.security.jwt;

import java.io.IOException;
import java.util.List;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.StringUtils;
import org.springframework.web.filter.OncePerRequestFilter;

import lombok.RequiredArgsConstructor;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.security.config.AccessURL;

/**
 * 헤더에 포함된 JWT를 검증하고, 새로운 토큰을 생성합니다.
 *
 * <p>현재 -> 응답을 생성하는 메서드가 정의되지 않았습니다.
 *
 * <p><b>상속 정보:</b><br>
 * OncePerRequestFilter
 *
 * @author jack8
 * @see JwtUtils
 * @see com.studypals.global.security.config.SecurityConfig SecurityConfig
 * @since 2025-04-02
 */
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtUtils jwtUtils;
    private final ObjectMapper objectMapper;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        if (isExcluded(AccessURL.PUBLIC.getUrls(), request.getRequestURI())) {
            filterChain.doFilter(request, response);
            return;
        }

        String token = resolveToken(request);

        if (token == null) {
            makeFailedResponse(response, JwtUtils.JwtStatus.INVALID);
            return;
        }

        JwtUtils.JwtData jwtData = jwtUtils.tokenInfo(token);
        if (jwtData.getJwtStatus() == JwtUtils.JwtStatus.VALID) {
            Long id = jwtData.getId();
            UsernamePasswordAuthenticationToken authentication =
                    new UsernamePasswordAuthenticationToken(id, null, List.of());
            SecurityContextHolder.getContext().setAuthentication(authentication);
        } else {
            makeFailedResponse(response, jwtData.getJwtStatus());
            return;
        }

        filterChain.doFilter(request, response);
    }

    /**
     * {@code List<String> urls}와 {@code String url}을 받아, 해당 url 이 urls에 포함되어 있는지 확인합니다. 혹은, 만약 urls
     * 중 "/"로 끝나는게 있다면, 해당 urls로 시작하는지 검사합니다.
     *
     * @param patterns url이 존재하는지 확인할 패턴
     * @param url 검사 대상
     * @return url이 urls에 포함되거나, prefix가 존재하면 true, 아니면 false
     */
    private boolean isExcluded(List<String> patterns, String url) {
        AntPathMatcher pathMatcher = new AntPathMatcher();
        for (String pattern : patterns) {
            if (pathMatcher.match(pattern, url)) {
                return true;
            }
        }
        return false;
    }

    /**
     * request 헤더에서 Authorization 의 값을 추출하여 검증 및 반환합니다. Bearer를 제외한, 나머지 토큰 부분을 반환합니다.
     *
     * @param request 요청
     * @return 토큰, 혹은 잘못되거나 없으면 null
     */
    private String resolveToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (StringUtils.hasText(bearerToken) && bearerToken.startsWith(JwtToken.BEARER_PREFIX)) {
            return bearerToken.substring(JwtToken.BEARER_PREFIX_LENGTH);
        }
        return null;
    }

    /**
     * 실패 시 응답을 생성합니다. {@code CommonResponse} 로 감싸서 처리합니다.
     *
     * @param response 응답
     * @throws IOException getWriter() 메서드가 던지는 예외가 해당 메서드 외부로 던지게 설정
     */
    private void makeFailedResponse(HttpServletResponse response, JwtUtils.JwtStatus status) throws IOException {
        response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        CommonResponse<?> errorResponse;

        if (status.equals(JwtUtils.JwtStatus.INVALID)) {
            errorResponse = CommonResponse.fail("U01-08", "Invalid JWT token");
        } else if (status.equals(JwtUtils.JwtStatus.EXPIRED)) {
            errorResponse = CommonResponse.fail("U01-08", "Expired JWT token");
        } else {
            errorResponse = CommonResponse.fail("U01-08", "Unauthorized access");
        }

        String json = objectMapper.writeValueAsString(errorResponse);
        response.getWriter().write(json);
    }
}
