package com.studypals.domain.groupManage.worker;

import java.time.LocalDateTime;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.groupEntryRepository.GroupEntryCodeRedisRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupEntryCode;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;
import com.studypals.global.utils.RandomUtils;
import com.studypals.global.utils.TimeUtils;

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
    private final TimeUtils timeUtils;

    /**
     * 지정된 groupId에 대한 초대 코드를 조회하거나 새로 생성합니다.
     *
     * <p>동작 방식은 다음과 같습니다.</p>
     * 1. Redis에서 groupId로 기존 초대 코드가 존재하는지 조회합니다. <br>
     * 2. 존재하면 해당 코드를 반환합니다. <br>
     * 3. 존재하지 않으면 중복되지 않는 새 코드를 생성합니다. <br>
     * 4. 초대 코드의 TTL을 설정하고 Redis에 저장합니다.
     * <p>기존 코드가 존재하고 만료 날짜가 하루 이내라면 갱신됩니다.</p>
     *
     * @param groupId 초대 코드가 속한 그룹의 식별자
     * @return 조회되거나 새로 생성된 초대 코드 문자열
     */
    public GroupEntryCode getOrCreateCode(Long groupId) {
        LocalDateTime now = timeUtils.getRawLocalDateTime();
        LocalDateTime threshold = now.plusDays(1);

        GroupEntryCode entryCode = groupEntryCodeRepository
                .findByGroupId(groupId)
                .orElseGet(() -> {
                    String code = generateNonDuplicatedCode();
                    return GroupEntryCode.builder()
                            .code(code)
                            .groupId(groupId)
                            .ttl(TimeUtils.getSecondOfDay(1L))
                            .expireAt(threshold)
                            .build();
                });

        if (entryCode.getTtl() != null && entryCode.getTtl() > 0) {
            LocalDateTime expiredAt = entryCode.getExpireAt();

            // 오류상황 - TTL 상으로는 기한이 존재하나 / expiredAt 이 null 인 경우 강제 초기화
            boolean missingExpireAt = (expiredAt == null);
            // 오류상황을 제외하고(NPE 방지) 만료 기간이 이후 24시간 이내인 경우
            boolean withinOneDay = (!missingExpireAt && !expiredAt.isAfter(threshold));

            // 만료 시간이 지금으로부터 1일 이내라면 1일로 갱신
            if (missingExpireAt || withinOneDay) {
                entryCode.setTtl(TimeUtils.getSecondOfDay(1L));
                entryCode.setExpireAt(threshold);
            }
        }

        groupEntryCodeRepository.save(entryCode);
        groupEntryCodeRepository.saveIdx(entryCode);

        return entryCode;
    }

    /**
     * 지정된 초대 코드의 만료 기간(TTL)을 갱신합니다.
     *
     * <p>초대 코드가 Redis에 존재하지 않는 경우 예외를 발생시킵니다.</p>
     * <br>
     * <p>TTL은 Redis key 단위로 적용되며, 기존 TTL은 전달받은 값으로 덮어씌워집니다.</p>
     *
     * @param groupId 만료 기간을 연장할 초대 코드 소유자 그룹 아이디
     * @param day 만료 날짜(day 기준, -1 / 1 ~ 30)
     * @throws GroupException 초대 코드가 존재하지 않는 경우
     */
    public void increaseExpire(Long groupId, Long day) {
        LocalDateTime today = timeUtils.getRawLocalDateTime();
        GroupEntryCode entryCode = groupEntryCodeRepository
                .findByGroupId(groupId)
                .orElseThrow(() -> new GroupException(
                        GroupErrorCode.GROUP_CODE_NOT_FOUND,
                        "[GroupEntryCodeManager#increaseExpire] unknown group while increase expire date"));

        // TTL 설정/연장, day = -1 인 경우 무제한
        Long ttl = (day > 0) ? TimeUtils.getSecondOfDay(day) : day;
        entryCode.setTtl(ttl);

        if (day < 0) entryCode.setExpireAt(null);
        else entryCode.setExpireAt(today.plusDays(day));

        groupEntryCodeRepository.save(entryCode);
        groupEntryCodeRepository.saveIdx(entryCode);
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
