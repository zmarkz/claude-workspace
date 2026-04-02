import client from './client';
import type {
  PortfolioResponse,
  PortfolioAnalysisResponse,
  HoldingResponse,
  CreatePortfolioRequest,
  AddHoldingRequest,
  TransactionResponse,
} from '../types';

export const createPortfolio = (data: CreatePortfolioRequest) =>
  client.post<PortfolioResponse>('/portfolios', data).then((r) => r.data);

export const getPortfolio = (id: number) =>
  client.get<PortfolioResponse>(`/portfolios/${id}`).then((r) => r.data);

export const getPortfolioAnalysis = (id: number) =>
  client.get<PortfolioAnalysisResponse>(`/portfolios/${id}/analysis`).then((r) => r.data);

export const addHolding = (portfolioId: number, data: AddHoldingRequest) =>
  client.post<HoldingResponse>(`/portfolios/${portfolioId}/holdings`, data).then((r) => r.data);

export const removeHolding = (portfolioId: number, holdingId: number) =>
  client.delete(`/portfolios/${portfolioId}/holdings/${holdingId}`);

export const getTransactions = (portfolioId: number) =>
  client.get<TransactionResponse[]>(`/portfolios/${portfolioId}/transactions`).then((r) => r.data);

export interface UploadResult {
  synced: number;
  created: number;
  total: number;
  errors: string[];
}

export const uploadHoldingsCsv = (portfolioId: number, file: File, replace: boolean) => {
  const form = new FormData();
  form.append('file', file);
  return client.post<UploadResult>(
    `/portfolios/${portfolioId}/holdings/upload?replace=${replace}`,
    form,
    { headers: { 'Content-Type': 'multipart/form-data' } }
  ).then((r) => r.data);
};
