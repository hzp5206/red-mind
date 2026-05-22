package com.redmind.modules.generate.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.redmind.common.config.RedMindAiProperties;
import com.redmind.common.exception.BizException;
import com.redmind.modules.generate.dto.AiGeneratedPayload;
import com.redmind.modules.generate.dto.GenerateRequest;
import com.redmind.modules.generate.dto.GeneratedVersion;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.StringUtils;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.client.RestTemplate;

@Component
public class OpenAiProvider implements AiProvider {

    private final RestTemplate restTemplate;
    private final RedMindAiProperties aiProperties;
    private final PromptBuilder promptBuilder;
    private final ObjectMapper objectMapper;

    public OpenAiProvider(RestTemplate restTemplate,
                          RedMindAiProperties aiProperties,
                          PromptBuilder promptBuilder,
                          ObjectMapper objectMapper) {
        this.restTemplate = restTemplate;
        this.aiProperties = aiProperties;
        this.promptBuilder = promptBuilder;
        this.objectMapper = objectMapper;
    }

    @Override
    public boolean support(String providerCode) {
        return "openai".equalsIgnoreCase(providerCode);
    }

    @Override
    public List<GeneratedVersion> generate(GenerateRequest request) {
        if (StringUtils.isBlank(aiProperties.getApiKey()) || "replace-me".equals(aiProperties.getApiKey())) {
            throw new BizException("请先配置真实 AI API Key");
        }

        Map<String, Object> requestBody = new HashMap<>();
        requestBody.put("model", aiProperties.getModel());
        requestBody.put("temperature", 0.85);
        requestBody.put("response_format", Collections.singletonMap("type", "json_object"));
        requestBody.put("messages", buildMessages(request));

        HttpHeaders headers = new HttpHeaders();
        headers.setBearerAuth(aiProperties.getApiKey());
        headers.setContentType(MediaType.APPLICATION_JSON);

        ResponseEntity<Map> response = restTemplate.exchange(
            normalizeBaseUrl(aiProperties.getBaseUrl()) + aiProperties.getChatPath(),
            HttpMethod.POST,
            new HttpEntity<>(requestBody, headers),
            Map.class
        );

        Map responseBody = response.getBody();
        if (responseBody == null) {
            throw new BizException("AI 服务返回为空");
        }

        List choices = (List) responseBody.get("choices");
        if (choices == null || choices.isEmpty()) {
            throw new BizException("AI 服务未返回有效内容");
        }

        Map firstChoice = (Map) choices.get(0);
        Map message = (Map) firstChoice.get("message");
        String content = message == null ? null : String.valueOf(message.get("content"));
        if (StringUtils.isBlank(content)) {
            throw new BizException("AI 内容解析失败");
        }

        try {
            AiGeneratedPayload payload = objectMapper.readValue(content, AiGeneratedPayload.class);
            if (payload.getVersions() == null || payload.getVersions().isEmpty()) {
                throw new BizException("AI 未生成有效版本");
            }
            return payload.getVersions();
        } catch (Exception exception) {
            throw new BizException("AI 返回格式不符合预期");
        }
    }

    private List<Map<String, String>> buildMessages(GenerateRequest request) {
        Map<String, String> system = new HashMap<>();
        system.put("role", "system");
        system.put("content", promptBuilder.buildSystemPrompt(request));

        Map<String, String> user = new HashMap<>();
        user.put("role", "user");
        user.put("content", promptBuilder.buildUserPrompt(request));
        return Arrays.asList(system, user);
    }

    private String normalizeBaseUrl(String baseUrl) {
        if (StringUtils.isBlank(baseUrl)) {
            throw new BizException("AI Base URL 未配置");
        }
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
