import { useQuery } from '@tanstack/react-query';
import { getPortfolioAnalysis } from '../api/portfolio';
import { HoldingsTable } from '../components/portfolio/HoldingsTable';

const DEFAULT_PORTFOLIO_ID = 1;

export const Holdings = () => {
  const { data, isLoading } = useQuery({
    queryKey: ['portfolio-analysis', DEFAULT_PORTFOLIO_ID],
    queryFn: () => getPortfolioAnalysis(DEFAULT_PORTFOLIO_ID),
    refetchInterval: 30000,
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
      <h1 className="text-2xl font-bold text-white">Holdings</h1>
      {data && <HoldingsTable holdings={data.holdings} />}
    </div>
  );
};
