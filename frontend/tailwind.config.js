/** @type {import('tailwindcss').Config} */
export default {
  content: [
    './index.html',
    './src/**/*.{js,ts,jsx,tsx}',
  ],
  darkMode: 'class',
  theme: {
    extend: {
      colors: {
        pulse: {
          bg: '#0f172a',
          card: '#1e293b',
          text: '#f1f5f9',
          positive: '#22c55e',
          negative: '#ef4444',
          neutral: '#6b7280',
          request: '#3b82f6',
          question: '#a855f7',
        },
      },
    },
  },
  plugins: [],
}
