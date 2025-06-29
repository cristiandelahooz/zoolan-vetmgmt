import * as path from 'path'
import { fileURLToPath } from 'url'
import type { UserConfig, ConfigEnv } from 'vite'
import { overrideVaadinConfig } from './vite.generated'

const __filename = fileURLToPath(import.meta.url)
const __dirname = path.dirname(__filename)

const customConfig = (env: ConfigEnv): UserConfig => ({
  resolve: {
    alias: {
      '@': path.resolve(__dirname, 'src/main/frontend'),
    },
  },
})

export default overrideVaadinConfig(customConfig)
