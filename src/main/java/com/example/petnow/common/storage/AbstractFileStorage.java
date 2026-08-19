package com.example.petnow.common.storage;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

/**
 * 저장 위치와 무관하게 똑같이 흘러가는 부분을 모아 둔 상위 클래스.
 *
 * <p>구현체가 새로 생겨도 아래 세 가지는 다시 짜지 않는다.
 * <ol>
 *   <li>업로드 전 형식 검사</li>
 *   <li>저장 키 생성 규칙</li>
 *   <li>삭제를 커밋 이후로 미루는 처리</li>
 * </ol>
 */
public abstract class AbstractFileStorage implements FileStorage {

    private static final DateTimeFormatter KEY_DATE_FORMAT = DateTimeFormatter.ofPattern("yyyy/MM");

    private final ImageValidator imageValidator;

    protected AbstractFileStorage(ImageValidator imageValidator) {
        this.imageValidator = imageValidator;
    }

    @Override
    public final String uploadImage(MultipartFile file, ImageCategory category) {
        ImageType imageType = imageValidator.validate(file);
        return store(file, imageType, createKey(category, imageType));
    }

    /**
     * 트랜잭션이 열려 있으면 실제 삭제를 커밋 이후로 미룬다.
     *
     * <p>미루지 않으면 DB 는 롤백됐는데 파일만 사라지는 상황이 생긴다. 행은 그대로 남아 있는데
     * 그 행이 가리키는 이미지가 없으니 화면이 깨지고, 되돌릴 방법도 없다.
     * 반대 순서(파일이 남고 DB 만 롤백)로 생기는 고아 객체는 S3 라이프사이클이 정리한다.
     *
     * <p>도메인마다 이 처리를 각자 짜면 한 곳만 빠뜨려도 같은 사고가 나므로 여기서 한 번에 처리한다.
     */
    @Override
    public final void deleteImage(String imageUrl) {
        if (!StringUtils.hasText(imageUrl)) {
            return;
        }

        if (!TransactionSynchronizationManager.isSynchronizationActive()) {
            deleteStoredImage(imageUrl);
            return;
        }

        TransactionSynchronizationManager.registerSynchronization(new TransactionSynchronization() {
            @Override
            public void afterCommit() {
                deleteStoredImage(imageUrl);
            }
        });
    }

    /**
     * 검사를 통과한 파일을 실제 저장소에 쓰고, DB 에 넣을 전체 URL 을 돌려준다.
     */
    protected abstract String store(MultipartFile file, ImageType imageType, String key);

    /**
     * 실제 저장소에서 지운다. 커밋 이후에 불리므로 여기서 예외를 던져 봐야 되돌릴 것이 없다.
     * 실패는 로그만 남기고 넘어간다.
     */
    protected abstract void deleteStoredImage(String imageUrl);

    /**
     * 저장 키는 {@code {prefix}/{yyyy}/{MM}/{UUID}.{ext}} 다.
     *
     * <p>원본 파일명을 키에 넣지 않는 이유는 두 가지다. 한글·공백·경로 문자가 섞여 들어와
     * 저장소마다 다르게 깨지고, 파일명 자체가 업로더에 대한 정보를 그대로 노출한다.
     * 연/월로 나누는 것은 나중에 라이프사이클 규칙이나 수동 정리를 기간 단위로 걸기 위해서다.
     */
    private String createKey(ImageCategory category, ImageType imageType) {
        return "%s/%s/%s.%s".formatted(
                category.getPrefix(),
                LocalDate.now().format(KEY_DATE_FORMAT),
                UUID.randomUUID(),
                imageType.getExtension());
    }
}
