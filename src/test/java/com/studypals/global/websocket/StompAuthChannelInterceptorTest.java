package com.studypals.global.websocket;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.*;

import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

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
        subscribe("/sub/chat/room/" + room1, String.class, 0);
        Thread.sleep(500);
        // then
        verify(userSubscribeInfoRepository, never()).saveMapById(any());

        ArgumentCaptor<UserSubscribeInfo> captor = ArgumentCaptor.forClass(UserSubscribeInfo.class);

        verify(userSubscribeInfoRepository).save(captor.capture());

        UserSubscribeInfo saved = captor.getValue();
        assertThat(saved.getUserId()).isEqualTo(userId);
        assertThat(saved.getRoomList()).containsEntry(room1, 17);
    }
}
