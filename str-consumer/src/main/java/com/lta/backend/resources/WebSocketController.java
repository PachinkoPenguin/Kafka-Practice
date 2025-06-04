package com.lta.backend.resources;

import com.lta.backend.model.KafkaMessage;
import com.lta.backend.services.MessageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.SendTo;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Controller;

import java.util.List;

@Controller
public class WebSocketController {

    private static final Logger log = LoggerFactory.getLogger(WebSocketController.class);

    @Autowired
    private MessageService messageService;
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    @MessageMapping("/send")
    @SendTo("/topic/messages")
    public KafkaMessage sendMessage(KafkaMessage message) {
        log.info("Mensaje recibido a través de WebSocket: {}", message);
        return message;
    }
    
    // Método para enviar mensajes existentes a un cliente que se acaba de conectar
    @MessageMapping("/fetchall")
    public void fetchAllMessages() {
        List<KafkaMessage> allMessages = messageService.getAllMessages();
        log.info("Enviando {} mensajes existentes al cliente", allMessages.size());
        
        // Enviar todos los mensajes existentes al cliente
        allMessages.forEach(message -> 
            messagingTemplate.convertAndSend("/topic/messages", message)
        );
    }
}
