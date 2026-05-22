package com.redmind.modules.library.controller;

import com.redmind.common.api.ApiResponse;
import com.redmind.modules.library.dto.LibraryItemResponse;
import com.redmind.modules.library.service.LibraryService;
import java.util.List;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/library")
public class LibraryController {

    private final LibraryService libraryService;

    public LibraryController(LibraryService libraryService) {
        this.libraryService = libraryService;
    }

    @GetMapping("/collections")
    public ApiResponse<List<LibraryItemResponse>> collections() {
        return ApiResponse.success(libraryService.myCollections());
    }

    @DeleteMapping("/collections/{id}")
    public ApiResponse<Void> delete(@PathVariable Long id) {
        libraryService.deleteCollection(id);
        return ApiResponse.success(null);
    }
}
