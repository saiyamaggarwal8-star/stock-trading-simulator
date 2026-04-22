package com.trading.controller;

import com.trading.service.GeminiService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.multipart.MultipartFile;

import java.util.HashMap;
import java.util.Map;

@RestController
@RequestMapping("/api/chat")
public class ChatController {

    @Autowired
    private GeminiService geminiService;

    @PostMapping
    public ResponseEntity<Map<String, String>> chat(
            @RequestParam("message") String message,
            @RequestParam(value = "image", required = false) MultipartFile image) {
        
        String reply = geminiService.generateResponse(message, image);
        Map<String, String> response = new HashMap<>();
        response.put("reply", reply);
        
        return ResponseEntity.ok(response);
    }
}
