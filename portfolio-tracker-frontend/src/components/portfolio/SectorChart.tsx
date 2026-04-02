import { PieChart, Pie, Cell, Tooltip, Legend, ResponsiveContainer } from 'recharts';

const COLORS = ['#3b82f6', '#10b981', '#f59e0b', '#ef4444', '#8b5cf6', '#ec4899', '#14b8a6', '#f97316'];

interface Props {
  sectorAllocation: Record<string, number>;
}

export const SectorChart = ({ sectorAllocation }: Props) => {
  const total = Object.values(sectorAllocation).reduce((a, b) => a + b, 0);
  const data = Object.entries(sectorAllocation).map(([name, value]) => ({
    name,
    value: parseFloat(((value / total) * 100).toFixed(1)),
    absValue: value,
  }));

  return (
    <div className="bg-slate-900 border border-slate-800 rounded-xl p-6">
      <h2 className="text-white font-semibold mb-4">Sector Allocation</h2>
      <ResponsiveContainer width="100%" height={280}>
        <PieChart>
          <Pie
            data={data}
            cx="50%"
            cy="50%"
            innerRadius={70}
            outerRadius={110}
            paddingAngle={2}
            dataKey="value"
          >
            {data.map((_, index) => (
              <Cell key={`cell-${index}`} fill={COLORS[index % COLORS.length]} />
            ))}
          </Pie>
          <Tooltip
            formatter={(value) => [`${value}%`, 'Weight']}
            contentStyle={{ background: '#1e293b', border: '1px solid #334155', borderRadius: '8px', color: '#f1f5f9' }}
          />
          <Legend
            formatter={(value) => <span style={{ color: '#94a3b8', fontSize: '12px' }}>{value}</span>}
          />
        </PieChart>
      </ResponsiveContainer>
    </div>
  );
};
