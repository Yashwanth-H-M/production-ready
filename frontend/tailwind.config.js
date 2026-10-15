/** @type {import('tailwindcss').Config} */
module.exports = {
  content: [
    './app/**/*.{js,ts,jsx,tsx,mdx}',
    './components/**/*.{js,ts,jsx,tsx,mdx}',
    './lib/**/*.{js,ts,jsx,tsx,mdx}',
  ],
  theme: {
    extend: {
      colors: {
        canopy: '#0F7C61',
        lime: '#D9FF5A',
        ink: '#18252A',
      },
      fontFamily: {
        brand: ['Manrope', 'Inter', 'sans-serif'],
        body: ['Inter', 'sans-serif'],
        data: ['IBM Plex Mono', 'monospace'],
      },
      boxShadow: {
        soft: '0 1px 2px rgba(16,24,40,0.02), 0 12px 30px rgba(23,36,39,0.035)',
      },
    },
  },
  plugins: [],
};
