<!-- src/views/Settings.vue -->
<template>
  <div class="settings-container" role="main" aria-labelledby="settings-title">
    <el-card class="settings-card">
      <h1 id="settings-title" class="sr-only">设置</h1>
      <el-tabs v-model="activeTab" class="settings-tabs" role="tablist" aria-label="设置分类">
        <!-- 通用设置 -->
        <el-tab-pane label="通用" name="general" role="tabpanel" aria-labelledby="tab-general">
          <h2 class="section-title">通用设置</h2>
          <el-form :model="generalForm" label-width="120px" class="settings-form" role="form">
            <el-form-item label="主题模式" role="group">
              <div class="theme-controls">
                <el-radio-group v-model="generalForm.theme" @change="handleThemeChange" class="theme-radio-group">
                  <el-radio-button value="light">浅色模式</el-radio-button>
                  <el-radio-button value="dark">深色模式</el-radio-button>
                  <el-radio-button value="auto">跟随系统</el-radio-button>
                </el-radio-group>
                
                <div class="theme-preview-container">
                  <p class="preview-title">点击预览图切换主题</p>
                  <div class="theme-previews">
                    <div 
                      class="theme-preview preview-light" 
                      :class="{ active: generalForm.theme === 'light' }" 
                      @click="setTheme('light')"
                    >
                      <div class="preview-window">
                        <div class="preview-header"></div>
                        <div class="preview-body">
                          <div class="preview-sidebar"></div>
                          <div class="preview-content">
                            <div class="preview-item"></div>
                            <div class="preview-item"></div>
                            <div class="preview-item"></div>
                          </div>
                        </div>
                      </div>
                      <span class="preview-label">浅色</span>
                    </div>
                    
                    <div 
                      class="theme-preview preview-dark" 
                      :class="{ active: generalForm.theme === 'dark' }" 
                      @click="setTheme('dark')"
                    >
                      <div class="preview-window">
                        <div class="preview-header"></div>
                        <div class="preview-body">
                          <div class="preview-sidebar"></div>
                          <div class="preview-content">
                            <div class="preview-item"></div>
                            <div class="preview-item"></div>
                            <div class="preview-item"></div>
                          </div>
                        </div>
                      </div>
                      <span class="preview-label">深色</span>
                    </div>
                    
                    <div 
                      class="theme-preview preview-auto" 
                      :class="{ active: generalForm.theme === 'auto' }" 
                      @click="setTheme('auto')"
                    >
                      <div class="preview-window">
                        <div class="preview-header">
                          <div class="auto-indicator">
                            <el-icon><Monitor /></el-icon>
                          </div>
                        </div>
                        <div class="preview-body">
                          <div class="preview-sidebar"></div>
                          <div class="preview-content">
                            <div class="preview-item"></div>
                            <div class="preview-item"></div>
                            <div class="preview-item"></div>
                          </div>
                        </div>
                      </div>
                      <span class="preview-label">跟随系统</span>
                    </div>
                  </div>
                </div>
                
                <!-- 当前主题状态显示 -->
                <div class="current-theme-status">
                  <el-alert
                    :title="currentThemeStatus.title"
                    :type="currentThemeStatus.type"
                    :description="currentThemeStatus.description"
                    :closable="false"
                    show-icon
                  />
                </div>
              </div>
            </el-form-item>
            
            <el-divider />
            
            <el-form-item label="默认打开方式">
              <el-select v-model="generalForm.defaultOpenMode" placeholder="选择默认打开方式">
                <el-option label="在线预览" value="preview" />
                <el-option label="下载后打开" value="download" />
              </el-select>
            </el-form-item>
            
            <el-form-item label="文件自动保存">
              <el-switch v-model="generalForm.autoSave" active-text="开启（每30秒）" />
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveGeneralSettings" class="save-button">
                <el-icon><Check /></el-icon>
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 通知设置 -->
        <el-tab-pane label="通知" name="notifications" role="tabpanel" aria-labelledby="tab-notifications">
          <h2 class="section-title">通知设置</h2>
          <el-form :model="notificationForm" label-width="120px" class="settings-form">
            <el-form-item label="新消息通知">
              <el-switch v-model="notificationForm.message" />
              <div class="setting-description">接收系统消息和通知</div>
            </el-form-item>
            
            <el-form-item label="文件共享通知">
              <el-switch v-model="notificationForm.share" />
              <div class="setting-description">当有人访问您的共享文件时通知您</div>
            </el-form-item>
            
            <el-form-item label="存储空间提醒">
              <el-switch v-model="notificationForm.storage" />
              <div class="setting-description">存储空间即将用完时发出提醒</div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="saveNotificationSettings" class="save-button">
                <el-icon><Check /></el-icon>
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 隐私设置 -->
        <el-tab-pane label="隐私" name="privacy" role="tabpanel" aria-labelledby="tab-privacy">
          <h2 class="section-title">隐私设置</h2>
          <el-form :model="privacyForm" label-width="120px" class="settings-form">
            <el-form-item label="允许文件被搜索">
              <el-switch v-model="privacyForm.allowSearch" />
              <div class="setting-description">允许其他用户通过搜索找到您的公开文件</div>
            </el-form-item>
            
            <el-form-item label="共享文件权限">
              <el-select v-model="privacyForm.sharePermission" placeholder="选择默认权限">
                <el-option label="仅查看" value="view" />
                <el-option label="可编辑" value="edit" />
                <el-option label="禁止共享" value="none" />
              </el-select>
              <div class="setting-description">设置共享文件的默认权限</div>
            </el-form-item>
            
            <el-form-item>
              <el-button type="primary" @click="savePrivacySettings" class="save-button">
                <el-icon><Check /></el-icon>
                保存设置
              </el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>

        <!-- 关于 -->
        <el-tab-pane label="关于" name="about" role="tabpanel" aria-labelledby="tab-about">
          <h2 class="section-title">关于</h2>
          <div class="about-section">
            <div class="app-info">
              <el-avatar :size="80" class="app-logo" shape="square">
                <Cloudy class="logo-icon" />
              </el-avatar>
              <div class="app-details">
                <h3>云存储 v1.0.0</h3>
                <p class="app-description">一款安全、高效的云端文件管理工具</p>
                <div class="app-features">
                  <el-tag type="success" size="small">安全可靠</el-tag>
                  <el-tag type="primary" size="small">高效便捷</el-tag>
                  <el-tag type="warning" size="small">跨平台</el-tag>
                </div>
              </div>
            </div>
            
            <el-divider />
            
            <div class="app-stats">
              <el-row :gutter="20">
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-value">256</div>
                    <div class="stat-label">文件数量</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-value">1.2 GB</div>
                    <div class="stat-label">已用空间</div>
                  </div>
                </el-col>
                <el-col :span="8">
                  <div class="stat-item">
                    <div class="stat-value">15</div>
                    <div class="stat-label">共享文件</div>
                  </div>
                </el-col>
              </el-row>
            </div>
            
            <el-divider />
            
            <div class="about-links">
              <h4>相关链接</h4>
              <div class="link-buttons">
                <el-button type="primary" text @click="openHelpCenter" class="link-button">
                  <el-icon><QuestionFilled /></el-icon>
                  帮助中心
                </el-button>
                <el-button type="primary" text @click="openTerms" class="link-button">
                  <el-icon><Document /></el-icon>
                  用户协议
                </el-button>
                <el-button type="primary" text @click="openPrivacyPolicy" class="link-button">
                  <el-icon><Lock /></el-icon>
                  隐私政策
                </el-button>
              </div>
            </div>
          </div>
        </el-tab-pane>
      </el-tabs>
    </el-card>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick } from 'vue'
