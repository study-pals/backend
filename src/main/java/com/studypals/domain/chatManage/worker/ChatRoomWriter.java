package com.studypals.domain.chatManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatRoomRepository;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.global.annotations.Worker;
import com.studypals.global.utils.RandomUtils;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-04-21
 */
@Worker
@RequiredArgsConstructor
public class ChatRoomWriter {
    private final ChatRoomRepository chatRoomRepository;

    public String create(String name) {
        String uuid = RandomUtils.generateUUID();
        ChatRoom chatRoom = ChatRoom.builder().id(uuid).name(name).build();

        try {
            chatRoomRepository.save(chatRoom);
            return uuid;
        } catch (Exception e) {
            throw new IllegalArgumentException("must change to custom");
        }
    }
}
