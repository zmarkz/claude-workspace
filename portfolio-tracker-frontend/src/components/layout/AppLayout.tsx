import { useState } from 'react';
import { Outlet } from 'react-router-dom';
import { Menu } from 'lucide-react';
import { Sidebar } from './Sidebar';
import { useWebSocket } from '../../hooks/useWebSocket';

export const AppLayout = () => {
  useWebSocket();
  const [sidebarOpen, setSidebarOpen] = useState(false);

  return (
    <div className="flex min-h-screen bg-slate-950">
      <Sidebar open={sidebarOpen} onClose={() => setSidebarOpen(false)} />

      <div className="flex-1 lg:ml-60 flex flex-col min-h-screen">
        {/* Mobile top bar */}
        <header className="lg:hidden flex items-center gap-3 px-4 py-3 bg-slate-900 border-b border-slate-800 sticky top-0 z-30">
          <button
            onClick={() => setSidebarOpen(true)}
            className="text-slate-400 hover:text-white p-1"
          >
            <Menu size={22} />
          </button>
          <span className="text-white font-semibold">PortfolioAI</span>
        </header>

        <main className="flex-1 p-4 sm:p-6 lg:p-8 overflow-auto">
          <Outlet />
        </main>
      </div>
    </div>
  );
};
