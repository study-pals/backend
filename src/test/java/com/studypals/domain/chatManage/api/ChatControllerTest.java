package com.studypals.domain.chatManage.api;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

import com.studypals.testModules.testSupport.WebsocketStompSupport;

/**
 *
 * @author jack8
 * @since 2025-11-20
 */
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
class ChatControllerTest extends WebsocketStompSupport {

    @Test
    void sendMessage_success() throws Exception {
        Long userId = 1L;
        verifyToken(userId, true);
        verifyRoom(room1, userId, true);

        connectSession();
        subscribe("/sub/chat/room/" + room1, String.class, 5);
    }
}
