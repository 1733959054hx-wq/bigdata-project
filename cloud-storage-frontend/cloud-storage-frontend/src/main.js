import { createApp } from 'vue'
import { createPinia } from 'pinia'
import App from './App.vue'
import ElementPlus from 'element-plus'
import 'element-plus/dist/index.css'
import router from './router'
import { initTheme } from './utils/theme'

// 引入样式文件
import './styles/theme.css'
import './styles/accessibility.css'
// ✨✨✨ 务必添加这一行，修复鼠标位置和布局问题 ✨✨✨
import './styles/force-fix.css' 

console.log('🚀 启动云存储前端应用...')

const app = createApp(App)
const pinia = createPinia()

app.use(pinia)
app.use(router)
app.use(ElementPlus)

initTheme()

app.mount('#app')

console.log('✅ 应用启动完成')