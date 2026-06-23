// src/stores/theme.js
import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import { initTheme, applyTheme, getCurrentTheme, getAppliedTheme, onThemeChange } from '@/utils/simple-theme'

export const useThemeStore = defineStore('theme', () => {
  const currentTheme = ref(getCurrentTheme())
  const appliedTheme = ref(getAppliedTheme())
  
  // 初始化主题
  const init = () => {
    initTheme()
    currentTheme.value = getCurrentTheme()
    appliedTheme.value = getAppliedTheme()
  }
  
  // 切换主题
  const setTheme = (theme) => {
    applyTheme(theme)
    currentTheme.value = theme
    appliedTheme.value = getAppliedTheme()
  }
  
  // 计算属性
  const isDark = computed(() => appliedTheme.value === 'dark')
  const isLight = computed(() => appliedTheme.value === 'light')
  const isAuto = computed(() => currentTheme.value === 'auto')
  
  // 监听主题变化
  onThemeChange((theme) => {
    appliedTheme.value = theme
  })
  
  return {
    currentTheme,
    appliedTheme,
    isDark,
    isLight,
    isAuto,
    init,
    setTheme
  }
})