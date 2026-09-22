package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.CategoryResponse;
import com.olympustornei.backend.service.CategoryService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public")
public class CategoryController {

    private final CategoryService categoryService;

    public CategoryController(CategoryService categoryService) {
        this.categoryService = categoryService;
    }

    @GetMapping("/tournaments/{tournamentId}/categories")
    public List<CategoryResponse> listByTournament(@PathVariable Long tournamentId) {
        return categoryService.listByTournament(tournamentId);
    }

    @GetMapping("/categories/{id}")
    public CategoryResponse getById(@PathVariable Long id) {
        return categoryService.getById(id);
    }
}
