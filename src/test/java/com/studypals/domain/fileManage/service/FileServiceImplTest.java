package com.studypals.domain.fileManage.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.fileManage.dao.AbstractFileRepository;
import com.studypals.domain.fileManage.dto.PresignedUrlReq;
import com.studypals.domain.fileManage.entity.FileType;

@ExtendWith(MockitoExtension.class)
class FileServiceImplTest {

    @Test
    @DisplayName("업로드 URL 발급 성공 - 올바른 리포지토리로 위임")
    void getUploadUrl_success() {
        // given
        // Mock Repositories 생성
        AbstractFileRepository profileRepo = mock(AbstractFileRepository.class);
        AbstractFileRepository chatRepo = mock(AbstractFileRepository.class);

        // 각 Mock이 담당할 FileType 설정 (생성자에서 Map 초기화 시 사용됨)
        given(profileRepo.getFileType()).willReturn(FileType.PROFILE);
        given(chatRepo.getFileType()).willReturn(FileType.CHAT_IMAGE);

        // Service 생성 (List 주입 시뮬레이션)
        FileServiceImpl fileService = new FileServiceImpl(List.of(profileRepo, chatRepo));

        // 테스트 데이터 준비
        String fileName = "profile.jpg";
        PresignedUrlReq request = new PresignedUrlReq(fileName, FileType.PROFILE);
        String expectedUrl = "https://bucket.s3.region.amazonaws.com/profile/uuid.jpg";

        // profileRepo가 호출될 때 예상되는 동작 설정
        given(profileRepo.getUploadUrl(fileName)).willReturn(expectedUrl);

        // when
        String result = fileService.getUploadUrl(request);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(profileRepo).getUploadUrl(fileName); // profileRepo가 호출되었는지 검증
    }

    @Test
    @DisplayName("업로드 URL 발급 실패 - 등록되지 않은 파일 타입")
    void getUploadUrl_fail_unsupported_type() {
        // given
        AbstractFileRepository profileRepo = mock(AbstractFileRepository.class);
        given(profileRepo.getFileType()).willReturn(FileType.PROFILE);

        // CHAT_IMAGE 타입의 리포지토리는 주입하지 않음 (지원하지 않는 상황 시뮬레이션)
        FileServiceImpl fileService = new FileServiceImpl(List.of(profileRepo));

        PresignedUrlReq request = new PresignedUrlReq("chat.jpg", FileType.CHAT_IMAGE);

        // when & then
        assertThatThrownBy(() -> fileService.getUploadUrl(request))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessage("지원하지 않는 파일 타입입니다.");
    }
}
