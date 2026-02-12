package com.studypals.global.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ImageUploadRes;
import com.studypals.global.file.entity.ImageType;
import com.studypals.global.file.worker.ImageManagerFactory;

@ExtendWith(MockitoExtension.class)
class ImageFileServiceImplTest {

    @InjectMocks
    private ImageFileServiceImpl imageFileService;

    @Mock
    private ImageManagerFactory imageManagerFactory;

    @Mock
    private AbstractImageManager imageManager;

    @Mock
    private MultipartFile multipartFile;

    @Test
    @DisplayName("프로필 이미지 업로드 - 성공")
    void uploadProfileImage_Success() {
        // given
        Long userId = 1L;
        String targetId = String.valueOf(userId);
        ImageUploadRes expectedRes = new ImageUploadRes(1L, "http://url");

        given(imageManagerFactory.getManager(ImageType.PROFILE_IMAGE)).willReturn(imageManager);
        given(imageManager.upload(multipartFile, userId, targetId)).willReturn(expectedRes);

        // when
        ImageUploadRes res = imageFileService.uploadProfileImage(multipartFile, userId);

        // then
        assertThat(res).isEqualTo(expectedRes);
        verify(imageManagerFactory).getManager(ImageType.PROFILE_IMAGE);
        verify(imageManager).upload(multipartFile, userId, targetId);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 - 성공")
    void uploadChatImage_Success() {
        // given
        Long userId = 1L;
        String chatRoomId = "room1";
        ImageUploadRes expectedRes = new ImageUploadRes(2L, "http://chat-url");

        given(imageManagerFactory.getManager(ImageType.CHAT_IMAGE)).willReturn(imageManager);
        given(imageManager.upload(multipartFile, userId, chatRoomId)).willReturn(expectedRes);

        // when
        ImageUploadRes res = imageFileService.uploadChatImage(multipartFile, chatRoomId, userId);

        // then
        assertThat(res).isEqualTo(expectedRes);
        verify(imageManagerFactory).getManager(ImageType.CHAT_IMAGE);
        verify(imageManager).upload(multipartFile, userId, chatRoomId);
    }
}
