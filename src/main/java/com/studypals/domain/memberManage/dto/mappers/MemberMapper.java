package com.studypals.domain.memberManage.dto.mappers;

import org.springframework.stereotype.Component;

import com.studypals.domain.memberManage.dto.MemberDetailsRes;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.global.file.ObjectStorage;

/**
 * Member 에 대한 mapping 클래스입니다.
 * <p>
 * MapStruct을 통하여 자동으로 구현체가 생성됩니다.
 *
 * <p><b>외부 모듈:</b><br>
 * MapperStruct
 *
 * @author jack8
 * @since 2025-04-16
 */
@Component
public class MemberMapper {

    public MemberDetailsRes toRes(Member member, ObjectStorage objectStorage) {
        return MemberDetailsRes.builder()
                .id(member.getId())
                .username(member.getUsername())
                .nickname(member.getNickname())
                .birthday(member.getBirthday())
                .imageUrl(objectStorage.convertKeyToFileUrl(member.getProfileImageObjectKey()))
                .createdDate(member.getCreatedDate())
                .token(member.getToken())
                .build();
    }
}
