package com.studypals.global.file.worker;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Component;

import com.studypals.global.exceptions.errorCode.FileErrorCode;
import com.studypals.global.exceptions.exception.FileException;
import com.studypals.global.file.FileType;
import com.studypals.global.file.dao.AbstractFileManager;
import com.studypals.global.file.dao.AbstractImageManager;

/**
 * 이미지 파일 처리를 담당하는 매니저({@link AbstractImageManager})들을 관리하고 제공하는 팩토리 클래스입니다.
 * <p>
 * 이 클래스는 애플리케이션 구동 시 Spring Context에 등록된 모든 {@link AbstractImageManager} 구현체를 수집하여
 * {@link FileType}을 키로 하는 Map으로 초기화합니다.
 * 이후 비즈니스 로직에서 특정 이미지 타입에 대한 처리가 필요할 때, 적절한 구현체를 찾아 반환하는 역할을 수행합니다.
 * <p>
 * 이를 통해 전략 패턴(Strategy Pattern)을 지원하며, 새로운 이미지 타입이 추가되더라도
 * 클라이언트 코드(Service 등)의 변경 없이 기능을 확장할 수 있습니다.
 *
 * @author sleepyhoon
 * @see AbstractImageManager
 * @see FileType
 * @since 2026-02-07
 */
@Component
public class ImageManagerFactory {
    private final Map<FileType, AbstractImageManager> managerMap;

    public ImageManagerFactory(List<AbstractImageManager> managers) {
        this.managerMap = managers.stream()
                .collect(Collectors.toMap(
                        AbstractFileManager::getFileType, Function.identity(), (existing, duplicate) -> {
                            throw new IllegalStateException(String.format(
                                    "FileType 중복 등록 오류. '%s' 타입이 '%s'와 '%s' 클래스에서 중복으로 처리됩니다.",
                                    existing.getFileType(),
                                    existing.getClass().getName(),
                                    duplicate.getClass().getName()));
                        }));
    }

    /**
     * 지정된 {@link FileType}에 해당하는 {@link AbstractImageManager}의 구현체를 타입 안전하게 조회합니다.
     *
     * @param fileType 조회할 파일 타입 (예: {@code ImageType.PROFILE_IMAGE})
     * @return 요청된 타입의 Manager 인스턴스
     * @throws FileException 해당 {@code fileType}을 처리하는 Manager가 등록되어 있지 않을 경우 발생
     */
    public AbstractImageManager getManager(FileType fileType) {
        AbstractImageManager manager = managerMap.get(fileType);
        if (manager == null) {
            throw new FileException(FileErrorCode.UNSUPPORTED_FILE_IMAGE_TYPE);
        }
        return managerMap.get(fileType);
    }
}
