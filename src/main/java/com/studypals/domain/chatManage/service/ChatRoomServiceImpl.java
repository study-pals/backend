package com.studypals.domain.chatManage.service;

import java.util.*;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dto.*;
import com.studypals.domain.chatManage.dto.mapper.ChatMessageMapper;
import com.studypals.domain.chatManage.dto.mapper.ChatRoomMapper;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.entity.ChatRoomMember;
import com.studypals.domain.chatManage.worker.ChatMessageReader;
import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.global.exceptions.errorCode.ChatErrorCode;
import com.studypals.global.exceptions.exception.ChatException;

/**
 * 채팅방 진입 시 필요한 정보를 조회하는 서비스 구현 클래스입니다.
 * <p>
 * 채팅방 메타데이터, 참여자 정보, 각 유저의 마지막 읽은 메시지 커서, 최근 채팅 로그를 조합하여
 * 클라이언트에서 한 번에 사용할 수 있는 ChatRoomInfoRes 응답을 생성합니다.
 * 도메인 조회는 Reader 계층에 위임하고, 여기서는 권한 검증 및 응답 조립에 집중합니다.
 * <p>
 * 빈 관리:<br>
 * - ChatRoomReader : 채팅방 및 채팅방 멤버 정보를 조회하는 Reader 계층 컴포넌트<br>
 * - ChatRoomMapper : ChatRoom, ChatRoomMember 엔티티를 응답용 DTO로 변환하는 매퍼<br>
 * - ChatMessageMapper : ChatMessage 엔티티를 OutgoingMessage DTO로 변환하는 매퍼<br>
 * - ChatMessageReader : 캐시와 DB를 활용해 채팅 로그를 조회하는 Worker 컴포넌트<br>
 * <p>
 * 외부 모듈:<br>
 * - Spring @Service, @Transactional 을 사용하여 서비스 계층 트랜잭션 경계를 정의합니다.<br>
 *
 * @author jack8
 * @see ChatRoomService
 * @see com.studypals.domain.chatManage.worker.ChatRoomReader
 * @see com.studypals.domain.chatManage.worker.ChatMessageReader
 * @see com.studypals.domain.chatManage.dto.ChatRoomInfoRes
 * @since 2025-05-10
 */
@Service
@RequiredArgsConstructor
public class ChatRoomServiceImpl implements ChatRoomService {

    private final ChatRoomReader chatRoomReader;
    private final ChatRoomMapper chatRoomMapper;
    private final ChatMessageMapper chatMessageMapper;
    private final ChatMessageReader chatMessageReader;
    private final MemberReader memberReader;

    /**
     * 특정 유저가 특정 채팅방에 입장할 때 필요한 전체 정보를 조회합니다.
     * <p>
     * 동작 순서<br>
     * - 채팅방 및 참여자 목록을 조회한다.<br>
     * - 요청 유저가 해당 채팅방에 속해 있는지 검증한다.<br>
     * - DB 및 캐시에 저장된 마지막 읽은 메시지 ID 정보를 통합하여 커서 정보를 만든다.<br>
     * - 기준 chatId 이후의 채팅 로그를 조회하고 DTO로 변환한다.<br>
     * - 위 정보를 모두 조합해 ChatRoomInfoRes 응답을 생성한다.<br>
     * <p>
     * 제약 및 주의사항<br>
     * - userId 가 채팅방 멤버 목록에 포함되지 않은 경우 ChatException 이 발생합니다.<br>
     * - chatId 는 조회 기준이 되는 메시지 ID 로, 존재하지 않더라도 그 이후 구간 조회 시 기준값으로 사용됩니다.<br>
     *
     * @param userId     채팅방 정보를 조회하는 사용자 ID
     * @param chatRoomId 조회 대상 채팅방 ID
     * @param chatId     이 메시지 ID를 포함하여 이후 로그를 조회하기 위한 기준 메시지 ID
     * @return 채팅방 메타데이터, 참여자 정보, 커서 정보, 최근 채팅 로그가 포함된 응답 DTO
     * @throws ChatException 사용자가 해당 채팅방의 멤버가 아닐 경우 발생
     */
    @Override
    @Transactional(readOnly = true)
    public ChatRoomInfoRes getChatRoomInfo(Long userId, String chatRoomId, String chatId) {
        // 채팅방 메타데이터 조회 (없으면 내부에서 예외 처리할 가능성 있음)
        ChatRoom chatRoom = chatRoomReader.getById(chatRoomId);

        // Fetch Join 을 통한 1 + N 문제 방지
        List<ChatRoomMember> members = chatRoomReader.findChatRoomMembersWithMember(chatRoom);

        // 유저 권한 검증: 요청 유저가 해당 채팅방의 멤버인지 확인
        boolean exist = members.stream().anyMatch(m -> m.getMember().getId().equals(userId));
        if (!exist) {
            throw new ChatException(
                    ChatErrorCode.CHAT_ROOM_PERMISSION_DENIED, "[ChatRoomService#getChatRoomInfo] not included user");
        }

        // DB 에 저장된 각 멤버의 마지막 읽은 메시지 ID를 기반으로 기본 커서 맵 구성
        Map<Long, String> cursorData = new HashMap<>();
        for (ChatRoomMember chatRoomMember : members) {
            cursorData.put(chatRoomMember.getId(), chatRoomMember.getLastReadMessage());
        }

        // 캐시에 저장된 최신 커서 정보로 덮어쓰기 (실시간 갱신분 반영)
        cursorData.putAll(chatRoomReader.getCachedCursor(chatRoomId).getLastMessage());

        // Map<Long, String> 을 ChatCursorRes 리스트로 변환
        List<ChatCursorRes> chatCursorRes = cursorData.entrySet().stream()
                .map(t -> new ChatCursorRes(t.getKey(), t.getValue()))
                .toList();

        // 기준 chatId 이후의 채팅 로그를 조회하고, 전송용 OutgoingMessage DTO 리스트로 변환
        List<OutgoingMessage> logs = chatMessageReader.getChatLog(chatRoomId, chatId).stream()
                .map(chatMessageMapper::toOutMessage)
                .toList();

        // 채팅방 정보, 유저 정보, 커서, 채팅 로그를 모두 조합하여 최종 응답 생성
        return ChatRoomInfoRes.builder()
                .id(chatRoomId)
                .name(chatRoom.getName())
                .userInfos(members.stream().map(chatRoomMapper::toDto).toList())
                .cursor(chatCursorRes)
                .logs(logs)
                .build();
    }

