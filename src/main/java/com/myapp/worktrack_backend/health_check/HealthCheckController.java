package com.myapp.worktrack_backend.health_check;

import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("check-health")
public class HealthCheckController {

    @GetMapping("Health")
    public static String checkHealth() {
        return "ok";
    }
}
