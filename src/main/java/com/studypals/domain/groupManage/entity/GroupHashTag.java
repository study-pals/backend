package com.studypals.domain.groupManage.entity;

import jakarta.persistence.*;

import lombok.*;

/**
 * group 과 hashTag 간의 매핑 테이블입니다. hashTag 조회 시 N + 1 문제를 조심해야 할 필요가 있습니다.
 *
 * @author jack8
 * @see HashTag
 * @see Group
 * @since 2025-12-23
 */
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(
        name = "group_hash_tag",
        indexes = {@Index(name = "idx_grouphashtag_group_id", columnList = "group_id")})
public class GroupHashTag {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "group_id")
    private Group group;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "hash_tag_id")
    private HashTag hashTag;

    @Column(name = "display_tag", nullable = false)
    private String displayTag;
}
