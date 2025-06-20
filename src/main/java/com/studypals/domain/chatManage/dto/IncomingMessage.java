package com.studypals.domain.chatManage.dto;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * client -> server 로의 메시지 형식을 정의합니다.
 * 사용자로부터 최소한의 정보를 입력받아 메시지를 보냅니다.
 *
 * @author jack8
 * @since 2025-06-19
 */
@Getter
@AllArgsConstructor
@NoArgsConstructor
public class IncomingMessage {
    private ChatType type;
    private String message;
    private String room;
}
