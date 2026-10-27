package com.splitmate.api;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    @GetMapping("/healthz")
    public java.util.Map<String, String> health() {
        return java.util.Map.of("status", "ok");
    }
}
