package com.example.petnow.common.storage;

import java.io.IOException;
import java.io.InputStream;

import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ImageErrorCode;

import lombok.extern.slf4j.Slf4j;

/**
 * 올라온 파일이 정말 우리가 허용한 이미지인지 검사한다.
 *
 * <p>검사는 세 단계이고 하나라도 어긋나면 거부한다.
 * <ol>
 *   <li>확장자가 화이트리스트({@link ImageType})에 있는가</li>
 *   <li>브라우저가 보낸 Content-Type 이 그 확장자와 맞는가</li>
 *   <li>파일 앞머리 바이트가 그 형식의 실제 시그니처와 맞는가</li>
 * </ol>
 *
 * <p>확장자와 Content-Type 은 둘 다 보내는 쪽이 마음대로 정할 수 있는 값이다.
 * {@code payload.jsp} 를 {@code cat.png} 로 바꾸고 Content-Type 을 {@code image/png} 로 위조하면
 * 앞 두 단계는 그냥 통과한다. 그래서 파일 내용 자체를 보는 매직바이트 검사가 마지막에 있어야 한다.
 */
@Slf4j
@Component
public class ImageValidator {

    /**
     * 형식을 확정하고 돌려준다. 저장 시 강제 지정할 Content-Type 과 확장자는 여기서 나온 값을 쓴다.
     */
    public ImageType validate(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ImageErrorCode.IMAGE_UPLOAD_FAILED, "업로드할 이미지가 비어 있습니다");
        }

        ImageType imageType = ImageType.fromFilename(file.getOriginalFilename())
                .orElseThrow(() -> new BusinessException(ImageErrorCode.IMAGE_TYPE_NOT_ALLOWED));

        if (!imageType.matchesDeclaredContentType(file.getContentType())) {
            log.warn("Content-Type 이 확장자와 맞지 않는다: filename={} contentType={}",
                    file.getOriginalFilename(), file.getContentType());
            throw new BusinessException(ImageErrorCode.IMAGE_TYPE_NOT_ALLOWED);
        }

        if (!imageType.matchesHeader(readHeader(file))) {
            log.warn("파일 내용이 {} 형식이 아니다: filename={}", imageType, file.getOriginalFilename());
            throw new BusinessException(ImageErrorCode.IMAGE_TYPE_NOT_ALLOWED);
        }

        return imageType;
    }

    private byte[] readHeader(MultipartFile file) {
        try (InputStream inputStream = file.getInputStream()) {
            return inputStream.readNBytes(ImageType.HEADER_LENGTH);
        } catch (IOException e) {
            log.error("업로드 파일을 읽지 못했다: filename={}", file.getOriginalFilename(), e);
            throw new BusinessException(ImageErrorCode.IMAGE_UPLOAD_FAILED);
        }
    }
}
