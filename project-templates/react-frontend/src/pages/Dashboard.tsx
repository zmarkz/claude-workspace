import { useAuthStore } from '../store/authStore'

export default function Dashboard() {
  const user = useAuthStore((state) => state.user)

  return (
    <div className="space-y-6">
      <div>
        <h1 className="text-4xl font-bold text-gray-900 dark:text-white mb-2">
          Welcome, {user?.name || 'User'}!
        </h1>
        <p className="text-gray-600 dark:text-gray-400">
          This is your dashboard. Start building your application here.
        </p>
      </div>

      <div className="grid grid-cols-1 md:grid-cols-3 gap-6">
        <div className="bg-white dark:bg-slate-900 rounded-lg p-6 border border-gray-200 dark:border-slate-800">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
            Getting Started
          </h3>
          <p className="text-gray-600 dark:text-gray-400 text-sm">
            Edit the Dashboard component to customize your homepage.
          </p>
        </div>

        <div className="bg-white dark:bg-slate-900 rounded-lg p-6 border border-gray-200 dark:border-slate-800">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
            Add Pages
          </h3>
          <p className="text-gray-600 dark:text-gray-400 text-sm">
            Create new pages in the src/pages directory and add routes to App.tsx.
          </p>
        </div>

        <div className="bg-white dark:bg-slate-900 rounded-lg p-6 border border-gray-200 dark:border-slate-800">
          <h3 className="text-lg font-semibold text-gray-900 dark:text-white mb-2">
            API Integration
          </h3>
          <p className="text-gray-600 dark:text-gray-400 text-sm">
            Use the API client in src/api/client.ts to fetch data from your backend.
          </p>
        </div>
      </div>
    </div>
  )
}
