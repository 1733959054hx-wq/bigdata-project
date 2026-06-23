// src/utils/theme.js
// 统一使用 CSS 类名控制主题，彻底废弃行内样式，解决冲突

export function getSystemTheme() {
  return window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

export function applyTheme(theme) {
  const root = document.documentElement
  
  // 1. 关键修复：清除可能残留的旧版行内样式
  // 之前版本的 theme.js 会设置 style="--el-color-primary: ...", 这会导致 CSS 类名无法生效
  root.removeAttribute('style')
  
  // 2. 移除所有相关类名，防止叠加
  root.classList.remove('light', 'dark', 'light-theme', 'dark-theme')
  
  // 3. 确定实际应用的主题（处理 'auto' 情况）
  let themeToApply = theme
  if (theme === 'auto') {
    themeToApply = getSystemTheme()
  }
  
  // 4. 添加新类名 (Element Plus 和自定义样式都依赖 'dark' 类)
  root.classList.add(themeToApply)
  
  // 5. 保存设置到本地存储
  localStorage.setItem('app-theme', theme)
  
  console.log(`🎨 主题已切换: ${themeToApply} (设置: ${theme})`)
}

export function initTheme() {
  const savedTheme = localStorage.getItem('app-theme') || 'auto'
  applyTheme(savedTheme)
  
  // 监听系统主题变化 (仅当设置为 auto 时生效)
  window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', (e) => {
    const currentSetting = localStorage.getItem('app-theme')
    if (currentSetting === 'auto') {
      applyTheme('auto')
    }
  })
  
  return savedTheme
}

export function getCurrentTheme() {
  return localStorage.getItem('app-theme') || 'auto'
}