package com.studypals.domain.chatManage.entity;

import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.domain.chatManage.worker.ChatRoomWriter;
import com.studypals.domain.memberManage.entity.Member;

/**
 * 채팅방 관련 캐싱 시 사용되는 value(prefix 에 들어가는 값)를 정의합니다.
 *
 * @author jack8
 * @see com.studypals.domain.chatManage.worker.ChatRoomReader ChatRoomReader
 * @since 2025-12-04
 */
public class ChatCacheValue {

    /**
     * @Cacheable {@link ChatRoomReader#findJoinedMemberId(String) }
     *
     * @CacheEvict
     * {@link ChatRoomWriter#leave(ChatRoom, Member) } <br>
     * {@link  ChatRoomWriter#join(ChatRoom, Member) } <br>
     * {@link  ChatRoomWriter#joinAsAdmin(ChatRoom, Member)} <br>
     */
    public static final String JOINED_MEMBER = "joinedMember";
}
