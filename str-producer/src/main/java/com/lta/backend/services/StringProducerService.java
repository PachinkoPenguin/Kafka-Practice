package com.lta.backend.services;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Service
public class StringProducerService {
    
    private static final Logger log = LoggerFactory.getLogger(StringProducerService.class);
    private static final String TOPIC_1 = "topico1";
    private static final String TOPIC_2 = "topico2";
    private static final String TOPIC_3 = "topico3";

    @Autowired
    private KafkaTemplate<String,String> kafkaTemplate;

    public void sendMessage(String message){
        String topicName = determineTopicName(message);
        
        log.info("Sending message to topic: {}", topicName);
        
        kafkaTemplate.send(topicName, message).whenComplete((result,ex) -> {
           if(ex != null){
               log.error("Error al enviar el mensaje: {}",ex.getMessage());
           } else {
               log.info("Mensaje enviado con éxito: {}",result.getProducerRecord().value());
               log.info("Topic: {}, Particion {}, Offset {}", 
                       result.getRecordMetadata().topic(),
                       result.getRecordMetadata().partition(),
                       result.getRecordMetadata().offset());
           }
        });
    }
    
    private String determineTopicName(String message) {
        String lowerCaseMessage = message.toLowerCase();
        
        if (lowerCaseMessage.contains("topico1")) {
            return TOPIC_1;
        } else if (lowerCaseMessage.contains("topico2")) {
            return TOPIC_2;
        } else if (lowerCaseMessage.contains("topico3")) {
            return TOPIC_3;
        } else {
            // Si no se encuentra ninguna palabra clave, se envía al tópico1 por defecto
            return TOPIC_1;
        }
    }
}
