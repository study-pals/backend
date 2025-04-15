package com.studypals.domain.groupManage.worker;

import org.springframework.stereotype.Component;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupEntryCodeRedisRepository;
import com.studypals.domain.groupManage.entity.GroupEntryCode;
import com.studypals.global.utils.RandomUtils;

/**
 * group 초대 코드 생성을 담당하는 Worker 클래스입니다.
 *
 * <p>그룹의 초대 코드를 생성합니다.
 *
 *
 * <p><b>빈 관리:</b><br>
 * Worker
 *
 * @author s0o0bn
 * @since 2025-04-15
 */
@Component
@RequiredArgsConstructor
public class GroupEntryCodeGenerator {
    private static final int GROUP_ENTRY_CODE_LENGTH = 6;

    private final GroupEntryCodeRedisRepository groupEntryCodeRepository;

    public String generate(Long groupId) {
        String code = RandomUtils.generateUpperAlphaNumericCode(GROUP_ENTRY_CODE_LENGTH);
        GroupEntryCode entryCode = new GroupEntryCode(groupId, code);
        groupEntryCodeRepository.save(entryCode);

        return code;
    }
}
