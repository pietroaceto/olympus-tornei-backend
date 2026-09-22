package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.BracketResponse;
import com.olympustornei.backend.dto.GenerateBracketRequest;
import com.olympustornei.backend.dto.ManualBracketRequest;
import com.olympustornei.backend.service.BracketService;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories/{categoryId}/bracket")
public class BracketAdminController {

    private final BracketService bracketService;

    public BracketAdminController(BracketService bracketService) {
        this.bracketService = bracketService;
    }

    @PostMapping("/generate")
    public BracketResponse generate(@PathVariable Long categoryId, @Valid @RequestBody GenerateBracketRequest request) {
        return bracketService.generateBracket(categoryId, request.qualifiedCount());
    }

    @PostMapping("/generate-random")
    public BracketResponse generateRandom(@PathVariable Long categoryId) {
        return bracketService.generateRandomBracket(categoryId);
    }

    @PostMapping("/generate-manual")
    public BracketResponse generateManual(@PathVariable Long categoryId, @Valid @RequestBody ManualBracketRequest request) {
        return bracketService.generateManualBracket(categoryId, request.slots());
    }

    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@PathVariable Long categoryId) {
        bracketService.resetBracket(categoryId);
    }
}
