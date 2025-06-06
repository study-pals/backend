package com.studypals.domain.groupManage.worker;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupEntryCodeRedisRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryCode;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;
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
@Worker
@RequiredArgsConstructor
public class GroupEntryCodeManager {
    private static final int GROUP_ENTRY_CODE_LENGTH = 6;

    private final GroupEntryCodeRedisRepository groupEntryCodeRepository;

    public String generate(Long groupId) {
        String code = generateNonDuplicatedCode();
        GroupEntryCode entryCode = new GroupEntryCode(code, groupId);
        groupEntryCodeRepository.save(entryCode);

        return code;
    }

    public Long getGroupId(String entryCode) {
        return groupEntryCodeRepository
                .findById(entryCode)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_CODE_NOT_FOUND))
                .getId();
    }

    public void validateCodeBelongsToGroup(Group group, String entryCode) {
        Long actualGroupId = getGroupId(entryCode);
        if (!actualGroupId.equals(group.getId())) {
            throw new GroupException(
                    GroupErrorCode.GROUP_CODE_INVALID,
                    String.format(
                            "Invalid group code: expected groupId=%d, actual groupId=%d",
                            group.getId(), actualGroupId));
        }
    }

    private String generateNonDuplicatedCode() {
        String code;
        do {
            code = RandomUtils.generateUpperAlphaNumericCode(GROUP_ENTRY_CODE_LENGTH);
        } while (isDuplicatedCode(code));

        return code;
    }

    private boolean isDuplicatedCode(String code) {
        return groupEntryCodeRepository.existsById(code);
    }
}
