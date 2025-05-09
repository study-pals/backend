package com.studypals.domain.chatManage.entity;

/**
 * 채팅방에서 각 멤버의 역할을 정의하는 enum 클래스입니다.
 * <pre>
 *     ADMIN : 해당 채팅방의 생성자(보통 그룹의 장)에게 주어집니다. 모든 권한을 소유하고 있으며 각 채팅방 별 하나만 존재합니다.
 *     MANAGER: 채팅방 관리를 위한 권한 일부를 가지고 있습니다. ADMIN 또는 MANAGER 에 의해 임명됩니다.
 *     MEMBER: 일반 사용자입니다.
 * </pre>
 *
 * @author jack8
 * @since 2025-05-09
 */
public enum ChatRoomRole {
    ADMIN,

    MANAGER,

    MEMBER
}
