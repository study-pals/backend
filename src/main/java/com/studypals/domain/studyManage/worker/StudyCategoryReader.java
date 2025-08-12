package com.studypals.domain.studyManage.worker;

import java.util.List;
import java.util.Map;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.domain.studyManage.entity.StudyType;
import com.studypals.global.annotations.Worker;
import com.studypals.global.exceptions.errorCode.StudyErrorCode;
import com.studypals.global.exceptions.exception.StudyException;

/**
 * 코드에 대한 전체적인 역할을 적습니다.
 * <p>
 * 코드에 대한 작동 원리 등을 적습니다.
 *
 * <p><b>상속 정보:</b><br>
 * 상속 정보를 적습니다.
 *
 * <p><b>주요 생성자:</b><br>
 * {@code ExampleClass(String example)}  <br>
 * 주요 생성자와 그 매개변수에 대한 설명을 적습니다. <br>
 *
 * <p><b>빈 관리:</b><br>
 * 필요 시 빈 관리에 대한 내용을 적습니다.
 *
 * <p><b>외부 모듈:</b><br>
 * 필요 시 외부 모듈에 대한 내용을 적습니다.
 *
 * @author jack8
 * @see
 * @since 2025-08-01
 */
@Worker
@RequiredArgsConstructor
public class StudyCategoryReader {

    private final StudyCategoryRepository studyCategoryRepository;

    public StudyCategory findById(Long id) {
        return studyCategoryRepository
                .findById(id)
                .orElseThrow(() -> new StudyException(
                        StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "[StudyCategoryReader#findById] can't find category"));
    }

    public List<StudyCategory> findByTypeAndTypeId(Map<StudyType, List<Long>> typeMap) {
        return studyCategoryRepository.findByTypeMap(typeMap);
    }

    public List<StudyCategory> findByGroupId(Long groupId) {
        return findByTypeAndTypeId(Map.of(StudyType.GROUP, List.of(groupId)));
    }

    public List<StudyCategory> findByMemberId(Long memberId) {
        return findByTypeAndTypeId(Map.of(StudyType.PERSONAL, List.of(memberId)));
    }

    public List<StudyCategory> findByStudyTypeAndTypeId(StudyType type, Long typeId) {
        return studyCategoryRepository.findByStudyTypeAndTypeId(type, typeId);
    }
}
