<template>
  <div class="page-container">
    <el-card class="content-card">
      <template #header>
        <div class="header">
          <h1>帮助中心</h1>
          <el-button @click="handleBack">返回设置</el-button>
        </div>
      </template>
      
      <div class="faq-section">
        <el-collapse v-model="activeNames">
          <el-collapse-item title="如何上传文件？" name="1">
            <p>在"我的文件"页面，点击顶部操作栏的"上传文件"按钮，选择您电脑中的文件即可开始上传。目前支持单文件最大 50MB。</p>
          </el-collapse-item>
          <el-collapse-item title="如何创建新文件夹？" name="2">
            <p>点击操作栏的"新建文件夹"按钮，输入文件夹名称后确认即可。您也可以进入文件夹后继续创建子文件夹。</p>
          </el-collapse-item>
          <el-collapse-item title="什么是协同编辑？" name="3">
            <p>协同编辑允许您和团队成员同时编辑同一个文档。点击"协同编辑"按钮进入房间，分享房间号给您的伙伴即可开始。</p>
          </el-collapse-item>
          <el-collapse-item title="如何找回密码？" name="4">
            <p>目前演示版本请联系系统管理员重置密码。在正式版本中，您可以通过绑定的邮箱找回。</p>
          </el-collapse-item>
          <el-collapse-item title="支持哪些文件格式预览？" name="5">
            <p>我们目前支持图片(jpg, png)、PDF文档、文本文档(txt, md)以及视频(mp4)的在线预览。Office文档(doc, xls)建议下载后查看。</p>
          </el-collapse-item>
        </el-collapse>
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'

const router = useRouter()
const activeNames = ref(['1'])

const handleBack = () => {
  // 优先尝试返回上一页
  if (window.history.state && window.history.state.back) {
    router.back()
  } else {
    // 如果没有历史记录（比如直接刷新了页面），强制跳回设置页
    router.push('/settings')
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
h1 { 
  font-size: 20px; 
  margin: 0; 
  color: var(--text-primary);
}
p {
  color: var(--text-secondary);
  line-height: 1.6;
}
.faq-section { margin-top: 20px; }

:deep(.el-collapse-item__header) {
  background-color: var(--card-bg);
  color: var(--text-primary);
  border-bottom-color: var(--border-light);
}
:deep(.el-collapse-item__content) {
  background-color: var(--card-bg);
  color: var(--text-secondary);
}
:deep(.el-collapse) {
  border-top-color: var(--border-light);
  border-bottom-color: var(--border-light);
}
</style>