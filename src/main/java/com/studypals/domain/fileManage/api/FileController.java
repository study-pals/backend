package com.studypals.domain.fileManage.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.fileManage.dto.ChatPresignedUrlReq;
import com.studypals.domain.fileManage.dto.PresignedUrlRes;
import com.studypals.domain.fileManage.dto.ProfilePresignedUrlReq;
import com.studypals.domain.fileManage.service.FileService;
import com.studypals.global.responses.CommonResponse;
import com.studypals.global.responses.Response;
import com.studypals.global.responses.ResponseCode;

/**
 * 파일 관련 로직을 처리하는 컨트롤러입니다.
 * 파일 업로드는 서버 측에서 presigned url을 발급하고 클라이언트 측에서 진행합니다.
 *
 * <pre>
 *     - POST /files/profile/presigned-url : 프로필 사진 업로드를 위한 URL 발급
 *     - POST /files/chat/presigned-url : 채팅 사진 업로드를 위한 URL 발급
 * </pre>
 *
 * @author sleepyhoon
 * @since 2026-01-10
 */
@RestController
@RequestMapping("/files")
@RequiredArgsConstructor
public class FileController {
    private final FileService fileService;

    @PostMapping("/profile/presigned-url")
    public ResponseEntity<Response<PresignedUrlRes>> getProfilePresignedUrl(
            @RequestBody ProfilePresignedUrlReq request) {
        String response = fileService.getProfileUploadUrl(request);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.IMAGE_UPLOAD, new PresignedUrlRes(response)));
    }

    @PostMapping("/chat/presigned-url")
    public ResponseEntity<Response<PresignedUrlRes>> getChatPresignedUrl(@RequestBody ChatPresignedUrlReq request) {
        String response = fileService.getChatUploadUrl(request);
        return ResponseEntity.ok(CommonResponse.success(ResponseCode.IMAGE_UPLOAD, new PresignedUrlRes(response)));
    }
}
