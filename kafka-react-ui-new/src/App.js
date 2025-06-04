import React, { useState, useEffect } from 'react';
import SockJS from 'sockjs-client';
import { Client } from '@stomp/stompjs';
import './App.css';

function App() {
  const [messages, setMessages] = useState([]);
  const [stompClient, setStompClient] = useState(null);
  const [connected, setConnected] = useState(false);

  // Configuración inicial de WebSocket
  useEffect(() => {
    // Cargar mensajes iniciales
    fetch('http://localhost:8100/api/messages')
      .then(response => response.json())
      .then(data => {
        setMessages(data);
      })
      .catch(error => console.error('Error fetching initial messages:', error));

    // Configurar conexión WebSocket
    const socket = new SockJS('http://localhost:8100/kafka-websocket');
    const client = new Client({
      webSocketFactory: () => socket,
      onConnect: () => {
        setConnected(true);
        client.subscribe('/topic/messages', message => {
          try {
            const receivedMessage = JSON.parse(message.body);
            setMessages(prevMessages => {
              // Evitar duplicados basados en offset y topic
              const exists = prevMessages.some(m => 
                m.offset === receivedMessage.offset && 
                m.topic === receivedMessage.topic && 
                m.partition === receivedMessage.partition
              );
              
              if (exists) {
                return prevMessages;
              }
              
              // Mantener solo los últimos 100 mensajes
              const newMessages = [receivedMessage, ...prevMessages];
              return newMessages.slice(0, 100);
            });
          } catch (error) {
            console.error('Error processing message:', error);
          }
        });
      },
      onDisconnect: () => {
        setConnected(false);
      },
      debug: str => {
        console.log(str);
      },
    });

    client.activate();
    setStompClient(client);

    // Limpiar al desmontar
    return () => {
      if (client) {
        client.deactivate();
      }
    };
  }, []);

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
