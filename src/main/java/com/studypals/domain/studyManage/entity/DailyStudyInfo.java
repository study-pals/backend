package com.studypals.domain.studyManage.entity;

import java.time.LocalDate;
import java.time.LocalTime;

import jakarta.persistence.*;

import lombok.*;

import com.studypals.domain.memberManage.entity.Member;

/**
 * 하루 동안의 공부 데이터에 대한 엔티티입니다.
 * 공부를 언제 시작했는지, 언제 마쳤는지, 메모 등의 데이터가 관리됩니다. 분류는 member, studiedDate를 통해 이루어집니다.
 *
 * @author jack8
 * @since 2025-04-17
 */
@Entity
@Getter
@Builder
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "daily_study_info",
        uniqueConstraints =
                @UniqueConstraint(
                        name = "uq_member_date",
                        columnNames = {"member_id", "studied_date"}))
public class DailyStudyInfo {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Column(name = "studied_date", nullable = false)
    private LocalDate studiedDate;

    @Column(name = "start_time", nullable = false)
    private LocalTime startTime;

    @Column(name = "end_time", nullable = true)
    @Setter
    private LocalTime endTime;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;
}
