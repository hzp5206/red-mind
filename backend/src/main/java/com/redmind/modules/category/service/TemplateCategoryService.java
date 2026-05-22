package com.redmind.modules.category.service;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.redmind.common.exception.BizException;
import com.redmind.modules.admin.service.OperationLogService;
import com.redmind.modules.category.dto.TemplateCategoryOptionResponse;
import com.redmind.modules.category.dto.TemplateCategorySaveRequest;
import com.redmind.modules.category.entity.TemplateCategory;
import com.redmind.modules.category.mapper.TemplateCategoryMapper;
import java.time.LocalDateTime;
import java.util.List;
import java.util.stream.Collectors;
import org.springframework.stereotype.Service;

@Service
public class TemplateCategoryService {

    private final TemplateCategoryMapper templateCategoryMapper;
    private final OperationLogService operationLogService;

    public TemplateCategoryService(TemplateCategoryMapper templateCategoryMapper,
                                   OperationLogService operationLogService) {
        this.templateCategoryMapper = templateCategoryMapper;
        this.operationLogService = operationLogService;
    }

    public List<TemplateCategoryOptionResponse> listActiveOptions() {
        return templateCategoryMapper.selectList(new LambdaQueryWrapper<TemplateCategory>()
                .eq(TemplateCategory::getIsActive, true)
                .orderByAsc(TemplateCategory::getSortOrder)
                .orderByAsc(TemplateCategory::getId))
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public List<TemplateCategoryOptionResponse> adminList() {
        return templateCategoryMapper.selectList(new LambdaQueryWrapper<TemplateCategory>()
                .orderByAsc(TemplateCategory::getSortOrder)
                .orderByAsc(TemplateCategory::getId))
            .stream()
            .map(this::toResponse)
            .collect(Collectors.toList());
    }

    public TemplateCategoryOptionResponse save(TemplateCategorySaveRequest request) {
        TemplateCategory entity = request.getId() == null ? new TemplateCategory() : templateCategoryMapper.selectById(request.getId());
        if (entity == null) {
            throw new BizException("分类不存在");
        }
        entity.setCategoryCode(request.getCategoryCode());
        entity.setCategoryName(request.getCategoryName());
        entity.setSortOrder(request.getSortOrder() == null ? 0 : request.getSortOrder());
        entity.setIsActive(request.getIsActive() == null ? true : request.getIsActive());
        if (entity.getId() == null) {
            entity.setCreatedAt(LocalDateTime.now());
            templateCategoryMapper.insert(entity);
            operationLogService.log("category", "create", "template_category", entity.getId(), "新增模板分类：" + entity.getCategoryName());
        } else {
            templateCategoryMapper.updateById(entity);
            operationLogService.log("category", "update", "template_category", entity.getId(), "更新模板分类：" + entity.getCategoryName());
        }
        return toResponse(entity);
    }

    public void delete(Long id) {
        TemplateCategory entity = templateCategoryMapper.selectById(id);
        templateCategoryMapper.deleteById(id);
        operationLogService.log("category", "delete", "template_category", id, "删除模板分类：" + (entity == null ? id : entity.getCategoryName()));
    }

    private TemplateCategoryOptionResponse toResponse(TemplateCategory item) {
        return TemplateCategoryOptionResponse.builder()
            .id(item.getId())
            .label(item.getCategoryName())
            .value(item.getCategoryCode())
            .sortOrder(item.getSortOrder())
            .isActive(item.getIsActive())
            .build();
    }
}
