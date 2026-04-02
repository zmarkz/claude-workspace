import { AlertTriangle, Lightbulb } from 'lucide-react';
import type { PortfolioAIAnalysis } from '../../types';

interface Props {
  analysis: PortfolioAIAnalysis;
}

const scoreColor = (score: number) => {
  if (score >= 8) return 'text-emerald-400';
  if (score >= 5) return 'text-amber-400';
  return 'text-red-400';
};

const riskColor: Record<string, string> = {
  Low: 'bg-emerald-900 text-emerald-300 border-emerald-800',
  Medium: 'bg-amber-900 text-amber-300 border-amber-800',
  High: 'bg-red-900 text-red-300 border-red-800',
};

export const PortfolioHealthCard = ({ analysis }: Props) => (
  <div className="bg-slate-900 border border-slate-800 rounded-xl p-6 space-y-5">
    <div className="flex items-start justify-between">
      <div>
        <h2 className="text-white font-semibold text-lg">Portfolio Health</h2>
        <p className="text-slate-400 text-sm mt-1">{analysis.summary}</p>
      </div>
      <div className="text-center ml-4 flex-shrink-0">
        <div className={`text-4xl font-bold ${scoreColor(analysis.healthScore)}`}>
          {analysis.healthScore}<span className="text-xl text-slate-500">/10</span>
        </div>
        <div className={`mt-1 text-xs px-2 py-0.5 rounded border ${riskColor[analysis.riskProfile] ?? 'bg-slate-800 text-slate-300 border-slate-700'}`}>
          {analysis.riskProfile} Risk
        </div>
      </div>
    </div>

    <div>
      <div className="flex items-center gap-2 text-red-400 text-sm font-medium mb-2">
        <AlertTriangle size={14} /> Risks
      </div>
      <ul className="space-y-1.5">
        {analysis.risks.map((r, i) => (
          <li key={i} className="text-slate-400 text-sm flex gap-2">
            <span className="text-red-500 mt-0.5">•</span> {r}
          </li>
        ))}
      </ul>
    </div>

    <div>
      <div className="flex items-center gap-2 text-emerald-400 text-sm font-medium mb-2">
        <Lightbulb size={14} /> Opportunities
      </div>
      <ul className="space-y-1.5">
        {analysis.opportunities.map((o, i) => (
          <li key={i} className="text-slate-400 text-sm flex gap-2">
            <span className="text-emerald-500 mt-0.5">•</span> {o}
          </li>
        ))}
      </ul>
    </div>
  </div>
);
