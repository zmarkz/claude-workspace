import { NavLink, useLocation } from 'react-router-dom';
import { BarChart2, Bot, Home, List, TrendingUp, LogOut, Menu, X } from 'lucide-react';
import { useAuthStore } from '../../store/authStore';
import { useState, useEffect } from 'react';

const navItems = [
  { to: '/dashboard', icon: Home, label: 'Dashboard' },
  { to: '/holdings', icon: List, label: 'Holdings' },
  { to: '/stocks', icon: TrendingUp, label: 'Stocks' },
  { to: '/transactions', icon: BarChart2, label: 'Transactions' },
  { to: '/ai', icon: Bot, label: 'AI Assessment' },
];

export const Sidebar = ({ open, onClose }: { open: boolean; onClose: () => void }) => {
  const logout = useAuthStore((s) => s.logout);
  const location = useLocation();

  // Close sidebar on route change (mobile)
  useEffect(() => {
    onClose();
  }, [location.pathname]);

  return (
    <>
      {/* Overlay for mobile */}
      {open && (
        <div
          className="fixed inset-0 bg-black/60 z-40 lg:hidden"
          onClick={onClose}
        />
      )}

      <aside
        className={`fixed left-0 top-0 h-full w-60 bg-slate-900 border-r border-slate-800 flex flex-col z-50 transition-transform duration-200 ${
          open ? 'translate-x-0' : '-translate-x-full'
        } lg:translate-x-0`}
      >
        <div className="p-6 border-b border-slate-800 flex items-center justify-between">
          <div className="flex items-center gap-2">
            <TrendingUp size={20} className="text-blue-400" />
            <span className="text-white font-semibold text-lg">PortfolioAI</span>
          </div>
          <button onClick={onClose} className="lg:hidden text-slate-400 hover:text-white">
            <X size={20} />
          </button>
        </div>

        <nav className="flex-1 p-4 space-y-1">
          {navItems.map(({ to, icon: Icon, label }) => (
            <NavLink
              key={to}
              to={to}
              className={({ isActive }) =>
                `flex items-center gap-3 px-3 py-2.5 rounded-lg text-sm font-medium transition-colors ${
                  isActive
                    ? 'bg-blue-600 text-white'
                    : 'text-slate-400 hover:text-white hover:bg-slate-800'
                }`
              }
            >
              <Icon size={18} />
              {label}
            </NavLink>
          ))}
        </nav>

        <div className="p-4 border-t border-slate-800">
          <button
            onClick={logout}
            className="flex items-center gap-3 px-3 py-2.5 w-full rounded-lg text-sm font-medium text-slate-400 hover:text-white hover:bg-slate-800 transition-colors"
          >
            <LogOut size={18} />
            Logout
          </button>
        </div>
      </aside>
    </>
  );
};
