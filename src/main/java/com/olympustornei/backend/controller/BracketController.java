package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.BracketResponse;
import com.olympustornei.backend.service.BracketService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/categories/{categoryId}/bracket")
public class BracketController {

    private final BracketService bracketService;

    public BracketController(BracketService bracketService) {
        this.bracketService = bracketService;
    }

    @GetMapping
    public BracketResponse getBracket(@PathVariable Long categoryId) {
        return bracketService.getBracket(categoryId);
    }
}
