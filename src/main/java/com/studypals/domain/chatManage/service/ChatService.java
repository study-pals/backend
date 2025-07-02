package com.studypals.domain.chatManage.service;

import com.studypals.domain.chatManage.dto.IncomingMessage;

/**
 * websocket 기반의 메시지에 대하여 채팅을 저장/브로드 캐스트 등의 기능을 수행합니다.
 * <p>
 * 채팅의 전처리 후 브로드캐스트, 내역 저장, 읽음 커서 업데이트 등의 채팅과 관련된 작업을 수행합니다.
 *
 * <p><b>상속 정보:</b><br>
 * ChatServiceImpl 의 부모 인터페이스입니다.
 *
 * @author jack8
 * @see ChatServiceImpl
 * @since 2025-06-20
 */
public interface ChatService {

    /**
     * 채팅을 해당 방의 구독자에게 브로드캐스트합니다.
     * 송신자의 id 와 시간 등을 추가하여, 전체 사용자에게 전파합니다.
     * @param userId 송신자 id
     * @param message 메시지 본문(송신자가 보낸 메시지)
     */
    void sendMessage(Long userId, IncomingMessage message);

    /**
     * 메시지를 읽고, 읽음 메시지를 처리합니다. 기본적으로 ScheduledExecutorService 를 이용해 묶어서 처리합니다.
     * 비동기 - 버퍼링 방식을 통해 처리합니다. 다만, 배치 처리를 고민하고 있습니다.
     * @param userId 송신자의 id
     * @param message 읽음 메시지, message 에는 자신이 마지막으로 읽은 메시지의 id
     */
    void readMessage(Long userId, IncomingMessage message);

    /**
     * 사용자가 메시지를 보낼 때, 해당 채팅방에 메시지를 송신할 권한이 있는지 확인합니다. 다-클라이언트 상황을 고려하여
     * userId 기반이 아닌, sessionId 기반으로 겁증을 진행합니다. 해당 값은 interceptor 에서 설정되며 {@link com.studypals.global.websocket.subscibeManage.UserSubscribeInfoRepository}
     * 에서 관리됩니다.
     * @param sessionId 사용자가 접속한 sessionId
     * @param roomId 사용자가 메시지를 보내고자 할 roomId
     * @throws com.studypals.global.exceptions.exception.ChatException 검증 실패 시 예외 반환
     */
    void sendDestinationValidate(String sessionId, String roomId);
}
