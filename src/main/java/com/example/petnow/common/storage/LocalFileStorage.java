package com.example.petnow.common.storage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.petnow.common.config.LocalStorageProperties;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ImageErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * 로컬 개발용 이미지 저장소. 설정한 디렉터리에 그대로 쓴다.
 *
 * <p>있는 이유는 하나다. AWS 자격증명이 없는 팀원도 업로드가 걸린 화면을 끝까지 만들고 확인할 수 있어야 한다.
 * 저장 키 규칙과 반환하는 URL 의 모양은 {@link S3FileStorage} 와 같아서, 프로필만 바꿔도 도메인 코드는 그대로 돈다.
 *
 * <p>저장된 파일은 {@code WebConfig} 가 매핑한 정적 경로로 열린다.
 */
@Slf4j
@Profile("local")
@Component
public class LocalFileStorage extends AbstractFileStorage {

    private final Path baseDirectory;
    private final String urlPrefix;

    public LocalFileStorage(ImageValidator imageValidator, LocalStorageProperties localStorageProperties) {
        super(imageValidator);
        this.baseDirectory = localStorageProperties.resolveDirectory();
        this.urlPrefix = StringUtils.trimTrailingCharacter(localStorageProperties.getUrlPrefix(), '/');
    }

    @Override
    protected String store(MultipartFile file, ImageType imageType, String key) {
        Path target = baseDirectory.resolve(key).normalize();

        try {
            Files.createDirectories(target.getParent());
            file.transferTo(target);
        } catch (IOException e) {
            log.error("로컬 저장 실패: path={}", target, e);
            throw new BusinessException(ImageErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return urlPrefix + "/" + key;
    }

    @Override
    protected void deleteStoredImage(String imageUrl) {
        String key = toKey(imageUrl);
        if (key == null) {
            log.warn("로컬 저장소의 URL 이 아니라 삭제를 건너뛴다: {}", imageUrl);
            return;
        }

        Path target = baseDirectory.resolve(key).normalize();
        if (!target.startsWith(baseDirectory)) {
            log.warn("저장 디렉터리 밖을 가리켜 삭제를 건너뛴다: {}", imageUrl);
            return;
        }

        try {
            Files.deleteIfExists(target);
        } catch (IOException e) {
            log.error("로컬 삭제 실패: path={}", target, e);
        }
    }

    private String toKey(String imageUrl) {
        String prefix = urlPrefix + "/";
        if (!imageUrl.startsWith(prefix)) {
            return null;
        }

        String key = imageUrl.substring(prefix.length());
        return StringUtils.hasText(key) ? key : null;
    }
}