    /**
     * 사용자가 참여 중인 채팅방 목록을 조회한다.
     *
     * 과정:
     * 1) 사용자의 ChatRoomMember 목록 조회
     * 2) 채팅방별 마지막 읽은 메시지 ID(cursor) 생성
     * 3) cursor 기반으로 언리드 메시지 개수와 마지막 메시지 정보 조회
     * 4) 채팅방 정보와 최신 메시지 정보를 합쳐 응답 객체로 변환
     *
     * @param userId 조회 대상 사용자 ID
     * @return 채팅방 목록 및 최신 메시지 정보를 담은 응답 객체
     */
    @Override
    public ChatRoomListRes getChatRoomList(Long userId) {
        // 1. 유저 기준으로 참여 채팅방 조회
        Member memberRef = memberReader.getRef(userId);
        List<ChatRoomMember> chatRoomMembers = chatRoomReader.findChatRoomMembers(memberRef);

        if (chatRoomMembers.isEmpty()) {
            return new ChatRoomListRes(Collections.emptyList());
        }

        // 2. 채팅방 ID -> 마지막 읽은 메시지 ID 매핑
        Map<String, String> cursor = chatRoomMembers.stream()
                .collect(Collectors.toMap(crm -> crm.getChatRoom().getId(), ChatRoomMember::getLastReadMessage));

        // 3. 채팅방별 언리드 카운트 및 마지막 메시지 조회
        Map<String, ChatroomLatestInfo> latestInfos = chatMessageReader.getLatestInfo(cursor);

        // 4. 응답 DTO 구성
        List<ChatRoomListRes.ChatRoomInfo> infos = chatRoomMembers.stream()
                .map(crm -> toChatRoomInfo(crm, latestInfos))
                .toList();

        return new ChatRoomListRes(infos);
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
    private ChatRoomListRes.ChatRoomInfo toChatRoomInfo(
            ChatRoomMember chatRoomMember, Map<String, ChatroomLatestInfo> latestInfos) {
        String chatRoomId = chatRoomMember.getChatRoom().getId();
        ChatroomLatestInfo info = latestInfos.get(chatRoomId);

        Long unreadCount = (info != null) ? info.getCnt() : -1;
        String lastMessage = (info != null) ? info.getMessage() : null;
        Long lastSender = (info != null) ? info.getSender() : null;

        return new ChatRoomListRes.ChatRoomInfo(
                chatRoomId,
                chatRoomMember.getChatRoom().getName(),
                chatRoomMember.getChatRoom().getImageUrl(),
                chatRoomMember.getChatRoom().getTotalMember(),
                unreadCount,
                lastMessage,
                lastMessage,
                lastSender);
    }
}
