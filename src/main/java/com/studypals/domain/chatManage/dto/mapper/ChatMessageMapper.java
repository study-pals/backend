package com.studypals.domain.chatManage.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.studypals.domain.chatManage.dto.IncomingMessage;
import com.studypals.domain.chatManage.dto.OutgoingMessage;

/**
 * ChatMessage 에 대한 mapper 클래스입니다.
 *
 * @author jack8
 * @since 2025-06-20
 */
@Mapper(componentModel = "spring")
public interface ChatMessageMapper {

    @Mapping(target = "room", ignore = true)
    @Mapping(target = "senderId", source = "senderId")
    @Mapping(target = "time", source = "time")
    OutgoingMessage toOutMessage(IncomingMessage message, Long senderId, String time);
}
