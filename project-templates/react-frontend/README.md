# {{APP_NAME}}

A modern React 19 frontend template with TypeScript, Tailwind CSS, and best practices built-in.

## Quick Start

### Prerequisites
- Node.js 18+
- npm or pnpm

### Installation

1. Clone and rename the project:
   ```bash
   cp -r react-frontend my-app
   cd my-app
   ```

2. Replace template placeholders:
   ```bash
   # Replace {{APP_NAME}} with your app name
   sed -i 's/{{APP_NAME}}/YourAppName/g' package.json index.html .env.example src/components/layout/Sidebar.tsx
   ```

3. Install dependencies:
   ```bash
   npm install
   ```

4. Configure environment:
   ```bash
   cp .env.example .env.local
   # Edit .env.local with your API URL
   ```

5. Start development server:
   ```bash
   npm run dev
   ```

Visit `http://localhost:5173` in your browser.

## Available Scripts

- `npm run dev` - Start dev server
- `npm run build` - Build for production
- `npm run preview` - Preview production build
- `npm run lint` - Type check with TypeScript

## Project Structure

```
src/
├── api/              # API client and endpoints
├── components/       # Reusable components
│   └── layout/      # Layout components
├── pages/           # Page components
├── store/           # Zustand stores
├── types/           # TypeScript types
├── App.tsx          # Root component with routing
├── main.tsx         # Entry point
└── index.css        # Global styles
```

## Features

- **React 19** with JSX transform
- **TypeScript** with strict mode
- **Vite** for fast builds and HMR
- **Tailwind CSS** with @tailwindcss/vite
- **React Router** for SPA routing
- **Zustand** for state management
- **React Query** for server state
- **Axios** with interceptors
- **Lucide React** for icons
- **Authentication** with token-based login
- **Protected routes** with role support
- **Dark mode** ready CSS

## Authentication

The template includes a complete auth system:

- Login page with form validation
- Zustand store for auth state (token, user)
- Axios interceptors for auth headers
- Protected routes that redirect to login
- Automatic logout on 401 responses
- Persist auth state to localStorage

## API Integration

The Axios client in `src/api/client.ts`:
- Uses `VITE_API_URL` environment variable
- Automatically adds auth token to requests
- Handles 401 errors by redirecting to login
- Provides typed error handling

## Docker Deployment

Build and run with Docker:

```bash
docker build -t my-app .
docker run -p 80:3000 my-app
```

The Dockerfile:
- Uses Node Alpine for builds
- Includes Nginx for serving
- Configures SPA routing with fallback to index.html
- Enables gzip compression
- Sets cache headers for assets

## Styling

Uses Tailwind CSS with dark mode support via CSS classes. Configure in `tailwind.config.js` if needed.

## Development Tips

1. **Add new pages**: Create in `src/pages/`, add route to `App.tsx`
2. **Add new components**: Create in `src/components/`
3. **Create API endpoints**: Add to `src/api/` files
4. **Add store slices**: Use Zustand in `src/store/`
5. **Type everything**: No `any` types - use TypeScript properly

## Production Build

```bash
npm run build
```

Output goes to `dist/` directory, ready for deployment.

## License

MIT
