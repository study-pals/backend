package com.studypals.domain.chatManage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

/**
 * 특정 채팅방에서 사용자가 읽지 않은 메시지 개수와,
 * 해당 채팅방의 가장 최신 메시지 정보를 담는 데이터 객체입니다.
 * <p>
 * Redis Streams 기반 캐시에서 조회된 결과를 전달하기 위한 DTO 역할을 하며,
 * unread count 계산, 최신 메시지 표시, 알림 뱃지 업데이트 등에 사용됩니다.
 * <br>
 * cnt 필드는 읽지 않은 메시지 수를 의미하며, id/type/message/sender 는 채팅방의 최신 메시지를 나타냅니다.
 *
 * <p><b>빈 관리:</b><br>
 * 단순 DTO 이므로 빈으로 관리되지 않으며 서비스·DAO 계층에서 직접 생성됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 특별한 외부 모듈 의존성은 없으며, ChatType(Enum) 만 참조합니다.
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.dto.ChatType ChatType
 * @since 2025-07-26
 */
@Getter
@AllArgsConstructor
public class ChatroomLatestInfo {

    /**
     * 사용자가 읽지 않은 메시지 개수.
     * <p>
     * Redis Streams 의 특성상 최대 MAX_LEN 만큼만 정확히 계산될 수 있으며,
     * 범위를 벗어난 경우 정책에 따라 잘린 값이 들어올 수 있습니다.
     */
    @Setter
    private long cnt;

    /**
     * 해당 채팅방의 가장 최신 메시지 ID.
     */
    private String id;

    /**
     * 최신 메시지의 타입(TEXT, IMAGE 등).
     */
    private ChatType type;

    /**
     * 최신 메시지의 본문 내용.
     */
    private String content;

    /**
     * 최신 메시지를 보낸 사용자의 ID.
     */
    private long sender;
}
