package com.example.petnow.common.controller;

import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ModelAttribute;

/**
 * HTML 화면의 Open Graph URL을 공통으로 만든다.
 *
 * <p>카카오톡 링크 미리보기 크롤러는 {@code og:image}에 절대 URL이 있어야 이미지를 안정적으로
 * 가져갈 수 있다. 운영 주소가 정해진 환경에서는 {@code APP_PUBLIC_URL}을 사용하고, 로컬에서는
 * 현재 요청의 scheme/host/port를 사용한다.
 */
@ControllerAdvice
public class OpenGraphControllerAdvice {

    private static final String OPEN_GRAPH_IMAGE_PATH = "/images/og-image.png";

    private final String configuredPublicUrl;

    public OpenGraphControllerAdvice(@Value("${app.public-url:}") String configuredPublicUrl) {
        this.configuredPublicUrl = stripTrailingSlash(configuredPublicUrl.trim());
    }

    @ModelAttribute
    public void addOpenGraphUrls(HttpServletRequest request, Model model) {
        String publicBaseUrl = configuredPublicUrl.isEmpty()
                ? requestBaseUrl(request)
                : configuredPublicUrl;

        model.addAttribute("openGraphUrl", publicBaseUrl + pathWithoutContextPath(request));
        model.addAttribute("openGraphImageUrl", publicBaseUrl + OPEN_GRAPH_IMAGE_PATH);
    }

    private String requestBaseUrl(HttpServletRequest request) {
        String scheme = request.getScheme();
        int port = request.getServerPort();
        boolean defaultPort = ("http".equalsIgnoreCase(scheme) && port == 80)
                || ("https".equalsIgnoreCase(scheme) && port == 443);

        return scheme + "://" + request.getServerName()
                + (defaultPort ? "" : ":" + port)
                + request.getContextPath();
    }

    private String pathWithoutContextPath(HttpServletRequest request) {
        String requestUri = request.getRequestURI();
        String contextPath = request.getContextPath();
        String path = requestUri.substring(contextPath.length());
        return path.isEmpty() ? "/" : path;
    }

    private static String stripTrailingSlash(String value) {
        int end = value.length();
        while (end > 0 && value.charAt(end - 1) == '/') {
            end--;
        }
        return value.substring(0, end);
    }
}
