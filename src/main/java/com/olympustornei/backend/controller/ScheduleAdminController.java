package com.olympustornei.backend.controller;

import com.olympustornei.backend.dto.RoundResponse;
import com.olympustornei.backend.service.RoundRobinService;
import java.util.List;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseStatus;
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

    /**
     * Reset esplicito: cancella giornate/partite/risultati del girone e sblocca
     * la categoria, così l'admin può correggere la rosa e rigenerare il calendario.
     */
    @PostMapping("/reset")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void reset(@PathVariable Long categoryId) {
        roundRobinService.resetGirone(categoryId);
    }
}
