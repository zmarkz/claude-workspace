export interface AuthResponse {
  token: string;
  userId: number;
  email: string;
  firstName: string;
  lastName: string;
}

export interface LoginRequest {
  email: string;
  password: string;
}

export interface RegisterRequest {
  email: string;
  password: string;
  firstName: string;
  lastName: string;
}

export interface HoldingResponse {
  id: number;
  symbol: string;
  companyName: string;
  quantity: number;
  averageBuyPrice: number;
  currentPrice: number;
  totalValue: number;
  gainLoss: number;
  gainLossPercentage: number;
}

export interface PortfolioResponse {
  id: number;
  name: string;
  description: string;
  currency: string;
  totalValue: number;
  totalGainLoss: number;
  holdings: HoldingResponse[];
  createdAt: string;
  updatedAt: string;
}

export interface PortfolioAnalysisResponse {
  portfolioId: number;
  portfolioName: string;
  totalValue: number;
  totalGainLoss: number;
  gainLossPercent: number;
  sectorAllocation: Record<string, number>;
  holdings: HoldingResponse[];
}

export interface CreatePortfolioRequest {
  userId: number;
  name: string;
  description?: string;
  currency?: string;
}

export interface AddHoldingRequest {
  stockId: number;
  quantity: number;
  buyPrice: number;
}

export interface StockResponse {
  id: number;
  symbol: string;
  companyName: string;
  exchange: string;
  sector: string;
  currentPrice: number;
  lastUpdatedAt: string;
}

export interface StockPriceResponse {
  symbol: string;
  companyName: string;
  currentPrice: number;
  lastUpdatedAt: string;
}

export interface TechnicalIndicators {
  sma50: number;
  sma200: number;
  rsi: number;
  macd: {
    macdLine: number;
    signalLine: number;
    histogram: number;
  };
}

export interface StockAnalysisResponse {
  symbol: string;
  companyName: string;
  currentPrice: number;
  indicators: TechnicalIndicators;
  insights: string[];
}

export interface TransactionResponse {
  id: number;
  portfolioId: number;
  symbol: string;
  companyName: string;
  type: 'BUY' | 'SELL';
  quantity: number;
  price: number;
  totalAmount: number;
  transactionDate: string;
  notes?: string;
}

export interface PortfolioAIAnalysis {
  healthScore: number;
  summary: string;
  riskProfile: string;
  risks: string[];
  opportunities: string[];
  stockRecommendations: StockRecommendation[];
}

export interface StockRecommendation {
  symbol: string;
  companyName?: string;
  action: 'BUY' | 'HOLD' | 'SELL';
  rationale: string;
  confidence: 'Low' | 'Medium' | 'High';
}

export interface RebalancingSuggestion {
  symbol: string;
  currentWeightPercent: number;
  targetWeightPercent: number;
  action: 'INCREASE' | 'DECREASE' | 'HOLD';
  reason: string;
}

export interface ChatMessage {
  role: 'user' | 'assistant';
  content: string;
}
