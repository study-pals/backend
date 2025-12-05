package com.studypals.domain.chatManage.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.*;

/**
 * chat_room 에 대한 JPA 엔티티입니다. 채팅방에 대한 정보가 포함됩니다.
 * 기존과 다르게 ID(식별자)는 UUID를 통해 생성되고 관리됩니다.
 * member 와 chat_room 사이의 매핑 테이블이 존재합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code builder} <br>
 * 빌더 패턴을 사용하여 생성합니다. <br>
 *
 * @author jack8
 * @since 2025-05-09
 */
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "chat_room")
public class ChatRoom {

    @Id
    @Column(name = "id", unique = true, nullable = false)
    private String id;

    @Column(name = "name", unique = true, nullable = false)
    private String name;

    @Column(name = "created_date")
    @CreatedDate
    private LocalDate createdDate;

    @Column(name = "image_url", nullable = true)
    private String imageUrl;

    @Column(name = "joined", nullable = false)
    private Long joined;
}
