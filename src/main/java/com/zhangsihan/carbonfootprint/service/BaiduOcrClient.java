package com.zhangsihan.carbonfootprint.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import com.zhangsihan.carbonfootprint.config.BaiduOcrProperties;
import java.io.IOException;
import java.net.URI;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;
import org.springframework.web.client.RestClientException;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.util.UriComponentsBuilder;

@Component
@RequiredArgsConstructor
public class BaiduOcrClient {

    private final BaiduOcrProperties properties;
    private final RestClient restClient = RestClient.create();
    private volatile String cachedAccessToken;
    private volatile Instant accessTokenExpiresAt = Instant.EPOCH;

    public List<String> recognize(MultipartFile file) {
        validateImage(file);
        String accessToken = getAccessToken();
        String imageBase64 = readAsBase64(file);

        MultiValueMap<String, String> body = new LinkedMultiValueMap<>();
        body.add("image", imageBase64);
        body.add("language_type", "CHN_ENG");

        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(properties.getOcrUrl())
                    .queryParam("access_token", accessToken)
                    .build()
                    .toUri();
            JsonNode response = restClient.post()
                    .uri(uri)
                    .contentType(MediaType.APPLICATION_FORM_URLENCODED)
                    .body(body)
                    .retrieve()
                    .body(JsonNode.class);
            return extractWords(response);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "百度 OCR 调用失败，请检查网络和 OCR 服务配置。");
        }
    }

    private void validateImage(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请上传需要识别的票据图片。");
        }
        if (file.getSize() > properties.getMaxUploadSize().toBytes()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片大小不能超过 " + properties.getMaxUploadSize().toMegabytes() + "MB。");
        }
        String contentType = file.getContentType();
        if (StringUtils.hasText(contentType) && !contentType.toLowerCase().startsWith("image/")) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持上传图片文件。");
        }
    }

    private String getAccessToken() {
        if (!properties.enabled()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请先配置百度 OCR 的 API Key 和 Secret Key。");
        }
        Instant now = Instant.now();
        if (StringUtils.hasText(cachedAccessToken) && now.isBefore(accessTokenExpiresAt)) {
            return cachedAccessToken;
        }

        synchronized (this) {
            if (StringUtils.hasText(cachedAccessToken) && Instant.now().isBefore(accessTokenExpiresAt)) {
                return cachedAccessToken;
            }
            JsonNode response = requestAccessToken();
            String token = response.path("access_token").asText("");
            if (!StringUtils.hasText(token)) {
                throw new BusinessException(ErrorCode.BAD_REQUEST, "百度 OCR 鉴权失败，请检查 API Key 和 Secret Key。");
            }
            long expiresIn = response.path("expires_in").asLong(2592000L);
            cachedAccessToken = token;
            // Leave a small buffer before the official expiry to avoid edge-case token failures.
            accessTokenExpiresAt = Instant.now().plusSeconds(Math.max(60L, expiresIn - 300L));
            return cachedAccessToken;
        }
    }

    private JsonNode requestAccessToken() {
        try {
            URI uri = UriComponentsBuilder.fromHttpUrl(properties.getTokenUrl())
                    .queryParam("grant_type", "client_credentials")
                    .queryParam("client_id", properties.getApiKey())
                    .queryParam("client_secret", properties.getSecretKey())
                    .build()
                    .toUri();
            return restClient.post().uri(uri).retrieve().body(JsonNode.class);
        } catch (RestClientException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "百度 OCR 鉴权请求失败，请检查网络和密钥配置。");
        }
    }

    private String readAsBase64(MultipartFile file) {
        try {
            return Base64.getEncoder().encodeToString(file.getBytes());
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片读取失败，请重新上传。");
        }
    }

    private List<String> extractWords(JsonNode response) {
        if (response == null) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "百度 OCR 未返回识别结果。");
        }
        if (response.has("error_code")) {
            String message = response.path("error_msg").asText("百度 OCR 识别失败。");
            throw new BusinessException(ErrorCode.BAD_REQUEST, "百度 OCR 识别失败：" + message);
        }
        List<String> words = new ArrayList<>();
        for (JsonNode item : response.path("words_result")) {
            String text = item.path("words").asText("");
            if (StringUtils.hasText(text)) {
                words.add(text.trim());
            }
        }
        if (words.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未从图片中识别到文字，请换一张更清晰的票据图片。");
        }
        return words;
    }
}
