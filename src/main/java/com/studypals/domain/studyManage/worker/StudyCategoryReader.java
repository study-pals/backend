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
 * {@link StudyCategory} 에 대한 읽기 전용 worker 클래스입니다.
 * <p>
 * {@link StudyCategoryRepository} 를 사용하여 아이디 검색 / type 및 typeId 검색 등의 메서드를 지원합니다.
 *
 *
 * @author jack8
 * @see StudyCategoryRepository
 * @since 2025-08-01
 */
@Worker
@RequiredArgsConstructor
public class StudyCategoryReader {

    private final StudyCategoryRepository studyCategoryRepository;

    /**
     * studyCategory id 에 대해 검색합니다. 없으면 예외를 던집니다.
     * @throws StudyException  StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, 카테고리 없음
     * @param id 찾고자 하는 카테고리 아이디
     * @return 검색된 StudyCategory
     */
    public StudyCategory getById(Long id) {
        return studyCategoryRepository
                .findById(id)
                .orElseThrow(() -> new StudyException(
                        StudyErrorCode.STUDY_CATEGORY_NOT_FOUND, "[StudyCategoryReader#getById] can't find category"));
    }

    /**
     * type들과 typeId들로 해당하는 StudyCategory 의 List 를 반환합니다. 빈 리스트가 반환될 수도 있습니다.
     * @param typeMap StudyType 과 그에 따른 typeId 의 List 가 담긴 Map <br>
     *                ({@code  Map<StudyType, List<Long>> typeMap})
     * @return StudyCategory 에 대한 리스트
     */
    public List<StudyCategory> findByTypesAndTypeIds(Map<StudyType, List<Long>> typeMap) {
        return studyCategoryRepository.findByTypeMap(typeMap);
    }

    /**
     * type 과 typeId로 해당하는 StudyCategory 의 List로 반환합니다. 빈 리스트가 반환될 수도 있습니다.
     * @param type 검색하고자 할 StudyType
     * @param typeId 검색하고자 할 StudyType 에 따른 typeId
     * @return StudyCategory 에 대한 리스트
     */
    public List<StudyCategory> findByStudyTypeAndTypeId(StudyType type, Long typeId) {
        return studyCategoryRepository.findByStudyTypeAndTypeId(type, typeId);
    }
}
