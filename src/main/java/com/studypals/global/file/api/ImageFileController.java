package com.studypals.global.file.api;

import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestPart;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.studypals.global.file.dto.ImageUploadRes;
import com.studypals.global.file.service.ImageFileService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 파일 관련 로직을 처리하는 컨트롤러입니다.
 * 클라이언트로부터 파일을 직접 받아 서버에서 스토리지로 업로드를 진행합니다.
 *
 * <pre>
 *     - POST /files/image/profile : 프로필 사진 업로드를 위한 URL 발급
 *     - POST /files/image/chat : 채팅 사진 업로드
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

    @PostMapping(value = "/profile", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<ImageUploadRes>> uploadProfileImage(
            @RequestPart("file") MultipartFile file, @AuthenticationPrincipal Long userId) {
        ImageUploadRes response = imageFileService.uploadProfileImage(file, userId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.FILE_IMAGE_UPLOAD, response));
    }

    @PostMapping(value = "/chat", consumes = MediaType.MULTIPART_FORM_DATA_VALUE)
    public ResponseEntity<Response<ImageUploadRes>> uploadChatImage(
            @RequestPart("file") MultipartFile file,
            @RequestParam("chatRoomId") String chatRoomId,
            @AuthenticationPrincipal Long userId) {
        ImageUploadRes response = imageFileService.uploadChatImage(file, chatRoomId, userId);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.FILE_IMAGE_UPLOAD, response));
    }
}
