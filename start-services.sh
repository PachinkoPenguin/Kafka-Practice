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
