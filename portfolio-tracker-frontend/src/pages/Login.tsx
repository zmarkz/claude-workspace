import { useState } from 'react';
import { useNavigate } from 'react-router-dom';
import { TrendingUp } from 'lucide-react';
import { login, register } from '../api/auth';
import { useAuthStore } from '../store/authStore';

export const Login = () => {
  const [mode, setMode] = useState<'login' | 'register'>('login');
  const [form, setForm] = useState({ email: '', password: '', firstName: '', lastName: '' });
  const [error, setError] = useState('');
  const [loading, setLoading] = useState(false);
  const setAuth = useAuthStore((s) => s.setAuth);
  const navigate = useNavigate();

  const handleSubmit = async (e: React.FormEvent) => {
    e.preventDefault();
    setError('');
    setLoading(true);
    try {
      const data = mode === 'login'
        ? await login({ email: form.email, password: form.password })
        : await register(form);
      setAuth(data);
      navigate('/dashboard');
    } catch (err: any) {
      setError(err.response?.data?.message ?? 'Authentication failed. Please try again.');
    } finally {
      setLoading(false);
    }
  };

  return (
    <div className="min-h-screen bg-slate-950 flex items-center justify-center p-4">
      <div className="w-full max-w-md">
        <div className="text-center mb-8">
          <div className="flex items-center justify-center gap-2 mb-4">
            <TrendingUp size={28} className="text-blue-400" />
            <span className="text-white text-2xl font-bold">PortfolioAI</span>
          </div>
          <p className="text-slate-400">Track your Zerodha portfolio with AI insights</p>
        </div>

        <div className="bg-slate-900 border border-slate-800 rounded-xl p-8">
          <div className="flex mb-6 bg-slate-800 rounded-lg p-1">
            {(['login', 'register'] as const).map((m) => (
              <button
                key={m}
                onClick={() => setMode(m)}
                className={`flex-1 py-2 rounded-md text-sm font-medium capitalize transition-colors ${
                  mode === m ? 'bg-blue-600 text-white' : 'text-slate-400 hover:text-white'
                }`}
              >
                {m}
              </button>
            ))}
          </div>

          <form onSubmit={handleSubmit} className="space-y-4">
            {mode === 'register' && (
              <div className="grid grid-cols-2 gap-4">
                <div>
                  <label className="text-slate-400 text-sm block mb-1.5">First Name</label>
                  <input
                    required
                    value={form.firstName}
                    onChange={(e) => setForm({ ...form, firstName: e.target.value })}
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-blue-500 text-sm"
                  />
                </div>
                <div>
                  <label className="text-slate-400 text-sm block mb-1.5">Last Name</label>
                  <input
                    required
                    value={form.lastName}
                    onChange={(e) => setForm({ ...form, lastName: e.target.value })}
                    className="w-full bg-slate-800 border border-slate-700 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-blue-500 text-sm"
                  />
                </div>
              </div>
            )}
            <div>
              <label className="text-slate-400 text-sm block mb-1.5">Email</label>
              <input
                type="email"
                required
                value={form.email}
                onChange={(e) => setForm({ ...form, email: e.target.value })}
                className="w-full bg-slate-800 border border-slate-700 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-blue-500 text-sm"
              />
            </div>
            <div>
              <label className="text-slate-400 text-sm block mb-1.5">Password</label>
              <input
                type="password"
                required
                value={form.password}
                onChange={(e) => setForm({ ...form, password: e.target.value })}
                className="w-full bg-slate-800 border border-slate-700 rounded-lg px-4 py-2.5 text-white focus:outline-none focus:border-blue-500 text-sm"
              />
            </div>

            {error && (
              <div className="bg-red-900/50 border border-red-700 rounded-lg px-4 py-2.5 text-red-300 text-sm">
                {error}
              </div>
            )}

            <button
              type="submit"
              disabled={loading}
              className="w-full bg-blue-600 hover:bg-blue-500 disabled:opacity-60 disabled:cursor-not-allowed text-white font-medium rounded-lg py-2.5 transition-colors"
            >
              {loading ? 'Please wait...' : mode === 'login' ? 'Sign In' : 'Create Account'}
            </button>
          </form>
        </div>
      </div>
    </div>
  );
};
