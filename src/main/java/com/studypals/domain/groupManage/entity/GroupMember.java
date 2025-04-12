package com.studypals.domain.groupManage.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.*;

import com.studypals.domain.memberManage.entity.Member;

/**
 * GroupMember 에 대한 엔티티입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code builder} <br>
 * 빌더 패턴을 사용하여 생성합니다. <br>
 *
 * @author s0o0bn
 * @since 2025-04-12
 */
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "group_member")
public class GroupMember {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @Enumerated(EnumType.STRING)
    @Column(name = "main_color", nullable = false)
    private GroupMainColor mainColor;

    @Column(name = "is_leader", nullable = false, columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isLeader = false;

    @Column(name = "joined_at", nullable = false)
    @CreatedDate
    private LocalDate joinedAt;

    public static GroupMember createLeader(Member member, Group group) {
        return GroupMember.builder()
                .member(member)
                .group(group)
                .isLeader(true)
                .mainColor(GroupMainColor.YELLOW)
                .build();
    }
}
