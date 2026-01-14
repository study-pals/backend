package com.studypals.global.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.entity.ImageType;

@ExtendWith(MockitoExtension.class)
class ImageFileServiceImplTest {

    // Service under test
    private ImageFileService imageFileService;

    @Mock
    private AbstractImageManager mockProfileImageManager;

    @Mock
    private AbstractImageManager mockChatImageManager;

    @BeforeEach
    void setUp() {
        when(mockProfileImageManager.getFileType()).thenReturn(ImageType.PROFILE_IMAGE);
        when(mockChatImageManager.getFileType()).thenReturn(ImageType.CHAT_IMAGE);

        imageFileService = new ImageFileServiceImpl(List.of(mockProfileImageManager, mockChatImageManager));
    }

    @Test
    @DisplayName("getProfileUploadUrl 호출 시 ProfileImageManager의 getUploadUrl을 호출해야 한다")
    void getProfileUploadUrl_shouldCallCorrectManager() {
        // given
        Long userId = 1L;
        ProfilePresignedUrlReq request = new ProfilePresignedUrlReq("profile.jpg");
        String expectedUrl = "http://s3.com/profile-upload-url";

        when(mockProfileImageManager.getUploadUrl(userId, "profile.jpg", "1")).thenReturn(expectedUrl);

        // when
        String actualUrl = imageFileService.getProfileUploadUrl(request, userId);

        // then
        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(mockProfileImageManager).getUploadUrl(userId, "profile.jpg", "1");
        verify(mockChatImageManager, never()).getUploadUrl(any(), any(), any());
    }

    @Test
    @DisplayName("getChatUploadUrl 호출 시 ChatImageManager의 getUploadUrl을 호출해야 한다")
    void getChatUploadUrl_shouldCallCorrectManager() {
        // given
        Long userId = 1L;
        ChatPresignedUrlReq request = new ChatPresignedUrlReq("chat-image.png", "chat-room-123");
        String expectedUrl = "http://s3.com/chat-upload-url";

        when(mockChatImageManager.getUploadUrl(userId, "chat-image.png", "chat-room-123"))
                .thenReturn(expectedUrl);

        // when
        String actualUrl = imageFileService.getChatUploadUrl(request, userId);

        // then
        assertThat(actualUrl).isEqualTo(expectedUrl);
        verify(mockChatImageManager).getUploadUrl(userId, "chat-image.png", "chat-room-123");
        verify(mockProfileImageManager, never()).getUploadUrl(any(), any(), any());
    }
}
