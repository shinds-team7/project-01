package com.example.petnow.support;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import org.springframework.test.web.servlet.ResultHandler;

/**
 * 렌더링된 화면을 파일로 내려받아 브라우저에서 눈으로 확인할 때 쓰는 보조 도구.
 *
 * <p>평소에는 아무 일도 하지 않는다. 환경 변수 {@code RENDER_DUMP} 에 디렉터리를 지정하고
 * 테스트를 돌리면 그 아래에 {@code <name>.html} 로 응답 본문을 저장한다.
 *
 * <pre>
 * RENDER_DUMP=/tmp/preview ./gradlew test --tests "*HomeControllerTest"
 * </pre>
 *
 * <p>화면 디자인을 프로토타입과 맞추는 동안 DB 없이 레이아웃을 확인하려고 두었다.
 */
public final class RenderDump {

    private static final String ENV = "RENDER_DUMP";

    private RenderDump() {
    }

    public static ResultHandler to(String name) {
        return result -> {
            String dir = System.getenv(ENV);
            if (dir == null || dir.isBlank()) {
                return;
            }
            Path target = Path.of(dir);
            Files.createDirectories(target);
            write(target.resolve(name + ".html"), result.getResponse().getContentAsString());
        };
    }

    private static void write(Path file, String html) throws IOException {
        Files.writeString(file, html);
    }
}
