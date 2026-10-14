import { defineConfig } from 'vite'
import uni from '@dcloudio/vite-plugin-uni'

export default defineConfig({
  plugins: [uni()],
  server: {
    port: 5173,
    proxy: {
      // 代理后端接口 /api/* 到 8080
      // 但 bypass 排除前端源码模块 /api/*.js(源码目录名与接口前缀撞名)
      '/api': {
        target: 'http://localhost:8090',
        changeOrigin: true,
        secure: false,
        bypass(req) {
          // .js 模块请求交给 Vite 处理,不走后端代理
          const url = (req.url || '').split('?')[0]
          if (url.endsWith('.js')) {
            return req.url
          }
        }
      }
    }
  }
})
