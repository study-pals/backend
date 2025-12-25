package com.studypals.domain.groupManage.worker;

import java.util.*;

import org.springframework.dao.DataIntegrityViolationException;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.groupManage.dao.GroupHashTagRepository;
import com.studypals.domain.groupManage.dao.HashTagRepository;
import com.studypals.domain.groupManage.entity.Group;
import com.studypals.domain.groupManage.entity.GroupHashTag;
import com.studypals.domain.groupManage.entity.HashTag;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.GroupErrorCode;
import com.studypals.global.exceptions.exception.GroupException;
import com.studypals.global.utils.StringUtils;

/**
 * 그룹 해시태그 관리를 담당하는 워커 클래스입니다.
 * <p>
 * 사용자가 입력한 해시태그 목록을 정규화하여 {@link HashTag} 엔티티로 저장하고,
 * 이미 존재하는 해시태그의 사용 횟수를 증가시키며,
 * {@link Group} 과 {@link HashTag} 사이의 관계를 {@link GroupHashTag} 엔티티로 관리합니다.
 * <p>
 * 해시태그 저장 시 UNIQUE 제약 조건으로 인한 {@link DataIntegrityViolationException}
 * (동시성 충돌)을 감지하면, 해당 태그들에 대해 재조회 및 재저장을 수행하여
 * 동시성 문제를 완화합니다.
 *
 * <p><b>주요 기능:</b><br>
 * - 입력 태그 문자열을 저장용 태그와 표시용 태그로 분리 및 정규화<br>
 * - 존재하는 해시태그의 사용 횟수(usedCount) 증가<br>
 * - 새 해시태그 생성 시 동시성 충돌 처리 및 재시도<br>
 * - 그룹과 해시태그 간의 매핑({@link GroupHashTag}) 생성 및 저장<br>
 *
 * @author jack8
 * @see com.studypals.domain.groupManage.dao.GroupHashTagRepository
 * @see com.studypals.domain.groupManage.dao.HashTagRepository
 * @since 2025-12-23
 */
@Worker
@RequiredArgsConstructor
public class GroupHashTagWorker {

    private final GroupHashTagRepository groupHashTagRepository;
    private final HashTagRepository hashTagRepository;
    private final StringUtils stringUtils;

    /**
     * 그룹이 생성될 때, 설정한 tag 를 같이 저장하는 메서드입니다. 데이터 정규화 / 저장 on duplicate update 구현
     * 등이 되어 있습니다.
     * @param group 저장대상 그룹
     * @param inputTags 그룹에 포함할 해시태그 문자열
     */
    public void saveTags(Group group, List<String> inputTags) {
        // hashTag 에 저장되는 정규화된 문자열과 사용자에게 보여질 입력 그대로 문자열을 분리
        Map<String, String> normalized = toNormalizedAndRowMap(inputTags);
        if (normalized.isEmpty()) return;

        Set<String> normalizedTags = new HashSet<>(normalized.keySet());

        // 기존 hash tag 조회
        List<HashTag> exists = hashTagRepository.findAllByTagIn(normalizedTags);
        List<HashTag> notExists = notExistTagToCreate(normalizedTags, exists);

        // 이미 존재하는 경우 usedCount 1 증가
        if (!exists.isEmpty()) {
            hashTagRepository.increaseUsedCountBulk(
                    exists.stream().map(HashTag::getTag).toList());
        }

        if (!notExists.isEmpty()) {
            try {
                hashTagRepository.saveAll(notExists);
                hashTagRepository.flush(); // unique 제약 조건 발생용 flush
            } catch (DataIntegrityViolationException e) { // 저장 실패 시 동시성 문제라 생각하고 1회 재시도
                retrySave(notExists.stream().map(HashTag::getTag).toList());
            }
        }
        // 최종 결과물 재조회
        exists = hashTagRepository.findAllByTagIn(normalizedTags);

        // 저장된 hashtag 를 기반으로 groupHashTag 저장
        List<GroupHashTag> groupHashTags = new ArrayList<>();
        for (HashTag tag : exists) {
            String val = normalized.getOrDefault(tag.getTag(), tag.getTag());

            groupHashTags.add(GroupHashTag.builder()
                    .hashTag(tag)
                    .displayTag(val)
                    .group(group)
                    .build());
        }
        groupHashTagRepository.saveAll(groupHashTags);
    }

    /**
     * 저장 재시도 메서드입니다. 새로운 hashtag 를 사용하여 그룹을 생성하는 와중, 조회할 당시에는 없는
     * 해시태그였으나 그 사이에 다른 세션에서 해시태그를 추가할 경우 unique 제약 조건이 생길 수 있습니다.
     * 이를 고려하여 단 1회 저장을 재시도하는 메서드입니다.
     * @param failToSave 저장에 실패한 엔티티
     * @return 저장 및 조회 성공 후 영속화되어 있는 엔티티. 즉, 현재 DB 에 모두 존재하는 hashTag
     */
    private void retrySave(List<String> failToSave) {
        List<HashTag> reExists = hashTagRepository.findAllByTagIn(failToSave);
        List<HashTag> reNotExists = notExistTagToCreate(new HashSet<>(failToSave), reExists);
        if (!reExists.isEmpty()) {
            hashTagRepository.increaseUsedCountBulk(
                    reExists.stream().map(HashTag::getTag).toList());
        }
        if (!reNotExists.isEmpty()) {
            try {
                hashTagRepository.saveAll(reNotExists);
                hashTagRepository.flush(); // unique 제약 조건 발생용 flush
            } catch (DataIntegrityViolationException e) {
                throw new GroupException(
                        GroupErrorCode.GROUP_HASHTAG_FAIL, "[GroupHashTagWorker#retrySave] cannot save hash tag");
            }
        }
        reExists.addAll(reNotExists);
    }

    /**
     * 각 태그에 대한 정규화 진행. 띄어쓰기는 _ 로 대체, 중복된 띄어쓰기 제거/특수문제 제거, trim 제거, lowercase
     * @param tags 정규화 대상 리스트
     * @return 정규화된 문자열 / raw 문자열로 이루어진 map
     */
    private Map<String, String> toNormalizedAndRowMap(List<String> tags) {
        Map<String, String> result = new HashMap<>();
        for (String tag : tags) {
            if (tag.isEmpty()) continue;
            String norm = stringUtils.normalize(tag);
            if (norm == null || norm.isBlank()) continue;
            result.putIfAbsent(norm, tag);
        }

        return result;
    }

    /**
     * 기존에 DB에 존재하지 않는 hash tag 를 찾아 새로운 엔티티를 생성하는 메서드
     * @param reqData 해시태그 생성을 요청하는 데이터, DB 에 이미 존재하거나 없어서 새로 만들어야 되는 값이 합쳐져 있음
     * @param existTags DB 에 이미 존재하는 데이터
     * @return 새롭게 만들어지는 영속화 전 데이터
     */
    private List<HashTag> notExistTagToCreate(Set<String> reqData, List<HashTag> existTags) {
        List<HashTag> result = new ArrayList<>();
        for (HashTag tag : existTags) {
            reqData.remove(tag.getTag());
        }
        for (String str : reqData) {
            result.add(HashTag.builder().tag(str).usedCount(1L).build());
        }

        return result;
    }
}
