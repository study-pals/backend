package com.studypals.domain.chatManage.dto.mapper;

import java.util.Map;

import org.springframework.stereotype.Component;

import com.studypals.domain.chatManage.dto.ChatRoomInfoRes;
import com.studypals.domain.chatManage.dto.ChatRoomInfoRes.UserInfo;
import com.studypals.domain.chatManage.dto.ChatRoomListRes;
import com.studypals.domain.chatManage.dto.ChatroomLatestInfo;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
import com.studypals.global.file.ObjectStorage;

/**
 * ChatRoom 도메인 관련 mapper 입니다.
 *
 * @author sleepyhoon
 * @see
 * @since 2026-01-27
 */
@Component
public class ChatRoomMapper {

    public ChatRoomInfoRes.UserInfo toDto(ChatRoomMember entity, ObjectStorage objectStorage) {
        return UserInfo.builder()
                .userId(entity.getMember().getId())
                .nickname(entity.getMember().getNickname())
                .role(entity.getRole())
                // TODO: 채팅방 이미지도 minio로 이동해야함. 아직 구현되지 않음.
                .imageUrl(objectStorage.convertKeyToFileUrl(entity.getChatRoom().getImageUrl()))
                .build();
    }

    /**
     * 단일 ChatRoomMember 객체와 최신 메시지 조회 결과를 기반으로
     * ChatRoomInfo DTO를 생성한다.
     * info 가 없으면 언리드 개수는 -1, 메시지/보낸이는 null 로 설정한다.
     *
     * @param chatRoomMember 사용자가 참여한 채팅방 정보
     * @param latestInfos 채팅방별 최신 메시지 및 언리드 정보
     * @return ChatRoomListRes.ChatRoomInfo 변환 결과
     */
    public ChatRoomListRes.ChatRoomInfo toChatRoomInfo(
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
