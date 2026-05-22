package com.redmind.modules.moderation.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redmind.common.exception.BizException;
import com.redmind.modules.admin.service.OperationLogService;
import com.redmind.modules.moderation.dto.SensitiveWordItemResponse;
import com.redmind.modules.moderation.entity.SensitiveWord;
import com.redmind.modules.moderation.mapper.SensitiveWordMapper;
import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import javax.annotation.PostConstruct;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Service;

@Service
public class SensitiveWordService {

    private AcAutomaton automaton = new AcAutomaton();
    private final Map<String, String> replacements = new HashMap<>();
    private final SensitiveWordMapper sensitiveWordMapper;
    private final OperationLogService operationLogService;

    public SensitiveWordService(SensitiveWordMapper sensitiveWordMapper,
                                OperationLogService operationLogService) {
        this.sensitiveWordMapper = sensitiveWordMapper;
        this.operationLogService = operationLogService;
    }

    @PostConstruct
    public void init() {
        List<SensitiveWord> words = sensitiveWordMapper.selectList(null);
        if (words == null || words.isEmpty()) {
            initFromFile();
            words = sensitiveWordMapper.selectList(null);
        }
        reloadFromDatabase(words);
    }

    public List<String> detect(String text) {
        if (text == null || text.isEmpty()) {
            return Collections.emptyList();
        }
        return automaton.search(text);
    }

    public String getReplacement(String word) {
        return replacements.getOrDefault(word, "友好表达");
    }

    public List<SensitiveWordItemResponse> listAll() {
        return sensitiveWordMapper.selectList(new LambdaQueryWrapper<SensitiveWord>().orderByAsc(SensitiveWord::getWord)).stream()
            .map(item -> new SensitiveWordItemResponse(item.getId(), item.getWord(), item.getReplacement(), item.getIsActive()))
            .sorted(Comparator.comparing(SensitiveWordItemResponse::getWord))
            .collect(Collectors.toList());
    }

    public void save(Long id, String word, String replacement, Boolean active) {
        SensitiveWord entity = id == null ? new SensitiveWord() : sensitiveWordMapper.selectById(id);
        if (entity == null) {
            entity = new SensitiveWord();
            entity.setCreatedAt(LocalDateTime.now());
        }
        entity.setWord(word);
        entity.setReplacement(replacement);
        entity.setIsActive(active == null ? true : active);
        if (entity.getId() == null) {
            sensitiveWordMapper.insert(entity);
            operationLogService.log("sensitive_word", "create", "sensitive_word", entity.getId(), "新增敏感词：" + entity.getWord());
        } else {
            sensitiveWordMapper.updateById(entity);
            operationLogService.log("sensitive_word", "update", "sensitive_word", entity.getId(), "更新敏感词：" + entity.getWord());
        }
        reloadFromDatabase(sensitiveWordMapper.selectList(null));
    }

    public void delete(Long id) {
        SensitiveWord entity = sensitiveWordMapper.selectById(id);
        sensitiveWordMapper.deleteById(id);
        operationLogService.log("sensitive_word", "delete", "sensitive_word", id, "删除敏感词：" + (entity == null ? id : entity.getWord()));
        reloadFromDatabase(sensitiveWordMapper.selectList(null));
    }

    public int importWords(String content) {
        if (content == null || content.trim().isEmpty()) {
            throw new BizException("导入内容不能为空");
        }
        int count = 0;
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            new ByteArrayInputStream(content.getBytes(StandardCharsets.UTF_8)), StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().filter(line -> !line.trim().isEmpty()).collect(Collectors.toList());
            for (String line : lines) {
                String[] arr = line.split("=");
                String word = arr[0].trim();
                String replacement = arr.length > 1 ? arr[1].trim() : "友好表达";
                if (word.isEmpty()) {
                    continue;
                }
                SensitiveWord existing = sensitiveWordMapper.selectOne(
                    new LambdaQueryWrapper<SensitiveWord>().eq(SensitiveWord::getWord, word).last("limit 1")
                );
                if (existing == null) {
                    SensitiveWord entity = new SensitiveWord();
                    entity.setWord(word);
                    entity.setReplacement(replacement);
                    entity.setIsActive(true);
                    entity.setCreatedAt(LocalDateTime.now());
                    sensitiveWordMapper.insert(entity);
                } else {
                    existing.setReplacement(replacement);
                    existing.setIsActive(true);
                    sensitiveWordMapper.updateById(existing);
                }
                count++;
            }
        } catch (Exception ex) {
            throw new BizException("导入失败，请检查格式");
        }
        reloadFromDatabase(sensitiveWordMapper.selectList(null));
        operationLogService.log("sensitive_word", "import", "sensitive_word", null, "批量导入敏感词数量：" + count);
        return count;
    }

    public String exportWords() {
        String content = sensitiveWordMapper.selectList(new LambdaQueryWrapper<SensitiveWord>().orderByAsc(SensitiveWord::getWord))
            .stream()
            .filter(Objects::nonNull)
            .map(item -> item.getWord() + "=" + item.getReplacement())
            .collect(Collectors.joining("\n"));
        operationLogService.log("sensitive_word", "export", "sensitive_word", null, "导出敏感词词库");
        return content;
    }

    private void initFromFile() {
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(
            new ClassPathResource("sensitive-words.txt").getInputStream(), StandardCharsets.UTF_8))) {
            List<String> lines = reader.lines().filter(line -> !line.trim().isEmpty()).collect(Collectors.toList());
            for (String line : lines) {
                String[] arr = line.split("=");
                SensitiveWord entity = new SensitiveWord();
                entity.setWord(arr[0].trim());
                entity.setReplacement(arr.length > 1 ? arr[1].trim() : "推荐");
                entity.setIsActive(true);
                entity.setCreatedAt(LocalDateTime.now());
                sensitiveWordMapper.insert(entity);
            }
        } catch (Exception ignored) {
        }
    }

    private void reloadFromDatabase(List<SensitiveWord> words) {
        replacements.clear();
        for (SensitiveWord word : words) {
            if (Boolean.TRUE.equals(word.getIsActive())) {
                replacements.put(word.getWord(), word.getReplacement());
            }
        }
        rebuildAutomaton();
    }

    private void rebuildAutomaton() {
        AcAutomaton latestAutomaton = new AcAutomaton();
        for (String word : new ArrayList<>(replacements.keySet())) {
            latestAutomaton.addWord(word);
        }
        latestAutomaton.build();
        this.automaton = latestAutomaton;
    }
}
