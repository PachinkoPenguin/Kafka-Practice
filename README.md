# Sistema de Mensajería Kafka con Spring Boot y React

Este proyecto implementa un sistema de mensajería basado en Kafka donde se envían mensajes desde Postman hacia un productor implementado en Spring Boot. El productor analiza el contenido del mensaje y lo envía dinámicamente a uno de tres tópicos de Kafka (topico1, topico2, topico3). Cada tópico tiene dos particiones.

## Descripción del Sistema

El sistema permite:
1. Enviar mensajes a través de una API REST implementada con Spring Boot
2. Análisis del contenido del mensaje para determinar su destino (usando palabras clave)
3. Consumo de mensajes por partición específica de cada tópico
4. Comunicación en tiempo real mediante WebSocket a una interfaz React
5. Visualización organizada de los mensajes por tópico y partición

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

### Método 1: Script de inicio automático

```bash
cd Kafka
./start-services.sh
```

Este script iniciará automáticamente todos los servicios en el orden correcto:
1. Kafka y Zookeeper con Docker Compose
2. El productor Spring Boot
3. El consumidor Spring Boot
4. La interfaz React

Aquí está el contenido del script `start-services.sh` para referencia:

```bash
#!/bin/bash

echo "Iniciando servicios Kafka y aplicaciones..."

# Iniciar Kafka (asumiendo que se usa docker-compose)
echo "Iniciando Kafka con docker-compose..."
docker-compose up -d

# Esperar a que Kafka se inicie completamente
echo "Esperando a que Kafka se inicie (30 segundos)..."
sleep 30

# Iniciar el productor Spring Boot
echo "Iniciando productor Spring Boot..."
cd str-producer
mvn spring-boot:run &
PRODUCER_PID=$!
cd ..

# Esperar a que el productor se inicie
echo "Esperando a que el productor se inicie (20 segundos)..."
sleep 20

# Iniciar el consumidor Spring Boot
echo "Iniciando consumidor Spring Boot..."
cd str-consumer
mvn spring-boot:run &
CONSUMER_PID=$!
cd ..

# Esperar a que el consumidor se inicie
echo "Esperando a que el consumidor se inicie (20 segundos)..."
sleep 20

# Iniciar la aplicación React
echo "Iniciando aplicación React..."
cd kafka-react-ui-new
npm start &
REACT_PID=$!
cd ..

echo "Todos los servicios han sido iniciados."
echo "Productor (PID: $PRODUCER_PID) - http://localhost:8000"
echo "Consumidor (PID: $CONSUMER_PID) - http://localhost:8100"
echo "React UI (PID: $REACT_PID) - http://localhost:3000"

# Para detener, presiona Ctrl+C
trap "kill $PRODUCER_PID $CONSUMER_PID $REACT_PID; docker-compose down; echo 'Todos los servicios han sido detenidos.'" EXIT

# Mantener el script corriendo
echo "Presiona Ctrl+C para detener todos los servicios"
wait
```

Asegúrate de dar permisos de ejecución al script antes de usarlo:

```bash
chmod +x start-services.sh
```

### Método 2: Inicio manual

#### 1. Iniciar Kafka y Zookeeper

```bash
cd Kafka
docker compose up -d
```

#### 2. Iniciar el Productor

```bash
cd Kafka/str-producer
./mvnw spring-boot:run
```

#### 3. Iniciar el Consumidor

```bash
cd Kafka/str-consumer
./mvnw spring-boot:run
```

#### 4. Iniciar la Interfaz React

