package com.studypals.domain.chatManage.dto.mapper;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.entity.ChatRoomMember;

/**
 * ChatRoom 에 대한 mapper 클래스입니다.
 *
 * @author jack8
 * @since 2025-05-22
 */
@Mapper(componentModel = "spring")
public interface ChatRoomMapper {
    /**
     * 트랜잭션 내에서만 처리되어야 합니다.
     */
    @Mapping(target = "userId", source = "member.id")
    @Mapping(target = "imageUrl", source = "member.imageUrl")
    ChatRoomInfoRes.UserInfo toDto(ChatRoomMember entity);
}
