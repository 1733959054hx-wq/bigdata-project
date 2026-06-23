// src/utils/simple-theme.js

// 创建响应式主题状态
let currentTheme = 'auto'
const themeListeners = new Set()

// 通知所有监听器主题已改变
function notifyThemeChange(theme) {
  themeListeners.forEach(listener => {
    listener(theme)
  })
}

export function applyTheme(theme) {
  const html = document.documentElement;
  
  // 移除所有主题类
  html.classList.remove('light', 'dark', 'light-theme', 'dark-theme');
  
  let themeToApply = theme;
  
  if (theme === 'auto') {
    // 检测系统主题
    const isDark = window.matchMedia('(prefers-color-scheme: dark)').matches;
    themeToApply = isDark ? 'dark' : 'light';
  }
  
  // 添加当前主题类 - 同时添加两种类名确保兼容性
  html.classList.add(themeToApply);
  html.classList.add(themeToApply + '-theme');
  
  // 设置 data-theme 属性供其他组件使用
  html.setAttribute('data-theme', themeToApply);
  
  // 保存到localStorage
  localStorage.setItem('app-theme', theme);
  currentTheme = theme
  
  console.log('应用主题:', themeToApply, '原始设置:', theme);
  
  // 触发自定义事件，让组件可以响应主题变化
  window.dispatchEvent(new CustomEvent('theme-change', { 
    detail: { theme: themeToApply, themeSetting: theme } 
  }));
  
  // 通知所有监听器
  notifyThemeChange(themeToApply)
}

export function initTheme() {
  const savedTheme = localStorage.getItem('app-theme') || 'auto';
  applyTheme(savedTheme);
  return savedTheme;
}

export function getCurrentTheme() {
  return localStorage.getItem('app-theme') || 'auto';
}

export function getAppliedTheme() {
  const html = document.documentElement;
  if (html.classList.contains('dark') || html.classList.contains('dark-theme')) {
    return 'dark';
  }
  return 'light';
}

// 添加主题监听器
export function onThemeChange(callback) {
  themeListeners.add(callback)
  return () => themeListeners.delete(callback)
}

// 创建响应式主题ref
export function useTheme() {
  const theme = ref(getAppliedTheme())
  
  onMounted(() => {
    const stop = onThemeChange((newTheme) => {
      theme.value = newTheme
    })
    onUnmounted(stop)
  })
  
  return theme
}