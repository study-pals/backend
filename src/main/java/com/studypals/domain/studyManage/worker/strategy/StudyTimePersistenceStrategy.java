package com.studypals.domain.studyManage.worker.strategy;

import java.time.LocalDate;
import java.util.Optional;

import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.studyManage.entity.StudyStatus;
import com.studypals.domain.studyManage.entity.StudyTime;
import com.studypals.domain.studyManage.entity.StudyType;

/**
 * StudyTIme 엔티티에 대해, 각기 다른 저장/조회 로직을 정의하기 위해 사용되는 전략패턴의 인터페이스입니다.
 * 모든 전략 객체는 해당 인터페이스를 부모로 두어야 합니다.
 *
 * <p><b>상속 정보:</b><br>
 * AbstractStudyPersistenceStrategy 및 다른 구체 클래스의 부모 인터페이스
 *
 * @author jack8
 * @see AbstractStudyPersistenceStrategy
 * @since 2025-05-06
 */
public interface StudyTimePersistenceStrategy {

    /**
     * 각 전략 객체의 호환되는 타입을 정의합니다.
     * @return 해당 전략 객체가 지원하는 StudyType
     */
    StudyType getType();

    /**
     * 각 전략 객체가 StudyStatus에 대해 자신이 이를 지원하는지에 대한 여부를 반환합니다. 팩토리에서 이를 통해
     * StudyStatus에 대응하는 전략 객체를 호출합니다.
     * @param status 공부 상태(redis에 저장되는) 를 받아 전략 객체 호환 여부 체크
     * @return 호환 여부
     */
    boolean supports(StudyStatus status);

    /**
     * 주어진 매개변수를 통하여 적절한 엔티티를 탐색하여 반환합니다. 직접적인 repository를 사용함으로서 transaction
     * 이 필수적으로 포함되어야 합니다.
     * @param member 검색하고자 하는 member
     * @param status 공부 상태
     * @param studiedDate 공부 날짜
     * @return 검색된 StudyTime의 Optional 래핑
     */
    Optional<StudyTime> find(Member member, StudyStatus status, LocalDate studiedDate);

    /**
     * 주어진 매개변수를 통하여 새로운 StudyTime을 생성하여 반환합니다. 엔티티를 저장하지 않고 반환함합니다.
     * @param member 만들고자 하는 member
     * @param status 새로운 status
     * @param studiedDate 공부한 날짜
     * @param time 공부한 시간
     * @return 생성된 StudyTime 비영속 엔티티
     */
    StudyTime create(Member member, StudyStatus status, LocalDate studiedDate, Long time);
}
