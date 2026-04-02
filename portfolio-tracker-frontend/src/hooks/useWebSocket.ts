import { useEffect, useRef } from 'react';
import { Client } from '@stomp/stompjs';
import SockJS from 'sockjs-client';
import { usePortfolioStore } from '../store/portfolioStore';

export const useWebSocket = () => {
  const clientRef = useRef<Client | null>(null);
  const updateLivePrices = usePortfolioStore((s) => s.updateLivePrices);

  useEffect(() => {
    const token = localStorage.getItem('token');
    if (!token) return;

    const stompClient = new Client({
      webSocketFactory: () => new SockJS('/ws'),
      connectHeaders: { Authorization: `Bearer ${token}` },
      reconnectDelay: 5000,
      onConnect: () => {
        stompClient.subscribe('/topic/prices', (message) => {
          try {
            const prices: Record<string, number> = JSON.parse(message.body);
            updateLivePrices(prices);
          } catch (e) {
            console.error('WS parse error', e);
          }
        });
      },
    });

    stompClient.activate();
    clientRef.current = stompClient;

    return () => { stompClient.deactivate(); };
  }, [updateLivePrices]);
};
