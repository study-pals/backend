package com.studypals.domain.memberManage.entity;

import java.time.LocalDate;

import jakarta.persistence.*;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import lombok.*;

/**
 * Member 에 대한 엔티티입니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code builder} <br>
 * 빌더 패턴을 사용하여 생성합니다. <br>
 *
 * @author jack8
 * @since 2025-04-02
 */
@Entity
@Builder
@Getter
@AllArgsConstructor
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
@Table(name = "member")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id", nullable = false, unique = true)
    private Long id;

    @Column(name = "username", nullable = false, unique = true, length = 255)
    private String username;

    @Column(name = "password", nullable = false, length = 255)
    private String password;

    @Column(name = "nickname", nullable = false, unique = true, length = 255)
    private String nickname;

    @Column(name = "birthday", nullable = true, columnDefinition = "DATE")
    private LocalDate birthday;

    @Column(name = "position", nullable = true, length = 255)
    private String position;

    @OneToOne(mappedBy = "member", cascade = CascadeType.ALL, orphanRemoval = true)
    @Setter
    private MemberProfileImage profileImage;

    @Column(name = "created_at")
    @CreatedDate
    private LocalDate createdDate;

    @Column(name = "token")
    @Builder.Default
    private Long token = 0L;

    public void addToken(Long token) {
        this.token += token;
    }

    public Member(String username, String password, String nickname) {
        this.username = username;
        this.password = password;
        this.nickname = nickname;
        this.token = 0L;
    }

    public void updateProfile(LocalDate birthday, String position) {
        this.birthday = birthday;
        this.position = position;
    }

    /**
     * 프로필 이미지의 Object Key를 반환합니다.
     * 프로필 이미지가 없는 경우 null을 반환합니다.
     */
    public String getProfileImageObjectKey() {
        return this.profileImage != null ? this.profileImage.getObjectKey() : null;
    }
}
