package com.studypals.global.file.dao;

import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.studypals.global.file.FileType;
import com.studypals.global.file.ObjectStorage;

/**
 * 모든 파일 관리자(Manager)의 최상위 추상 클래스입니다.
 * <p>
 * 이 클래스는 파일 관리에 필요한 공통 기능과 기본 계약을 정의합니다.
 * 특정 도메인의 파일을 관리하는 모든 구체적인 Manager 클래스(예: {@link AbstractImageManager})는
 * 이 클래스를 상속받아야 합니다.
 * <p>
 * <b>주요 역할:</b>
 * <ul>
 *     <li>{@link ObjectStorage}에 대한 의존성을 가지며, 이를 통해 실제 스토리지 작업을 수행합니다.</li>
 *     <li>파일 URL을 이용한 공통 삭제 로직({@link #delete})을 제공합니다.</li>
 *     <li>하위 클래스가 어떤 종류의 파일을 처리하는지 명시하도록 {@link #getFileType} 메서드를 강제합니다.
 *     이는 다양한 파일 타입의 Manager를 동적으로 선택하는 전략 패턴의 기반이 됩니다.</li>
 * </ul>
 *
 * @author sleepyhoon
 * @since 2026-01-14
 * @see ObjectStorage
 * @see AbstractImageManager
 */
@RequiredArgsConstructor
public abstract class AbstractFileManager {

    /**
     * 실제 객체 스토리지와의 상호작용을 담당하는 구현체입니다.
     * 하위 클래스에서 스토리지 기능에 접근할 수 있도록 {@code protected}로 선언되었습니다.
     */
    protected final ObjectStorage objectStorage;

    /**
     * 스토리지에 저장된 파일을 삭제합니다.
     * <p>
     * 전체 파일 URL을 입력받아 내부적으로 객체 키(Object Key)를 추출한 후,
     * {@link ObjectStorage#delete}를 호출하여 실제 파일을 삭제합니다.
     *
     * @param url 삭제할 파일의 전체 URL
     */
    public void delete(String url) {
        String destination = objectStorage.parsePath(url);
        objectStorage.delete(destination);
    }

    /**
     * 이 Manager가 담당하는 파일의 종류({@link FileType})를 반환합니다.
     * <p>
     * 이 추상 메서드는 하위 클래스에서 반드시 구현해야 합니다.
     * 반환된 값은 {@code ImageFileServiceImpl} 등에서 적절한 Manager를 찾는 키로 사용됩니다.
     *
     * @return 이 Manager가 처리하는 {@link FileType}
     */
    public abstract FileType getFileType();

    /**
     * 파일을 스토리지에 업로드합니다.
     * <p>
     * 이 추상 메서드는 하위 클래스에서 반드시 구현해야 합니다.
     * @param file      업로드할 파일
     * @param objectKey 스토리지에 저장될 키
     * @return 업로드된 파일의 접근 URL
     */
    public String upload(MultipartFile file, String objectKey) {
        return objectStorage.upload(file, objectKey);
    }
}
