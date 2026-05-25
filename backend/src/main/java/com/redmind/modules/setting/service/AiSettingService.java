package com.redmind.modules.setting.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redmind.common.config.RedMindAiProperties;
import com.redmind.common.exception.BizException;
import com.redmind.modules.admin.service.OperationLogService;
import com.redmind.modules.setting.dto.AiConnectivityTestResponse;
import com.redmind.modules.setting.dto.AiSettingResponse;
import com.redmind.modules.setting.dto.AiSettingSaveRequest;
import com.redmind.modules.setting.entity.SystemSetting;
import com.redmind.modules.setting.mapper.SystemSettingMapper;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class AiSettingService {

    private static final String KEY_PROVIDER = "ai.provider";
    private static final String KEY_BASE_URL = "ai.base_url";
    private static final String KEY_API_KEY = "ai.api_key";
    private static final String KEY_MODEL = "ai.model";
    private static final String KEY_CHAT_PATH = "ai.chat_path";

    private static final List<String> PROVIDER_OPTIONS = Arrays.asList("mock", "openai", "deepseek");
    private static final List<String> MODEL_OPTIONS = Arrays.asList(
        "gpt-4o-mini",
        "gpt-4.1-mini",
        "deepseek-v4-flash",
        "deepseek-v4-pro"
    );

    private final SystemSettingMapper systemSettingMapper;
    private final RedMindAiProperties redMindAiProperties;
    private final OperationLogService operationLogService;
    private final RestTemplate restTemplate;

    public AiSettingService(SystemSettingMapper systemSettingMapper,
                            RedMindAiProperties redMindAiProperties,
                            OperationLogService operationLogService,
                            RestTemplate restTemplate) {
        this.systemSettingMapper = systemSettingMapper;
        this.redMindAiProperties = redMindAiProperties;
        this.operationLogService = operationLogService;
        this.restTemplate = restTemplate;
    }

    public AiSettingResponse getCurrent() {
        refreshRuntimeProperties();
        String apiKey = redMindAiProperties.getApiKey();
        return AiSettingResponse.builder()
            .provider(redMindAiProperties.getProvider())
            .baseUrl(redMindAiProperties.getBaseUrl())
            .model(redMindAiProperties.getModel())
            .chatPath(redMindAiProperties.getChatPath())
            .apiKeyValue("replace-me".equals(apiKey) ? "" : apiKey)
            .apiKeyMasked(maskApiKey(apiKey))
            .apiKeyConfigured(StringUtils.isNotBlank(apiKey) && !"replace-me".equals(apiKey))
            .providerOptions(PROVIDER_OPTIONS)
            .modelOptions(MODEL_OPTIONS)
            .build();
    }

    public AiSettingResponse save(AiSettingSaveRequest request) {
        String provider = StringUtils.lowerCase(StringUtils.trimToEmpty(request.getProvider()));
        if (!PROVIDER_OPTIONS.contains(provider)) {
            throw new BizException("不支持的 AI Provider：" + request.getProvider());
        }
        upsert(KEY_PROVIDER, provider, "AI Provider");
        upsert(KEY_BASE_URL, StringUtils.trimToEmpty(request.getBaseUrl()), "AI Base URL");
        upsert(KEY_MODEL, StringUtils.trimToEmpty(request.getModel()), "AI Model");
        upsert(KEY_CHAT_PATH, normalizePath(request.getChatPath()), "AI Chat Path");
        if (StringUtils.isNotBlank(request.getApiKey())) {
            upsert(KEY_API_KEY, StringUtils.trimToEmpty(request.getApiKey()), "AI API Key");
        }
        refreshRuntimeProperties();
        operationLogService.log("ai_setting", "update", "system_setting", 0L, "更新 AI 运行时配置：" + provider);
        return getCurrent();
    }

    public AiConnectivityTestResponse testConnectivity() {
        refreshRuntimeProperties();
        String provider = StringUtils.defaultIfBlank(redMindAiProperties.getProvider(), "mock");
        if ("mock".equalsIgnoreCase(provider)) {
            return AiConnectivityTestResponse.builder()
                .success(true)
                .provider(provider)
                .model(redMindAiProperties.getModel())
                .baseUrl(redMindAiProperties.getBaseUrl())
                .message("Mock Provider 无需联网，可直接用于本地功能联调。")
                .build();
        }
        if (StringUtils.isBlank(redMindAiProperties.getApiKey()) || "replace-me".equals(redMindAiProperties.getApiKey())) {
            return AiConnectivityTestResponse.builder()
                .success(false)
                .provider(provider)
                .model(redMindAiProperties.getModel())
                .baseUrl(redMindAiProperties.getBaseUrl())
                .message("未配置有效 API Key")
                .build();
        }
        try {
            HttpHeaders headers = new HttpHeaders();
            headers.setBearerAuth(redMindAiProperties.getApiKey());
            headers.setContentType(MediaType.APPLICATION_JSON);
            String url = normalizeBaseUrl(redMindAiProperties.getBaseUrl()) + normalizePath(redMindAiProperties.getChatPath());
            ResponseEntity<String> response = restTemplate.exchange(
                url,
                HttpMethod.POST,
                new HttpEntity<>(buildTestPayload(), headers),
                String.class
            );
            boolean ok = response.getStatusCode().is2xxSuccessful();
            String message = ok ? "连通成功，可正常发起文案生成请求" : "请求已发出，但返回状态不是 2xx";
            operationLogService.log("ai_setting", "test", "system_setting", 0L, "测试 AI 连通性：" + provider + " => " + response.getStatusCodeValue());
            return AiConnectivityTestResponse.builder()
                .success(ok)
                .provider(provider)
                .model(redMindAiProperties.getModel())
                .baseUrl(redMindAiProperties.getBaseUrl())
                .message(message)
                .build();
        } catch (Exception exception) {
            operationLogService.log("ai_setting", "test", "system_setting", 0L, "测试 AI 连通性失败：" + provider);
            return AiConnectivityTestResponse.builder()
                .success(false)
                .provider(provider)
                .model(redMindAiProperties.getModel())
                .baseUrl(redMindAiProperties.getBaseUrl())
                .message(StringUtils.abbreviate(exception.getMessage(), 200))
                .build();
        }
    }

    public void refreshRuntimeProperties() {
        redMindAiProperties.setProvider(readValue(KEY_PROVIDER, redMindAiProperties.getProvider()));
        redMindAiProperties.setBaseUrl(readValue(KEY_BASE_URL, redMindAiProperties.getBaseUrl()));
        redMindAiProperties.setApiKey(readValue(KEY_API_KEY, redMindAiProperties.getApiKey()));
        redMindAiProperties.setModel(readValue(KEY_MODEL, redMindAiProperties.getModel()));
        redMindAiProperties.setChatPath(readValue(KEY_CHAT_PATH, redMindAiProperties.getChatPath()));
    }

    private String readValue(String key, String defaultValue) {
        SystemSetting setting = systemSettingMapper.selectOne(new LambdaQueryWrapper<SystemSetting>()
            .eq(SystemSetting::getSettingKey, key)
            .last("limit 1"));
        if (setting == null || StringUtils.isBlank(setting.getSettingValue())) {
            return defaultValue;
        }
        return setting.getSettingValue();
    }

    private void upsert(String key, String value, String description) {
        SystemSetting current = systemSettingMapper.selectOne(new LambdaQueryWrapper<SystemSetting>()
            .eq(SystemSetting::getSettingKey, key)
            .last("limit 1"));
        if (current == null) {
            current = new SystemSetting();
            current.setSettingKey(key);
            current.setSettingValue(value);
            current.setDescriptionText(description);
            current.setCreatedAt(LocalDateTime.now());
            current.setUpdatedAt(LocalDateTime.now());
            systemSettingMapper.insert(current);
            return;
        }
        current.setSettingValue(value);
        current.setDescriptionText(description);
        current.setUpdatedAt(LocalDateTime.now());
        systemSettingMapper.updateById(current);
    }

    private String maskApiKey(String apiKey) {
        if (StringUtils.isBlank(apiKey) || "replace-me".equals(apiKey)) {
            return null;
        }
        if (apiKey.length() <= 8) {
            return "****";
        }
        return apiKey.substring(0, 4) + "****" + apiKey.substring(apiKey.length() - 4);
    }

    private String normalizePath(String chatPath) {
        String path = StringUtils.trimToEmpty(chatPath);
        if (!path.startsWith("/")) {
            path = "/" + path;
        }
        return path;
    }

    private String normalizeBaseUrl(String baseUrl) {
        String value = StringUtils.trimToEmpty(baseUrl);
        if (value.endsWith("/")) {
            return value.substring(0, value.length() - 1);
        }
        return value;
    }

    private String buildTestPayload() {
        return "{\"model\":\"" + redMindAiProperties.getModel()
            + "\",\"messages\":[{\"role\":\"user\",\"content\":\"ping\"}],\"max_tokens\":8}";
    }
}
