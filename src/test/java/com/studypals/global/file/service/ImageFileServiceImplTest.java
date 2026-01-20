package com.studypals.global.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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

import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.worker.ChatImageManager;
import com.studypals.domain.chatManage.worker.ChatImageWriter;
import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberProfileImageManager;
import com.studypals.domain.memberManage.worker.MemberProfileImageWriter;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.PresignedUrlRes;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.entity.ImageType;

@ExtendWith(MockitoExtension.class)
class ImageFileServiceImplTest {

    // Service under test
    private ImageFileService imageFileService;

    @Mock
    private MemberProfileImageManager mockProfileImageManager;

    @Mock
    private ChatImageManager mockChatImageManager;

    @Mock
    private MemberReader memberReader;

    @Mock
    private ChatRoomReader chatRoomReader;

    @Mock
    private MemberProfileImageWriter memberProfileImageWriter;

    @Mock
    private ChatImageWriter chatImageWriter;

    @BeforeEach
    void setUp() {
        when(mockProfileImageManager.getFileType()).thenReturn(ImageType.PROFILE_IMAGE);
        when(mockChatImageManager.getFileType()).thenReturn(ImageType.CHAT_IMAGE);

        imageFileService = new ImageFileServiceImpl(
                List.of(mockProfileImageManager, mockChatImageManager),
                memberReader,
                chatRoomReader,
                memberProfileImageWriter,
                chatImageWriter);
    }

    @Test
    @DisplayName("getProfileUploadUrl 호출 시 ProfileImageManager의 getUploadUrl을 호출해야 한다")
    void getProfileUploadUrl_shouldCallCorrectManager() {
        // given
        Long userId = 1L;
        ProfilePresignedUrlReq request = new ProfilePresignedUrlReq("profile.jpg");
        String expectedObjectKey = "profile/1/some-uuid.jpg";
        Long expectedImageId = 99L;
        String expectedUrl = "http://s3.com/profile-upload-url";

        // 서비스가 호출할 Mock 객체의 동작을 모두 정의합니다.
        when(mockProfileImageManager.createObjectKey(userId, request.fileName(), String.valueOf(userId)))
                .thenReturn(expectedObjectKey);
        when(memberReader.getRef(userId)).thenReturn(Member.builder().id(userId).build());
        when(memberProfileImageWriter.save(any(Member.class), eq(expectedObjectKey), eq(request.fileName())))
                .thenReturn(expectedImageId);
        when(mockProfileImageManager.getPresignedGetUrl(expectedObjectKey)).thenReturn(expectedUrl);

        // when
        PresignedUrlRes actualResult = imageFileService.getProfileUploadUrl(request, userId);

        // then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.id()).isEqualTo(expectedImageId);
        assertThat(actualResult.url()).isEqualTo(expectedUrl);

        // 올바른 메서드가 올바른 인자와 함께 호출되었는지 검증합니다.
        verify(mockProfileImageManager).createObjectKey(userId, request.fileName(), String.valueOf(userId));
        verify(memberProfileImageWriter).save(any(Member.class), eq(expectedObjectKey), eq(request.fileName()));
        verify(mockProfileImageManager).getPresignedGetUrl(expectedObjectKey);
        verify(mockChatImageManager, never()).getPresignedGetUrl(any());
    }

    @Test
    @DisplayName("getChatUploadUrl 호출 시 ChatImageManager의 getUploadUrl을 호출해야 한다")
    void getChatUploadUrl_shouldCallCorrectManager() {
        // given
        Long userId = 1L;
        ChatPresignedUrlReq request = new ChatPresignedUrlReq("chat-image.png", "chat-room-123");
        String expectedObjectKey = "chat/chat-room-123/some-uuid.png";
        Long expectedImageId = 100L;
        String expectedUrl = "http://s3.com/chat-upload-url";

        // 서비스가 호출할 Mock 객체의 동작을 모두 정의합니다.
        when(mockChatImageManager.createObjectKey(userId, request.fileName(), request.chatRoomId()))
                .thenReturn(expectedObjectKey);
        when(chatRoomReader.getById(request.chatRoomId()))
                .thenReturn(ChatRoom.builder().id(request.chatRoomId()).build());
        when(chatImageWriter.save(any(ChatRoom.class), eq(expectedObjectKey), eq(request.fileName())))
                .thenReturn(expectedImageId);
        when(mockChatImageManager.getPresignedGetUrl(expectedObjectKey)).thenReturn(expectedUrl);

        // when
        PresignedUrlRes actualResult = imageFileService.getChatUploadUrl(request, userId);

        // then
        assertThat(actualResult).isNotNull();
        assertThat(actualResult.id()).isEqualTo(expectedImageId);
        assertThat(actualResult.url()).isEqualTo(expectedUrl);

        // 올바른 메서드가 올바른 인자와 함께 호출되었는지 검증합니다.
        verify(mockChatImageManager).createObjectKey(userId, request.fileName(), request.chatRoomId());
        verify(chatImageWriter).save(any(ChatRoom.class), eq(expectedObjectKey), eq(request.fileName()));
        verify(mockChatImageManager).getPresignedGetUrl(expectedObjectKey);
        verify(mockProfileImageManager, never()).getPresignedGetUrl(any());
    }
}
