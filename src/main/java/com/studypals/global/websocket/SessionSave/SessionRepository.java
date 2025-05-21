package com.studypals.global.websocket.SessionSave;

import java.util.Optional;

/**
 * 세션을 저장하기 위한 로직읜 인터페이스입니다.
 * <p>
 * 저장 방식에 따라 이를 구분하기 위해 인터페이스로 추상화를 하였습니다. 저장/검색/갱신/삭제에 대한 메서드가 정의되어 있습니다.
 *
 * @author jack8
 * @since 2025-04-22
 */
public interface SessionRepository {

    /**
     * sessionId에 따른 userId,chatRoomId, connectedAt 을 저장합니다.
     * chatRoomId 는 null 일 수 있습니다.
     * @param info 세션에 대한 정보입니다. 세션 아이디 및 그에 따른 정보가 포함되어 있습니다.
     */
    void save(SessionInfo info);

    /**
     * sessionId를 기반으로 정보를 조회합니다.
     * @param sessionId 조회할 session id
     * @return optional / session info
     */
    Optional<SessionInfo> findBySessionId(String sessionId);

    /**
     * sessionId에 따른 chatRoomId를 갱신합니다. 자동으로 connectedAt 도 갱신이 됩니다.
     * @param sessionId 갱신할 session 의 id
     * @param roomId 새롭게 접근한 채팅방 id
     */
    void updateRoom(String sessionId, String roomId);

    /**
     * session에 대한 기록을 말소합니다. 유저가 websocket 통신을 끊은 경우 발생합니다.
     * @param sessionId 삭제할 세션 아이디
     */
    void clear(String sessionId);
}
