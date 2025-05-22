package com.studypals.domain.chatManage.dao;

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.testModules.testSupport.DataJpaSupport;

/**
 * {@link  ChatRoomMemberRepository} 에 대한 테스트코드입니다.
 *
 * @author jack8
 * @since 2025-05-16
 */
@DisplayName("ChatRoomMember_JPA_test")
class ChatRoomMemberRepositoryTest extends DataJpaSupport {

    @Autowired
    private TestEntityManager em;

    @Autowired
    private ChatRoomMemberRepository chatRoomMemberRepository;

    private Member insertMember(String name) {
        return em.persist(Member.builder()
                .username("user" + name)
                .password("password")
                .nickname("nick" + name)
                .build());
    }

    private ChatRoom insertChatRoom(String id) {
        return em.persist(ChatRoom.builder()
                .id(id)
                .name("chatroom" + id)
                .createdDate(LocalDate.of(2025, 4, 1))
                .build());
    }

    private ChatRoomMember make(ChatRoom chatRoom, Member member) {
        return ChatRoomMember.builder()
                .chatRoom(chatRoom)
                .member(member)
                .lastReadMessage("someMessageId")
                .build();
    }

    @Test
    void findAllByChatRoomId_success() {
        // given
        Member member1 = insertMember("member1");
        Member member2 = insertMember("member2");
        Member member3 = insertMember("member3");
        Member member4 = insertMember("member4");

        ChatRoom chatRoom = insertChatRoom("chatroom");

        em.persist(make(chatRoom, member1));
        em.persist(make(chatRoom, member2));
        em.persist(make(chatRoom, member3));
        em.persist(make(chatRoom, member4));

        em.flush();
        em.clear();

        // when
        List<ChatRoomMember> results = chatRoomMemberRepository.findAllByChatRoomId(chatRoom.getId());

        // then
        assertThat(results).hasSize(4);
    }

    @Test
    void findAllByChatRoomIdWithMember_success() {
        // given
        Member member1 = insertMember("member1");
        Member member2 = insertMember("member2");
        Member member3 = insertMember("member3");
        Member member4 = insertMember("member4");

        ChatRoom chatRoom = insertChatRoom("chatroom");

        em.persist(make(chatRoom, member1));
        em.persist(make(chatRoom, member2));
        em.persist(make(chatRoom, member3));
        em.persist(make(chatRoom, member4));

        em.flush();
        em.clear();

        // when
        List<ChatRoomMember> results = chatRoomMemberRepository.findAllByChatRoomIdWithMember(chatRoom.getId());

        // then
        assertThat(results).hasSize(4);
    }

    @Test
    void findAllByMemberId_success() {
        // given
        Member member = insertMember("member1");

        ChatRoom room1 = insertChatRoom("1");
        ChatRoom room2 = insertChatRoom("2");
        ChatRoom room3 = insertChatRoom("3");
        ChatRoom room4 = insertChatRoom("4");

        em.persist(make(room1, member));
        em.persist(make(room2, member));
        em.persist(make(room3, member));
        em.persist(make(room4, member));

        em.flush();
        em.clear();

        // when
        List<ChatRoomMember> results = chatRoomMemberRepository.findAllByMemberId(member.getId());

        // then
        assertThat(results).hasSize(4);
    }

    @Test
    void findChatRoomIdAndMemberId_success() {
        // given
        Member member = insertMember("member");
        ChatRoom chatRoom = insertChatRoom("chatroom");

        em.persist(make(chatRoom, member));
        em.flush();
        em.clear();

        // when
        Optional<ChatRoomMember> result =
                chatRoomMemberRepository.findByChatRoomIdAndMemberId(chatRoom.getId(), member.getId());

        // then
        assertThat(result).isPresent();
    }
}
