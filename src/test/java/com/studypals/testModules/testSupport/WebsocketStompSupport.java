package com.studypals.testModules.testSupport;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;

import java.lang.reflect.Type;
import java.util.*;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mock;
import org.springframework.boot.test.web.server.LocalServerPort;
import org.springframework.messaging.converter.MappingJackson2MessageConverter;
import org.springframework.messaging.simp.stomp.*;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.web.socket.WebSocketHttpHeaders;
import org.springframework.web.socket.client.standard.StandardWebSocketClient;
import org.springframework.web.socket.messaging.WebSocketStompClient;

import lombok.Getter;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.studypals.domain.chatManage.dao.ChatRoomMemberRepository;
import com.studypals.global.security.jwt.JwtUtils;
import com.studypals.global.websocket.subscibeManage.UserSubscribeInfo;
import com.studypals.global.websocket.subscibeManage.UserSubscribeInfoRepository;

/**
 * stomp 테스트를 위한 support 클래스입니다.
 * 웹소켓 연결 및 stomp 프로토콜로의 추상화 과정을 담고 있습니다.
 * <p>
 * connect - subscribe - send - disconnect 에 대한 메서드가 정의되어 있으며
 * 일부 인증/인가 과정에 대한 mockitoBean 정의가 포함되어 있습니다.
 *
 *
 * @author jack8
 * @since 2025-06-29
 */
public abstract class WebsocketStompSupport {

    protected final ObjectMapper objectMapper = new ObjectMapper();

    protected final String WS_ENDPOINT = "/ws";
    protected final long TIMEOUT = 3;
    protected final String room1 = UUID.randomUUID().toString();
    protected final String ACCESS_TOKEN = "access-token";

    protected WebSocketStompClient client;
    protected StompSession session;

    @LocalServerPort
    protected int port;

    @MockitoBean
    protected JwtUtils jwtUtils;

    @MockitoBean
    protected ChatRoomMemberRepository chatRoomMemberRepository;

    @MockitoBean
    protected UserSubscribeInfoRepository userSubscribeInfoRepository;

    @Mock
    protected JwtUtils.JwtData mockJwtData;

    @Mock
    private UserSubscribeInfo userSubscribeInfo;

    protected static boolean ENABLE_TEST = false;

    @AfterEach
    void disconnectSession() {
        if (session != null && session.isConnected()) {
            session.disconnect();
        }
    }

    @BeforeEach
    void beforeEach() {
        Assumptions.assumeTrue(ENABLE_TEST, "SKIP");

        given(chatRoomMemberRepository.existsByChatRoomIdAndMemberId(any(), any()))
                .willReturn(true);
        given(userSubscribeInfoRepository.existById(any())).willReturn(true);
    }

    protected String getToken() {
        return ACCESS_TOKEN;
    }

    /**
     * 세션을 연결합니다. 이 경우 {@code getToken} 을 헤더 삼아 인증/인가 과정을
     * 진행하도록 합니다. client 에 연결 정보가 포함됩니다.
     */
    protected void connectSession() throws Exception {
        client = new WebSocketStompClient(new StandardWebSocketClient());
        client.setMessageConverter(new MappingJackson2MessageConverter());

        StompHeaders connectHeaders = new StompHeaders();
        WebSocketHttpHeaders httpHeaders = new WebSocketHttpHeaders();
        httpHeaders.add("Authorization", "Bearer " + getToken());
        connectHeaders.add("Authorization", "Bearer " + getToken());

        CompletableFuture<StompSession> sessionFuture = new CompletableFuture<>();

        client.connectAsync(
                String.format("ws://localhost:%d%s", port, WS_ENDPOINT),
                httpHeaders,
                connectHeaders,
                new DefaultStompSessionHandler() {
                    @Override
                    public void afterConnected(StompSession session, StompHeaders connectedHeaders) {
                        sessionFuture.complete(session);
                    }

                    @Override
                    public void handleTransportError(StompSession session, Throwable exception) {
                        sessionFuture.completeExceptionally(exception);
                    }
                });

        this.session = sessionFuture.get(TIMEOUT, TimeUnit.SECONDS);
    }

    /**
     * 구독을 위한 메서드입니다. 이미 열려있는 client에 대하여 SUBSCRIBE 를 보냅니다.
     * 반환값의 경우, 추후 해당 구독 정보로 도착하는 메시지가 포함됩니다.
     * 즉, 해당 구독 경로로 누가 메시지를 생산한다면, 이것이 반환값으로 오게 됩니다.
     * @param destination /sub/chat/room/{roomId}
     * @param type 반환 타입 정보
     * @return 해당 토픽에 오는 메시지 , 단 get으로 가져와야 함
     * @param <T> type에 입력되는 반환 타입 정보
     */
    protected <T> SubscribeRes<T> subscribe(String destination, Class<T> type, int cnt) {

        CountDownLatch latch = new CountDownLatch(cnt);
        List<T> messages = Collections.synchronizedList(new ArrayList<>());
        session.subscribe(destination, new StompFrameHandler() {
            @Override
            public Type getPayloadType(StompHeaders headers) {
                return type;
            }

            @Override
            public void handleFrame(StompHeaders headers, Object payload) {
                messages.add(type.cast(payload));
                latch.countDown();
            }
        });

        return new SubscribeRes<>(latch, messages);
    }

    protected static class SubscribeRes<T> {
        private final CountDownLatch latch;

        @Getter
        private final List<T> messages;

        public SubscribeRes(CountDownLatch latch, List<T> messages) {
            this.latch = latch;
            this.messages = messages;
        }

        public void await() throws InterruptedException {
            latch.await();
        }
    }

    /**
     * 메시지를 보냅니다.
     * @param destination /pub/send/message 등
     * @param payload 보통 IncommingMessage 로 들어가도록 합니다.
     */
    protected void send(String destination, Object payload) {
        session.send(destination, payload);
    }

    private static class DefaultStompSessionHandler extends StompSessionHandlerAdapter {
        @Override
        public void handleException(
                StompSession session, StompCommand command, StompHeaders headers, byte[] payload, Throwable exception) {
            exception.printStackTrace();
        }
    }

    protected void verifyToken(Long userId, boolean isValid) {
        given(jwtUtils.tokenInfo(ACCESS_TOKEN)).willReturn(mockJwtData);
        given(mockJwtData.isInvalid()).willReturn(!isValid);
        given(mockJwtData.getId()).willReturn(userId);
    }

    protected void verifyRoom(String roomId, Long userId, boolean isValid) {
        given(chatRoomMemberRepository.existsByChatRoomIdAndMemberId(roomId, userId))
                .willReturn(isValid);
    }

    protected void verifySend() {
        given(userSubscribeInfoRepository.findById(any())).willReturn(Optional.of(userSubscribeInfo));
        given(userSubscribeInfo.getRoomList()).willReturn(Map.of(room1, 17));
    }
}
