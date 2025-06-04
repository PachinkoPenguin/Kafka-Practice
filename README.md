# Sistema de Mensajería Kafka con Spring Boot y React

Este proyecto implementa un sistema de mensajería basado en Kafka donde se envían mensajes desde Postman hacia un productor implementado en Spring Boot. El productor analiza el contenido del mensaje y lo envía dinámicamente a uno de tres tópicos de Kafka.

## Estructura del Proyecto

- **str-producer**: Aplicación Spring Boot que actúa como productor de Kafka
- **str-consumer**: Aplicación Spring Boot que actúa como consumidor de Kafka y proporciona una API REST/WebSocket
- **kafka-react-ui**: Aplicación React que muestra los mensajes en tiempo real

## Requisitos Previos

- Docker y Docker Compose
- Java 17+
- Maven
- Node.js y npm

## Iniciar el Sistema

### 1. Iniciar Kafka y Zookeeper

```bash
cd /home/ada/Development/School/Kafka
docker compose up -d
```

### 2. Iniciar el Productor

```bash
cd /home/ada/Development/School/Kafka/str-producer
./mvnw spring-boot:run
```

### 3. Iniciar el Consumidor

```bash
cd /home/ada/Development/School/Kafka/str-consumer
./mvnw spring-boot:run
```

### 4. Iniciar la Interfaz React

```bash
cd /home/ada/Development/School/Kafka/kafka-react-ui-new
npm install
npm start
```

## Enviar Mensajes con Postman

1. Abrir Postman
2. Crear una solicitud POST a `http://localhost:8000/producer`
3. En el cuerpo (Body) de la solicitud, seleccionar "raw" y tipo "Text"
4. Introducir mensajes que contengan las palabras clave:
   - "topico1" para enviar al tópico 1
   - "topico2" para enviar al tópico 2
   - "topico3" para enviar al tópico 3

Ejemplos de mensajes:
- `Este mensaje va al topico1, por defecto`
- `Este mensaje contiene topico2 y se enviará al segundo tópico`
- `Enviando mensaje importante al topico3`

## Visualizar Mensajes

Abrir la aplicación React en el navegador: http://localhost:3000 o http://localhost:3001

La interfaz mostrará los mensajes recibidos por cada consumidor, agrupados por tópico y partición, incluyendo:
- Contenido del mensaje
- Nombre del tópico
- Número de partición
- Offset
- Timestamp

## Herramientas Adicionales

Para visualizar los tópicos de Kafka y su estado, visita Kafdrop: http://localhost:19000