import { ElMessage } from 'element-plus'
import { 
  Cloudy, 
  Check, 
  Monitor, 
  QuestionFilled, 
  Document, 
  Lock 
} from '@element-plus/icons-vue'
import { useStorage } from '@/hooks/useStorage'
import { useThemeStore } from '@/stores/theme'

const { getItem, setItem } = useStorage()
const themeStore = useThemeStore()

// 表单数据
const activeTab = ref('general')

// 使用 themeStore 中的主题状态
const generalForm = reactive({
  theme: themeStore.currentTheme || 'auto',
  defaultOpenMode: getItem('settings.general.defaultOpenMode') || 'preview',
  autoSave: getItem('settings.general.autoSave') !== false
})

const notificationForm = reactive({
  message: getItem('settings.notification.message') !== false,
  share: getItem('settings.notification.share') !== false,
  storage: getItem('settings.notification.storage') !== false
})

const privacyForm = reactive({
  allowSearch: getItem('settings.privacy.allowSearch') !== false,
  sharePermission: getItem('settings.privacy.sharePermission') || 'view'
})

// 计算当前主题状态显示
const currentThemeStatus = computed(() => {
  const isDark = themeStore.isDark
  const currentTheme = themeStore.currentTheme
  const appliedTheme = themeStore.appliedTheme
  
  if (currentTheme === 'auto') {
    return {
      title: '跟随系统主题',
      description: `当前系统主题为 ${appliedTheme === 'dark' ? '深色' : '浅色'} 模式，应用已自动适配`,
      type: 'info'
    }
  } else if (currentTheme === 'dark') {
    return {
      title: '深色模式已启用',
      description: '界面已切换为深色主题，适合在暗光环境下使用',
      type: 'success'
    }
  } else {
    return {
      title: '浅色模式已启用',
      description: '界面已切换为浅色主题，适合在明亮环境下使用',
      type: 'warning'
    }
  }
})

