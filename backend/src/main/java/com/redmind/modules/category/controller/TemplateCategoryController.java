package com.redmind.modules.category.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.category.dto.TemplateCategoryOptionResponse;
import com.redmind.modules.category.dto.TemplateCategorySaveRequest;
import com.redmind.modules.category.service.TemplateCategoryService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/template-categories")
public class TemplateCategoryController {

    private final TemplateCategoryService templateCategoryService;
    private final AdminGuard adminGuard;

    public TemplateCategoryController(TemplateCategoryService templateCategoryService, AdminGuard adminGuard) {
        this.templateCategoryService = templateCategoryService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    public ApiResponse<List<TemplateCategoryOptionResponse>> list() {
        return ApiResponse.success(templateCategoryService.listActiveOptions());
    }

    @GetMapping("/admin")
    public ApiResponse<List<TemplateCategoryOptionResponse>> adminList() {
        adminGuard.check(AdminPermission.CATEGORY_MANAGE);
        return ApiResponse.success(templateCategoryService.adminList());
    }

    @PostMapping("/admin")
    public ApiResponse<TemplateCategoryOptionResponse> save(@Valid @RequestBody TemplateCategorySaveRequest request) {
        adminGuard.check(AdminPermission.CATEGORY_MANAGE);
        return ApiResponse.success(templateCategoryService.save(request));
    }

    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminGuard.check(AdminPermission.CATEGORY_MANAGE);
        templateCategoryService.delete(id);
        return ApiResponse.success(null);
    }
}
