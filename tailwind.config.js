/** @type {import('tailwindcss').Config} */
export default {
  content: [
    "./index.html",
    "./src/**/*.{js,ts,jsx,tsx}",
  ],
  theme: {
    extend: {
      colors: {
        navy: {
          900: '#0F172A',
          950: '#0A0F1D',
          800: '#1E293B',
          700: '#334155',
        },
        brand: {
          blue: '#1E40AF',
          lightBlue: '#3B82F6',
          cyan: '#06B6D4',
          indigo: '#4F46E5',
        },
        safety: {
          green: '#10B981',
          greenDark: '#047857',
          amber: '#F59E0B',
          red: '#EF4444',
          redDark: '#B91C1C',
        }
      },
      fontFamily: {
        sans: ['Inter', 'system-ui', '-apple-system', 'BlinkMacSystemFont', 'Segoe UI', 'Roboto', 'sans-serif'],
        mono: ['JetBrains Mono', 'Fira Code', 'Courier New', 'monospace'],
      },
      animation: {
        'scan-beam': 'scanBeam 2s ease-in-out infinite alternate',
        'pulse-fast': 'pulse 1s cubic-bezier(0.4, 0, 0.6, 1) infinite',
      },
      keyframes: {
        scanBeam: {
          '0%': { top: '5%', opacity: '0.9' },
          '100%': { top: '92%', opacity: '0.9' },
        }
      }
    },
  },
  plugins: [],
}
