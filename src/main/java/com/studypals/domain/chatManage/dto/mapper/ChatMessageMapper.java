package com.studypals.domain.chatManage.dto.mapper;

import org.mapstruct.Mapper;

import com.studypals.domain.chatManage.dto.IncomingMessage;
import com.studypals.domain.chatManage.dto.OutgoingMessage;
import com.studypals.domain.chatManage.entity.ChatMessage;

/**
 * ChatMessage 에 대한 mapper 클래스입니다.
 *
 * @author jack8
 * @since 2025-06-20
 */
@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    OutgoingMessage toOutMessage(IncomingMessage message, Long senderId, String time);

    ChatMessage toEntity(IncomingMessage message, String id, Long sender);
}
