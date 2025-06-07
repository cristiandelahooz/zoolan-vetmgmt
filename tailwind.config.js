const PREFIX = './src/main/frontend'

/** @type {import('tailwindcss').Config} */
export default {
  content: [`${PREFIX}/index.html`, `${PREFIX}/**/*.{js,ts,jsx,tsx,html}`],
  theme: {
    extend: {}
  },
  plugins: []
}
