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

    /**
     * 지정된 groupId에 대한 초대 코드를 조회하거나 새로 생성합니다.
     *
     * <p>동작 방식은 다음과 같습니다.</p>
     * 1. Redis에서 groupId로 기존 초대 코드가 존재하는지 조회합니다. <br>
     * 2. 존재하면 해당 코드를 반환합니다. <br>
     * 3. 존재하지 않으면 중복되지 않는 새 코드를 생성합니다. <br>
     * 4. 초대 코드의 TTL을 설정하고 Redis에 저장합니다.
     * <p>기존 코드가 존재하는 경우에도 TTL은 항상 갱신됩니다.</p>
     *
     * @param groupId 초대 코드가 속한 그룹의 식별자
     * @return 조회되거나 새로 생성된 초대 코드 문자열
     */
    public String getOrCreateCode(Long groupId) {
        GroupEntryCode entryCode = groupEntryCodeRepository
                .findFirstByGroupId(groupId)
                .orElseGet(() -> {
                    String code = generateNonDuplicatedCode();
                    return GroupEntryCode.builder().code(code).groupId(groupId).build();
                });
        entryCode.setTtl(1L);
        groupEntryCodeRepository.save(entryCode);

        return entryCode.getCode();
    }

    /**
     * 지정된 초대 코드의 만료 기간(TTL)을 갱신합니다.
     *
     * <p>초대 코드가 Redis에 존재하지 않는 경우 예외를 발생시킵니다.</p>
     * <br>
     * <p>TTL은 Redis key 단위로 적용되며, 기존 TTL은 전달받은 값으로 덮어씌워집니다.</p>
     *
     * @param code 만료 기간을 연장할 초대 코드
     * @param day 설정할 TTL 값 (단위는 Redis 설정에 따름)
     * @throws GroupException 초대 코드가 존재하지 않는 경우
     */
    public void increaseExpire(String code, Long day) {
        //
        GroupEntryCode entryCode = groupEntryCodeRepository
                .findById(code)
                .orElseThrow(() -> new GroupException(
                        GroupErrorCode.GROUP_CODE_NOT_FOUND,
                        "[GroupEntryCodeManager#increaseExpire] unknown code while increase expire date"));

        entryCode.setTtl(day);
        groupEntryCodeRepository.save(entryCode);
    }

    /**
     * 초대 코드에 해당하는 그룹 ID를 조회합니다.
     *
     * <p>초대 코드가 Redis에 존재하지 않는 경우 예외를 발생시킵니다.</p>
     *
     * @param entryCode 조회할 초대 코드
     * @return 초대 코드가 속한 그룹의 식별자
     * @throws GroupException 초대 코드가 존재하지 않는 경우
     */
    public Long getGroupId(String entryCode) {
        return groupEntryCodeRepository
                .findById(entryCode)
                .orElseThrow(() -> new GroupException(GroupErrorCode.GROUP_CODE_NOT_FOUND))
                .getGroupId();
    }

    /**
     * 주어진 초대 코드가 특정 그룹에 속하는지 검증합니다.
     *
     * <p>초대 코드로 조회한 그룹 ID와 전달받은 그룹의 ID가 다를 경우 예외를 발생시킵니다.</p>
     *
     * @param group 검증 대상 그룹
     * @param entryCode 검증할 초대 코드
     * @throws GroupException 초대 코드가 다른 그룹에 속한 경우
     */
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

    /**
     * 중복되지 않는 초대 코드를 생성합니다.
     *
     * <p>랜덤으로 코드를 생성한 뒤, Redis에 동일한 코드가 존재하는지 확인합니다.</p>
     * <br>
     * <p>중복되는 경우 재시도하며, 중복되지 않는 코드가 생성될 때까지 반복합니다.</p>
     *
     * @return 중복되지 않는 초대 코드 문자열
     */
    private String generateNonDuplicatedCode() {
        String code;
        do {
            code = RandomUtils.generateUpperAlphaNumericCode(GROUP_ENTRY_CODE_LENGTH);
        } while (isDuplicatedCode(code));

        return code;
    }

    /**
     * 주어진 초대 코드가 이미 Redis에 존재하는지 여부를 확인합니다.
     *
     * @param code 중복 여부를 확인할 초대 코드
     * @return 이미 존재하면 true, 존재하지 않으면 false
     */
    private boolean isDuplicatedCode(String code) {
        return groupEntryCodeRepository.existsById(code);
    }
}
