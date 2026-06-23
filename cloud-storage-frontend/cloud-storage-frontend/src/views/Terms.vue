<template>
  <div class="page-container">
    <el-card class="content-card">
      <template #header>
        <div class="header">
          <h1>用户协议</h1>
          <el-button @click="handleBack">返回</el-button>
        </div>
      </template>
      
      <div class="text-content">
        <h3>1. 服务说明</h3>
        <p>欢迎使用云存储服务。本服务旨在为您提供安全、便捷的文件存储和管理功能。使用本服务即表示您同意本协议的所有条款。</p>
        
        <h3>2. 用户行为规范</h3>
        <p>用户在使用本服务时，不得上传、存储或分享包含以下内容的文件：</p>
        <ul>
          <li>违反当地法律法规的信息；</li>
          <li>侵犯他人知识产权的内容；</li>
          <li>包含病毒、木马等破坏性程序的文件；</li>
          <li>其他不当或违法内容。</li>
        </ul>

        <h3>3. 账号安全</h3>
        <p>您有责任妥善保管您的账号和密码。因您保管不善导致的损失，由您自行承担。</p>

        <h3>4. 服务变更</h3>
        <p>这是一个演示项目，我们保留随时修改或中断服务的权利，不需对用户或第三方负责。</p>
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
  // 🧠 智能判断逻辑：
  // 1. 优先使用浏览器历史记录返回（最自然的方式）
  //    如果你是从登录页点进来的，back() 就会回登录页
  //    如果你是从设置页点进来的，back() 就会回设置页
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    // 2. 兜底逻辑（防止在新标签页直接打开，没有上一页历史）
    //    如果用户已登录 -> 强制去设置页
    //    如果用户没登录 -> 强制去登录页
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
ul { 
  padding-left: 20px; 
  color: var(--text-secondary);
}
p {
  color: var(--text-secondary);
}
</style>