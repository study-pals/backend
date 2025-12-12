package com.studypals.domain.chatManage.dto.mapper;

import java.util.Map;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.dto.ChatRoomListRes;
import com.studypals.domain.chatManage.dto.ChatroomLatestInfo;
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

    /**
     * 단일 ChatRoomMember 객체와 최신 메시지 조회 결과를 기반으로
     * ChatRoomInfo DTO를 생성한다.
     * info 가 없으면 언리드 개수는 -1, 메시지/보낸이는 null 로 설정한다.
     *
     * @param chatRoomMember 사용자가 참여한 채팅방 정보
     * @param latestInfos 채팅방별 최신 메시지 및 언리드 정보
     * @return ChatRoomListRes.ChatRoomInfo 변환 결과
     */
    default ChatRoomListRes.ChatRoomInfo toChatRoomInfo(
            ChatRoomMember chatRoomMember, Map<String, ChatroomLatestInfo> latestInfos) {
        String chatRoomId = chatRoomMember.getChatRoom().getId();
        ChatroomLatestInfo info = latestInfos.get(chatRoomId);

        return new ChatRoomListRes.ChatRoomInfo(
                chatRoomId,
                chatRoomMember.getChatRoom().getName(),
                chatRoomMember.getChatRoom().getImageUrl(),
                chatRoomMember.getChatRoom().getTotalMember(),
                info != null ? info.getCnt() : -1,
                info != null ? info.getContent() : null,
                info != null ? info.getId() : null,
                info != null ? info.getSender() : null);
    }
}
