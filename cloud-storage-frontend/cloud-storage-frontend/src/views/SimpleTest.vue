<!-- src/views/SimpleTest.vue -->
<template>
  <div style="padding: 50px; text-align: center;">
    <h1 style="color: green;">🎉 测试页面 - Vue 正常工作！</h1>
    <p>当前时间: {{ new Date().toLocaleTimeString() }}</p>
    <p>当前路由: {{ $route.path }}</p>
    <p>登录状态: {{ userStore.isLoggedIn ? '已登录' : '未登录' }}</p>
    
    <el-button @click="testClick" type="primary" style="margin: 10px;">
      测试按钮
    </el-button>
    
    <el-button @click="goHome" style="margin: 10px;">
      返回首页
    </el-button>
  </div>
</template>

<script setup>
import { useRouter } from 'vue-router'
import { useSimpleUserStore } from '@/stores/simple-user'

const router = useRouter()
const userStore = useSimpleUserStore()

const testClick = () => {
  alert('Vue 事件系统正常工作！')
  console.log('按钮点击测试成功')
}

const goHome = () => {
  router.push('/files')
}
</script>