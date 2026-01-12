package com.studypals.domain.fileManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.global.file.dao.AbstractFileManager;
import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.entity.FileType;
import com.studypals.global.file.service.FileServiceImpl;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Test
    @DisplayName("업로드 URL 발급 성공 - 올바른 리포지토리로 위임")
    void getProfileUploadUrl_success() {
        // given
        // Mock Repositories 생성
        AbstractFileManager profileRepo = mock(AbstractFileManager.class);
        AbstractFileManager chatRepo = mock(AbstractFileManager.class);

        // 각 Mock이 담당할 FileType 설정 (생성자에서 Map 초기화 시 사용됨)
        given(profileRepo.getFileType()).willReturn(FileType.PROFILE);
        given(chatRepo.getFileType()).willReturn(FileType.CHAT_IMAGE);

        // Service 생성 (List 주입 시뮬레이션)
        FileServiceImpl fileService = new FileServiceImpl(List.of(profileRepo, chatRepo));

        // 테스트 데이터 준비
        String fileName = "profile.jpg";
        ProfilePresignedUrlReq request = new ProfilePresignedUrlReq(fileName);
        String expectedUrl = "https://bucket.s3.region.amazonaws.com/profile/uuid.jpg";

        // profileRepo가 호출될 때 예상되는 동작 설정
        given(profileRepo.getUploadUrl(fileName)).willReturn(expectedUrl);

        // when
        String result = fileService.getProfileUploadUrl(request);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(profileRepo).getUploadUrl(fileName); // profileRepo가 호출되었는지 검증
    }

    @Test
    @DisplayName("채팅 업로드 URL 발급 성공")
    void getChatUploadUrl_success() {
        // given
        AbstractFileManager chatRepo = mock(AbstractFileManager.class);
        given(chatRepo.getFileType()).willReturn(FileType.CHAT_IMAGE);

        FileServiceImpl fileService = new FileServiceImpl(List.of(chatRepo));

        ChatPresignedUrlReq request = new ChatPresignedUrlReq("chat.jpg", "chat-room-1");
        String expectedUrl = "https://bucket.s3.region.amazonaws.com/chat/uuid.jpg";

        given(chatRepo.getUploadUrl("chat.jpg", "chat-room-1")).willReturn(expectedUrl);

        // when & then
        String result = fileService.getChatUploadUrl(request);
        assertThat(result).isEqualTo(expectedUrl);
    }
}
