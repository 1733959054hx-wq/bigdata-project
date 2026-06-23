<!-- src/views/ThemeTest.vue -->
<template>
  <div class="theme-test-container">
    <div class="theme-header">
      <h1>🎨 主题测试页面</h1>
      <p class="theme-subtitle">测试完整的深色主题系统</p>
    </div>
    
    <!-- 主题切换控制 -->
    <el-card class="theme-control-card">
      <template #header>
        <div class="card-header">
          <span>主题切换</span>
          <el-tag :type="currentThemeTagType">{{ currentThemeDisplay }}</el-tag>
        </div>
      </template>
      
      <div class="theme-buttons">
        <el-button 
          @click="setTheme('light')" 
          :type="appliedTheme === 'light' ? 'primary' : ''"
          class="theme-button"
        >
          <el-icon><Sunny /></el-icon>
          浅色模式
        </el-button>
        
        <el-button 
          @click="setTheme('dark')" 
          :type="appliedTheme === 'dark' ? 'primary' : ''"
          class="theme-button"
        >
          <el-icon><Moon /></el-icon>
          深色模式
        </el-button>
        
        <el-button 
          @click="setTheme('auto')" 
          :type="currentTheme === 'auto' ? 'primary' : ''"
          class="theme-button"
        >
          <el-icon><Monitor /></el-icon>
          跟随系统
        </el-button>
      </div>
      
      <div class="theme-info">
        <el-descriptions :column="2" border>
          <el-descriptions-item label="当前设置">{{ currentThemeDisplay }}</el-descriptions-item>
          <el-descriptions-item label="系统主题">{{ systemThemeDisplay }}</el-descriptions-item>
          <el-descriptions-item label="应用主题">{{ appliedThemeDisplay }}</el-descriptions-item>
          <el-descriptions-item label="主题状态">
            <el-tag :type="isDark ? '' : 'warning'">
              {{ isDark ? '深色模式' : '浅色模式' }}
            </el-tag>
          </el-descriptions-item>
        </el-descriptions>
      </div>
    </el-card>

    <!-- Element Plus 组件测试 -->
    <div class="components-test">
      <el-row :gutter="20">
        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="component-card">
            <template #header>
              <span>表单组件</span>
            </template>
            
            <el-form :model="form" label-width="80px">
              <el-form-item label="用户名">
                <el-input 
                  v-model="form.username" 
                  placeholder="请输入用户名"
                  clearable
                />
              </el-form-item>
              
              <el-form-item label="邮箱">
                <el-input 
                  v-model="form.email" 
                  placeholder="请输入邮箱"
                  type="email"
                />
              </el-form-item>
              
              <el-form-item label="角色">
                <el-select v-model="form.role" placeholder="请选择角色">
                  <el-option label="管理员" value="admin" />
                  <el-option label="用户" value="user" />
                  <el-option label="访客" value="guest" />
                </el-select>
              </el-form-item>
              
              <el-form-item label="通知">
                <el-switch v-model="form.notifications" />
                <span class="switch-label">{{ form.notifications ? '开启' : '关闭' }}</span>
              </el-form-item>
              
              <el-form-item label="偏好">
                <el-radio-group v-model="form.preference">
                  <el-radio label="option1">选项一</el-radio>
                  <el-radio label="option2">选项二</el-radio>
                </el-radio-group>
              </el-form-item>
            </el-form>
            
            <div class="form-actions">
              <el-button type="primary">保存设置</el-button>
              <el-button>取消</el-button>
              <el-button type="danger" plain>删除</el-button>
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="component-card">
            <template #header>
              <span>数据展示</span>
            </template>
            
            <el-table :data="tableData" style="width: 100%">
              <el-table-column prop="date" label="日期" width="120" />
              <el-table-column prop="name" label="姓名" width="120" />
              <el-table-column prop="address" label="地址" />
              <el-table-column label="操作" width="120">
                <template #default>
                  <el-button link type="primary" size="small">编辑</el-button>
                  <el-button link type="danger" size="small">删除</el-button>
                </template>
              </el-table-column>
            </el-table>
            
            <div class="pagination-demo">
              <el-pagination
                layout="prev, pager, next"
                :total="50"
                :page-size="10"
              />
            </div>
          </el-card>
        </el-col>

        <el-col :xs="24" :sm="12" :md="8">
          <el-card class="component-card">
            <template #header>
              <span>消息与反馈</span>
            </template>
            
            <div class="message-demo">
              <el-button @click="showMessage('success')" type="success">
                成功消息
              </el-button>
              <el-button @click="showMessage('warning')" type="warning">
                警告消息
              </el-button>
              <el-button @click="showMessage('error')" type="danger">
                错误消息
              </el-button>
              <el-button @click="showMessage('info')" type="info">
                信息消息
              </el-button>
            </div>
            
            <el-divider />
            
            <div class="progress-demo">
              <el-progress :percentage="70" />
              <el-progress :percentage="100" status="success" />
              <el-progress :percentage="50" status="exception" />
            </div>
            
            <el-divider />
            
            <div class="tag-demo">
              <el-tag>默认标签</el-tag>
              <el-tag type="success">成功标签</el-tag>
              <el-tag type="info">信息标签</el-tag>
              <el-tag type="warning">警告标签</el-tag>
              <el-tag type="danger">危险标签</el-tag>
            </div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <!-- 颜色测试区域 -->
    <el-card class="color-test-card">
      <template #header>
        <span>颜色变量测试</span>
      </template>
      
      <div class="color-grid">
        <div class="color-item bg-primary">
          <span>主色</span>
          <code>--primary</code>
        </div>
        <div class="color-item bg-success">
          <span>成功色</span>
          <code>--success</code>
        </div>
        <div class="color-item bg-warning">
          <span>警告色</span>
          <code>--warning</code>
        </div>
        <div class="color-item bg-error">
          <span>错误色</span>
          <code>--error</code>
        </div>
        <div class="color-item bg-card">
          <span>卡片背景</span>
          <code>--card-bg</code>
        </div>
        <div class="color-item bg-main">
          <span>主背景</span>
          <code>--main-bg</code>
        </div>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { Sunny, Moon, Monitor } from '@element-plus/icons-vue'
