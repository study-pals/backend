package com.studypals.domain.chatManage.api;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import com.studypals.domain.chatManage.dto.*;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;
import com.studypals.domain.chatManage.entity.ChatMessage;
import com.studypals.domain.chatManage.worker.ChatMessageReader;
import com.studypals.testModules.testSupport.WebsocketStompSupport;

import reactor.core.publisher.Flux;

/**
 *
 * @author jack8
 * @since 2025-11-20
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerTest extends WebsocketStompSupport {

    @MockitoBean
    private ChatMessageMapper chatMessageMapper;

    @MockitoBean
    private ChatMessageReader chatMessageReader;

    @Test
    void sendMessage_success() throws Exception {
        // given
        Long userId = 1L;
        int expected = 1;
        IncomingMessage message = new IncomingMessage(ChatType.TEXT, "payload message", room1);
        OutgoingMessage outMessage = new OutgoingMessage(null, ChatType.TEXT, "payload message", userId);
        verifyToken(userId, true);
        verifyRoom(room1, userId, true);
        verifySend();
        given(chatMessageMapper.toOutMessage(any(), any())).willReturn(outMessage);

        // when
        connectSession();
        SubscribeRes<OutgoingMessage> res = subscribe("/sub/chat/room/" + room1, OutgoingMessage.class, expected);
        send("/pub/send/message", message);

        res.await();

        // then
        OutgoingMessage received = res.getMessages().get(0);
        assertThat(received.getMessage()).isEqualTo("payload message");
        assertThat(received.getType()).isEqualTo(ChatType.TEXT);
        assertThat(received.getSenderId()).isEqualTo(userId);
    }

    @Test
    void readMessage_success() throws Exception {
        // given
        long usrId = 1L;
        int expected = 1;
        IncomingMessage message = new IncomingMessage(ChatType.READ, "1111", room1);

        verifyToken(usrId, true);
        verifyRoom(room1, usrId, true);

        connectSession();
        SubscribeRes<OutgoingMessage> res = subscribe("/sub/chat/room/" + room1, OutgoingMessage.class, expected);

        send("/pub/read/message", message);

        res.await();

        assertThat(res.getMessages().size()).isEqualTo(1);
        assertThat(res.getMessages().get(0).getMessage()).isEqualTo("{\"1\":\"1111\"}");
    }

    @Test
    void getLog_success() throws Exception {
        // given
        long userId = 1L;
        int expected = 4;
        int messageCnt = 172;

        SendChatLogReq req = new SendChatLogReq(room1, "1111");
        List<ChatMessage> chatMessages = new ArrayList<>();
        Flux<ChatMessage> mockFlux = Flux.fromIterable(chatMessages);
        for (int i = 0; i < messageCnt; i++) {
            chatMessages.add(new ChatMessage("1111" + i, ChatType.TEXT, room1, userId, "message" + i));
        }

        verifyToken(userId, true);
        verifyRoom(room1, userId, true);
        verifySend();
        given(chatMessageReader.getChatLog(room1, "1111")).willReturn(mockFlux);
        System.out.println();
        connectSession();
        SubscribeRes<ChatLogRes> res = subscribe("/user/queue", ChatLogRes.class, expected);
        send("/req/log", req);
        res.await();

        assertThat(res.getMessages()).hasSize(4);
        assertThat(res.getMessages().get(0).messages()).hasSize(50);
        assertThat(res.getMessages().get(3).messages()).hasSize(22);
        assertThat(res.getMessages().get(3).last()).isTrue();
    }
}
