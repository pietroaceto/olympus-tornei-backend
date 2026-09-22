package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.RoundResponse;
import com.olympustornei.backend.service.RoundRobinService;
import java.util.List;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/admin/categories/{categoryId}/schedule")
public class ScheduleAdminController {

    private final RoundRobinService roundRobinService;

    public ScheduleAdminController(RoundRobinService roundRobinService) {
        this.roundRobinService = roundRobinService;
    }

    @PostMapping("/generate")
    public List<RoundResponse> generate(@PathVariable Long categoryId) {
        return roundRobinService.generateSchedule(categoryId);
    }
}
