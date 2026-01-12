package com.studypals.global.file.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.studypals.global.file.dao.AbstractFileManager;
import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.entity.FileType;

/**
 * 파일을 처리하는 로직을 정의한 구현 클래스입니다.
 * 파일 업로드를 위한 presigned url을 발급을 진행합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-10
 */
@Service
public class FileServiceImpl implements FileService {

    private final Map<FileType, AbstractFileManager> repositoryMap;

    public FileServiceImpl(List<AbstractFileManager> repositories) {
        this.repositoryMap = repositories.stream()
                .collect(Collectors.toMap(
                        AbstractFileManager::getFileType, Function.identity(), (existing, duplicate) -> {
                            throw new IllegalStateException(
                                    "Duplicate FileType mapping detected during FileServiceImpl initialization: "
                                            + existing.getFileType());
                        }));
    }

    @Override
    public String getProfileUploadUrl(ProfilePresignedUrlReq request) {
        AbstractFileManager repository = getRepository(FileType.PROFILE);
        return repository.getUploadUrl(request.fileName());
    }

    @Override
    public String getChatUploadUrl(ChatPresignedUrlReq request) {
        AbstractFileManager repository = getRepository(FileType.CHAT_IMAGE);
        return repository.getUploadUrl(request.fileName(), request.targetId());
    }

    private AbstractFileManager getRepository(FileType fileType) {
        AbstractFileManager repository = repositoryMap.get(fileType);
        if (repository == null) {
            throw new IllegalArgumentException("지원하지 않는 파일 타입입니다.");
        }
        return repository;
    }
}
