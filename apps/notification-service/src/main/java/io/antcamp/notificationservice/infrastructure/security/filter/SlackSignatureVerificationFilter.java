package io.antcamp.notificationservice.infrastructure.security.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ReadListener;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletInputStream;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletRequestWrapper;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import javax.crypto.Mac;
import javax.crypto.spec.SecretKeySpec;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HashMap;
import java.util.HexFormat;
import java.util.Map;

@Slf4j
@Component
public class SlackSignatureVerificationFilter extends OncePerRequestFilter {

    private static final String SIGNATURE_HEADER = "X-Slack-Signature";
    private static final String TIMESTAMP_HEADER = "X-Slack-Request-Timestamp";
    private static final long MAX_AGE_SECONDS = 300;
    private static final String TARGET_PATH = "/api/notifications/interactions";

    @Value("${slack.signing-secret}")
    private String signingSecret;

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        return !TARGET_PATH.equals(request.getRequestURI());
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain chain)
            throws ServletException, IOException {
        // 헤더 확인
        String timestamp = request.getHeader(TIMESTAMP_HEADER);
        String slackSignature = request.getHeader(SIGNATURE_HEADER);

        if (timestamp == null || slackSignature == null) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        long requestTime;
        try {
            requestTime = Long.parseLong(timestamp);
        } catch (NumberFormatException e) {
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        // Replay attack 방지: 5분 이상 지난 요청 거부
        if (Math.abs(System.currentTimeMillis() / 1000 - requestTime) > MAX_AGE_SECONDS) {
            log.warn("Slack 요청 timestamp 만료: {}", timestamp);
            response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }

        byte[] bodyBytes = request.getInputStream().readAllBytes();
        String baseString = "v0:" + timestamp + ":" + new String(bodyBytes, StandardCharsets.UTF_8);

        try {
            Mac mac = Mac.getInstance("HmacSHA256");
            mac.init(new SecretKeySpec(signingSecret.getBytes(StandardCharsets.UTF_8), "HmacSHA256"));
            String computed = "v0=" + HexFormat.of().formatHex(mac.doFinal(baseString.getBytes(StandardCharsets.UTF_8)));

            if (!MessageDigest.isEqual(computed.getBytes(StandardCharsets.UTF_8), slackSignature.getBytes(StandardCharsets.UTF_8))) {
                log.warn("Slack 서명 검증 실패");
                response.sendError(HttpServletResponse.SC_UNAUTHORIZED);
                return;
            }
        } catch (Exception e) {
            log.error("Slack 서명 계산 실패: {}", e.getMessage());
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        // body를 소비했으므로 컨트롤러에서 다시 읽을 수 있도록 캐싱된 요청으로 교체
        chain.doFilter(new CachedBodyRequest(request, bodyBytes), response);
    }

    private static class CachedBodyRequest extends HttpServletRequestWrapper {

        private final byte[] body;

        CachedBodyRequest(HttpServletRequest request, byte[] body) {
            super(request);
            this.body = body;
        }

        @Override
        public ServletInputStream getInputStream() {
            ByteArrayInputStream stream = new ByteArrayInputStream(body);
            return new ServletInputStream() {
                @Override public boolean isFinished() { return stream.available() == 0; }
                @Override public boolean isReady() { return true; }
                @Override public void setReadListener(ReadListener listener) {}
                @Override public int read() { return stream.read(); }
            };
        }

        @Override
        public BufferedReader getReader() {
            return new BufferedReader(new InputStreamReader(getInputStream(), StandardCharsets.UTF_8));
        }

        @Override
        public String getParameter(String name) {
            String[] values = getParameterMap().get(name);
            return (values != null && values.length > 0) ? values[0] : null;
        }

        @Override
        public Map<String, String[]> getParameterMap() {
            String contentType = getContentType();
            if (contentType != null && contentType.contains("application/x-www-form-urlencoded")) {
                Map<String, String[]> params = new HashMap<>();
                String bodyStr = new String(body, StandardCharsets.UTF_8);
                for (String pair : bodyStr.split("&")) {
                    String[] parts = pair.split("=", 2);
                    if (parts.length == 2) {
                        try {
                            String key = URLDecoder.decode(parts[0], StandardCharsets.UTF_8);
                            String value = URLDecoder.decode(parts[1], StandardCharsets.UTF_8);
                            params.put(key, new String[]{value});
                        } catch (Exception ignored) {}
                    }
                }
                return params;
            }
            return super.getParameterMap();
        }
    }
}