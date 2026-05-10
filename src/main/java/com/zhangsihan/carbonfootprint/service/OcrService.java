package com.zhangsihan.carbonfootprint.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.zhangsihan.carbonfootprint.common.BusinessException;
import com.zhangsihan.carbonfootprint.common.ErrorCode;
import com.zhangsihan.carbonfootprint.config.BaiduOcrProperties;
import com.zhangsihan.carbonfootprint.config.DeepSeekProperties;
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
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class OcrService {

    private static final Set<String> SUPPORTED_TYPES = Set.of("TRANSPORT_TICKET", "UTILITY_BILL");
    private static final Set<String> SUPPORTED_IMAGE_TYPES = Set.of("image/jpeg", "image/jpg", "image/png", "image/bmp");
    private static final Set<String> ACTIVITY_TYPES = Set.of("TRANSPORT", "HOME_ENERGY");
    private static final Set<String> TRANSPORT_SUB_TYPES = Set.of("BUS", "SUBWAY", "BIKE", "WALK", "TAXI", "TRAIN");
    private static final Set<String> UTILITY_SUB_TYPES = Set.of("ELECTRICITY", "NATURAL_GAS", "WATER");

    private final BaiduOcrProperties baiduProperties;
    private final DeepSeekProperties deepSeekProperties;
    private final ObjectMapper objectMapper;
    private final HttpClient httpClient = HttpClient.newBuilder().build();

    private String cachedAccessToken;
    private Instant tokenExpiresAt = Instant.EPOCH;

    public OcrParseResultVO parse(String documentType, MultipartFile image) {
        String type = normalizeDocumentType(documentType);
        validateImage(image);

        List<String> words = recognizeWords(image);
        String rawText = String.join("\n", words);
        Map<String, String> fields = analyzeWithDeepSeek(type, rawText);
        String message = buildResultMessage(fields);

        return OcrParseResultVO.builder()
                .supported(true)
                .provider("baidu-ocr + deepseek")
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
        if (image.getSize() > baiduProperties.getMaxImageSize()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片大小不能超过 8MB");
        }
        String contentType = image.getContentType();
        if (!StringUtils.hasText(contentType) || !SUPPORTED_IMAGE_TYPES.contains(contentType.toLowerCase())) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "仅支持 JPG、PNG 或 BMP 格式的图片");
        }
        if (!baiduProperties.hasCredentials()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请先配置百度 OCR API Key 和 Secret Key");
        }
        if (!deepSeekProperties.hasCredentials()) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "请先配置 DeepSeek API Key");
        }
    }

    private List<String> recognizeWords(MultipartFile image) {
        try {
            String accessToken = getBaiduAccessToken();
            String imageBase64 = Base64.getEncoder().encodeToString(image.getBytes());
            String requestBody = formEncode(Map.of(
                    "image", imageBase64,
                    "detect_direction", "true",
                    "paragraph", "false",
                    "probability", "false"
            ));
            URI uri = URI.create(baiduProperties.getRecognizeUrl() + "?access_token=" + encode(accessToken));
            HttpRequest request = HttpRequest.newBuilder(uri)
                    .timeout(baiduProperties.getReadTimeout())
                    .header("Content-Type", "application/x-www-form-urlencoded")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody, StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode root = parseJson(response.body());
            assertRemoteSuccess(response.statusCode(), root, "百度 OCR 识别失败");
            return readWords(root);
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "图片读取失败，请重新选择图片");
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "百度 OCR 调用被中断，请稍后重试");
        }
    }

    private synchronized String getBaiduAccessToken() {
        if (StringUtils.hasText(cachedAccessToken) && Instant.now().isBefore(tokenExpiresAt)) {
            return cachedAccessToken;
        }

        try {
            String query = formEncode(Map.of(
                    "grant_type", "client_credentials",
                    "client_id", baiduProperties.getApiKey(),
                    "client_secret", baiduProperties.getSecretKey()
            ));
            HttpRequest request = HttpRequest.newBuilder(URI.create(baiduProperties.getTokenUrl() + "?" + query))
                    .timeout(baiduProperties.getConnectTimeout())
                    .GET()
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode root = parseJson(response.body());
            assertRemoteSuccess(response.statusCode(), root, "百度 OCR 授权失败");
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

    private Map<String, String> analyzeWithDeepSeek(String documentType, String rawText) {
        try {
            Map<String, Object> payload = new LinkedHashMap<>();
            payload.put("model", deepSeekProperties.getModel());
            payload.put("temperature", 0);
            payload.put("response_format", Map.of("type", "json_object"));
            payload.put("messages", List.of(
                    Map.of("role", "system", "content", buildSystemPrompt()),
                    Map.of("role", "user", "content", buildUserPrompt(documentType, rawText))
            ));

            HttpRequest request = HttpRequest.newBuilder(URI.create(deepSeekProperties.getChatUrl()))
                    .timeout(deepSeekProperties.getReadTimeout())
                    .header("Authorization", "Bearer " + deepSeekProperties.getApiKey())
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(payload), StandardCharsets.UTF_8))
                    .build();
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            JsonNode root = parseJson(response.body());
            assertRemoteSuccess(response.statusCode(), root, "DeepSeek 分析失败");
            String content = root.path("choices").path(0).path("message").path("content").asText("");
            return normalizeDeepSeekFields(documentType, parseJson(extractJson(content)));
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "DeepSeek 分析被中断，请稍后重试");
        } catch (IOException exception) {
            throw new BusinessException(ErrorCode.INTERNAL_ERROR, "DeepSeek 分析请求失败，请检查网络或返回格式");
        }
    }

    private String buildSystemPrompt() {
        return """
                你是个人碳足迹系统的票据字段抽取器。只根据用户提供的 OCR 文本抽取结构化字段。
                必须只输出一个 JSON 对象，不要 Markdown，不要解释。
                JSON 字段固定为 activityType, subType, amount, unit。
                activityType 只能是 TRANSPORT 或 HOME_ENERGY。
                TRANSPORT 的 subType 只能是 BUS, SUBWAY, BIKE, WALK, TAXI, TRAIN，unit 必须是 km。
                HOME_ENERGY 的 subType 只能是 ELECTRICITY, NATURAL_GAS, WATER；对应 unit 分别是 kWh, m3, ton。
                amount 必须是数字；如果无法确定，返回空字符串。
                如果是火车或高铁行程，请根据中国铁路官方公布的《铁路客运运价里程表》计算两车站间的铁路距离，并把距离作为 amount，单位为 km。
                不要臆造 OCR 文本中不存在的数值。
                """;
    }

    private String buildUserPrompt(String documentType, String rawText) {
        return """
                前端选择的票据类型：%s
                OCR 文本如下：
                %s

                请抽取最适合自动填入碳记录表单的字段。
                """.formatted(documentType, rawText);
    }

    private Map<String, String> normalizeDeepSeekFields(String documentType, JsonNode node) {
        Map<String, String> fields = new LinkedHashMap<>();
        String activityType = sanitizeActivityType(documentType, node.path("activityType").asText(""));
        String subType = sanitizeSubType(activityType, node.path("subType").asText(""));
        String amount = sanitizeAmount(node.path("amount").asText(""));
        String unit = sanitizeUnit(activityType, subType, node.path("unit").asText(""));

        fields.put("activityType", activityType);
        fields.put("subType", subType);
        fields.put("amount", amount);
        fields.put("unit", unit);
        return fields;
    }

    private String sanitizeActivityType(String documentType, String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        if (ACTIVITY_TYPES.contains(normalized)) {
            return normalized;
        }
        return "UTILITY_BILL".equals(documentType) ? "HOME_ENERGY" : "TRANSPORT";
    }

    private String sanitizeSubType(String activityType, String value) {
        String normalized = value == null ? "" : value.trim().toUpperCase();
        if ("TRANSPORT".equals(activityType)) {
            return TRANSPORT_SUB_TYPES.contains(normalized) ? normalized : "TRAIN";
        }
        return UTILITY_SUB_TYPES.contains(normalized) ? normalized : "ELECTRICITY";
    }

    private String sanitizeAmount(String value) {
        if (!StringUtils.hasText(value)) {
            return "";
        }
        try {
            return new BigDecimal(value.trim()).setScale(2, RoundingMode.HALF_UP).stripTrailingZeros().toPlainString();
        } catch (NumberFormatException exception) {
            return "";
        }
    }

    private String sanitizeUnit(String activityType, String subType, String value) {
        if ("TRANSPORT".equals(activityType)) {
            return "km";
        }
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
            return "识别完成，已经提取可用于碳核算的字段";
        }
        return "百度 OCR 识别完成，DeepSeek 未能确定数值，请带入表单后手动补充。";
    }

    private JsonNode parseJson(String body) throws IOException {
        return objectMapper.readTree(body == null ? "{}" : body);
    }

    private String extractJson(String content) {
        String text = content == null ? "" : content.trim();
        int start = text.indexOf('{');
        int end = text.lastIndexOf('}');
        if (start >= 0 && end > start) {
            return text.substring(start, end + 1);
        }
        throw new BusinessException(ErrorCode.INTERNAL_ERROR, "DeepSeek 未返回有效 JSON");
    }

    private void assertRemoteSuccess(int statusCode, JsonNode root, String defaultMessage) {
        if (statusCode >= 200 && statusCode < 300 && !root.has("error_code") && !root.has("error")) {
            return;
        }
        JsonNode error = root.path("error");
        String message = root.path("error_msg").asText(error.path("message").asText(defaultMessage));
        throw new BusinessException(ErrorCode.BAD_REQUEST, message);
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
