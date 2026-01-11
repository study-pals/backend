package com.studypals.domain.fileManage.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import com.studypals.domain.fileManage.dao.AbstractFileRepository;
import com.studypals.domain.fileManage.dto.PresignedUrlReq;
import com.studypals.domain.fileManage.entity.FileType;

/**
 * 파일을 처리하는 로직을 정의한 구현 클래스입니다.
 * 파일 업로드를 위한 presigned url을 발급을 진행합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-10
 */
@Service
public class FileServiceImpl implements FileService {

    private final Map<FileType, AbstractFileRepository> repositoryMap;

    public FileServiceImpl(List<AbstractFileRepository> repositories) {
        this.repositoryMap = repositories.stream()
                .collect(Collectors.toMap(AbstractFileRepository::getFileType, Function.identity()));
    }

    @Override
    public String getUploadUrl(PresignedUrlReq request) {
        AbstractFileRepository repository = repositoryMap.get(request.type());
        if (repository == null) {
            throw new IllegalArgumentException("지원하지 않는 파일 타입입니다.");
        }
        return repository.getUploadUrl(request.fileName());
    }
}
