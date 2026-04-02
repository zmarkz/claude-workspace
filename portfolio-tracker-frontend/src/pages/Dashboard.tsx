import { useQuery } from '@tanstack/react-query';
import { useAuthStore } from '../store/authStore';
import { usePortfolioStore } from '../store/portfolioStore';
import { getPortfolioAnalysis, uploadHoldingsCsv } from '../api/portfolio';
import { MetricsBar } from '../components/portfolio/MetricsBar';
import { HoldingsTable } from '../components/portfolio/HoldingsTable';
import { SectorChart } from '../components/portfolio/SectorChart';
import { connectKite, getKiteLoginUrl, getKiteStatus, syncKiteHoldings } from '../api/kite';
import { useEffect, useRef, useState } from 'react';
import { RefreshCw, Zap, Upload, ClipboardPaste, X } from 'lucide-react';

const DEFAULT_PORTFOLIO_ID = 1;

export const Dashboard = () => {
  const user = useAuthStore((s) => s.user);
  const { setPortfolio } = usePortfolioStore();
  const [kiteConnected, setKiteConnected] = useState(false);
  const [kiteConfigured, setKiteConfigured] = useState(true);
  const [syncing, setSyncing] = useState(false);
  const [uploading, setUploading] = useState(false);
  const [uploadMsg, setUploadMsg] = useState<{ text: string; type: 'success' | 'error' } | null>(null);
  const [showPasteModal, setShowPasteModal] = useState(false);
  const [csvText, setCsvText] = useState('');
  const fileInputRef = useRef<HTMLInputElement>(null);

  const portfolioId = DEFAULT_PORTFOLIO_ID;

  const { data, isLoading, refetch } = useQuery({
    queryKey: ['portfolio-analysis', portfolioId],
    queryFn: () => getPortfolioAnalysis(portfolioId),
    refetchInterval: 30000,
  });

  useEffect(() => {
    if (data) setPortfolio(data);
  }, [data, setPortfolio]);

  useEffect(() => {
    getKiteStatus().then((s) => { setKiteConnected(s.connected); setKiteConfigured(s.configured); }).catch(() => {});
    const params = new URLSearchParams(window.location.search);
    if (params.get('kite_connected') === 'true') {
      const accessToken = params.get('token');
      if (accessToken) {
        connectKite(accessToken).then(() => setKiteConnected(true));
      }
      window.history.replaceState({}, '', '/dashboard');
    }
  }, []);

  const handleSync = async () => {
    setSyncing(true);
    try {
      const result = await syncKiteHoldings(portfolioId);
      alert(`Synced ${result.synced} of ${result.total} holdings`);
      refetch();
    } catch {
      alert('Sync failed. Make sure Zerodha is connected.');
    } finally {
      setSyncing(false);
    }
  };

  const doUpload = async (file: File) => {
    setUploading(true);
    setUploadMsg(null);
    try {
      const result = await uploadHoldingsCsv(portfolioId, file, true);
      setUploadMsg({
        text: `Uploaded ${result.synced} of ${result.total} holdings${result.errors.length > 0 ? ` (${result.errors.length} errors)` : ''}`,
        type: result.synced > 0 ? 'success' : 'error',
      });
      if (result.synced > 0) refetch();
    } catch (err: any) {
      setUploadMsg({
        text: err.response?.data?.message ?? 'Upload failed. Check CSV format.',
        type: 'error',
      });
    } finally {
      setUploading(false);
    }
  };

  const handleFileUpload = async (e: React.ChangeEvent<HTMLInputElement>) => {
    const file = e.target.files?.[0];
    if (!file) return;
    await doUpload(file);
    if (fileInputRef.current) fileInputRef.current.value = '';
  };

  const handlePasteSubmit = async () => {
    if (!csvText.trim()) return;
    const blob = new Blob([csvText], { type: 'text/csv' });
    const file = new File([blob], 'holdings.csv', { type: 'text/csv' });
    await doUpload(file);
    setShowPasteModal(false);
    setCsvText('');
  };

  if (isLoading) {
    return (
      <div className="flex items-center justify-center h-64">
        <div className="w-8 h-8 border-2 border-blue-500 border-t-transparent rounded-full animate-spin" />
      </div>
    );
  }

  return (
    <div className="space-y-6">
      <div className="flex flex-col sm:flex-row sm:items-center justify-between gap-3">
        <div>
          <h1 className="text-2xl font-bold text-white">Dashboard</h1>
          <p className="text-slate-400 text-sm mt-1">Welcome back, {user?.firstName}</p>
        </div>
        <div className="flex flex-wrap gap-2">
          {/* File upload (works on desktop) */}
          <input
            ref={fileInputRef}
            type="file"
            accept=".csv,.xlsx,.xls"
            onChange={handleFileUpload}
            className="hidden"
          />
          <button
            onClick={() => fileInputRef.current?.click()}
            disabled={uploading}
            className="hidden sm:flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-60 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
          >
            <Upload size={16} className={uploading ? 'animate-pulse' : ''} />
            {uploading ? 'Uploading...' : 'Upload CSV'}
          </button>

          {/* Paste CSV (works on mobile + desktop) */}
          <button
            onClick={() => setShowPasteModal(true)}
            disabled={uploading}
            className="flex items-center gap-2 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-60 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors sm:bg-slate-800 sm:hover:bg-slate-700"
          >
            <ClipboardPaste size={16} />
            <span className="sm:hidden">{uploading ? 'Uploading...' : 'Import Holdings'}</span>
            <span className="hidden sm:inline">Paste CSV</span>
          </button>

          {/* Kite Connect */}
          {!kiteConfigured ? (
            <span className="flex items-center gap-2 bg-slate-800 text-slate-400 text-sm px-4 py-2 rounded-lg border border-slate-700 cursor-default" title="Set KITE_API_KEY and KITE_API_SECRET on the server">
              <Zap size={16} /> Zerodha not configured
            </span>
          ) : !kiteConnected ? (
            <a
              href={getKiteLoginUrl()}
              className="flex items-center gap-2 bg-amber-600 hover:bg-amber-500 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors"
            >
              <Zap size={16} /> Connect Zerodha
            </a>
          ) : (
            <button
              onClick={handleSync}
              disabled={syncing}
              className="flex items-center gap-2 bg-slate-800 hover:bg-slate-700 text-white text-sm font-medium px-4 py-2 rounded-lg transition-colors disabled:opacity-60"
            >
              <RefreshCw size={16} className={syncing ? 'animate-spin' : ''} />
              {syncing ? 'Syncing...' : 'Sync Holdings'}
            </button>
          )}
        </div>
      </div>

      {/* Upload feedback */}
      {uploadMsg && (
        <div className={`rounded-lg px-4 py-3 text-sm ${
          uploadMsg.type === 'success'
            ? 'bg-emerald-950 border border-emerald-800 text-emerald-300'
            : 'bg-red-950 border border-red-800 text-red-300'
        }`}>
          {uploadMsg.text}
          <button onClick={() => setUploadMsg(null)} className="ml-3 text-xs opacity-60 hover:opacity-100">dismiss</button>
        </div>
      )}

      {/* Paste CSV Modal */}
      {showPasteModal && (
        <div className="fixed inset-0 bg-black/70 z-50 flex items-end sm:items-center justify-center p-0 sm:p-4">
          <div className="bg-slate-900 border border-slate-700 rounded-t-2xl sm:rounded-xl w-full sm:max-w-lg max-h-[90vh] flex flex-col">
            <div className="flex items-center justify-between p-4 border-b border-slate-800">
              <h3 className="text-white font-semibold">Import Holdings</h3>
              <button onClick={() => setShowPasteModal(false)} className="text-slate-400 hover:text-white">
                <X size={20} />
              </button>
            </div>
            <div className="p-4 flex-1 overflow-auto space-y-3">
              <p className="text-slate-400 text-sm">
                Paste your CSV data below. Copy from Zerodha Console (Holdings → select all → copy) or any spreadsheet.
              </p>
              <p className="text-slate-500 text-xs">
                Format: <code className="text-slate-400">Symbol, Qty, Avg Price, LTP</code> (one per line)
              </p>
              <textarea
                value={csvText}
                onChange={(e) => setCsvText(e.target.value)}
                placeholder={`Instrument,Qty.,Avg. cost,LTP\nRELIANCE,25,2650.00,2945.50\nTCS,15,3450.00,3820.75\nHDFCBANK,40,1520.00,1685.30`}
                rows={10}
                className="w-full bg-slate-800 border border-slate-700 rounded-lg px-3 py-2.5 text-white text-sm font-mono placeholder-slate-600 focus:outline-none focus:border-blue-500 resize-none"
              />
            </div>
            <div className="p-4 border-t border-slate-800 flex gap-3">
              <button
                onClick={() => setShowPasteModal(false)}
                className="flex-1 bg-slate-800 hover:bg-slate-700 text-white text-sm font-medium px-4 py-2.5 rounded-lg transition-colors"
              >
                Cancel
              </button>
              <button
                onClick={handlePasteSubmit}
                disabled={!csvText.trim() || uploading}
                className="flex-1 bg-emerald-600 hover:bg-emerald-500 disabled:opacity-50 text-white text-sm font-medium px-4 py-2.5 rounded-lg transition-colors"
              >
                {uploading ? 'Uploading...' : 'Import'}
              </button>
            </div>
          </div>
        </div>
      )}

      {data && (
        <>
          <MetricsBar data={data} />
          <div className="grid grid-cols-1 lg:grid-cols-3 gap-6">
            <div className="lg:col-span-2">
              <HoldingsTable holdings={data.holdings} />
            </div>
            <div>
              <SectorChart sectorAllocation={data.sectorAllocation} />
            </div>
          </div>
        </>
      )}

      {!data && (
        <div className="bg-slate-900 border border-slate-800 rounded-xl p-12 text-center">
          <p className="text-slate-400">No portfolio data yet.</p>
          <p className="text-slate-500 text-sm mt-2">Upload a CSV from Zerodha Console or connect Zerodha to sync your holdings.</p>
        </div>
      )}
    </div>
  );
};
