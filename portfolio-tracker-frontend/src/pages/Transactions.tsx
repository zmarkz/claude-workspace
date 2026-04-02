import { useQuery } from '@tanstack/react-query';
import { getTransactions } from '../api/portfolio';
import type { TransactionResponse } from '../types';

const DEFAULT_PORTFOLIO_ID = 1;

const fmt = (n: number) =>
  new Intl.NumberFormat('en-IN', { style: 'currency', currency: 'INR' }).format(n);

export const Transactions = () => {
  const { data, isLoading } = useQuery<TransactionResponse[]>({
    queryKey: ['transactions', DEFAULT_PORTFOLIO_ID],
    queryFn: () => getTransactions(DEFAULT_PORTFOLIO_ID),
  });

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <h1 className="text-2xl font-bold text-white">Transactions</h1>

      <div className="bg-slate-900 border border-slate-800 rounded-xl overflow-hidden">
        <div className="overflow-x-auto">
          <table className="w-full">
            <thead>
              <tr className="text-slate-400 text-xs uppercase border-b border-slate-800">
                <th className="px-6 py-3 text-left">Date</th>
                <th className="px-6 py-3 text-left">Symbol</th>
                <th className="px-6 py-3 text-center">Type</th>
                <th className="px-6 py-3 text-right">Qty</th>
                <th className="px-6 py-3 text-right">Price</th>
                <th className="px-6 py-3 text-right">Total</th>
              </tr>
            </thead>
            <tbody className="divide-y divide-slate-800">
              {data?.map((t) => (
                <tr key={t.id} className="hover:bg-slate-800/30">
                  <td className="px-6 py-4 text-slate-400 text-sm">
                    {new Date(t.transactionDate).toLocaleDateString('en-IN')}
                  </td>
                  <td className="px-6 py-4">
                    <div className="text-white font-medium">{t.symbol}</div>
                    <div className="text-slate-500 text-xs">{t.companyName}</div>
                  </td>
                  <td className="px-6 py-4 text-center">
                    <span className={`text-xs px-2 py-0.5 rounded-full ${
                      t.type === 'BUY'
                        ? 'bg-emerald-900 text-emerald-300 border border-emerald-800'
                        : 'bg-red-900 text-red-300 border border-red-800'
                    }`}>
                      {t.type}
                    </span>
                  </td>
                  <td className="px-6 py-4 text-right text-white">{t.quantity}</td>
                  <td className="px-6 py-4 text-right text-slate-300">{fmt(t.price)}</td>
                  <td className="px-6 py-4 text-right text-white font-medium">{fmt(t.totalAmount)}</td>
                </tr>
              ))}
            </tbody>
          </table>
          {(!data || data.length === 0) && (
            <div className="text-center text-slate-500 py-12">No transactions yet.</div>
          )}
        </div>
      </div>
    </div>
  );
};