// 设置主题 - 使用 themeStore
const setTheme = (theme) => {
  generalForm.theme = theme
  handleThemeChange(theme)
}

// 主题变更时立即应用 - 使用 themeStore
const handleThemeChange = (theme) => {
  // 使用 themeStore 设置主题
  themeStore.setTheme(theme)
  
  // 保存到本地存储
  setItem('settings.general.theme', theme)
  
  const themeText = {
    'light': '浅色模式',
    'dark': '深色模式', 
    'auto': '跟随系统'
  }[theme]
  
  ElMessage.success(`主题已切换为${themeText}`)
  
  // 关键修改：添加延迟后强制刷新页面，确保主题完全应用
  setTimeout(() => {
    console.log('🔄 强制刷新页面以应用主题')
    window.location.reload()
  }, 500) // 500ms 延迟让用户看到成功消息
}

// 保存设置
const saveGeneralSettings = () => {
  setItem('settings.general', generalForm)
  ElMessage.success('通用设置已保存')
}

const saveNotificationSettings = () => {
  setItem('settings.notification', notificationForm)
  ElMessage.success('通知设置已保存')
}

const savePrivacySettings = () => {
  setItem('settings.privacy', privacyForm)
  ElMessage.success('隐私设置已保存')
}

// 关于页面功能
const openHelpCenter = () => {
  window.open('/help', '_blank')
}

const openTerms = () => {
  window.open('/terms', '_blank')
}

const openPrivacyPolicy = () => {
  window.open('/privacy', '_blank')
}

// 初始化时应用当前主题
onMounted(() => {
  // 确保主题存储已初始化
  themeStore.init()
  
  // 更新表单中的主题值
  generalForm.theme = themeStore.currentTheme
})
</script>

<style scoped>
.settings-container {
  padding: 24px;
  max-width: 900px;
  margin: 0 auto;
  background: var(--main-bg);
  min-height: 100vh;
  transition: background-color 0.3s ease;
}

.settings-card {
  border-radius: 16px;
  box-shadow: var(--shadow-md);
  border: 1px solid var(--border-light);
  background: var(--card-bg);
  transition: all 0.3s ease;
}

.settings-tabs {
  padding: 0;
}

:deep(.el-tabs__header) {
  margin: 0;
  padding: 0 24px;
  background: var(--card-bg);
  border-bottom: 1px solid var(--border-light);
}

:deep(.el-tabs__nav-wrap) {
  padding: 0;
}

:deep(.el-tabs__item) {
  padding: 20px 24px;
  font-weight: 500;
  color: var(--text-secondary);
  transition: all 0.3s ease;
}

:deep(.el-tabs__item.is-active) {
  color: var(--primary);
  font-weight: 600;
}

:deep(.el-tabs__active-bar) {
  background-color: var(--primary);
}

