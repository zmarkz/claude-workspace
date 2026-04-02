import { TrendingDown, TrendingUp, Wallet, BarChart2 } from 'lucide-react';
import type { PortfolioAnalysisResponse } from '../../types';

interface Props {
  data: PortfolioAnalysisResponse;
}

const fmt = (n: number) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR', maximumFractionDigits: 0 }).format(n);

const fmtPct = (n: number) => `${n > 0 ? '+' : ''}${n.toFixed(2)}%`;

export const MetricsBar = ({ data }: Props) => {
  const isPositive = data.totalGainLoss >= 0;

  return (
    <div className="grid grid-cols-2 lg:grid-cols-4 gap-4">
      <Card
        icon={<Wallet size={20} className="text-blue-400" />}
        label="Total Value"
        value={fmt(data.totalValue)}
        sub=""
        color="blue"
      />
      <Card
        icon={isPositive
          ? <TrendingUp size={20} className="text-emerald-400" />
          : <TrendingDown size={20} className="text-red-400" />}
        label="Overall P&L"
        value={fmt(data.totalGainLoss)}
        sub={fmtPct(data.gainLossPercent)}
        color={isPositive ? 'green' : 'red'}
      />
      <Card
        icon={<BarChart2 size={20} className="text-purple-400" />}
        label="Holdings"
        value={String(data.holdings.length)}
        sub="positions"
        color="purple"
      />
      <Card
        icon={<BarChart2 size={20} className="text-amber-400" />}
        label="Sectors"
        value={String(Object.keys(data.sectorAllocation).length)}
        sub="diversified"
        color="amber"
      />
    </div>
  );
};

interface CardProps {
  icon: React.ReactNode;
  label: string;
  value: string;
  sub: string;
  color: string;
}

const Card = ({ icon, label, value, sub, color }: CardProps) => (
  <div className="bg-slate-900 border border-slate-800 rounded-xl p-5">
    <div className="flex items-center justify-between mb-3">
      <span className="text-slate-400 text-sm">{label}</span>
      {icon}
    </div>
    <div className="text-2xl font-bold text-white">{value}</div>
    {sub && <div className={`text-sm mt-1 ${color === 'green' ? 'text-emerald-400' : color === 'red' ? 'text-red-400' : 'text-slate-400'}`}>{sub}</div>}
  </div>
);
