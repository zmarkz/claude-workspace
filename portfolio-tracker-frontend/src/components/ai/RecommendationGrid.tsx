import type { StockRecommendation } from '../../types';

interface Props {
  recommendations: StockRecommendation[];
}

const actionStyle: Record<string, string> = {
  BUY: 'bg-emerald-900 text-emerald-300 border border-emerald-700',
  HOLD: 'bg-amber-900 text-amber-300 border border-amber-700',
  SELL: 'bg-red-900 text-red-300 border border-red-700',
};

const confidenceStyle: Record<string, string> = {
  High: 'text-emerald-400',
  Medium: 'text-amber-400',
  Low: 'text-slate-400',
};

export const RecommendationGrid = ({ recommendations }: Props) => (
  <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">
    <h2 className="text-white font-semibold mb-4">Stock Recommendations</h2>
    <div className="grid gap-3">
      {recommendations.map((rec) => (
        <div key={rec.symbol} className="flex items-start gap-4 p-4 bg-slate-800 rounded-lg">
          <div className="flex-shrink-0">
            <div className="text-white font-bold text-sm">{rec.symbol}</div>
            <span className={`text-xs px-2 py-0.5 rounded-full mt-1 inline-block ${actionStyle[rec.action] ?? ''}`}>
              {rec.action}
            </span>
          </div>
          <div className="flex-1 min-w-0">
            <p className="text-slate-300 text-sm leading-relaxed">{rec.rationale}</p>
          </div>
          <div className={`text-xs flex-shrink-0 ${confidenceStyle[rec.confidence] ?? ''}`}>
            {rec.confidence}
          </div>
        </div>
      ))}
    </div>
  </div>
);
