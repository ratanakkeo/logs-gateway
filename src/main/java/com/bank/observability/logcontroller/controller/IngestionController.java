package com.bank.observability.logcontroller.controller;

import com.bank.observability.logcontroller.model.LogPayload;
import com.bank.observability.logcontroller.service.LogIngestionService;
import jakarta.validation.Valid;
import org.springframework.http.ResponseEntity;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@Validated
@RestController
@RequestMapping("/v1/logs")
public class IngestionController {

    private final LogIngestionService logIngestionService;

    public IngestionController(LogIngestionService logIngestionService) {
        this.logIngestionService = logIngestionService;
    }

    @PostMapping("/ingest")
    public ResponseEntity<Void> ingest(@Valid @RequestBody LogPayload payload) {
        logIngestionService.ingestAsync(payload);
        return ResponseEntity.accepted().build();
    }
}
