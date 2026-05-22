package com.redmind.modules.template.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.redmind.common.exception.BizException;
import com.redmind.modules.admin.service.OperationLogService;
import com.redmind.modules.common.dto.PageResponse;
import com.redmind.modules.template.dto.TemplateQueryRequest;
import com.redmind.modules.template.dto.TemplateSaveRequest;
import com.redmind.modules.template.entity.Template;
import com.redmind.modules.template.mapper.TemplateMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

@Service
public class TemplateService {

    private final TemplateMapper templateMapper;
    private final OperationLogService operationLogService;

    public TemplateService(TemplateMapper templateMapper,
                           OperationLogService operationLogService) {
        this.templateMapper = templateMapper;
        this.operationLogService = operationLogService;
    }

    public List<Template> listByCategory(String category) {
        LambdaQueryWrapper<Template> wrapper = new LambdaQueryWrapper<Template>()
            .eq(Template::getIsActive, true)
            .orderByDesc(Template::getId);
        if (StringUtils.isNotBlank(category)) {
            wrapper.eq(Template::getCategory, category);
        }
        return templateMapper.selectList(wrapper);
    }

    public PageResponse<Template> adminList(TemplateQueryRequest request, Integer pageNum, Integer pageSize) {
        LambdaQueryWrapper<Template> wrapper = new LambdaQueryWrapper<Template>()
            .orderByDesc(Template::getId);
        if (request != null) {
            if (StringUtils.isNotBlank(request.getCategory())) {
                wrapper.eq(Template::getCategory, request.getCategory());
            }
            if (request.getIsActive() != null) {
                wrapper.eq(Template::getIsActive, request.getIsActive());
            }
            if (StringUtils.isNotBlank(request.getKeyword())) {
                wrapper.and(query -> query
                    .like(Template::getTitle, request.getKeyword())
                    .or()
                    .like(Template::getContentExample, request.getKeyword())
                    .or()
                    .like(Template::getTags, request.getKeyword()));
            }
        }
        Page<Template> page = new Page<>(pageNum, pageSize);
        Page<Template> result = templateMapper.selectPage(page, wrapper);
        return new PageResponse<>(result.getTotal(), result.getRecords());
    }

    public Template create(TemplateSaveRequest request) {
        Template template = new Template();
        fillTemplate(template, request);
        template.setCreatedAt(LocalDateTime.now());
        templateMapper.insert(template);
        operationLogService.log("template", "create", "template", template.getId(), "创建模板：" + template.getTitle());
        return template;
    }

    public Template update(Long id, TemplateSaveRequest request) {
        Template template = templateMapper.selectById(id);
        if (template == null) {
            throw new BizException("模板不存在");
        }
        fillTemplate(template, request);
        templateMapper.updateById(template);
        operationLogService.log("template", "update", "template", template.getId(), "更新模板：" + template.getTitle());
        return template;
    }

    public void delete(Long id) {
        Template template = templateMapper.selectById(id);
        templateMapper.deleteById(id);
        operationLogService.log("template", "delete", "template", id, "删除模板：" + (template == null ? id : template.getTitle()));
    }

    public void toggle(Long id, Boolean active) {
        Template template = templateMapper.selectById(id);
        if (template == null) {
            throw new BizException("模板不存在");
        }
        template.setIsActive(active);
        templateMapper.updateById(template);
        operationLogService.log("template", "toggle", "template", template.getId(), "更新模板状态为：" + (Boolean.TRUE.equals(active) ? "启用" : "停用"));
    }

    private void fillTemplate(Template template, TemplateSaveRequest request) {
        template.setCategory(request.getCategory());
        template.setTitle(request.getTitle());
        template.setContentExample(request.getContentExample());
        template.setTags(request.getTags());
        template.setStyle(request.getStyle());
        template.setIsActive(request.getIsActive());
    }
}
