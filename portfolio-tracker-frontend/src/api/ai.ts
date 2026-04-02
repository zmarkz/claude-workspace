import client from './client';
import type { PortfolioAIAnalysis, RebalancingSuggestion, StockRecommendation, ChatMessage } from '../types';

export const getAIStatus = () =>
  client.get<{ configured: boolean }>('/ai/status').then((r) => r.data);

export const analyzePortfolio = (portfolioId: number) =>
  client.post<PortfolioAIAnalysis>(`/ai/portfolio/${portfolioId}/analyze`).then((r) => r.data);

export const getRebalancingSuggestions = (portfolioId: number) =>
  client.get<RebalancingSuggestion[]>(`/ai/portfolio/${portfolioId}/rebalance`).then((r) => r.data);

export const getStockRecommendation = (portfolioId: number, symbol: string) =>
  client.get<StockRecommendation>(`/ai/portfolio/${portfolioId}/stock/${symbol}`).then((r) => r.data);

export const chatWithPortfolio = async (
  portfolioId: number,
  message: string,
  history: ChatMessage[],
  onToken: (token: string) => void,
  onDone: () => void,
  onError: (msg: string) => void
) => {
  const token = localStorage.getItem('token');
  const response = await fetch(`/api/ai/portfolio/${portfolioId}/chat`, {
    method: 'POST',
    headers: {
      'Content-Type': 'application/json',
      Authorization: `Bearer ${token}`,
      Accept: 'text/event-stream',
    },
    body: JSON.stringify({ message, history }),
  });

  if (!response.ok) {
    let errMsg = 'AI chat failed';
    try {
      const body = await response.json();
      errMsg = body.message || errMsg;
    } catch {}
    if (response.status === 503) errMsg = 'Claude API key not configured. Set the ANTHROPIC_API_KEY environment variable.';
    onError(errMsg);
    return;
  }

  const reader = response.body!.getReader();
  const decoder = new TextDecoder();
  let buffer = '';
  let currentEvent = '';

  while (true) {
    const { done, value } = await reader.read();
    if (done) break;

    buffer += decoder.decode(value, { stream: true });
    const lines = buffer.split('\n');
    buffer = lines.pop() ?? '';

    for (const line of lines) {
      if (line.startsWith('event:')) {
        currentEvent = line.slice(6).trim();
      } else if (line.startsWith('data:')) {
        const data = line.slice(5).trim();
        if (currentEvent === 'error') {
          onError(data);
          return;
        } else if (currentEvent === 'done' || data === '[DONE]') {
          onDone();
          return;
        } else if (currentEvent === 'token' && data) {
          onToken(data);
        }
        currentEvent = '';
      }
    }
  }
  onDone();
};
