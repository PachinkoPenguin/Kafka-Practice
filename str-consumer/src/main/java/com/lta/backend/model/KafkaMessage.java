package com.lta.backend.model;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class KafkaMessage {
    private String content;
    private String topic;
    private int partition;
    private long offset;
    private long timestamp;
    // No se necesitan constructores, getters, setters ni toString() 
    // gracias a las anotaciones Lombok (@Data, @NoArgsConstructor, @AllArgsConstructor)
}
