package com.studypals.global.file.service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import com.studypals.domain.chatManage.entity.ChatRoom;
import com.studypals.domain.chatManage.worker.ChatImageManager;
import com.studypals.domain.chatManage.worker.ChatImageWriter;
import com.studypals.domain.chatManage.worker.ChatRoomReader;
import com.studypals.domain.memberManage.entity.Member;
import com.studypals.domain.memberManage.entity.MemberProfileImage;
import com.studypals.domain.memberManage.worker.MemberProfileImageManager;
import com.studypals.domain.memberManage.worker.MemberProfileImageWriter;
import com.studypals.domain.memberManage.worker.MemberReader;
import com.studypals.global.exceptions.errorCode.FileErrorCode;
import com.studypals.global.exceptions.exception.FileException;
import com.studypals.global.file.FileType;
import com.studypals.global.file.FileUtils;
import com.studypals.global.file.dao.AbstractFileManager;
import com.studypals.global.file.dao.AbstractImageManager;
import com.studypals.global.file.dto.ImageUploadDto;
import com.studypals.global.file.dto.ImageUploadRes;
import com.studypals.global.file.entity.ImageType;

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
public class ImageFileServiceImpl implements ImageFileService {

    private final Map<FileType, AbstractImageManager> managerMap;
    private final MemberReader memberReader;
    private final ChatRoomReader chatRoomReader;
    private final MemberProfileImageWriter profileImageWriter;
    private final ChatImageWriter chatImageWriter;

    /**
     * 의존성 주입을 위한 생성자입니다.
     * <p>
     * Spring 컨텍스트에 등록된 모든 {@link AbstractImageManager} 타입의 빈을 리스트로 주입받아,
     * 각 Manager가 처리하는 {@link FileType}을 키로 하는 맵을 구성합니다.
     * 만약 서로 다른 Manager가 동일한 FileType을 처리하려고 할 경우, 애플리케이션 구동 시점에
     * {@link IllegalStateException}을 발생시켜 설정 오류를 방지합니다.
     *
     * <p>
     * managerMap 안에는 아래와 같이 AbstractFileManager을 상속한 스프링 빈이 저장됩니다.
     * <pre>
     * {
     *   ImageType.PROFILE_IMAGE : (MemberProfileImageManager 인스턴스),
     *   ImageType.CHAT_IMAGE    : (ChatImageManager 인스턴스)
     * }
     * </pre>
     * 이를 통해 이후의 서비스 메서드에서는 파일 타입만으로 적절한 Manager를 즉시 찾아 사용할 수 있습니다.
     *
     * @param managers Spring 컨텍스트에 의해 주입되는 {@code AbstractImageManager}의 모든 구현체 리스트
     */
    public ImageFileServiceImpl(
            List<AbstractImageManager> managers,
            MemberReader memberReader,
            ChatRoomReader chatRoomReader,
            MemberProfileImageWriter profileImageWriter,
            ChatImageWriter chatImageWriter) {
        this.managerMap = managers.stream()
                .collect(Collectors.toMap(
                        AbstractFileManager::getFileType, Function.identity(), (existing, duplicate) -> {
                            throw new IllegalStateException(String.format(
                                    "FileType 중복 등록 오류. '%s' 타입이 '%s'와 '%s' 클래스에서 중복으로 처리됩니다.",
                                    existing.getFileType(),
                                    existing.getClass().getName(),
                                    duplicate.getClass().getName()));
                        }));
        this.memberReader = memberReader;
        this.chatRoomReader = chatRoomReader;
        this.profileImageWriter = profileImageWriter;
        this.chatImageWriter = chatImageWriter;
    }

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
    public ImageUploadRes uploadProfileImage(MultipartFile file, Long userId) {
        MemberProfileImageManager manager = getManager(ImageType.PROFILE_IMAGE, MemberProfileImageManager.class);

        // 1. [MinIO] 프로필 사진 업로드, 업로드가 성공하면 file 정보는 전부 유효합니다.
        ImageUploadDto uploadDto = manager.upload(file, userId);

        // DB 업데이트를 위한 준비
        Member member = memberReader.get(userId);
        String extension = FileUtils.extractExtension(file.getOriginalFilename());
        MemberProfileImage currentProfile = member.getProfileImage();
        Long imageId;

        if (currentProfile != null) {
            // 2. [DB] 기존 정보가 있으면 -> DB 먼저 업데이트
            String oldObjectKey = currentProfile.getObjectKey(); // 삭제할 키 미리 백업

            currentProfile.update(uploadDto.objectKey(), file.getOriginalFilename(), extension);
            imageId = profileImageWriter.saveEntity(currentProfile); // 더티 체킹을 하지 않으므로 명시적 저장

            // 3. [MinIO] 모든 처리가 끝난 후 -> 기존 파일 삭제
            manager.delete(oldObjectKey);
        } else {
            // [DB] 기존 프로필이 없으면 그냥 저장
            imageId = profileImageWriter.save(member, uploadDto.objectKey(), file.getOriginalFilename());
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
    public ImageUploadRes uploadChatImage(MultipartFile file, String chatRoomId, Long userId) {
        ChatImageManager manager = getManager(ImageType.CHAT_IMAGE, ChatImageManager.class);

        ImageUploadDto uploadDto = manager.upload(file, userId, chatRoomId);

        ChatRoom chatRoom = chatRoomReader.getById(chatRoomId);

        Long imageId = chatImageWriter.save(chatRoom, uploadDto.objectKey(), file.getOriginalFilename());

        return new ImageUploadRes(imageId, uploadDto.imageUrl());
    }

    /**
     * 지정된 {@link FileType}에 해당하는 {@link AbstractImageManager}의 구현체를 타입 안전하게 조회합니다.
     *
     * @param fileType 조회할 파일 타입 (예: {@code ImageType.PROFILE_IMAGE})
     * @param managerClass 반환받고자 하는 Manager의 클래스 타입
     * @param <T> {@code AbstractImageManager}를 상속하는 특정 Manager 타입
     * @return 요청된 타입의 Manager 인스턴스
     * @throws FileException 해당 {@code fileType}을 처리하는 Manager가 등록되어 있지 않을 경우 발생
     * @throws IllegalStateException 조회된 Manager가 요청된 {@code managerClass} 타입과 일치하지 않을 경우 발생.
     *                               이는 심각한 설정 오류를 의미합니다.
     */
    private <T extends AbstractImageManager> T getManager(FileType fileType, Class<T> managerClass) {
        AbstractImageManager manager = managerMap.get(fileType);
        if (manager == null) {
            throw new FileException(FileErrorCode.UNSUPPORTED_FILE_IMAGE_TYPE);
        }
        if (!managerClass.isInstance(manager)) {
            // 잘못된 타입의 Manager가 매핑된 경우, 이는 심각한 설정 오류입니다.
            throw new IllegalStateException(String.format(
                    "요청된 FileType '%s'에 대한 Manager 타입이 일치하지 않습니다. 기대값: %s, 실제값: %s",
                    fileType, managerClass.getName(), manager.getClass().getName()));
        }
        return managerClass.cast(manager);
    }
}
