package com.studypals.domain.chatManage.entity;

import jakarta.persistence.Id;

import org.springframework.data.mongodb.core.mapping.Document;

import lombok.*;

import com.studypals.domain.chatManage.dto.ChatType;

/**
 * 채팅 메시지를 저장하는 mongoDB 엔티티입니다. 채팅방, 송신 유저, 내용이 담깁니다.
 * <p>
 * ID 자체적으로 timestamp를 추출할 수 있으며, 생성 순서에 따른 정렬을 지원합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code builder} <br>
 * 빌더 패턴을 사용하여 생성합니다. <br>
 *
 * @author jack8
 * @since 2025-07-14
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(collection = "chat_message")
public class ChatMessage {
    @Id
    private String id;

    private ChatType type;
    private String room;
    private Long sender;
    private String message;

    @RequiredArgsConstructor
    @Getter
    public enum FieldName {
        ID("id"),
        TYPE("type"),
        ROOM("room"),
        SENDER("sender"),
        MESSAGE("message");

        private final String name;
    }
}
