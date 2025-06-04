package com.lta.backend.resources;

import com.lta.backend.model.KafkaMessage;
import com.lta.backend.services.MessageService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.stereotype.Controller;

@Controller
public class WebSocketController {

    @Autowired
    private MessageService messageService;

    @MessageMapping("/send")
    @SendTo("/topic/messages")
    public KafkaMessage sendMessage(KafkaMessage message) {
        return message;
    }
}