import { useThemeStore } from '@/stores/theme'

const themeStore = useThemeStore()

const form = ref({
  username: '',
  email: '',
  role: '',
  notifications: true,
  preference: 'option1'
})

const systemTheme = ref('')

const tableData = [
  { date: '2024-01-01', name: '张三', address: '北京市朝阳区' },
  { date: '2024-01-02', name: '李四', address: '上海市浦东新区' },
  { date: '2024-01-03', name: '王五', address: '广州市天河区' }
]

// 使用 store 中的响应式数据
const currentTheme = computed(() => themeStore.currentTheme)
const appliedTheme = computed(() => themeStore.appliedTheme)
const isDark = computed(() => themeStore.isDark)

const currentThemeDisplay = computed(() => {
  const names = { light: '浅色', dark: '深色', auto: '自动' }
  return names[currentTheme.value] || currentTheme.value
})

const systemThemeDisplay = computed(() => {
  return systemTheme.value === 'dark' ? '深色' : '浅色'
})

const appliedThemeDisplay = computed(() => {
  return appliedTheme.value === 'dark' ? '深色' : '浅色'
})

const currentThemeTagType = computed(() => {
  if (currentTheme.value === 'auto') return 'info'
  return appliedTheme.value === 'dark' ? '' : 'warning'
})

// 方法
const setTheme = (theme) => {
  themeStore.setTheme(theme)
  ElMessage.success(`已切换到${theme === 'auto' ? '自动模式' : theme === 'dark' ? '深色模式' : '浅色模式'}`)
  
  // 添加延迟后强制刷新页面
  setTimeout(() => {
    console.log('🔄 强制刷新页面以应用主题')
    window.location.reload()
  }, 300) // 500ms 延迟让用户看到成功消息
}