```bash
cd Kafka/kafka-react-ui-new
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

Los mensajes se actualizarán **automáticamente en tiempo real** cuando se envíe un nuevo mensaje desde Postman, sin necesidad de refrescar la página. La aplicación mantiene una conexión WebSocket activa con el backend y muestra el estado de la conexión en la parte superior de la página.

## Herramientas Adicionales

Para visualizar los tópicos de Kafka y su estado, visita Kafdrop: http://localhost:19000

## Solución de Problemas

### Si los mensajes no aparecen en tiempo real:

1. Verifica la consola del navegador para ver si hay errores de conexión WebSocket
2. Comprueba que el backend esté configurado correctamente para enviar mensajes por WebSocket
3. Asegúrate que el CORS esté correctamente configurado en el backend
4. La aplicación incluye un mecanismo de reconexión automática si se pierde la conexión WebSocket
5. Como respaldo, la interfaz refresca los mensajes cada 30 segundos mediante una petición HTTP

### Si hay problemas con Lombok:

Este proyecto utiliza Lombok para reducir el código repetitivo. Si encuentras errores como "Cannot find symbol":
1. Asegúrate de tener el plugin de Lombok instalado en tu IDE
2. Verifica que la dependencia de Lombok esté correctamente configurada en el pom.xml
3. Ejecuta `mvn clean install` para regenerar las clases

## Arquitectura del Sistema

```
┌─────────────┐     ┌─────────────┐     ┌─────────────────────┐
│   Postman   │────▶│  Producer   │────▶│ Kafka (3 tópicos,   │
└─────────────┘     │ (Spring Boot)│     │  2 particiones c/u) │
                    └─────────────┘     └──────────┬──────────┘
                                                  │
                                                  ▼
┌─────────────┐     ┌─────────────┐     ┌─────────────────────┐
│   React UI  │◀────│  Consumer   │◀────│ 6 consumidores      │
│  (Browser)  │     │ (Spring Boot)│     │ (1 por partición)   │
└─────────────┘     └─────────────┘     └─────────────────────┘
     WebSocket/REST
```

## Flujo de Trabajo

1. El usuario envía un mensaje mediante Postman al endpoint del productor
2. El productor analiza el contenido del mensaje para determinar el tópico de destino
3. El mensaje es enviado al tópico correspondiente en Kafka
4. Los consumidores específicos de cada partición reciben el mensaje
5. El consumidor procesa el mensaje y lo envía a la interfaz React a través de WebSocket
6. La interfaz React muestra el mensaje recibido en tiempo real

## Detalles de Implementación

- **Productor**: Utiliza Spring Kafka para enviar mensajes a los tópicos
- **Consumidor**: Implementa listeners específicos para cada partición de cada tópico
- **API REST**: Para consultar históricos de mensajes
- **WebSocket**: Para comunicación en tiempo real entre el backend y el frontend
- **React**: Interfaz de usuario que muestra los mensajes organizados por tópico y partición

## Solución de Problemas Comunes

### Errores en el Consumidor

Si encuentras errores relacionados con la variable `log` en el consumidor:
```
cannot find symbol symbol: variable log location: class ...
```
Este error puede ocurrir si la anotación `@Log4j2` no está siendo procesada correctamente. Asegúrate de que:
- La dependencia de Lombok está correctamente configurada
- La herramienta de procesamiento de anotaciones está habilitada en tu IDE

### Errores en React

Si la aplicación React muestra el error `Cannot find module 'resolve'`:
```
Error: Cannot find module 'resolve'
```
Este problema puede solucionarse de las siguientes maneras:
1. Instalar el módulo faltante: `npm install resolve`
2. Si persiste el error, crear un nuevo proyecto React y migrar el código

### Problemas de CORS

Si experimentas errores de CORS al conectar el frontend con el backend:
1. Asegúrate de que la configuración CORS en `StringConsumerConfig.java` incluye los orígenes correctos
2. Verifica que el puerto de tu aplicación React (3000 o 3001) esté permitido en la configuración

### Problemas con Docker

Si hay problemas con los contenedores de Docker:
1. Detén los contenedores: `docker compose down`
2. Elimina los volúmenes si es necesario: `docker compose down -v`
3. Reinicia los contenedores: `docker compose up -d`
