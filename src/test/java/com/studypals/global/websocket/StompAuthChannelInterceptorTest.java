package com.studypals.global.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import java.util.Map;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.studypals.domain.chatManage.dto.ChatType;
import com.studypals.domain.chatManage.dto.IncomingMessage;
import com.studypals.domain.chatManage.dto.OutgoingMessage;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;
import com.studypals.global.websocket.subscibeManage.UserSubscribeInfo;
import com.studypals.testModules.testSupport.WebsocketStompSupport;

/**
 * websocket stomp protocol 에 대한 테스트 코드입니다.
 * connect, subscribe, send에 대한 기본적인 테스트 코드를 포함합니다.
 *
 * @author jack8
 * @see WebsocketStompSupport
 * @since 2025-06-30
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext
class StompAuthChannelInterceptorTest extends WebsocketStompSupport {

    @MockitoBean
    private ChatMessageMapper chatMessageMapper;

    @Mock
    private UserSubscribeInfo userSubscribeInfo;

    @Test
    void connectAndSubsribe_success() throws Exception {
        // given
        Long userId = 1L;

        verifyToken(userId, true);
        verifyRoom(room1, userId, true);
        given(userSubscribeInfoRepository.existById(any())).willReturn(false);

        // when
        connectSession();
        subscribe("/sub/chat/room/" + room1, String.class);
        Thread.sleep(500);
        // then
        verify(userSubscribeInfoRepository, never()).saveMapById(any());

        ArgumentCaptor<UserSubscribeInfo> captor = ArgumentCaptor.forClass(UserSubscribeInfo.class);

        verify(userSubscribeInfoRepository).save(captor.capture());

        UserSubscribeInfo saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getRoomList()).containsEntry(room1, 17);
    }

    @Test
    void sendMessage_success() throws Exception {
        // given
        Long userId = 1L;
        IncomingMessage message = new IncomingMessage(ChatType.TEXT, "payload message", room1);
        OutgoingMessage outMessage = new OutgoingMessage(null, ChatType.TEXT, "payload message", userId, "time");
        verifyToken(userId, true);
        verifyRoom(room1, userId, true);
        given(userSubscribeInfoRepository.existById(any())).willReturn(true);
        given(userSubscribeInfoRepository.findById(any())).willReturn(Optional.of(userSubscribeInfo));
        given(userSubscribeInfo.getRoomList()).willReturn(Map.of(room1, 17));
        given(chatMessageMapper.toOutMessage(any(), any(), any())).willReturn(outMessage);

        // when
        connectSession();
        CompletableFuture<OutgoingMessage> messageFuture = subscribe("/sub/chat/room/" + room1, OutgoingMessage.class);
        send("/pub/send/message", message);

        // then
        OutgoingMessage received = messageFuture.get(3, TimeUnit.SECONDS);
        assertThat(received.getMessage()).isEqualTo("payload message");
        assertThat(received.getType()).isEqualTo(ChatType.TEXT);
        assertThat(received.getSenderId()).isEqualTo(userId);
    }
}
