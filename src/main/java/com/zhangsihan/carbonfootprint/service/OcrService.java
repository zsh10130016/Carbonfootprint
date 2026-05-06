package com.zhangsihan.carbonfootprint.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import com.zhangsihan.carbonfootprint.config.BaiduOcrProperties;
import com.zhangsihan.carbonfootprint.vo.OcrParseResultVO;
import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.URLEncoder;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.util.ArrayList;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OcrService {

    private static final Set<String> SUPPORTED_TYPES = Set.of("TRANSPORT_TICKET", "UTILITY_BILL");
    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/bmp");
    private static final Pattern DISTANCE_PATTERN = Pattern.compile("(?i)(\\d+(?:\\.\\d+)?)\\s*(?:km|公里|千米)");
    private static final Pattern DISTANCE_LABEL_PATTERN = Pattern.compile("(?:里程|距离|行程)[^0-9]{0,10}(\\d+(?:\\.\\d+)?)");
    private static final Pattern ELECTRICITY_PATTERN = Pattern.compile("(?i)(\\d+(?:\\.\\d+)?)\\s*(?:度|千瓦时|kwh)");
    private static final Pattern WATER_PATTERN = Pattern.compile("(?i)(\\d+(?:\\.\\d+)?)\\s*(?:吨|立方米|m3|m³)");

    private final BaiduOcrProperties properties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    private String cachedAccessToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public OcrParseResultVO parse(String documentType, MultipartFile image) {
        String type = normalizeDocumentType(documentType);
        validateImage(image);

        List<String> words = recognizeWords(image);
        String rawText = String.join("\n", words);
        Map<String, String> fields = extractFields(type, rawText);
        String message = buildResultMessage(fields);

        return OcrParseResultVO.builder()
                .supported(true)
                .provider("baidu-ocr")
                .documentType(type)
                .message(message)
                .rawText(rawText)
                .words(words)
                .fields(fields)
                .build();
    }

    private String normalizeDocumentType(String documentType) {
        String type = documentType == null ? "" : documentType.trim().toUpperCase();
        if (!SUPPORTED_TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "暂不支持该票据类型");
        }
        return type;
    }

    private void validateImage(MultipartFile image) {
        if (image == null || image.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请上传需要识别的票据图片");
        }
        if (image.getSize() > properties.getMaxImageSize()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片大小不能超过 8MB");
        }
        String contentType = image.getContentType();
        if (!StringUtils.hasText(contentType) || !SUPPORTED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 JPG、PNG 或 BMP 格式的图片");
        }
        if (!properties.hasCredentials()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请先配置百度 OCR API Key 和 Secret Key");
        }
    }

    private List<String> recognizeWords(MultipartFile image) {
        try {
            String accessToken = getAccessToken();
            String imageBase64 = Base64.getEncoder().encodeToString(image.getBytes());
            String requestBody = formEncode(Map.of(
                    "image", imageBase64,
                    "detect_direction", "true",
                    "paragraph", "false",
                    "probability", "false"
            ));
            URI uri = URI.create(properties.getRecognizeUrl() + "?access_token=" + encode(accessToken));
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(properties.getReadTimeout())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode root = parseJson(response.body());
            assertBaiduSuccess(response.statusCode(), root, "百度 OCR 识别失败");
            return readWords(root);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片读取失败，请重新选择图片");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "百度 OCR 调用被中断，请稍后重试");
        }
    }

    private synchronized String getAccessToken() {
        if (StringUtils.hasText(cachedAccessToken) && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedAccessToken;
        }

        try {
            String query = formEncode(Map.of(
                    "grant_type", "client_credentials",
                    "client_id", properties.getApiKey(),
                    "client_secret", properties.getSecretKey()
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create(properties.getTokenUrl() + "?" + query))
                    .timeout(properties.getConnectTimeout())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode root = parseJson(response.body());
            assertBaiduSuccess(response.statusCode(), root, "百度 OCR 授权失败");
            cachedAccessToken = root.path("access_token").asText("");
            long expiresIn = root.path("expires_in").asLong(0);
            if (!StringUtils.hasText(cachedAccessToken)) {
                throw new BusinessException(ErrorCode.INTERNAL_ERROR, "百度 OCR 授权未返回 access_token");
            }
            tokenExpiresAt = Instant.now().plusSeconds(Math.max(60, expiresIn - 300));
            return cachedAccessToken;
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "百度 OCR 授权被中断，请稍后重试");
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "百度 OCR 授权请求失败，请检查网络");
        }
    }

    private JsonNode parseJson(String body) throws IOException {
        return objectMapper.readTree(body == null ? "{}" : body);
    }

    private void assertBaiduSuccess(int statusCode, JsonNode root, String defaultMessage) {
        if (statusCode < 200 || statusCode >= 300 || root.has("error_code") || root.has("error")) {
            String message = root.path("error_msg").asText(root.path("error_description").asText(defaultMessage));
            throw new BusinessException(ErrorCode.BAD_REQUEST, message);
        }
    }

    private List<String> readWords(JsonNode root) {
        List<String> words = new ArrayList<>();
        JsonNode wordResults = root.path("words_result");
        if (wordResults.isArray()) {
            for (JsonNode item : wordResults) {
                String text = item.path("words").asText("");
                if (StringUtils.hasText(text)) {
                    words.add(text.trim());
                }
            }
        }
        if (words.isEmpty()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "未能从图片中识别到文字，请上传更清晰的票据图片");
        }
        return words;
    }

    private Map<String, String> extractFields(String documentType, String rawText) {
        Map<String, String> fields = new LinkedHashMap<>();
        if ("TRANSPORT_TICKET".equals(documentType)) {
            fields.put("activityType", "TRANSPORT");
            fields.put("subType", detectTransportSubType(rawText));
            fields.put("unit", "km");
            findFirstNumber(DISTANCE_PATTERN, rawText)
                    .or(() -> findFirstNumber(DISTANCE_LABEL_PATTERN, rawText))
                    .ifPresent(amount -> fields.put("amount", amount));
            return fields;
        }

        String subType = detectUtilitySubType(rawText);
        fields.put("activityType", "HOME_ENERGY");
        fields.put("subType", subType);
        fields.put("unit", unitOfUtility(subType));
        extractUtilityAmount(subType, rawText).ifPresent(amount -> fields.put("amount", amount));
        return fields;
    }

    private String detectTransportSubType(String rawText) {
        String text = rawText.toLowerCase();
        if (containsAny(text, "地铁", "轨道交通", "subway", "metro")) {
            return "SUBWAY";
        }
        if (containsAny(text, "公交", "公共汽车", "bus")) {
            return "BUS";
        }
        if (containsAny(text, "出租", "打车", "网约车", "滴滴", "taxi")) {
            return "TAXI";
        }
        if (containsAny(text, "骑行", "单车", "bike", "bicycle")) {
            return "BIKE";
        }
        if (containsAny(text, "步行", "walk")) {
            return "WALK";
        }
        return "TRAIN";
    }

    private String detectUtilitySubType(String rawText) {
        String text = rawText.toLowerCase();
        if (containsAny(text, "水费", "用水", "水量", "water")) {
            return "WATER";
        }
        if (containsAny(text, "燃气", "天然气", "气费", "用气", "gas")) {
            return "NATURAL_GAS";
        }
        return "ELECTRICITY";
    }

    private Optional<String> extractUtilityAmount(String subType, String rawText) {
        if ("ELECTRICITY".equals(subType)) {
            return findFirstNumber(ELECTRICITY_PATTERN, rawText);
        }
        return findFirstNumber(WATER_PATTERN, rawText);
    }

    private Optional<String> findFirstNumber(Pattern pattern, String text) {
        Matcher matcher = pattern.matcher(text);
        if (!matcher.find()) {
            return Optional.empty();
        }
        return Optional.of(formatAmount(matcher.group(1)));
    }

    private String formatAmount(String value) {
        return new BigDecimal(value).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
    }

    private String unitOfUtility(String subType) {
        if ("WATER".equals(subType)) {
            return "ton";
        }
        if ("NATURAL_GAS".equals(subType)) {
            return "m3";
        }
        return "kWh";
    }

    private String buildResultMessage(Map<String, String> fields) {
        if (StringUtils.hasText(fields.get("amount"))) {
            return "百度 OCR 识别完成，已提取到可用于碳核算的关键字段。";
        }
        return "百度 OCR 识别完成，但未识别到明确的里程或用量，请带入表单后手动补充数值。";
    }

    private boolean containsAny(String text, String... candidates) {
        for (String candidate : candidates) {
            if (text.contains(candidate.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private String formEncode(Map<String, String> values) {
        return values.entrySet().stream()
                .map(entry -> encode(entry.getKey()) + "=" + encode(entry.getValue()))
                .reduce((left, right) -> left + "&" + right)
                .orElse("");
    }

    private String encode(String value) {
        return URLEncoder.encode(value, StandardCharsets.UTF_8);
    }
}
