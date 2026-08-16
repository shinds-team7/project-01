package com.example.petnow.common.storage;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

import com.example.petnow.common.config.S3Properties;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ImageErrorCode;

import lombok.extern.slf4j.Slf4j;
import software.amazon.awssdk.core.exception.SdkException;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;

/**
 * 운영용 이미지 저장소. S3 에 올린다.
 *
 * <p>URL 을 만드는 곳과 URL 에서 키를 뽑는 곳이 이 클래스 안에만 있다. 나중에 CloudFront 도메인으로
 * 바꾸거나 버킷을 옮길 때 고칠 곳이 여기 하나로 끝난다. 도메인 코드에는 URL 규칙이 새어 나가지 않는다.
 */
@Slf4j
@Profile("prod")
@Component
public class S3FileStorage extends AbstractFileStorage {

    private final S3Client s3Client;
    private final String bucket;
    private final String baseUrl;

    public S3FileStorage(ImageValidator imageValidator, S3Client s3Client, S3Properties s3Properties) {
        super(imageValidator);

        Assert.hasText(s3Properties.getBucket(), "cloud.aws.s3.bucket 이 비어 있다. 환경변수 AWS_S3_BUCKET 을 확인할 것");
        Assert.hasText(s3Properties.getRegion(), "cloud.aws.s3.region 이 비어 있다. 환경변수 AWS_S3_REGION 을 확인할 것");

        this.s3Client = s3Client;
        this.bucket = s3Properties.getBucket();
        this.baseUrl = resolveBaseUrl(s3Properties);
    }

    /**
     * Content-Type 은 브라우저가 보낸 값이 아니라 화이트리스트에서 확정한 값을 넣는다.
     *
     * <p>보낸 값을 그대로 넣으면 {@code text/html} 로 저장된 파일이 버킷 도메인에서 HTML 로 실행된다.
     */
    @Override
    protected String store(MultipartFile file, ImageType imageType, String key) {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(imageType.getContentType())
                .contentLength(file.getSize())
                .build();

        try (InputStream inputStream = file.getInputStream()) {
            s3Client.putObject(request, RequestBody.fromInputStream(inputStream, file.getSize()));
        } catch (IOException | SdkException e) {
            log.error("S3 업로드 실패: bucket={} key={}", bucket, key, e);
            throw new BusinessException(ImageErrorCode.IMAGE_UPLOAD_FAILED);
        }

        return toUrl(key);
    }

    @Override
    protected void deleteStoredImage(String imageUrl) {
        String key = toKey(imageUrl);
        if (key == null) {
            log.warn("우리 버킷의 URL 이 아니라 삭제를 건너뛴다: {}", imageUrl);
            return;
        }

        try {
            s3Client.deleteObject(DeleteObjectRequest.builder()
                    .bucket(bucket)
                    .key(key)
                    .build());
        } catch (SdkException e) {
            log.error("S3 삭제 실패: bucket={} key={}", bucket, key, e);
        }
    }

    private String toUrl(String key) {
        return baseUrl + "/" + key;
    }

    /**
     * DB 에 저장된 전체 URL 에서 S3 키를 뽑는다. 우리 버킷의 URL 이 아니면 {@code null} 을 돌려준다.
     */
    private String toKey(String imageUrl) {
        String prefix = baseUrl + "/";
        if (!imageUrl.startsWith(prefix)) {
            return null;
        }

        String key = imageUrl.substring(prefix.length());
        return StringUtils.hasText(key) ? key : null;
    }

    private static String resolveBaseUrl(S3Properties s3Properties) {
        if (!StringUtils.hasText(s3Properties.getBaseUrl())) {
            return "https://%s.s3.%s.amazonaws.com".formatted(s3Properties.getBucket(), s3Properties.getRegion());
        }

        return StringUtils.trimTrailingCharacter(s3Properties.getBaseUrl().trim(), '/');
    }
}
