package com.studypals.domain.chatManage.worker;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.studypals.domain.chatManage.dao.ChatImageRepository;
import com.studypals.domain.chatManage.entity.ChatImage;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.global.file.entity.ImageStatus;

@ExtendWith(MockitoExtension.class)
class ChatImageWriterTest {

    @InjectMocks
    private ChatImageWriter chatImageWriter;

    @Mock
    private ChatImageRepository chatImageRepository;

    @Test
    @DisplayName("채팅 이미지 메타데이터 저장에 성공한다")
    void save_Success() {
        // given
        ChatRoom chatRoom = mock(ChatRoom.class);
        String objectKey = "chat/room1/uuid.jpg";
        String fileName = "chat-image.jpg";
        Long expectedImageId = 100L;

        ChatImage savedImage = ChatImage.builder()
                .id(expectedImageId)
                .chatRoom(chatRoom)
                .objectKey(objectKey)
                .originalFileName(fileName)
                .mimeType("jpg")
                .imageStatus(ImageStatus.PENDING)
                .build();

        given(chatImageRepository.save(any(ChatImage.class))).willReturn(savedImage);

        // when
        Long actualImageId = chatImageWriter.save(chatRoom, objectKey, fileName);

        // then
        assertThat(actualImageId).isEqualTo(expectedImageId);

        ArgumentCaptor<ChatImage> imageCaptor = ArgumentCaptor.forClass(ChatImage.class);
        verify(chatImageRepository).save(imageCaptor.capture());

        ChatImage capturedImage = imageCaptor.getValue();
        assertThat(capturedImage.getChatRoom()).isEqualTo(chatRoom);
        assertThat(capturedImage.getObjectKey()).isEqualTo(objectKey);
        assertThat(capturedImage.getOriginalFileName()).isEqualTo(fileName);
        assertThat(capturedImage.getMimeType()).isEqualTo("jpg");
        assertThat(capturedImage.getImageStatus()).isEqualTo(ImageStatus.PENDING);
    }
}
