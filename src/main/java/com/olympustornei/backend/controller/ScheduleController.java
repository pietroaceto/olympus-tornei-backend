package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.RoundResponse;
import com.olympustornei.backend.service.RoundRobinService;
import java.util.List;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/public/categories/{categoryId}/schedule")
public class ScheduleController {

    private final RoundRobinService roundRobinService;

    public ScheduleController(RoundRobinService roundRobinService) {
        this.roundRobinService = roundRobinService;
    }

    @GetMapping
    public List<RoundResponse> getSchedule(@PathVariable Long categoryId) {
        return roundRobinService.getSchedule(categoryId);
    }
}
