package com.studypals.domain.chatManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import java.util.List;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;
import com.studypals.global.file.ObjectStorage;
import com.studypals.global.file.entity.ImageType;

@ExtendWith(MockitoExtension.class)
class ChatImageManagerTest {

    @Mock
    private ObjectStorage objectStorage;

    @Mock
    private ChatRoomReader chatRoomReader;

    private ChatImageManager chatImageManager;

    @BeforeEach
    void setUp() {
        chatImageManager = new ChatImageManager(objectStorage, chatRoomReader);
        // 부모 클래스의 @Value 필드 주입
        ReflectionTestUtils.setField(chatImageManager, "acceptableExtensions", List.of("jpg", "png"));
        ReflectionTestUtils.setField(chatImageManager, "presignedUrlExpireTime", 600);
    }

    @Test
    @DisplayName("업로드 URL 발급 성공 - 채팅방 멤버인 경우")
    void getUploadUrl_success() {
        // given
        Long userId = 1L;
        String chatRoomId = "room1";
        String fileName = "image.jpg";
        String expectedUrl = "https://example.com/presigned-url";

        given(chatRoomReader.isMemberOfChatRoom(userId, chatRoomId)).willReturn(true);
        given(objectStorage.createPresignedPutUrl(anyString(), anyInt())).willReturn(expectedUrl);

        // when
        String result = chatImageManager.getUploadUrl(userId, fileName, chatRoomId);

        // then
        assertThat(result).isEqualTo(expectedUrl);
        verify(chatRoomReader).isMemberOfChatRoom(userId, chatRoomId);
    }

    @Test
    @DisplayName("업로드 URL 발급 실패 - 채팅방 멤버가 아닌 경우")
    void getUploadUrl_fail_notMember() {
        // given
        Long userId = 1L;
        String chatRoomId = "room1";
        String fileName = "image.jpg";

        given(chatRoomReader.isMemberOfChatRoom(userId, chatRoomId)).willReturn(false);

        // when & then
        assertThatThrownBy(() -> chatImageManager.getUploadUrl(userId, fileName, chatRoomId))
                .isInstanceOf(ChatException.class)
                .hasFieldOrPropertyWithValue("errorCode", ChatErrorCode.CHAT_ROOM_NOT_CONTAIN_MEMBER);
    }

    @Test
    @DisplayName("파일 타입 반환 확인")
    void getFileType() {
        // when
        ImageType type = chatImageManager.getFileType();

        // then
        assertThat(type).isEqualTo(ImageType.CHAT_IMAGE);
    }
}
