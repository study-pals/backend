package com.studypals.domain.studyManage.worker.strategy;

import java.time.LocalDate;
import java.util.Optional;

import org.springframework.stereotype.Component;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.dao.PersonalStudyCategoryRepository;
import com.studypals.domain.studyManage.dao.StudyTimeRepository;
import com.studypals.domain.studyManage.entity.*;

/**
 * StudyType이 PERSONAL 인 레코드에 대해, 검색/생성 및 전략 패턴에서의 호환성 여부를 정의합니다.
 * 다만, 이는 AbstractStudyPersistenceStrategy 추상 클래스의 구현 클래스이며, 추상 클래스에 공통된 부분에 대한 메서드가
 * 정의되어 있습니다. 따라서 해당 객체에서는 StudyType만 정의하였습니다.
 *
 * @author jack8
 * @see AbstractStudyPersistenceStrategy
 * @see StudyTimePersistenceStrategy
 * @since 2025-05-06
 */
@Component
public class PersonalStudyPersistenceStrategy extends AbstractStudyPersistenceStrategy {

    private final PersonalStudyCategoryRepository personalStudyCategoryRepository;

    public PersonalStudyPersistenceStrategy(
            StudyTimeRepository studyTimeRepository, PersonalStudyCategoryRepository personalStudyCategoryRepository) {
        super(studyTimeRepository);
        this.personalStudyCategoryRepository = personalStudyCategoryRepository;
    }

    @Override
    public StudyType getType() {
        return StudyType.PERSONAL;
    }

    @Override
    public StudyTime create(Member member, StudyStatus status, LocalDate studiedDate, Long time) {
        return StudyTime.builder()
                .member(member)
                .studyType(getType())
                .typeId(status.getTypeId())
                .studiedDate(studiedDate)
                .time(time)
                .name(status.getName())
                .goal(status.getGoal())
                .build();
    }

    @Override
    public Optional<PersonalStudyCategory> getCategoryInfo(Member member, Long typeId) {
        Long userId = member.getId();
        Optional<PersonalStudyCategory> optionalCategory = personalStudyCategoryRepository.findById(typeId);
        if (optionalCategory.isEmpty()) {
            throw new IllegalArgumentException("[PersonalStudyPersistenceStrategy#getCategoryInfo] unknown category");
        }
        if (!optionalCategory.get().isOwner(userId)) {
            throw new IllegalArgumentException(
                    "[PersonalStudyPersistenceStrategy#getCategoryInfo] is not owner of category");
        }

        return optionalCategory;
    }
}
