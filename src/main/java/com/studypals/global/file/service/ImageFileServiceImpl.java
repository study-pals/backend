package com.studypals.global.file.service;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import lombok.RequiredArgsConstructor;

import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.worker.ChatImageWriter;
import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.entity.MemberProfileImage;
import com.studypals.domain.memberManage.worker.MemberProfileImageWriter;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.global.file.FileUtils;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ImageUploadDto;
import com.studypals.global.file.dto.ImageUploadRes;
import com.studypals.global.file.entity.ImageType;
import com.studypals.global.file.worker.ImageManagerFactory;

/**
 * 이미지 파일 관련 비즈니스 로직을 처리하는 서비스 구현체입니다.
 * <p>
 * 이 서비스는 클라이언트로부터 받은 파일을 스토리지에 직접 업로드하는 역할을 담당합니다.
 * {@link ImageType}에 따라 적절한 {@link AbstractImageManager}를 동적으로 선택하여 로직을 위임하는 전략 패턴을 사용합니다.
 * 이를 통해 새로운 이미지 타입이 추가되더라도 서비스 코드의 변경 없이 유연하게 확장할 수 있습니다.
 *
 * <p><b>주요 흐름:</b>
 * <ol>
 *     <li>클라이언트로부터 이미지 파일과 메타데이터를 받습니다.</li>
 *     <li>요청 타입에 맞는 Manager를 조회합니다.</li>
 *     <li>Manager를 통해 스토리지에 저장될 고유한 Object Key를 생성합니다.</li>
 *     <li>이미지 리사이징 로직을 수행합니다.</li>
 *     <li>ObjectStorage를 통해 파일을 스토리지에 업로드합니다.</li>
 *     <li>업로드된 파일 정보와 메타데이터를 데이터베이스에 저장합니다.</li>
 *     <li>저장된 이미지 ID와 접근 가능한 URL을 반환합니다.</li>
 * </ol>
 *
 * @author sleepyhoon
 * @since 2026-01-10
 * @see ImageFileService
 * @see AbstractImageManager
 */
@Service
@RequiredArgsConstructor
public class ImageFileServiceImpl implements ImageFileService {

    private final ImageManagerFactory imageManagerFactory;
    private final MemberReader memberReader;
    private final ChatRoomReader chatRoomReader;
    private final MemberProfileImageWriter profileImageWriter;
    private final ChatImageWriter chatImageWriter;

    /**
     * 사용자 프로필 이미지를 업로드합니다.
     * <p>
     * {@link ImageType#PROFILE_IMAGE} 타입에 맞는 Manager를 찾아 다음을 수행합니다:
     * <ol>
     *     <li>사용자 ID와 파일명을 기반으로 Object Key를 생성합니다.</li>
     *     <li>스토리지에 파일을 업로드합니다.</li>
     *     <li>이미지 정보를 DB에 저장합니다.</li>
     * </ol>
     *
     * @param file 업로드할 이미지 파일
     * @param userId Presigned URL을 요청한 사용자의 ID
     * @return 생성된 이미지 ID와 접근 URL이 포함된 응답 DTO
     */
    @Override
    @Transactional
    public ImageUploadRes uploadProfileImage(MultipartFile file, Long userId) {
        AbstractImageManager manager = imageManagerFactory.getManager(ImageType.PROFILE_IMAGE);

        // 1. [MinIO] 프로필 사진 업로드, 업로드가 성공하면 file 정보는 전부 유효합니다.
        ImageUploadDto uploadDto = manager.uploadImage(file, userId, String.valueOf(userId));

        // DB 업데이트를 위한 준비
        Member member = memberReader.get(userId);
        MemberProfileImage currentProfile = member.getProfileImage();
        Long imageId;

        if (currentProfile != null) {
            // 2. [DB] 기존 정보가 있으면 -> DB 먼저 업데이트
            String oldObjectKey = currentProfile.getObjectKey(); // 삭제할 키 미리 백업
            String extension = FileUtils.extractExtension(file.getOriginalFilename());

            currentProfile.update(uploadDto.objectKey(), file.getOriginalFilename(), extension);

            imageId = currentProfile.getId();

            // 성공적으로 수정이 되었을 때만 minio에서 기존 프로필을 delete
            if (TransactionSynchronizationManager.isSynchronizationActive()) {
                TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
                    @Override
                    public void afterCommit() {
                        manager.delete(oldObjectKey);
                    }
                });
            }
        } else {
            // [DB] 기존 프로필이 없으면 그냥 저장
            MemberProfileImage savedImage =
                    profileImageWriter.save(member, uploadDto.objectKey(), file.getOriginalFilename());

            imageId = savedImage.getId();

            member.setProfileImage(savedImage);
        }

        return new ImageUploadRes(imageId, uploadDto.imageUrl());
    }

    /**
     * 채팅방 내 이미지를 업로드합니다.
     * <p>
     * {@link ImageType#CHAT_IMAGE} 타입에 맞는 Manager를 찾아 다음을 수행합니다:
     * <ol>
     *     <li>채팅방 ID, 사용자 ID, 파일명을 기반으로 Object Key를 생성합니다.</li>
     *     <li>스토리지에 파일을 업로드합니다.</li>
     *     <li>이미지 정보를 DB에 저장합니다.</li>
     * </ol>
     *
     * @param file 업로드할 이미지 파일
     * @param chatRoomId 채팅방 ID
     * @param userId Presigned URL을 요청한 사용자의 ID
     * @return 생성된 이미지 ID와 접근 URL이 포함된 응답 DTO
     */
    @Override
    @Transactional
    public ImageUploadRes uploadChatImage(MultipartFile file, String chatRoomId, Long userId) {
        AbstractImageManager manager = imageManagerFactory.getManager(ImageType.CHAT_IMAGE);

        ImageUploadDto uploadDto = manager.uploadImage(file, userId, chatRoomId);

        ChatRoom chatRoom = chatRoomReader.getById(chatRoomId);

        Long imageId = chatImageWriter.save(chatRoom, uploadDto.objectKey(), file.getOriginalFilename());

        return new ImageUploadRes(imageId, uploadDto.imageUrl());
    }
}
