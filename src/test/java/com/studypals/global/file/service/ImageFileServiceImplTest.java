package com.studypals.global.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.entity.ImageType;

@ExtendWith(MockitoExtension.class)
class ImageFileServiceImplTest {

    @Test
    @DisplayName("채팅 이미지 업로드 URL 발급 성공")
    void getChatUploadUrl_success() {
        // given
        AbstractImageManager chatImageManager = mock(AbstractImageManager.class);
        given(chatImageManager.getImageType()).willReturn(ImageType.CHAT);

        ImageFileServiceImpl imageFileService = new ImageFileServiceImpl(List.of(chatImageManager));

        Long userId = 1L;
        String fileName = "test.jpg";
        String targetId = "chat-room-id";
        ChatPresignedUrlReq req = new ChatPresignedUrlReq(fileName, targetId);
        String expectedUrl = "https://example.com/presigned-url";

        // userId가 포함된 메서드 호출 검증 설정
        given(chatImageManager.getUploadUrl(userId, fileName, targetId)).willReturn(expectedUrl);

        // when
        String result = imageFileService.getChatUploadUrl(req, userId);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(chatImageManager).getUploadUrl(userId, fileName, targetId);
    }

    @Test
    @DisplayName("프로필 이미지 업로드 URL 발급 성공")
    void getProfileUploadUrl_success() {
        // given
        AbstractImageManager profileImageManager = mock(AbstractImageManager.class);
        given(profileImageManager.getImageType()).willReturn(ImageType.PROFILE);

        ImageFileServiceImpl imageFileService = new ImageFileServiceImpl(List.of(profileImageManager));

        Long userId = 1L;
        String fileName = "profile.jpg";
        ProfilePresignedUrlReq req = new ProfilePresignedUrlReq(fileName);
        String expectedUrl = "https://example.com/presigned-url";

        given(profileImageManager.getUploadUrl(userId, fileName, String.valueOf(userId)))
                .willReturn(expectedUrl);

        // when
        String result = imageFileService.getProfileUploadUrl(req, userId);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(profileImageManager).getUploadUrl(userId, fileName, String.valueOf(userId));
    }
}
