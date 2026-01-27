package com.studypals.global.file.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.worker.ChatImageManager;
import com.studypals.domain.chatManage.worker.ChatImageWriter;
import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.entity.MemberProfileImage;
import com.studypals.domain.memberManage.worker.MemberProfileImageManager;
import com.studypals.domain.memberManage.worker.MemberProfileImageWriter;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ImageUploadDto;
import com.studypals.global.file.dto.ImageUploadRes;
import com.studypals.global.file.entity.ImageStatus;
import com.studypals.global.file.entity.ImageType;

@ExtendWith(MockitoExtension.class)
class ImageFileServiceImplTest {

    private ImageFileService imageFileService;

    @Mock
    private MemberProfileImageManager profileImageManager;

    @Mock
    private ChatImageManager chatImageManager;

    @Mock
    private MemberReader memberReader;

    @Mock
    private ChatRoomReader chatRoomReader;

    @Mock
    private MemberProfileImageWriter profileImageWriter;

    @Mock
    private ChatImageWriter chatImageWriter;

    @Mock
    private MultipartFile multipartFile;

    @BeforeEach
    void setUp() {
        TransactionSynchronizationManager.initSynchronization();
        given(profileImageManager.getFileType()).willReturn(ImageType.PROFILE_IMAGE);
        given(chatImageManager.getFileType()).willReturn(ImageType.CHAT_IMAGE);

        imageFileService = new ImageFileServiceImpl(
                List.of(profileImageManager, chatImageManager),
                memberReader,
                chatRoomReader,
                profileImageWriter,
                chatImageWriter);
    }

    @AfterEach
    void tearDown() {
        TransactionSynchronizationManager.clear();
    }

    @Test
    @DisplayName("프로필 이미지 업로드 - 성공 (기존 프로필 없음)")
    void uploadProfileImage_Success_NoExistingProfile() {
        // given
        Long userId = 1L;
        String originalFilename = "test.jpg";
        String objectKey = "profile/1/uuid.jpg";
        String imageUrl = "http://test.com/profile/1/uuid.jpg";
        Member member = Member.builder().id(1L).build();

        Long expectedImageId = 99L;
        MemberProfileImage expectedImage = MemberProfileImage.builder()
                .id(expectedImageId)
                .member(member)
                .objectKey(objectKey)
                .originalFileName(originalFilename)
                .mimeType("jpg")
                .imageStatus(ImageStatus.PENDING)
                .build();

        given(multipartFile.getOriginalFilename()).willReturn(originalFilename);

        ImageUploadDto uploadDto = new ImageUploadDto(objectKey, imageUrl);
        given(profileImageManager.upload(multipartFile, userId)).willReturn(uploadDto);

        given(memberReader.get(userId)).willReturn(member);

        given(profileImageWriter.save(eq(member), eq(objectKey), eq(originalFilename)))
                .willReturn(expectedImage);

        // when
        ImageUploadRes res = imageFileService.uploadProfileImage(multipartFile, userId);

        // then
        assertThat(res.imageId()).isEqualTo(expectedImageId);
        assertThat(res.imageUrl()).isEqualTo(imageUrl);

        verify(profileImageManager).upload(multipartFile, userId);
        verify(profileImageWriter).save(eq(member), eq(objectKey), eq(originalFilename));
        verify(profileImageManager, never()).delete(anyString());
    }

    @Test
    @DisplayName("프로필 이미지 업로드 - 성공 (기존 프로필 존재 -> 업데이트 및 기존 파일 삭제)")
    void uploadProfileImage_Success_ExistingProfile() {
        // given
        Long userId = 1L;
        String originalFilename = "new.jpg";
        String newObjectKey = "profile/1/new.jpg";
        String newImageUrl = "http://test.com/profile/1/new.jpg";
        String oldObjectKey = "profile/1/old.jpg";

        given(multipartFile.getOriginalFilename()).willReturn(originalFilename);

        ImageUploadDto uploadDto = new ImageUploadDto(newObjectKey, newImageUrl);
        given(profileImageManager.upload(multipartFile, userId)).willReturn(uploadDto);

        Member member = mock(Member.class);
        MemberProfileImage existingProfile = mock(MemberProfileImage.class);

        given(memberReader.get(userId)).willReturn(member);
        given(member.getProfileImage()).willReturn(existingProfile);
        given(existingProfile.getObjectKey()).willReturn(oldObjectKey);

        // when
        ImageUploadRes res = imageFileService.uploadProfileImage(multipartFile, userId);

        // 트랜잭션 커밋 후 동작(파일 삭제)을 검증하기 위해 수동으로 트리거
        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        // then
        assertThat(res.imageUrl()).isEqualTo(newImageUrl);

        verify(existingProfile).update(eq(newObjectKey), eq(originalFilename), anyString());
        verify(profileImageManager).delete(oldObjectKey);
    }

    @Test
    @DisplayName("채팅방 이미지 업로드 - 성공")
    void uploadChatImage_Success() {
        // given
        Long userId = 1L;
        String chatRoomId = "room1";
        String originalFilename = "chat.jpg";
        String objectKey = "chat/room1/uuid.jpg";
        String imageUrl = "http://test.com/chat/room1/uuid.jpg";

        given(multipartFile.getOriginalFilename()).willReturn(originalFilename);

        ImageUploadDto uploadDto = new ImageUploadDto(objectKey, imageUrl);
        given(chatImageManager.upload(multipartFile, userId, chatRoomId)).willReturn(uploadDto);

        ChatRoom chatRoom = mock(ChatRoom.class);
        given(chatRoomReader.getById(chatRoomId)).willReturn(chatRoom);

        Long savedImageId = 20L;
        given(chatImageWriter.save(eq(chatRoom), eq(objectKey), eq(originalFilename)))
                .willReturn(savedImageId);

        // when
        ImageUploadRes res = imageFileService.uploadChatImage(multipartFile, chatRoomId, userId);

        // then
        assertThat(res.imageId()).isEqualTo(savedImageId);
        assertThat(res.imageUrl()).isEqualTo(imageUrl);

        verify(chatImageManager).upload(multipartFile, userId, chatRoomId);
        verify(chatImageWriter).save(eq(chatRoom), eq(objectKey), eq(originalFilename));
    }

    @Test
    @DisplayName("생성자 - 중복 FileType 등록 시 예외 발생")
    void constructor_DuplicateFileType() {
        // given
        AbstractImageManager manager1 = mock(AbstractImageManager.class);
        AbstractImageManager manager2 = mock(AbstractImageManager.class);

        given(manager1.getFileType()).willReturn(ImageType.PROFILE_IMAGE);
        given(manager2.getFileType()).willReturn(ImageType.PROFILE_IMAGE); // 중복 타입

        List<AbstractImageManager> managers = List.of(manager1, manager2);

        // when & then
        assertThatThrownBy(() -> new ImageFileServiceImpl(
                        managers, memberReader, chatRoomReader, profileImageWriter, chatImageWriter))
                .isInstanceOf(IllegalStateException.class);
    }
}
