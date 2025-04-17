package com.studypals.domain.studyManage.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.*;

import com.studypals.domain.memberManage.entity.Member;

/**
 * 하루 동안 공부한 총 양에 대한 읽기 전용 엔티티입니다.
 * <p>
 * 쓸 수 없습니다. 해당 엔티티의 생성은 불가능합니다.
 *
 * <p><b>주요 생성자:</b><br>
 * JPA 프록시 객체 생성을 위한 protected no arg constructor 밖에 없습니다. <br>
 *
 * @author jack8
 * @since 2025-04-17
 */
@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "daily_study_summary",
        indexes = {@Index(name = "idx_member_studied", columnList = "member_id, studied_at")})
public class DailyStudySummary {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "studied_at", nullable = false)
    private LocalDate studiedAt;

    @Column(name = "time", nullable = false)
    private Long time;
}
