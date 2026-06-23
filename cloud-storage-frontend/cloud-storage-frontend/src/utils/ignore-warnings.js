// src/utils/simple-ignore-warnings.js
export function ignoreWarnings() {
  if (import.meta.env.DEV) {
    const originalWarn = console.warn
    console.warn = function(...args) {
      const message = args[0]?.toString() || ''
      // 忽略 Element Plus 的 CSS 兼容性警告
      if (message.includes('-moz-appearance') || 
          message.includes('scrollbar-width') ||
          message.includes('mask')) {
        return
      }
      originalWarn.apply(console, args)
    }
  }
}