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

    /**
     * 채팅 이미지의 메타데이터를 생성하고 데이터베이스에 저장합니다.
     * <p>
     * 이 메서드는 클라이언트가 서버를 통해 이미지를 업로드할 때 호출되며,
     * 서버가 파일을 처리하고 스토리지에 저장하는 흐름 속에서 해당 파일의 메타데이터를 데이터베이스에 기록합니다.
     *
     * @param chatRoom 이미지가 속한 채팅방 엔티티
     * @param objectKey 스토리지에 저장될 파일의 고유 객체 키
     * @param fileName 원본 파일의 이름
     * @return 데이터베이스에 저장된 {@link ChatImage}의 고유 ID
     */
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
