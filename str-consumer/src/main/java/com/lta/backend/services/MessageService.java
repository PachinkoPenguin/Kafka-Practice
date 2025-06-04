package com.lta.backend.services;

import com.lta.backend.model.KafkaMessage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.messaging.simp.SimpMessagingTemplate;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

@Service
public class MessageService {

    private static final Logger log = LoggerFactory.getLogger(MessageService.class);
    private final List<KafkaMessage> messages = new CopyOnWriteArrayList<>();
    private static final int MAX_MESSAGES = 100; // Máximo número de mensajes a almacenar
    
    @Autowired
    private SimpMessagingTemplate messagingTemplate;

    public void addMessage(KafkaMessage message) {
        log.info("Mensaje recibido: {}", message);
        
        // Añadir mensaje a la lista
        messages.add(message);
        
        // Si hay más mensajes que el límite, eliminar los más antiguos
        while (messages.size() > MAX_MESSAGES) {
            messages.remove(0);
        }
        
        // Enviar mensaje a través de WebSocket
        messagingTemplate.convertAndSend("/topic/messages", message);
    }

    public List<KafkaMessage> getAllMessages() {
        return new ArrayList<>(messages);
    }
}
