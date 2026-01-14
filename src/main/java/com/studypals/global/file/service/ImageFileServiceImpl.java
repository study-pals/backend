package com.studypals.global.file.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ChatPresignedUrlReq;
import com.studypals.global.file.dto.ProfilePresignedUrlReq;
import com.studypals.global.file.entity.ImageType;

/**
 * 파일을 처리하는 로직을 정의한 구현 클래스입니다.
 * 파일 업로드를 위한 presigned url을 발급을 진행합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-10
 */
@Service
public class ImageFileServiceImpl implements ImageFileService {

    private final Map<ImageType, AbstractImageManager> repositoryMap;

    public ImageFileServiceImpl(List<AbstractImageManager> repositories) {
        this.repositoryMap = repositories.stream()
                .collect(Collectors.toMap(
                        AbstractImageManager::getFileType, Function.identity(), (existing, duplicate) -> {
                            throw new IllegalStateException("중복된 레포지토리입니다. 중복된 타입: " + existing.getFileType());
                        }));
    }

    @Override
    public String getProfileUploadUrl(ProfilePresignedUrlReq request, Long userId) {
        AbstractImageManager repository = getRepository(ImageType.PROFILE_IMAGE);
        return repository.getUploadUrl(userId, request.fileName(), String.valueOf(userId));
    }

    @Override
    public String getChatUploadUrl(ChatPresignedUrlReq request, Long userId) {
        AbstractImageManager repository = getRepository(ImageType.CHAT_IMAGE);
        return repository.getUploadUrl(userId, request.fileName(), request.targetId());
    }

    private AbstractImageManager getRepository(ImageType imageType) {
        AbstractImageManager repository = repositoryMap.get(imageType);
        if (repository == null) {
            throw new IllegalArgumentException("지원하지 않는 파일 타입입니다.");
        }
        return repository;
    }
}
