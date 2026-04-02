import { usePortfolioStore } from '../../store/portfolioStore';
import type { HoldingResponse } from '../../types';
import { TrendingDown, TrendingUp } from 'lucide-react';
import { Link } from 'react-router-dom';

interface Props {
  holdings: HoldingResponse[];
}

const fmt = (n: number) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(n);

export const HoldingsTable = ({ holdings }: Props) => {
  const livePrices = usePortfolioStore((s) => s.livePrices);

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
      <div className="px-6 py-4 border-b border-slate-800">
        <h2 className="text-white font-semibold">Holdings</h2>
      </div>
      <div className="overflow-x-auto">
        <table className="w-full">
          <thead>
            <tr className="text-slate-400 text-xs uppercase border-b border-slate-800">
              <th className="px-6 py-3 text-left">Symbol</th>
              <th className="px-6 py-3 text-right">Qty</th>
              <th className="px-6 py-3 text-right">Avg Buy</th>
              <th className="px-6 py-3 text-right">LTP</th>
              <th className="px-6 py-3 text-right">Current Value</th>
              <th className="px-6 py-3 text-right">P&L</th>
              <th className="px-6 py-3 text-right">P&L %</th>
            </tr>
          </thead>
          <tbody className="divide-y divide-slate-800">
            {holdings.map((h) => {
              const livePrice = livePrices[h.symbol] ?? h.currentPrice;
              const gainLoss = (livePrice - h.averageBuyPrice) * h.quantity;
              const gainPct = ((livePrice - h.averageBuyPrice) / h.averageBuyPrice) * 100;
              const positive = gainLoss >= 0;

              return (
                <tr key={h.id} className="hover:bg-slate-800/50 transition-colors">
                  <td className="px-6 py-4">
                    <Link to={`/stocks/${h.symbol}`} className="hover:text-blue-400">
                      <div className="text-white font-medium">{h.symbol}</div>
                      <div className="text-slate-400 text-xs">{h.companyName}</div>
                    </Link>
                  </td>
                  <td className="px-6 py-4 text-right text-white">{h.quantity}</td>
                  <td className="px-6 py-4 text-right text-slate-300">{fmt(h.averageBuyPrice)}</td>
                  <td className="px-6 py-4 text-right text-white font-medium">{fmt(livePrice)}</td>
                  <td className="px-6 py-4 text-right text-white">{fmt(livePrice * h.quantity)}</td>
                  <td className={`px-6 py-4 text-right font-medium ${positive ? 'text-emerald-400' : 'text-red-400'}`}>
                    <div className="flex items-center justify-end gap-1">
                      {positive ? <TrendingUp size={14} /> : <TrendingDown size={14} />}
                      {fmt(gainLoss)}
                    </div>
                  </td>
                  <td className={`px-6 py-4 text-right font-medium ${positive ? 'text-emerald-400' : 'text-red-400'}`}>
                    {gainPct > 0 ? '+' : ''}{gainPct.toFixed(2)}%
                  </td>
                </tr>
              );
            })}
          </tbody>
        </table>
        {holdings.length === 0 && (
          <div className="text-center text-slate-500 py-12">No holdings yet. Sync from Zerodha or add manually.</div>
        )}
      </div>
    </div>
  );
};
