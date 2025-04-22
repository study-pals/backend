package com.studypals.global.websocket.SessionSave;

import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

import lombok.RequiredArgsConstructor;

/**
 * 세션에 대한 정보를 jvm 메모리에 저장하는 repository 입니다.
 * <p>
 * {@code ConcurrentHashMap} 을 사용하여 sessionId 와 / userId, chatRoomId 등을 저장합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link SessionRepository} 의 구현 클래스입니다.
 *
 * <p><b>빈 관리:</b><br>
 * 사용 시 해당 클래스를 등록하여야 합니다.
 *
 * @author jack8
 * @see SessionRepository
 * @since 2025-04-22
 */
// @Repository
@RequiredArgsConstructor
public class InMemorySessionRepository implements SessionRepository {

    // 실제 값을 저장할 concurrentHashMap 입니다. thread-safe 합니다.
    private final ConcurrentMap<String, SessionInfo> sessionMap = new ConcurrentHashMap<>();

    @Override
    public void save(SessionInfo session) {
        sessionMap.put(session.sessionId(), session);
    }

    @Override
    public Optional<SessionInfo> findBySessionId(String sessionId) {
        return Optional.ofNullable(sessionMap.get(sessionId));
    }

    @Override
    public void updateRoom(String sessionId, String roomId) {
        sessionMap.computeIfPresent(
                sessionId, (sid, info) -> new SessionInfo(sid, info.userId(), info.roomId(), info.connectedAt()));
    }

    @Override
    public void clear(String sessionId) {
        sessionMap.remove(sessionId);
    }
}
