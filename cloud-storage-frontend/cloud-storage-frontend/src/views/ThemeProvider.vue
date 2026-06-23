<!-- src/components/ThemeProvider.vue -->
<template>
  <div :key="themeStore.version" class="theme-provider">
    <slot />
  </div>
</template>

<script setup>
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()
</script>

<style scoped>
.theme-provider {
  /* 这个组件本身没有样式，只是用于强制重新渲染 */
}
</style>