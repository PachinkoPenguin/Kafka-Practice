import React, { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import './App.css';

function App() {
  const [messages, setMessages] = useState([]);
  const [stompClient, setStompClient] = useState(null);
  const [connected, setConnected] = useState(false);
  const [lastRefresh, setLastRefresh] = useState(new Date());

  // Cargar mensajes iniciales
  useEffect(() => {
    fetch('http://localhost:8100/api/messages')
      .then(response => response.json())
      .then(data => {
        console.log("Mensajes iniciales cargados:", data.length);
        setMessages(data);
      })
      .catch(error => console.error('Error fetching initial messages:', error));
  }, []);

  // Configuración de WebSocket
  useEffect(() => {
    console.log("Configurando conexión WebSocket...");
    
    // Crear conexión WebSocket
    const socket = new SockJS('http://localhost:8100/kafka-websocket');
    const client = new Client({
      webSocketFactory: () => socket,
      reconnectDelay: 5000,
      heartbeatIncoming: 4000,
      heartbeatOutgoing: 4000,
      onConnect: () => {
        console.log("WebSocket conectado exitosamente");
        setConnected(true);
        
        // Suscribirse al tópico de mensajes
        client.subscribe('/topic/messages', message => {
          try {
            console.log("Mensaje recibido vía WebSocket:", message.body);
            const receivedMessage = JSON.parse(message.body);
            
            setMessages(prevMessages => {
              // Evitar duplicados
              const exists = prevMessages.some(m => 
                m.offset === receivedMessage.offset && 
                m.topic === receivedMessage.topic && 
                m.partition === receivedMessage.partition
              );
              
              if (exists) {
                console.log("Mensaje duplicado descartado");
                return prevMessages;
              }
              
              console.log("Nuevo mensaje añadido");
              // Añadir el nuevo mensaje al principio y mantener los últimos 100
              const newMessages = [receivedMessage, ...prevMessages];
              setLastRefresh(new Date());
              return newMessages.slice(0, 100);
            });
          } catch (error) {
            console.error('Error processing message:', error);
          }
        });
      },
      onStompError: frame => {
        console.error('STOMP error:', frame);
      },
      onDisconnect: () => {
        console.log("WebSocket desconectado");
        setConnected(false);
      },
      onWebSocketClose: () => {
        console.log("Conexión WebSocket cerrada");
        setConnected(false);
      },
    });

    client.activate();
    setStompClient(client);

    // Limpiar al desmontar
    return () => {
      console.log("Limpiando conexión WebSocket");
      if (client && client.connected) {
        client.deactivate();
      }
    };
  }, []);

  // Reconexión automática si se pierde la conexión
  useEffect(() => {
    if (!connected && stompClient) {
      const timer = setTimeout(() => {
        console.log("Intento de reconexión automática...");
        try {
          stompClient.activate();
        } catch (error) {
          console.error("Error en reconexión:", error);
        }
      }, 5000);
      
      return () => clearTimeout(timer);
    }
  }, [connected, stompClient]);

  // Refrescar mensajes cada 30 segundos como respaldo
  useEffect(() => {
    const intervalId = setInterval(() => {
      if (connected) {
        console.log("Refrescando mensajes...");
        fetch('http://localhost:8100/api/messages')
          .then(response => response.json())
          .then(data => {
            console.log("Mensajes refrescados:", data.length);
            setMessages(data);
            setLastRefresh(new Date());
          })
          .catch(error => console.error('Error refreshing messages:', error));
      }
    }, 30000);
    
    return () => clearInterval(intervalId);
  }, [connected]);

  // Renderizar los mensajes agrupados por tópico y partición
  const renderMessageGroups = () => {
    // Agrupar mensajes por tópico y partición
    const groups = {};
    messages.forEach(msg => {
      if (msg && msg.topic && msg.partition !== undefined) {
        const key = `${msg.topic}-${msg.partition}`;
        if (!groups[key]) {
          groups[key] = [];
        }
        groups[key].push(msg);
      }
    });

    return Object.entries(groups).map(([key, groupMessages]) => {
      const [topic, partition] = key.split('-');
      return (
        <div key={key} className="message-group">
          <h3>
            Tópico: {topic}, Partición: {partition}
            <span className="message-count">
              ({groupMessages.length} mensajes)
            </span>
          </h3>
          <div className="messages">
            {groupMessages.map((msg, idx) => (
              <div key={`${msg.offset}-${idx}`} className="message">
                <div className="message-content">{msg.content}</div>
                <div className="message-info">
                  Offset: {msg.offset} | {new Date(msg.timestamp).toLocaleTimeString()}
                </div>
              </div>
            ))}
          </div>
        </div>
      );
    });
  };

  return (
    <div className="app">
      <header className="app-header">
        <h1>Kafka Message Viewer</h1>
        <div className="connection-status">
          Estado: {connected ? 'Conectado' : 'Desconectado'}
          <div className="last-refresh">
            Última actualización: {lastRefresh.toLocaleTimeString()}
          </div>
        </div>
      </header>
      <div className="app-content">
        {messages.length === 0 ? (
          <div className="no-messages">
            No hay mensajes. Envía mensajes desde Postman al productor.
          </div>
        ) : (
          <div className="message-groups">{renderMessageGroups()}</div>
        )}
      </div>
    </div>
  );
}

export default App;
