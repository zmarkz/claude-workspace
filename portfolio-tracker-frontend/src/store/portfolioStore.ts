import { create } from 'zustand';
import type { PortfolioAnalysisResponse } from '../types';

interface PortfolioState {
  activePortfolioId: number | null;
  portfolio: PortfolioAnalysisResponse | null;
  livePrices: Record<string, number>;
  setActivePortfolioId: (id: number) => void;
  setPortfolio: (p: PortfolioAnalysisResponse) => void;
  updateLivePrice: (symbol: string, price: number) => void;
  updateLivePrices: (prices: Record<string, number>) => void;
}

export const usePortfolioStore = create<PortfolioState>((set) => ({
  activePortfolioId: null,
  portfolio: null,
  livePrices: {},

  setActivePortfolioId: (id) => set({ activePortfolioId: id }),
  setPortfolio: (portfolio) => set({ portfolio }),

  updateLivePrice: (symbol, price) =>
    set((state) => ({ livePrices: { ...state.livePrices, [symbol]: price } })),

  updateLivePrices: (prices) =>
    set((state) => ({ livePrices: { ...state.livePrices, ...prices } })),
}));
