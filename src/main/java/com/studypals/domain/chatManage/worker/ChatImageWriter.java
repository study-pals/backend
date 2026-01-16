package com.studypals.domain.chatManage.worker;

import org.springframework.transaction.annotation.Transactional;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.dao.ChatImageRepository;
import com.studypals.domain.chatManage.entity.ChatImage;
import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.global.annotations.Worker;
import com.studypals.global.file.FileUtils;

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
