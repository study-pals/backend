package com.studypals.domain.studyManage.worker;

import java.util.List;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.studyManage.dao.StudyCategoryRepository;
import com.studypals.domain.studyManage.entity.StudyCategory;
import com.studypals.global.annotations.Worker;

/**
 * 공부 category 의 읽기에 대한 로직을 수행합니다.
 *
 * <p><b>빈 관리:</b><br>
 * worker
 *
 * @author jack8
 * @since 2025-04-17
 */
@Worker
@RequiredArgsConstructor
public class StudyCategoryReader {

    private final StudyCategoryRepository studyCategoryRepository;

    /**
     * 특정 멤버의 모든 카테고리를 반환하는 메서드
     * @param userId 검색할 user 의 아이디
     * @return 카테고리 리스트
     */
    public List<StudyCategory> findByMember(Long userId) {
        return studyCategoryRepository.findByMemberId(userId);
    }

    /**
     * 특정 날짜의, 해당 유저의 카테고리를 가져오는 메서드
     * @param userId 검색할 유저의 아이디
     * @param dayBit 검색할 요일 / 비트 / 가령 수요일이면 0b0000100 (4) 로 정의
     * @return 카테고리 리스트
     */
    public List<StudyCategory> findByMemberAndDay(Long userId, int dayBit) {

        return studyCategoryRepository.findByMemberId(userId).stream()
                .filter(category -> (category.getDayBelong() & dayBit) != 0)
                .toList();
    }
}
