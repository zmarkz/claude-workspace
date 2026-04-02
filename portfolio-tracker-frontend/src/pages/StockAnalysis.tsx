import { useParams } from 'react-router-dom';
import { useQuery } from '@tanstack/react-query';
import client from '../api/client';
import type { StockAnalysisResponse } from '../types';

const getStockAnalysis = (symbol: string) => {
  const end = new Date().toISOString().split('T')[0];
  const start = new Date(Date.now() - 200 * 86400000).toISOString().split('T')[0];
  return client.get<StockAnalysisResponse>(
    `/stocks/${symbol}/analysis?startDate=${start}&endDate=${end}`
  ).then((r) => r.data);
};

const IndicatorCard = ({ label, value, color }: { label: string; value: string | number; color?: string }) => (
  <div className="bg-slate-800 rounded-lg p-4">
    <div className="text-slate-400 text-xs mb-1">{label}</div>
    <div className={`text-lg font-bold ${color ?? 'text-white'}`}>{value}</div>
  </div>
);

const getRSIColor = (rsi: number) => {
  if (rsi > 70) return 'text-red-400';
  if (rsi < 30) return 'text-emerald-400';
  return 'text-white';
};

export const StockAnalysis = () => {
  const { symbol } = useParams<{ symbol: string }>();

  const { data, isLoading } = useQuery({
    queryKey: ['stock-analysis', symbol],
    queryFn: () => getStockAnalysis(symbol!),
    enabled: !!symbol,
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  if (!data) return null;

  const { indicators } = data;

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-2xl font-bold text-white">{data.symbol}</h1>
        <p className="text-slate-400">{data.companyName}</p>
        <div className="text-3xl font-bold text-white mt-2">
          ₹{data.currentPrice?.toFixed(2) ?? 'N/A'}
        </div>
      </div>

      <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">
        <h2 className="text-white font-semibold mb-4">Technical Indicators</h2>
        <div className="grid grid-cols-2 md:grid-cols-4 gap-4">
          <IndicatorCard label="SMA 50" value={`₹${indicators.sma50?.toFixed(2) ?? '—'}`} />
          <IndicatorCard label="SMA 200" value={`₹${indicators.sma200?.toFixed(2) ?? '—'}`} />
          <IndicatorCard
            label="RSI (14)"
            value={indicators.rsi?.toFixed(2) ?? '—'}
            color={indicators.rsi ? getRSIColor(indicators.rsi) : undefined}
          />
          {indicators.macd && (
            <IndicatorCard
              label="MACD"
              value={indicators.macd.macdLine?.toFixed(2) ?? '—'}
              color={indicators.macd.macdLine >= 0 ? 'text-emerald-400' : 'text-red-400'}
            />
          )}
        </div>

        {indicators.macd && (
          <div className="mt-6 grid grid-cols-3 gap-4">
            <IndicatorCard label="MACD Line" value={indicators.macd.macdLine?.toFixed(2)} />
            <IndicatorCard label="Signal Line" value={indicators.macd.signalLine?.toFixed(2)} />
            <IndicatorCard
              label="Histogram"
              value={indicators.macd.histogram?.toFixed(2)}
              color={indicators.macd.histogram >= 0 ? 'text-emerald-400' : 'text-red-400'}
            />
          </div>
        )}

        {indicators.rsi && (
          <div className="mt-6 p-4 bg-slate-800 rounded-lg">
            <div className="flex justify-between text-xs text-slate-400 mb-2">
              <span>Oversold (30)</span>
              <span>RSI: {indicators.rsi.toFixed(2)}</span>
              <span>Overbought (70)</span>
            </div>
            <div className="relative h-2 bg-slate-700 rounded-full">
              <div
                className="absolute top-0 left-0 h-full rounded-full bg-gradient-to-r from-emerald-500 via-amber-400 to-red-500"
                style={{ width: '100%' }}
              />
              <div
                className="absolute top-1/2 -translate-y-1/2 w-3 h-3 bg-white rounded-full shadow-lg"
                style={{ left: `${indicators.rsi}%`, transform: 'translate(-50%, -50%)' }}
              />
            </div>
          </div>
        )}
      </div>

      {data.insights && data.insights.length > 0 && (
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">
          <h2 className="text-white font-semibold mb-3">Insights</h2>
          <ul className="space-y-2">
            {data.insights.map((insight, i) => (
              <li key={i} className="text-slate-300 text-sm flex gap-2">
                <span className="text-blue-400">•</span> {insight}
              </li>
            ))}
          </ul>
        </div>
      )}
    </div>
  );
};
