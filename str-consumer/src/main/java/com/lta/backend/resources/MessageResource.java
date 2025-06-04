package com.lta.backend.resources;

import com.lta.backend.model.KafkaMessage;
import com.lta.backend.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@CrossOrigin(origins = "http://localhost:3000")
public class MessageResource {

    @Autowired
    private MessageService messageService;

    @GetMapping
    public ResponseEntity<List<KafkaMessage>> getAllMessages() {
        return ResponseEntity.ok(messageService.getAllMessages());
    }
}
