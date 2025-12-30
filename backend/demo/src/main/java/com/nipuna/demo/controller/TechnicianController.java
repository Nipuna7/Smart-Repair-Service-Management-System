package com.nipuna.demo.controller;

import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/technician")
@CrossOrigin(origins = "*", maxAge = 3600)
public class TechnicianController {

    @GetMapping("/dashboard")
    public ResponseEntity<?> getDashboard(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Welcome to Technician Dashboard");
        response.put("user", authentication.getName());
        response.put("authorities", authentication.getAuthorities());
        return ResponseEntity.ok(response);
    }

    @GetMapping("/tasks")
    public ResponseEntity<?> getTasks(Authentication authentication) {
        Map<String, Object> response = new HashMap<>();
        response.put("message", "Technician Tasks");
        response.put("user", authentication.getName());
        return ResponseEntity.ok(response);
    }
}

