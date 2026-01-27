package com.studypals.domain.chatManage.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.Index;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.experimental.SuperBuilder;

import com.studypals.global.file.entity.ImageFile;

/**
 * 채팅방(ChatRoom) 내에서 전송된 이미지의 메타데이터를 관리하는 엔티티입니다.
 * <p>
 * 이 엔티티는 {@link ImageFile}을 상속받아 이미지 파일의 공통 속성을 관리하며,
 * {@link ChatRoom}과 다대일(Many-to-One) 관계를 맺습니다.
 * 채팅 이미지는 한 번 생성되면 수정되지 않는 불변(Immutable)의 특성을 가집니다.
 *
 * <p><b>주요 특징:</b>
 * <ul>
 *     <li><b>상속 관계:</b> {@link ImageFile}의 모든 속성을 상속받습니다.</li>
 *     <li><b>연관 관계:</b> 여러 개의 채팅 이미지가 하나의 {@link ChatRoom}에 속합니다.</li>
 *     <li><b>불변성:</b> 생성 후 상태가 변경되지 않습니다. (수정 기능 없음)</li>
 *     <li><b>인덱싱:</b> 이미지 처리 상태({@code imageStatus})와 생성일({@code createdAt})에 복합 인덱스가 설정되어 있어,
 *     리사이징 등 비동기 처리 대상 조회 시 성능을 향상시킵니다.</li>
 * </ul>
 *
 * @author sleepyhoon
 * @since 2026-01-15
 * @see ImageFile
 * @see ChatRoom
 * @see com.studypals.domain.chatManage.worker.ChatImageManager
 */
@Entity
@Getter
@SuperBuilder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "chat_image",
        indexes = @Index(name = "idx_chat_image_status_created_at", columnList = "imageStatus, createdAt"))
public class ChatImage extends ImageFile {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private ChatRoom chatRoom;
}
