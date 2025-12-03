package com.studypals.domain.devManage.api;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class DevController {

    @GetMapping("/ping")
    public ResponseEntity<String> pong() {
        return ResponseEntity.ok("pong");
    }
}
