package com.studypals.domain.chatManage.worker;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatImageRepository;
import com.studypals.domain.chatManage.entity.ChatImage;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.global.annotations.Worker;
import com.studypals.global.file.FileUtils;

/**
 * 채팅 이미지의 메타데이터를 데이터베이스에 저장하는 역할을 전담하는 Worker 클래스입니다.
 * <p>
 * 이 클래스는 CQRS(Command Query Responsibility Segregation) 패턴의 'Command' 측면을 담당하며,
 * 시스템의 상태를 변경하는 '쓰기(Write)' 작업에만 집중합니다.
 * {@link Transactional} 어노테이션을 통해 데이터 저장 작업의 원자성을 보장합니다.
 *
 * @author sleepyhoon
 * @since 2026-01-15
 * @see ChatImage
 * @see ChatImageRepository
 */
@Worker
@RequiredArgsConstructor
public class ChatImageWriter {
    private final ChatImageRepository chatImageRepository;

    @Transactional
    public Long save(ChatRoom chatRoom, String objectKey, String fileName) {
        String extension = FileUtils.extractExtension(fileName);

        ChatImage savedImage = chatImageRepository.save(ChatImage.builder()
                .chatRoom(chatRoom)
                .objectKey(objectKey)
                .originalFileName(fileName)
                .mimeType(extension)
                .build());

        return savedImage.getId();
    }
}
