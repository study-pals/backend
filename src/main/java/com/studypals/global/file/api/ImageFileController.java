package com.studypals.global.file.api;

import jakarta.validation.Valid;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.PresignedUrlRes;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.service.ImageFileService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 파일 관련 로직을 처리하는 컨트롤러입니다.
 * 파일 업로드는 서버 측에서 presigned url을 발급하고 클라이언트 측에서 진행합니다.
 *
 * <pre>
 *     - POST /files/image/profile : 프로필 사진 업로드를 위한 URL 발급
 *     - POST /files/image/chat : 채팅 사진 업로드를 위한 URL 발급
 * </pre>
 *
 * @author sleepyhoon
 * @since 2026-01-10
 */
@RestController
@RequestMapping("/files/image")
@RequiredArgsConstructor
public class ImageFileController {
    private final ImageFileService imageFileService;

    @PostMapping("/profile")
    public ResponseEntity<Response<PresignedUrlRes>> getUploadUrl(
            @Valid @RequestBody ProfilePresignedUrlReq request, @AuthenticationPrincipal Long userId) {
        PresignedUrlRes response = imageFileService.getProfileUploadUrl(request, userId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.FILE_IMAGE_UPLOAD, response));
    }

    @PostMapping("/chat")
    public ResponseEntity<Response<PresignedUrlRes>> getUploadUrl(
            @Valid @RequestBody ChatPresignedUrlReq request, @AuthenticationPrincipal Long userId) {
        PresignedUrlRes response = imageFileService.getChatUploadUrl(request, userId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.FILE_IMAGE_UPLOAD, response));
    }
}
