import { useState } from 'react';
import { useQuery, useMutation } from '@tanstack/react-query';
import { analyzePortfolio, getRebalancingSuggestions } from '../api/ai';
import { AIChat } from '../components/ai/AIChat';
import { PortfolioHealthCard } from '../components/ai/PortfolioHealthCard';
import { RecommendationGrid } from '../components/ai/RecommendationGrid';
import { RebalancingPanel } from '../components/ai/RebalancingPanel';
import { Sparkles, RefreshCw } from 'lucide-react';

const DEFAULT_PORTFOLIO_ID = 1;

export const AIAssessment = () => {
  const portfolioId = DEFAULT_PORTFOLIO_ID;
  const [tab, setTab] = useState<'chat' | 'analysis' | 'rebalance'>('chat');

  const analysisMutation = useMutation({
    mutationFn: () => analyzePortfolio(portfolioId),
  });

  const { data: rebalancing, refetch: refetchRebalance, isFetching: rebalanceFetching, isError: rebalanceError, error: rebalanceErrorDetail } = useQuery({
    queryKey: ['rebalancing', portfolioId],
    queryFn: () => getRebalancingSuggestions(portfolioId),
    enabled: false,
  });

  const tabs = [
    { key: 'chat', label: 'AI Chat' },
    { key: 'analysis', label: 'Portfolio Analysis' },
    { key: 'rebalance', label: 'Rebalancing' },
  ] as const;

  return (
    <div className="space-y-6 h-full">
      <div className="flex items-center justify-between">
        <div>
          <h1 className="text-2xl font-bold text-white">AI Assessment</h1>
          <p className="text-slate-400 text-sm mt-1">Powered by Claude Sonnet</p>
        </div>
      </div>

      <div className="flex gap-2 bg-slate-900 border border-slate-800 rounded-xl p-1 w-fit">
        {tabs.map((t) => (
          <button
            key={t.key}
            onClick={() => setTab(t.key)}
            className={`px-4 py-2 rounded-lg text-sm font-medium transition-colors ${
              tab === t.key ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
            }`}
          >
            {t.label}
          </button>
        ))}
      </div>

      {tab === 'chat' && (
        <div className="h-[calc(100vh-280px)]">
          <AIChat portfolioId={portfolioId} />
        </div>
      )}

      {tab === 'analysis' && (
        <div className="space-y-4">
          {!analysisMutation.data && !analysisMutation.isPending && (
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-12 text-center">
              <Sparkles size={40} className="mx-auto text-blue-400 mb-4" />
              <p className="text-white font-medium mb-2">Get AI-powered portfolio analysis</p>
              <p className="text-slate-400 text-sm mb-6">Claude will analyze your holdings, assess risks, and provide recommendations</p>
              <button
                onClick={() => analysisMutation.mutate()}
                className="bg-blue-600 hover:bg-blue-500 text-white px-6 py-2.5 rounded-lg font-medium transition-colors"
              >
                Analyze My Portfolio
              </button>
            </div>
          )}
          {analysisMutation.isPending && (
            <div className="flex items-center justify-center h-40 gap-3 text-slate-400">
              <div className="w-5 h-5 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
              Claude is analyzing your portfolio...
            </div>
          )}
          {analysisMutation.isError && (
            <div className="bg-red-950 border border-red-800 rounded-xl p-6 text-center">
              <p className="text-red-400 font-medium">Analysis failed</p>
              <p className="text-red-500 text-sm mt-1">{(analysisMutation.error as Error)?.message ?? 'Claude API key not configured. Set ANTHROPIC_API_KEY on the server.'}</p>
              <button onClick={() => analysisMutation.mutate()} className="mt-4 text-sm text-slate-400 hover:text-white transition-colors">Try again</button>
            </div>
          )}
          {analysisMutation.data && (
            <div className="space-y-4">
              <div className="flex justify-end">
                <button
                  onClick={() => analysisMutation.mutate()}
                  className="flex items-center gap-2 text-slate-400 hover:text-white text-sm transition-colors"
                >
                  <RefreshCw size={14} /> Re-analyze
                </button>
              </div>
              <PortfolioHealthCard analysis={analysisMutation.data} />
              <RecommendationGrid recommendations={analysisMutation.data.stockRecommendations} />
            </div>
          )}
        </div>
      )}

      {tab === 'rebalance' && (
        <div className="space-y-4">
          {!rebalancing && (
            <div className="bg-slate-900 border border-slate-800 rounded-xl p-12 text-center">
              <p className="text-white font-medium mb-2">Get rebalancing suggestions</p>
              <p className="text-slate-400 text-sm mb-6">Claude will suggest how to improve your portfolio allocation</p>
              <button
                onClick={() => refetchRebalance()}
                disabled={rebalanceFetching}
                className="bg-blue-600 hover:bg-blue-500 disabled:opacity-60 text-white px-6 py-2.5 rounded-lg font-medium transition-colors flex items-center gap-2 mx-auto"
              >
                {rebalanceFetching && <div className="w-4 h-4 border-2 border-white border-t-transparent rounded-full animate-spin" />}
                Get Suggestions
              </button>
            </div>
          )}
          {rebalanceError && (
            <div className="bg-red-950 border border-red-800 rounded-xl p-6 text-center">
              <p className="text-red-400 font-medium">Could not load suggestions</p>
              <p className="text-red-500 text-sm mt-1">{(rebalanceErrorDetail as Error)?.message ?? 'Claude API key not configured. Set ANTHROPIC_API_KEY on the server.'}</p>
              <button onClick={() => refetchRebalance()} className="mt-4 text-sm text-slate-400 hover:text-white transition-colors">Try again</button>
            </div>
          )}
          {rebalancing && <RebalancingPanel suggestions={rebalancing} />}
        </div>
      )}
    </div>
  );
};