.section-title {
  margin: 0 0 24px 0;
  font-size: 20px;
  font-weight: 600;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.settings-form {
  padding: 24px;
}

.theme-controls {
  width: 100%;
}

.theme-radio-group {
  margin-bottom: 24px;
}

.theme-preview-container {
  margin-top: 16px;
}

.preview-title {
  margin: 0 0 16px 0;
  font-size: 14px;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.theme-previews {
  display: flex;
  gap: 24px;
  justify-content: space-between;
  margin-bottom: 24px;
}

.theme-preview {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 12px;
  cursor: pointer;
  transition: all 0.3s ease;
}

.theme-preview:hover {
  transform: translateY(-4px);
}

.theme-preview.active {
  transform: translateY(-4px);
}

.preview-window {
  width: 100%;
  max-width: 180px;
  border-radius: 12px;
  overflow: hidden;
  box-shadow: var(--shadow-md);
  border: 2px solid transparent;
  transition: all 0.3s ease;
}

.theme-preview.active .preview-window {
  border-color: var(--primary);
  box-shadow: var(--shadow-lg);
}

.preview-light .preview-window {
  background: #ffffff;
  border: 1px solid #e4e7ed;
}

.preview-dark .preview-window {
  background: #141414;
  border: 1px solid #4e4e4e;
}

.preview-auto .preview-window {
  background: linear-gradient(135deg, #f5f7fa 50%, #141414 50%);
  border: 1px solid #e4e7ed;
  position: relative;
}

.preview-header {
  height: 12px;
  background: var(--card-bg);
  border-bottom: 1px solid var(--border-light);
  position: relative;
}

.preview-auto .preview-header {
  background: linear-gradient(135deg, #ffffff 50%, #1a1a1a 50%);
}

.auto-indicator {
  position: absolute;
  top: 50%;
  left: 50%;
  transform: translate(-50%, -50%);
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  background: rgba(255, 255, 255, 0.2);
  border-radius: 50%;
}

.preview-body {
  display: flex;
  height: 60px;
}

.preview-sidebar {
  width: 25%;
  background: var(--sidebar-bg);
  border-right: 1px solid var(--border-light);
}

.preview-content {
  flex: 1;
  padding: 8px;
  display: flex;
  flex-direction: column;
  gap: 4px;
  background: var(--main-bg);
}

.preview-item {
  height: 4px;
  border-radius: 2px;
  background: var(--text-tertiary);
  opacity: 0.6;
}

.preview-item:nth-child(2) {
  width: 80%;
}

.preview-item:nth-child(3) {
  width: 60%;
}

.preview-label {
  font-size: 14px;
  font-weight: 500;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.theme-preview.active .preview-label {
  color: var(--primary);
  font-weight: 600;
}

.current-theme-status {
  margin-top: 16px;
}

.setting-description {
  margin-top: 4px;
  font-size: 12px;
  color: var(--text-tertiary);
  transition: color 0.3s ease;
}

.save-button {
  padding: 10px 24px;
  border-radius: 8px;
  font-weight: 500;
}

.about-section {
  padding: 24px;
}

.app-info {
  display: flex;
  align-items: center;
  gap: 20px;
  margin-bottom: 24px;
}

.app-logo {
  background: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
}

.logo-icon {
  font-size: 40px;
  color: white;
}

.app-details h3 {
  margin: 0 0 8px 0;
  font-size: 24px;
  font-weight: 600;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.app-description {
  margin: 0 0 12px 0;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.app-features {
  display: flex;
  gap: 8px;
}

.app-stats {
  margin: 24px 0;
}

.stat-item {
  text-align: center;
  padding: 16px;
  border-radius: 12px;
  background: var(--el-fill-color-light);
  transition: background-color 0.3s ease;
}

.stat-value {
  font-size: 24px;
  font-weight: 600;
  color: var(--primary);
  margin-bottom: 8px;
}

.stat-label {
  font-size: 14px;
  color: var(--text-secondary);
  transition: color 0.3s ease;
}

.about-links h4 {
  margin: 0 0 16px 0;
  font-size: 16px;
  font-weight: 600;
  color: var(--text-primary);
  transition: color 0.3s ease;
}

.link-buttons {
  display: flex;
  gap: 16px;
  flex-wrap: wrap;
}

.link-button {
  padding: 8px 16px;
  border-radius: 6px;
}

.sr-only {
  position: absolute;
  width: 1px;
  height: 1px;
  padding: 0;
  margin: -1px;
  overflow: hidden;
  clip: rect(0, 0, 0, 0);
  white-space: nowrap;
  border: 0;
}

/* 确保 Element Plus 组件在深色模式下正确显示 */
:deep(.el-form-item__label) {
  color: var(--el-text-color-primary) !important;
  font-weight: 500;
}

:deep(.el-tabs__item) {
  color: var(--el-text-color-primary) !important;
}

:deep(.el-tabs__item.is-active) {
  color: var(--el-color-primary) !important;
}

:deep(.el-radio-button__inner) {
  background: var(--card-bg) !important;
  color: var(--el-text-color-primary) !important;
  border-color: var(--el-border-color) !important;
}

:deep(.el-select .el-input__inner) {
  background: var(--card-bg) !important;
  color: var(--el-text-color-primary) !important;
  border-color: var(--el-border-color) !important;
}

:deep(.el-divider) {
  border-color: var(--border-light) !important;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .settings-container {
    padding: 16px;
  }
  
  .theme-previews {
    flex-direction: column;
    gap: 16px;
  }
  
  .theme-preview {
    flex-direction: row;
    justify-content: flex-start;
  }
  
  .preview-window {
    max-width: 100px;
  }
  
  .app-info {
    flex-direction: column;
    text-align: center;
  }
  
  .link-buttons {
    flex-direction: column;
  }
  
  .link-button {
    width: 100%;
    justify-content: flex-start;
  }
}
</style>