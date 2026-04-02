import type { RebalancingSuggestion } from '../../types';
import { ArrowUp, ArrowDown, Minus } from 'lucide-react';

interface Props {
  suggestions: RebalancingSuggestion[];
}

const actionIcon = (action: string) => {
  if (action === 'INCREASE') return <ArrowUp size={14} className="text-emerald-400" />;
  if (action === 'DECREASE') return <ArrowDown size={14} className="text-red-400" />;
  return <Minus size={14} className="text-slate-400" />;
};

export const RebalancingPanel = ({ suggestions }: Props) => (
  <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">
    <h2 className="text-white font-semibold mb-4">Rebalancing Suggestions</h2>
    <div className="overflow-x-auto">
      <table className="w-full">
        <thead>
          <tr className="text-slate-400 text-xs uppercase border-b border-slate-800">
            <th className="pb-3 text-left">Symbol</th>
            <th className="pb-3 text-right">Current %</th>
            <th className="pb-3 text-right">Target %</th>
            <th className="pb-3 text-center">Action</th>
            <th className="pb-3 text-left pl-4">Reason</th>
          </tr>
        </thead>
        <tbody className="divide-y divide-slate-800">
          {suggestions.map((s) => (
            <tr key={s.symbol} className="hover:bg-slate-800/30">
              <td className="py-3 text-white font-medium">{s.symbol}</td>
              <td className="py-3 text-right text-slate-300">{s.currentWeightPercent.toFixed(1)}%</td>
              <td className="py-3 text-right text-slate-300">{s.targetWeightPercent.toFixed(1)}%</td>
              <td className="py-3">
                <div className="flex items-center justify-center gap-1">
                  {actionIcon(s.action)}
                  <span className="text-xs text-slate-400">{s.action}</span>
                </div>
              </td>
              <td className="py-3 pl-4 text-slate-400 text-sm">{s.reason}</td>
            </tr>
          ))}
        </tbody>
      </table>
    </div>
  </div>
);
