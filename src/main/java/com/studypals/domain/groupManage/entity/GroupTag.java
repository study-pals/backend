package com.studypals.domain.groupManage.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * GroupTag 에 대한 엔티티입니다.
 *
 * <p>
 *     해당 엔티티는 조회에만 사용됩니다.
 * </p>
 *
 * <p><b>주요 생성자:</b><br>
 * {@code builder} <br>
 * 빌더 패턴을 사용하여 생성합니다. <br>
 *
 * @author s0o0bn
 * @since 2025-04-13
 */
@Entity
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@Table(name = "group_tag")
public class GroupTag {

    @Id
    @Column(name = "name", nullable = false, unique = true)
    private String name;
}
