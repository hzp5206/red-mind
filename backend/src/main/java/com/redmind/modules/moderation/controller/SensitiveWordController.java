package com.redmind.modules.moderation.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.common.security.AdminGuard;
import com.redmind.common.security.AdminPermission;
import com.redmind.modules.moderation.dto.SensitiveWordImportRequest;
import com.redmind.modules.moderation.dto.SensitiveWordItemResponse;
import com.redmind.modules.moderation.dto.SensitiveWordSaveRequest;
import com.redmind.modules.moderation.service.SensitiveWordService;
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
@RequestMapping("/api/v1/admin/sensitive-words")
public class SensitiveWordController {

    private final SensitiveWordService sensitiveWordService;
    private final AdminGuard adminGuard;

    public SensitiveWordController(SensitiveWordService sensitiveWordService, AdminGuard adminGuard) {
        this.sensitiveWordService = sensitiveWordService;
        this.adminGuard = adminGuard;
    }

    @GetMapping
    public ApiResponse<List<SensitiveWordItemResponse>> list() {
        adminGuard.check(AdminPermission.SENSITIVE_WORD_MANAGE);
        return ApiResponse.success(sensitiveWordService.listAll());
    }

    @PostMapping
    public ApiResponse<Void> save(@Valid @RequestBody SensitiveWordSaveRequest request) {
        adminGuard.check(AdminPermission.SENSITIVE_WORD_MANAGE);
        sensitiveWordService.save(request.getId(), request.getWord(), request.getReplacement(), request.getIsActive());
        return ApiResponse.success(null);
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        adminGuard.check(AdminPermission.SENSITIVE_WORD_MANAGE);
        sensitiveWordService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/import")
    public ApiResponse<Integer> importWords(@Valid @RequestBody SensitiveWordImportRequest request) {
        adminGuard.check(AdminPermission.SENSITIVE_WORD_MANAGE);
        return ApiResponse.success(sensitiveWordService.importWords(request.getContent()));
    }

    @GetMapping("/export")
    public ApiResponse<String> exportWords() {
        adminGuard.check(AdminPermission.SENSITIVE_WORD_MANAGE);
        return ApiResponse.success(sensitiveWordService.exportWords());
    }
}
