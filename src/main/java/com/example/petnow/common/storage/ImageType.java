package com.example.petnow.common.storage;

import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Optional;

import lombok.RequiredArgsConstructor;

/**
 * 업로드를 허용하는 이미지 형식의 화이트리스트.
 *
 * <p>확장자 · Content-Type · 파일 앞머리(매직바이트)를 형식마다 한 줄로 묶어 둔다.
 * 허용 형식을 늘리거나 줄이는 변경은 이 파일에서 끝난다.
 *
 * <p>여기에 없는 형식은 전부 거부된다. 특히 두 가지를 의도적으로 뺐다.
 * <ul>
 *   <li>SVG — XML 이라 {@code <script>} 를 품을 수 있고, 우리 도메인에서 그대로 열리면 저장형 XSS 가 된다</li>
 *   <li>GIF — 서비스에서 쓸 일이 없는데 애니메이션이라 용량만 크다</li>
 * </ul>
 */
@RequiredArgsConstructor
public enum ImageType {

    JPEG(List.of("jpg", "jpeg"), List.of("image/jpeg", "image/jpg")) {
        @Override
        boolean matchesHeader(byte[] header) {
            return matchesAt(header, 0, 0xFF, 0xD8, 0xFF);
        }
    },

    PNG(List.of("png"), List.of("image/png")) {
        @Override
        boolean matchesHeader(byte[] header) {
            return matchesAt(header, 0, 0x89, 0x50, 0x4E, 0x47, 0x0D, 0x0A, 0x1A, 0x0A);
        }
    },

    WEBP(List.of("webp"), List.of("image/webp")) {
        /**
         * WebP 는 RIFF 컨테이너라 앞 4바이트가 "RIFF", 8~11바이트가 "WEBP" 다.
         * 가운데 4바이트는 파일 크기라 값이 매번 다르므로 건너뛴다.
         */
        @Override
        boolean matchesHeader(byte[] header) {
            return matchesAt(header, 0, 0x52, 0x49, 0x46, 0x46)
                    && matchesAt(header, 8, 0x57, 0x45, 0x42, 0x50);
        }
    };

    /**
     * 매직바이트 검사에 필요한 최소 길이. WebP 의 "WEBP" 표식이 11번째 바이트에서 끝난다.
     */
    public static final int HEADER_LENGTH = 12;

    private final List<String> extensions;
    private final List<String> contentTypes;

    /**
     * 파일명 확장자로 형식을 찾는다. 확장자가 없거나 화이트리스트에 없으면 비어 있는 값을 돌려준다.
     */
    public static Optional<ImageType> fromFilename(String filename) {
        if (filename == null) {
            return Optional.empty();
        }

        int dotIndex = filename.lastIndexOf('.');
        if (dotIndex < 0 || dotIndex == filename.length() - 1) {
            return Optional.empty();
        }

        String extension = filename.substring(dotIndex + 1).toLowerCase(Locale.ROOT);
        return Arrays.stream(values())
                .filter(imageType -> imageType.extensions.contains(extension))
                .findFirst();
    }

    /**
     * S3 에 강제로 지정하고 저장 키에 붙일 대표 확장자.
     */
    public String getExtension() {
        return extensions.get(0);
    }

    /**
     * 저장할 때 강제 지정할 Content-Type. 브라우저가 보낸 값을 그대로 쓰지 않는다.
     */
    public String getContentType() {
        return contentTypes.get(0);
    }

    /**
     * 브라우저가 보낸 Content-Type 이 이 형식과 맞는지 본다.
     *
     * <p>{@code image/jpeg; charset=...} 처럼 파라미터가 붙어 올 수 있어 세미콜론 앞만 비교한다.
     */
    boolean matchesDeclaredContentType(String declaredContentType) {
        if (declaredContentType == null) {
            return false;
        }

        String mimeType = declaredContentType.split(";")[0].trim().toLowerCase(Locale.ROOT);
        return contentTypes.contains(mimeType);
    }

    /**
     * 파일 앞머리가 이 형식의 실제 바이트와 일치하는지 본다.
     */
    abstract boolean matchesHeader(byte[] header);

    private static boolean matchesAt(byte[] header, int offset, int... expected) {
        if (header.length < offset + expected.length) {
            return false;
        }

        for (int i = 0; i < expected.length; i++) {
            if ((header[offset + i] & 0xFF) != expected[i]) {
                return false;
            }
        }
        return true;
    }
}
