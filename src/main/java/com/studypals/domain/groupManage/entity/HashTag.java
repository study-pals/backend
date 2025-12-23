package com.studypals.domain.groupManage.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import lombok.*;

/**
 * group 에 대한 hashtag 엔티티입니다. group 과 N:M 관계이며 중간 매핑 테이블이 존재합니다.
 *
 * @author jack8
 * @see Group
 * @since 2025-12-23
 */
@Entity
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Builder
@Getter
@Table(
        name = "hash_tag",
        uniqueConstraints = {@UniqueConstraint(columnNames = "tag")})
public class HashTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "tag", nullable = false, unique = true)
    private String tag;

    @Builder.Default
    @Column(name = "used_count", nullable = false, columnDefinition = "DEFAULT 0")
    private Long usedCount = 0L;

    @Column(name = "last_used_date", nullable = true)
    private LocalDate lastUsedDate;
}
