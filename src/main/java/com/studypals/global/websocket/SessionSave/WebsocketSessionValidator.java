package com.studypals.global.websocket.SessionSave;

import java.util.Optional;

import lombok.RequiredArgsConstructor;

import com.studypals.global.annotations.Worker;

/**
 * 세션 정보와, 유저가 보낸 값을 검증하기 위한 클래스입니다.
 * <p>
 * sessionRepository 로부터 값을 가져와 입력 받은 값과 일치 여부를 검증합니다.
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author jack8
 * @since 2025-04-22
 */
@Worker
@RequiredArgsConstructor
public class WebsocketSessionValidator {

    private final SessionRepository sessionRepository;

    /**
     * sessionId, uerId, roomId 를 기반으로, MessageMapping 바인딩 컨트롤러 메서드의
     * 입력 값을 검증합니다.
     * @param sessionId 세션 아이디
     * @param userId 유저 아이디
     * @param roomId 채팅방 아이디
     * @return 유효 여부
     */
    public boolean isValidSession(String sessionId, Long userId, String roomId) {
        Optional<SessionInfo> optionalInfo = sessionRepository.findBySessionId(sessionId);
        if (optionalInfo.isEmpty()) {
            return false;
        }
        SessionInfo info = optionalInfo.get();
        return userId.equals(info.userId()) && roomId.equals(info.roomId());
    }
}
