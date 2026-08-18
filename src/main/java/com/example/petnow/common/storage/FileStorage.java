package com.example.petnow.common.storage;

import java.util.List;
import java.util.Objects;

import org.springframework.web.multipart.MultipartFile;

/**
 * 이미지 저장소. 도메인은 이 인터페이스만 알면 되고 S3 인지 로컬 디스크인지는 몰라도 된다.
 *
 * <p>구현체는 실행 프로필이 고른다. 운영은 {@link S3FileStorage}, 로컬은 {@link LocalFileStorage} 다.
 * 덕분에 AWS 자격증명이 없는 팀원도 로컬에서 업로드 기능을 그대로 개발하고 테스트할 수 있다.
 *
 * <p>돌려주는 값은 DB {@code image_url} 컬럼에 그대로 넣는 <b>전체 URL</b> 이다.
 *
 * <pre>{@code
 * @Transactional
 * public void createPet(Long userId, PetCreateRequest request) {
 *     petMapper.insertPet(pet);
 *
 *     MultipartFile image = request.getImage();
 *     if (image != null && !image.isEmpty()) {
 *         String imageUrl = fileStorage.uploadImage(image, ImageCategory.PET);
 *         petPhotoMapper.insertPhoto(...);
 *     }
 * }
 * }</pre>
 */
public interface FileStorage {

    /**
     * 이미지 한 장을 올리고 저장된 URL 을 돌려준다.
     *
     * <p>형식 검사는 구현체가 아니라 이 메서드 안에서 이미 끝난다. 도메인이 따로 검사할 필요가 없다.
     *
     * @throws com.example.petnow.exception.BusinessException 허용하지 않는 형식이거나 저장에 실패한 경우
     */
    String uploadImage(MultipartFile file, ImageCategory category);

    /**
     * 저장된 이미지를 지운다. 트랜잭션 안에서 부르면 실제 삭제는 커밋 이후로 미뤄진다.
     *
     * <p>URL 이 비어 있거나 우리 저장소의 것이 아니면 아무 일도 하지 않는다.
     */
    void deleteImage(String imageUrl);

    /**
     * 여러 장을 한 번에 올린다. 도메인별 최대 장수({@link ImageCategory#getMaxCount()})를 먼저 검사한다.
     *
     * <p>비어 있는 파트는 세지 않는다. 브라우저는 파일을 고르지 않은 {@code <input type="file">} 도
     * 빈 파트로 함께 보내므로, 그것까지 세면 실제로 올린 장수보다 많게 나온다.
     */
    default List<String> uploadImages(List<MultipartFile> files, ImageCategory category) {
        if (files == null || files.isEmpty()) {
            return List.of();
        }

        List<MultipartFile> uploadTargets = files.stream()
                .filter(Objects::nonNull)
                .filter(file -> !file.isEmpty())
                .toList();

        category.validateCount(uploadTargets.size());

        return uploadTargets.stream()
                .map(file -> uploadImage(file, category))
                .toList();
    }
}
