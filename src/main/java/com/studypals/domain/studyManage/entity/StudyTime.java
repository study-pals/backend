package com.studypals.domain.studyManage.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.*;

import com.studypals.domain.memberManage.entity.Member;

/**
 * 공부 시간을 나타내는 JPA entity 클래스입니다.
 * study_time 테이블과 매핑됩니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code Builder}  <br>
 * builder 패턴을 통해 생성합니다. <br>
 *
 * @author jack8
 * @since 2025-04-10
 */
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "study_time",
        indexes = {@Index(name = "idx_member_studied", columnList = "member_id, studied_date")})
public class StudyTime {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "study_type", nullable = false)
    @Enumerated(EnumType.STRING)
    private StudyType studyType;

    @Column(name = "type_id")
    private Long typeId;

    @Column(name = "name", nullable = true, length = 255)
    private String name;

    @Column(name = "studied_date", nullable = false)
    private LocalDate studiedDate;

    @Column(name = "time", nullable = false)
    @Builder.Default
    private Long time = 0L;

    @Column(name = "goal", nullable = true)
    private Integer goal;

    public void addTime(Long time) {
        this.time += time;
    }
}
