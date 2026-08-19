package com.example.petnow.common.storage;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;
import org.springframework.web.multipart.MultipartFile;

import com.example.petnow.common.config.LocalStorageProperties;
import com.example.petnow.exception.BusinessException;
import com.example.petnow.exception.ImageErrorCode;

/**
 * 저장소 구현과 무관하게 지켜야 하는 규칙을 로컬 구현으로 검증한다.
 *
 * <p>키 생성과 삭제 지연은 {@link AbstractFileStorage} 에 있으므로 S3 구현에도 그대로 적용된다.
 */
class LocalFileStorageTest {

    private static final String URL_PREFIX = "/uploads";

    @TempDir
    Path tempDir;

    private LocalFileStorage fileStorage;

    @BeforeEach
    void setUp() {
        LocalStorageProperties properties = new LocalStorageProperties();
        properties.setDirectory(tempDir.toString());
        properties.setUrlPrefix(URL_PREFIX);

        fileStorage = new LocalFileStorage(new ImageValidator(), properties);
    }

    @AfterEach
    void tearDown() {
        if (TransactionSynchronizationManager.isSynchronizationActive()) {
            TransactionSynchronizationManager.clearSynchronization();
        }
    }

    private static MultipartFile jpegFile(String filename) {
        byte[] jpeg = {(byte) 0xFF, (byte) 0xD8, (byte) 0xFF, (byte) 0xE0, 0x00, 0x10, 0x4A, 0x46, 0x49, 0x46, 0x00, 0x01};
        return new MockMultipartFile("image", filename, "image/jpeg", jpeg);
    }

    @Test
    @DisplayName("저장 키는 {prefix}/{yyyy}/{MM}/{UUID}.{ext} 이고 원본 파일명이 들어가지 않는다")
    void uploadImage_usesGeneratedKey() {
        String imageUrl = fileStorage.uploadImage(jpegFile("우리집 강아지.jpg"), ImageCategory.PET);

        String yearMonth = LocalDate.now().format(DateTimeFormatter.ofPattern("yyyy/MM"));
        assertThat(imageUrl)
                .startsWith(URL_PREFIX + "/pets/" + yearMonth + "/")
                .endsWith(".jpg")
                .doesNotContain("우리집");
        assertThat(imageUrl).matches(URL_PREFIX + "/pets/\\d{4}/\\d{2}/[0-9a-f-]{36}\\.jpg");
    }

    @Test
    @DisplayName("올린 파일이 실제로 디렉터리에 저장된다")
    void uploadImage_writesFile() {
        String imageUrl = fileStorage.uploadImage(jpegFile("cat.jpg"), ImageCategory.PLACE);

        assertThat(Files.exists(pathOf(imageUrl))).isTrue();
    }

    @Test
    @DisplayName("도메인별 최대 장수를 넘기면 한 장도 올리지 않는다")
    void uploadImages_rejectsWhenCountExceeded() {
        List<MultipartFile> files = List.of(jpegFile("a.jpg"), jpegFile("b.jpg"));

        assertThatThrownBy(() -> fileStorage.uploadImages(files, ImageCategory.PET))
                .isInstanceOf(BusinessException.class)
                .extracting(e -> ((BusinessException) e).getErrorCode())
                .isEqualTo(ImageErrorCode.IMAGE_COUNT_EXCEEDED);
    }

    @Test
    @DisplayName("파일을 고르지 않아 비어 있는 파트는 장수에 세지 않는다")
    void uploadImages_ignoresEmptyParts() {
        List<MultipartFile> files = List.of(
                jpegFile("a.jpg"),
                new MockMultipartFile("image", "", "application/octet-stream", new byte[0]));

        assertThat(fileStorage.uploadImages(files, ImageCategory.PET)).hasSize(1);
    }

    @Test
    @DisplayName("트랜잭션이 없으면 즉시 지운다")
    void deleteImage_deletesImmediatelyWithoutTransaction() {
        String imageUrl = fileStorage.uploadImage(jpegFile("cat.jpg"), ImageCategory.USER);

        fileStorage.deleteImage(imageUrl);

        assertThat(Files.exists(pathOf(imageUrl))).isFalse();
    }

    @Test
    @DisplayName("트랜잭션 안에서는 커밋 이후에야 실제로 지운다")
    void deleteImage_isDeferredUntilCommit() {
        String imageUrl = fileStorage.uploadImage(jpegFile("cat.jpg"), ImageCategory.USER);
        Path storedPath = pathOf(imageUrl);

        TransactionSynchronizationManager.initSynchronization();
        fileStorage.deleteImage(imageUrl);

        assertThat(Files.exists(storedPath))
                .as("커밋 전에 지워지면 롤백된 뒤에도 파일이 돌아오지 않는다")
                .isTrue();

        TransactionSynchronizationManager.getSynchronizations().forEach(TransactionSynchronization::afterCommit);

        assertThat(Files.exists(storedPath)).isFalse();
    }

    @Test
    @DisplayName("롤백되면 파일은 그대로 남는다")
    void deleteImage_keepsFileWhenRolledBack() {
        String imageUrl = fileStorage.uploadImage(jpegFile("cat.jpg"), ImageCategory.USER);

        TransactionSynchronizationManager.initSynchronization();
        fileStorage.deleteImage(imageUrl);
        TransactionSynchronizationManager.clearSynchronization();

        assertThat(Files.exists(pathOf(imageUrl))).isTrue();
    }

    @Test
    @DisplayName("우리 저장소의 URL 이 아니면 아무 일도 하지 않는다")
    void deleteImage_ignoresForeignUrl() throws IOException {
        Path outside = tempDir.resolve("outside.jpg");
        Files.writeString(outside, "지워지면 안 된다");

        fileStorage.deleteImage("https://cdn.example.com/pets/2026/08/other.jpg");
        fileStorage.deleteImage(null);
        fileStorage.deleteImage("  ");

        assertThat(Files.exists(outside)).isTrue();
    }

    private Path pathOf(String imageUrl) {
        return tempDir.resolve(imageUrl.substring(URL_PREFIX.length() + 1));
    }
}
