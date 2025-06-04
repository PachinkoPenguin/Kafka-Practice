package com.lta.backend.listeners;

import com.lta.backend.model.KafkaMessage;
import com.lta.backend.services.MessageService;
import lombok.extern.log4j.Log4j2;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.kafka.annotation.TopicPartition;
import org.springframework.stereotype.Component;

@Log4j2
@Component
public class StrConsumerListener {

    @Autowired
    private MessageService messageService;

    // Tópico 1, Partición 0
    @KafkaListener(groupId = "topico1-group",
            topicPartitions = @TopicPartition(topic = "topico1", partitions = {"0"})
            ,containerFactory = "validMessageContainerFactory")
    public void topico1Partition0Listener(ConsumerRecord<String, String> record) {
        processMessage(record, "TOPICO1-P0");
    }

    // Tópico 1, Partición 1
    @KafkaListener(groupId = "topico1-group",
            topicPartitions = @TopicPartition(topic = "topico1", partitions = {"1"})
            ,containerFactory = "validMessageContainerFactory")
    public void topico1Partition1Listener(ConsumerRecord<String, String> record) {
        processMessage(record, "TOPICO1-P1");
    }

    // Tópico 2, Partición 0
    @KafkaListener(groupId = "topico2-group",
            topicPartitions = @TopicPartition(topic = "topico2", partitions = {"0"})
            ,containerFactory = "validMessageContainerFactory")
    public void topico2Partition0Listener(ConsumerRecord<String, String> record) {
        processMessage(record, "TOPICO2-P0");
    }

    // Tópico 2, Partición 1
    @KafkaListener(groupId = "topico2-group",
            topicPartitions = @TopicPartition(topic = "topico2", partitions = {"1"})
            ,containerFactory = "validMessageContainerFactory")
    public void topico2Partition1Listener(ConsumerRecord<String, String> record) {
        processMessage(record, "TOPICO2-P1");
    }

    // Tópico 3, Partición 0
    @KafkaListener(groupId = "topico3-group",
            topicPartitions = @TopicPartition(topic = "topico3", partitions = {"0"})
            ,containerFactory = "validMessageContainerFactory")
    public void topico3Partition0Listener(ConsumerRecord<String, String> record) {
        processMessage(record, "TOPICO3-P0");
    }

    // Tópico 3, Partición 1
    @KafkaListener(groupId = "topico3-group",
            topicPartitions = @TopicPartition(topic = "topico3", partitions = {"1"})
            ,containerFactory = "validMessageContainerFactory")
    public void topico3Partition1Listener(ConsumerRecord<String, String> record) {
        processMessage(record, "TOPICO3-P1");
    }

    private void processMessage(ConsumerRecord<String, String> record, String listenerName) {
        System.out.println(listenerName + " ::: Recibiendo mensaje: " + record.value() + 
                           ", Partición: " + record.partition() + ", Offset: " + record.offset());
        
        // Crear objeto de mensaje usando el constructor con parámetros
        KafkaMessage message = new KafkaMessage(
                record.value(),
                record.topic(),
                record.partition(),
                record.offset(),
                record.timestamp());
        
        // Añadir mensaje al servicio
        messageService.addMessage(message);
    }
}
