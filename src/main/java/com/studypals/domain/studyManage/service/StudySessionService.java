package com.studypals.domain.studyManage.service;

import java.time.LocalTime;

import com.studypals.domain.studyManage.dto.StartStudyReq;
import com.studypals.domain.studyManage.dto.StartStudyRes;
import com.studypals.domain.studyManage.dto.StudyStatusRes;

/**
 * 공부 시간 시작/종료 등 세션에 대한 관리를 담당합니다.
 *
 * <p><b>상속 정보:</b><br>
 * {@link StudySessionServiceImpl} 에 대한 인터페이스입니다.
 *
 * @author jack8
 * @see StudySessionServiceImpl
 * @since 2025-04-13
 */
public interface StudySessionService {

    /**
     * 공부 시작 시 해당 메서드를 사용합니다. 작동 원리는 다음과 같습니다.
     * <pre>
     * 0. 해당 메서드의 매개변수인 startStudyReq 는 categoryId 와 temporaryName 이 동시에 존재하면 안됩니다.
     * 1. redis 에서 해당 사용자의 정보를 찾습니다.
     * 2. 만약 존재하지 않는다면, 현재 공부 중 / 시작 시간 / 과목(category or name) 을 생성하고 저장합니다.
     * 3. 만약 존재하고, 공부 중이 아니라면, 공부 중 / 시작 시간 / 과목 을 갱신하여 저장합니다.
     * 4. 만약 존재하고, 공부 중이라면, 현재 저장되어 있는 상태 그대로 반환합니다.
     *
     * -> 컨트롤러 혹은 클라이언트는 보낸 시간과, 반환받은 시작 시간을 비교하여 기존에 공부 중 이었는지 여부를 파악할 수 있습니다.
     * </pre>
     * @param userId 시작 요청을 보낸 사용자의 id
     * @param dto 카테고리 혹은 이름 , 시작 시간
     * @return 카테고리, 시작 시간  등
     */
    StartStudyRes startStudy(Long userId, StartStudyReq dto);

    /**
     * 공부 종료 시 해당 메서드를 사용합니다. 모든 공부 시간은 오전 6시에 초기화 됩니다. 배치 작업에 대하여 6시
     * 전까지의 데이터를 옮기고, 새롭게 적용한 데이터를 redis에 넣는다고 가정합니다.작동 원리는 다음과 같습니다.
     * <pre>
     * 1. "오늘"의 날짜를 받아옵니다. 6시 전후로 갈립니다.
     * 2. redis에서 사용자의 정보를 받아옵니다.
     * 3. 해당 정보가 유효한지 검사합니다.(가령, null 여부, 필수 필드 누락)
     * 4. 공부 시간을 second 로 환산합니다. 단, 시작이 22:00 , 종료가 02:00 과 같은 경우, 이를 고려합니다.
     * 5. studyTime 을 갱신 혹은 저장합니다.
     * 6. redis에 사용자 정보를 갱신 혹은 저장합니다.
     * </pre>
     * @param userId 종료 요청을 보낸 사용자의 id
     * @param endTime 공부가 종료된 시간
     * @return 이번 시간동안 공부한 총 양
     */
    Long endStudy(Long userId, LocalTime endTime);

    /**
     * 사용자가 학습 중인지 확인합니다.
     * @param userId 요청을 보낸 사용자 id
     * @return 학습 정보
     */
    StudyStatusRes checkStudyStatus(Long userId);
}
