package com.example.petnow.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.nio.charset.StandardCharsets;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.mock.web.MockMultipartFile;

import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ImageErrorCode;

/**
 * 업로드 검증이 확장자 · Content-Type · 파일 내용을 모두 보는지 확인한다.
 *
 * <p>가장 중요한 회귀는 "확장자와 Content-Type 만 맞으면 통과하는 것"이다. 둘 다 보내는 쪽이
 * 마음대로 정하는 값이라, 그것만 믿으면 이미지가 아닌 파일이 버킷에 그대로 올라간다.
 */
class ImageValidatorTest {

    private final ImageValidator imageValidator = new ImageValidator();

    private static byte[] jpegBytes() {
        return new byte[] {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01};
    }

    private static byte[] pngBytes() {
        return new byte[] {(byte) 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A, 0x00, 0x00, 0x00, 0x0D};
    }

    private static byte[] webpBytes() {
        return new byte[] {0x52, 0x49, 0x46, 0x46, 0x24, 0x00, 0x00, 0x00, 0x57, 0x45, 0x42, 0x50};
    }

    @Test
    @DisplayName("확장자 · Content-Type · 매직바이트가 모두 맞으면 형식을 돌려준다")
    void validate_returnsImageType() {
        MockMultipartFile file = new MockMultipartFile("image", "cat.jpg", "image/jpeg", jpegBytes());

        assertThat(imageValidator.validate(file)).isEqualTo(ImageType.JPEG);
    }

    @Test
    @DisplayName("png · webp 도 통과한다")
    void validate_allowsPngAndWebp() {
        MockMultipartFile png = new MockMultipartFile("image", "cat.png", "image/png", pngBytes());
        MockMultipartFile webp = new MockMultipartFile("image", "cat.webp", "image/webp", webpBytes());

        assertThat(imageValidator.validate(png)).isEqualTo(ImageType.PNG);
        assertThat(imageValidator.validate(webp)).isEqualTo(ImageType.WEBP);
    }

    @Test
    @DisplayName("확장자 대소문자는 가리지 않는다")
    void validate_ignoresExtensionCase() {
        MockMultipartFile file = new MockMultipartFile("image", "CAT.JPEG", "image/jpeg", jpegBytes());

        assertThat(imageValidator.validate(file)).isEqualTo(ImageType.JPEG);
    }

    @Test
    @DisplayName("Content-Type 을 위조해도 파일 내용이 이미지가 아니면 거부한다")
    void validate_rejectsForgedContentType() {
        byte[] script = "<?php system($_GET['c']); ?>".getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("image", "cat.png", "image/png", script);

        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ImageErrorCode.IMAGE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("확장자와 Content-Type 이 서로 다르면 거부한다")
    void validate_rejectsMismatchedContentType() {
        MockMultipartFile file = new MockMultipartFile("image", "cat.png", "image/jpeg", pngBytes());

        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ImageErrorCode.IMAGE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("SVG 는 스크립트를 품을 수 있으므로 거부한다")
    void validate_rejectsSvg() {
        byte[] svg = "<svg xmlns=\"http://www.w3.org/2000/svg\"><script>alert(1)</script></svg>"
                .getBytes(StandardCharsets.UTF_8);
        MockMultipartFile file = new MockMultipartFile("image", "cat.svg", "image/svg+xml", svg);

        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ImageErrorCode.IMAGE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("GIF 는 허용 목록에 없으므로 거부한다")
    void validate_rejectsGif() {
        byte[] gif = new byte[] {0x47, 0x49, 0x46, 0x38, 0x39, 0x61, 0x01, 0x00, 0x01, 0x00, 0x00, 0x00};
        MockMultipartFile file = new MockMultipartFile("image", "cat.gif", "image/gif", gif);

        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ImageErrorCode.IMAGE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("확장자가 없으면 거부한다")
    void validate_rejectsFilenameWithoutExtension() {
        MockMultipartFile file = new MockMultipartFile("image", "cat", "image/jpeg", jpegBytes());

        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ImageErrorCode.IMAGE_TYPE_NOT_ALLOWED);
    }

    @Test
    @DisplayName("빈 파일은 업로드 대상이 아니다")
    void validate_rejectsEmptyFile() {
        MockMultipartFile file = new MockMultipartFile("image", "cat.jpg", "image/jpeg", new byte[0]);

        assertThatThrownBy(() -> imageValidator.validate(file))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ImageErrorCode.IMAGE_UPLOAD_FAILED);
    }
}
