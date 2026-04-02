import { NavLink } from 'react-router-dom'
import { Home, LogOut } from 'lucide-react'
import { useAuthStore } from '../../store/authStore'

export default function Sidebar() {
  const logout = useAuthStore((state) => state.logout)
  const user = useAuthStore((state) => state.user)

  const handleLogout = () => {
    logout()
  }

  const navLinkClass = ({ isActive }: { isActive: boolean }) =>
    `flex items-center gap-3 px-4 py-3 rounded-lg transition-colors ${
      isActive
        ? 'bg-blue-100 text-blue-900 dark:bg-blue-950 dark:text-blue-100'
        : 'text-gray-700 hover:bg-gray-100 dark:text-gray-400 dark:hover:bg-slate-800'
    }`

  return (
    <aside className="w-64 bg-white dark:bg-slate-900 border-r border-gray-200 dark:border-slate-800 flex flex-col">
      <div className="p-6 border-b border-gray-200 dark:border-slate-800">
        <h1 className="text-xl font-bold text-gray-900 dark:text-white">{{APP_NAME}}</h1>
        <p className="text-sm text-gray-600 dark:text-gray-400 mt-1">{user?.email}</p>
      </div>

      <nav className="flex-1 p-4 space-y-2">
        <NavLink to="/" className={navLinkClass}>
          <Home className="w-5 h-5" />
          <span>Dashboard</span>
        </NavLink>
      </nav>

      <div className="p-4 border-t border-gray-200 dark:border-slate-800">
        <button
          onClick={handleLogout}
          className="w-full flex items-center gap-3 px-4 py-3 text-red-600 hover:bg-red-50 dark:text-red-400 dark:hover:bg-red-950 rounded-lg transition-colors"
        >
          <LogOut className="w-5 h-5" />
          <span>Logout</span>
        </button>
      </div>
    </aside>
  )
}
