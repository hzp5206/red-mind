package com.redmind.modules.history.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.modules.history.dto.HistoryPageResponse;
import com.redmind.modules.history.service.HistoryService;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/history")
public class HistoryController {

    private final HistoryService historyService;

    public HistoryController(HistoryService historyService) {
        this.historyService = historyService;
    }

    @GetMapping
    public ApiResponse<HistoryPageResponse> page(@RequestParam(defaultValue = "1") Integer page,
                                                 @RequestParam(defaultValue = "20") Integer limit,
                                                 @RequestParam(required = false) String style,
                                                 @RequestParam(required = false) String startDate,
                                                 @RequestParam(required = false) String endDate) {
        return ApiResponse.success(historyService.page(page, limit, style, startDate, endDate));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        historyService.delete(id);
        return ApiResponse.success(null);
    }

    @PostMapping("/{id}/collect")
    public ApiResponse<Void> collect(@PathVariable Long id) {
        historyService.collect(id);
        return ApiResponse.success(null);
    }
}
