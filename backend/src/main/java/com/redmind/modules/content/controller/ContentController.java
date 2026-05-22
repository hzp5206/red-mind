package com.redmind.modules.content.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.modules.content.dto.PurifyRequest;
import com.redmind.modules.content.dto.PurifyResponse;
import com.redmind.modules.content.service.ContentPurifyService;
import javax.validation.Valid;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/content")
public class ContentController {

    private final ContentPurifyService contentPurifyService;

    public ContentController(ContentPurifyService contentPurifyService) {
        this.contentPurifyService = contentPurifyService;
    }

    @PostMapping("/purify")
    public ApiResponse<PurifyResponse> purify(@Valid @RequestBody PurifyRequest request) {
        return ApiResponse.success(contentPurifyService.purify(request.getContent()));
    }
}
