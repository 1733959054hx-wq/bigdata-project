<template>
  <div class="page-container">
    <el-card class="content-card">
      <template #header>
        <div class="header">
          <h1>隐私政策</h1>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>
      
      <div class="text-content">
        <p class="effective-date">生效日期：2024年1月1日</p>
        <p>我们非常重视您的隐私保护。本隐私政策将说明我们如何收集、使用和保护您的个人信息。</p>

        <h3>1. 信息收集</h3>
        <p>为了提供服务，我们可能会收集您的用户名、邮箱地址以及您上传的文件信息。在演示模式下，大部分数据仅存储在您的本地浏览器(LocalStorage)中。</p>

        <h3>2. 信息使用</h3>
        <p>您的信息仅用于身份验证、文件管理和系统功能的正常运行。我们不会向任何第三方出售您的个人信息。</p>

        <h3>3. 数据安全</h3>
        <p>我们采取合理的技术手段保护您的数据安全。但在互联网环境下，请您理解无法保证100%的安全。</p>

        <h3>4. Cookie的使用</h3>
        <p>我们使用Cookie或类似技术来保持您的登录状态和个性化设置（如主题模式）。</p>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = useRouter()
const userStore = useUserStore()

const handleBack = () => {
  // 同样的智能返回逻辑
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    if (userStore.isLoggedIn) {
      router.push('/settings')
    } else {
      router.push('/login')
    }
  }
}
</script>

<style scoped>
.page-container {
  padding: 20px;
  max-width: 800px;
  margin: 0 auto;
  min-height: 100vh;
  background-color: var(--main-bg);
  transition: background-color 0.3s ease;
}
.content-card {
  border-radius: 12px;
  background-color: var(--card-bg);
  border: 1px solid var(--border-light);
}
.header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}
.text-content {
  line-height: 1.8;
  color: var(--text-primary);
}
h1 {
  color: var(--text-primary);
  font-size: 20px;
  margin: 0;
}
h3 { 
  margin-top: 20px; 
  margin-bottom: 10px; 
  color: var(--primary); 
}
p {
  color: var(--text-secondary);
}
.effective-date {
  font-weight: bold;
  margin-bottom: 20px;
  color: var(--text-primary);
}
</style>