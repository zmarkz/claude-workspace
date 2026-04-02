import client from './client';

export const getKiteStatus = () =>
  client.get<{ connected: boolean; configured: boolean }>('/kite/status').then((r) => r.data);

export const connectKite = (accessToken: string) =>
  client.post('/kite/connect', { accessToken }).then((r) => r.data);

export const syncKiteHoldings = (portfolioId: number) =>
  client.post<{ synced: number; total: number }>(`/kite/sync?portfolioId=${portfolioId}`).then((r) => r.data);

export const getKiteLoginUrl = () => '/api/kite/login';
