package com.studypals.domain.fileManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.worker.ChatImageManager;
import com.studypals.domain.memberManage.worker.MemberProfileManager;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.entity.ImageType;
import com.studypals.global.file.service.ImageFileServiceImpl;

@ExtendWith(MockitoExtension.class)
class ImageFileServiceImplTest {

    @Mock
    private MemberProfileManager profileRepo;

    @Mock
    private ChatImageManager chatRepo;

    private ImageFileServiceImpl fileService;

    @BeforeEach
    void setUp() {
        // Mock 객체들의 동작 정의 (생성자에서 호출됨)
        given(profileRepo.getFileType()).willReturn(ImageType.PROFILE_IMAGE);
        given(chatRepo.getFileType()).willReturn(ImageType.CHAT_IMAGE);

        // List에 Mock 객체들을 담아서 직접 주입
        List<AbstractImageManager> repositories = List.of(profileRepo, chatRepo);
        fileService = new ImageFileServiceImpl(repositories);
    }

    @Test
    @DisplayName("업로드 URL 발급 성공 - 올바른 리포지토리로 위임")
    void getProfileUploadUrl_success() {
        // given
        String fileName = "profile.jpg";
        Long userId = 1L;
        ProfilePresignedUrlReq request = new ProfilePresignedUrlReq(fileName);
        String expectedUrl = "https://bucket.s3.region.amazonaws.com/profile/uuid.jpg";

        given(profileRepo.getUploadUrl(fileName, String.valueOf(userId))).willReturn(expectedUrl);

        // when
        String result = fileService.getProfileUploadUrl(request, userId);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(profileRepo).getUploadUrl(fileName, String.valueOf(userId));
    }

    @Test
    @DisplayName("채팅 업로드 URL 발급 성공")
    void getChatUploadUrl_success() {
        // given
        ChatPresignedUrlReq request = new ChatPresignedUrlReq("chat.jpg", "chat-room-1");
        String expectedUrl = "https://bucket.s3.region.amazonaws.com/chat/uuid.jpg";

        given(chatRepo.getUploadUrl("chat.jpg", "chat-room-1")).willReturn(expectedUrl);

        // when & then
        String result = fileService.getChatUploadUrl(request);
        assertThat(result).isEqualTo(expectedUrl);
    }
}