const showMessage = (type) => {
  const messages = {
    success: '这是一条成功消息',
    warning: '这是一条警告消息',
    error: '这是一条错误消息',
    info: '这是一条信息消息'
  }
  ElMessage[type](messages[type])
}

const updateSystemTheme = () => {
  systemTheme.value = window.matchMedia('(prefers-color-scheme: dark)').matches ? 'dark' : 'light'
}

// 生命周期
onMounted(() => {
  // 初始化主题
  themeStore.init()
  
  updateSystemTheme()
  
  // 监听系统主题变化
  const mediaQuery = window.matchMedia('(prefers-color-scheme: dark)')
  mediaQuery.addEventListener('change', updateSystemTheme)
  
  // 保存清理函数
  onUnmounted(() => {
    mediaQuery.removeEventListener('change', updateSystemTheme)
  })
})
</script>

<style scoped>
.theme-test-container {
  padding: 20px;
  background: var(--main-bg);
  min-height: 100vh;
  transition: background-color 0.3s ease;
}

.theme-header {
  text-align: center;
  margin-bottom: 30px;
}

.theme-header h1 {
  color: var(--text-primary);
  margin-bottom: 8px;
  transition: color 0.3s ease;
}

.theme-subtitle {
  color: var(--text-secondary);
  font-size: 16px;
  transition: color 0.3s ease;
}

.theme-control-card,
.component-card,
.color-test-card {
  background: var(--card-bg);
  border: 1px solid var(--border-light);
  margin-bottom: 20px;
  transition: all 0.3s ease;
}

.card-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.theme-buttons {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.theme-button {
  flex: 1;
  min-width: 120px;
}

.theme-info {
  margin-top: 20px;
}

.components-test {
  margin: 30px 0;
}

.switch-label {
  margin-left: 8px;
  color: var(--text-secondary);
  font-size: 14px;
  transition: color 0.3s ease;
}

.form-actions {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
  margin-top: 20px;
}

.pagination-demo {
  display: flex;
  justify-content: center;
  margin-top: 20px;
}

.message-demo {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
  margin-bottom: 20px;
}

.progress-demo {
  display: flex;
  flex-direction: column;
  gap: 15px;
}

.tag-demo {
  display: flex;
  gap: 8px;
  flex-wrap: wrap;
}

.color-test-card {
  margin-top: 30px;
}

.color-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(150px, 1fr));
  gap: 16px;
}

.color-item {
  padding: 20px;
  border-radius: 8px;
  text-align: center;
  color: white;
  font-weight: 500;
  box-shadow: var(--shadow-sm);
  transition: all 0.3s ease;
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.color-item code {
  background: rgba(0, 0, 0, 0.3);
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
  font-family: 'Monaco', 'Consolas', monospace;
}

.bg-primary { background: var(--primary); }
.bg-success { background: var(--success); }
.bg-warning { background: var(--warning); }
.bg-error { background: var(--error); }
.bg-card { 
  background: var(--card-bg); 
  color: var(--text-primary);
  border: 1px solid var(--border-light);
}
.bg-card code { background: var(--bg-tertiary); }
.bg-main { 
  background: var(--main-bg); 
  color: var(--text-primary);
  border: 1px solid var(--border-light);
}
.bg-main code { background: var(--bg-tertiary); }

/* 响应式设计 */
@media (max-width: 768px) {
  .theme-test-container {
    padding: 16px;
  }
  
  .theme-buttons {
    flex-direction: column;
  }
  
  .theme-button {
    width: 100%;
  }
  
  .color-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 480px) {
  .theme-test-container {
    padding: 12px;
  }
  
  .color-grid {
    grid-template-columns: 1fr;
  }
  
  .message-demo {
    flex-direction: column;
  }
  
  .form-actions {
    flex-direction: column;
  }
}
</style>