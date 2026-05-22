package com.redmind.modules.template.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.common.dto.PageResponse;
import com.redmind.modules.template.dto.TemplateQueryRequest;
import com.redmind.modules.template.dto.TemplateSaveRequest;
import com.redmind.modules.template.entity.Template;
import com.redmind.modules.template.service.TemplateService;
import java.util.List;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/templates")
public class TemplateController {

    private final TemplateService templateService;
    private final AdminGuard adminGuard;

    public TemplateController(TemplateService templateService, AdminGuard adminGuard) {
        this.templateService = templateService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    public ApiResponse<List<Template>> list(@RequestParam(required = false) String category) {
        return ApiResponse.success(templateService.listByCategory(category));
    }

    @GetMapping("/admin")
    public ApiResponse<PageResponse<Template>> adminList(TemplateQueryRequest request,
                                                         @RequestParam(defaultValue = "1") Integer page,
                                                         @RequestParam(defaultValue = "10") Integer pageSize) {
        adminGuard.check(AdminPermission.TEMPLATE_MANAGE);
        return ApiResponse.success(templateService.adminList(request, page, pageSize));
    }

    @PostMapping("/admin")
    public ApiResponse<Template> create(@Valid @RequestBody TemplateSaveRequest request) {
        adminGuard.check(AdminPermission.TEMPLATE_MANAGE);
        return ApiResponse.success(templateService.create(request));
    }

    @PutMapping("/admin/{id}")
    public ApiResponse<Template> update(@PathVariable Long id, @Valid @RequestBody TemplateSaveRequest request) {
        adminGuard.check(AdminPermission.TEMPLATE_MANAGE);
        return ApiResponse.success(templateService.update(id, request));
    }

    @PatchMapping("/admin/{id}/status")
    public ApiResponse<Void> toggle(@PathVariable Long id, @RequestParam Boolean active) {
        adminGuard.check(AdminPermission.TEMPLATE_MANAGE);
        templateService.toggle(id, active);
        return ApiResponse.success(null);
    }

    @DeleteMapping("/admin/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminGuard.check(AdminPermission.TEMPLATE_MANAGE);
        templateService.delete(id);
        return ApiResponse.success(null);
    }
}
