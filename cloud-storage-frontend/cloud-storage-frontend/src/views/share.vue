<template>
  <div class="share-page">
    <div class="operation-bar">
      <div class="page-title">
        <el-icon class="title-icon"><Share /></el-icon>
        <span class="title-text">文件分享</span>
      </div>
      <div class="operation-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索分享文件..."
          prefix-icon="Search"
          style="width: 240px; margin-right: 12px;"
          clearable
          class="search-input"
          @keyup.enter="handleSearch"
          @clear="handleSearch"
        >
          <template #append>
            <el-button :icon="Search" @click="handleSearch" />
          </template>
        </el-input>

        <el-button 
          type="primary" 
          @click="showCreateDialog = true" 
          class="create-btn"
        >
          <el-icon><Plus /></el-icon> 创建分享
        </el-button>
        <el-button @click="handleRefresh" :loading="loading" class="refresh-btn">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <el-card class="shares-card" shadow="never">
      <el-table 
        :data="filteredShares" 
        v-loading="loading"
        empty-text="暂无分享链接"
        style="width: 100%"
        class="shares-table"
        :header-cell-style="{ background: 'var(--el-fill-color-light)', color: 'var(--el-text-color-primary)' }"
      >
        <el-table-column prop="file.name" label="文件" min-width="250">
          <template #default="scope">
            <div class="file-info">
              <el-icon class="file-icon">
                <Document v-if="isDocument(scope.row.file.name)" />
                <Picture v-else-if="isImage(scope.row.file.name)" />
                <VideoCamera v-else-if="isVideo(scope.row.file.name)" />
                <Folder v-else />
              </el-icon>
              <span class="file-name">{{ scope.row.file.name }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="shareUrl" label="分享链接" min-width="200">
          <template #default="scope">
            <div class="share-link">
              <span class="url-text">{{ scope.row.shareUrl }}</span>
              <el-button size="small" @click="copyLink(scope.row)" class="copy-btn" link>
                <el-icon><CopyDocument /></el-icon> 复制
              </el-button>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="expireTime" label="有效期" width="150">
          <template #default="scope">
            <span class="expire-time">{{ scope.row.expireTime ? formatTime(scope.row.expireTime) : '永久' }}</span>
          </template>
        </el-table-column>

        <el-table-column prop="views" label="访问量" width="100" align="center">
          <template #default="scope">
            <span class="views-count">{{ scope.row.views }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="createTime" label="创建时间" width="150">
          <template #default="scope">
            <span class="create-time">{{ formatTime(scope.row.createTime) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="120" fixed="right">
          <template #default="scope">
            <el-button size="small" type="danger" @click="deleteShare(scope.row)" link>
              <el-icon><Delete /></el-icon> 删除
            </el-button>
          </template>
        </el-table-column>
      </el-table>
    </el-card>

    <div v-if="!loading && filteredShares.length === 0" class="empty-state">
      <el-empty description="暂无分享链接">
        <el-button type="primary" @click="showCreateDialog = true">创建第一个分享</el-button>
      </el-empty>
    </div>

<el-dialog v-model="showCreateDialog" title="创建分享" width="500px">
      <el-form :model="shareForm" ref="shareFormRef" label-width="80px" class="share-form">
        <el-form-item label="选择文件" prop="fileId">
          <el-select 
            v-model="shareForm.fileId" 
            placeholder="请选择要分享的文件" 
            style="width: 100%" 
            filterable
            :loading="loadingFiles" 
          >
            <el-option v-for="file in realFileList" :key="file.id" :label="file.fileName" :value="file.id">
              <div class="file-option">
                <el-icon class="file-option-icon">
                  <Document v-if="isDocument(file.fileName)" />
                  <Picture v-else-if="isImage(file.fileName)" />
                  <VideoCamera v-else-if="isVideo(file.fileName)" />
                  <Folder v-else />
                </el-icon>
                <span class="file-option-name">{{ file.fileName }}</span>
              </div>
            </el-option>
          </el-select>
        </el-form-item>
        </el-form>
      <template #footer>
        <span class="dialog-footer">
          <el-button @click="showCreateDialog = false">取消</el-button>
          <el-button 
            class="white-confirm-btn" 
            @click="createShare" 
            :loading="creating"
          >
            创建分享
          </el-button>
        </span>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Share, Plus, Refresh, CopyDocument, Delete, Search,
  Document, Picture, VideoCamera, Folder
} from '@element-plus/icons-vue'
import { useFileStore } from '@/stores/fileStore'
import { useThemeStore } from '@/stores/theme'
// ✨✨✨ 新增：导入获取文件列表的 API ✨✨✨
import { getFileList } from '@/api/file'

const router = useRouter()
const route = useRoute()
const fileStore = useFileStore()
const themeStore = useThemeStore()

const loading = ref(false)
const showCreateDialog = ref(false)
const creating = ref(false)
// ✨✨✨ 新增：真实文件列表状态 ✨✨✨
const realFileList = ref([]) 
const loadingFiles = ref(false)

// 1. 初始化时从 URL 读取搜索词
const searchKeyword = ref(route.query.q || '')

// 2. ✨ 监听 URL 变化，自动同步搜索词
// 这确保了即使组件复用，或者点击刷新按钮清空了 URL，输入框也会跟着变
watch(() => route.query.q, (newVal) => {
  searchKeyword.value = newVal || ''
})

const shareForm = reactive({ fileId: '', expireType: '7days', password: '' })
const shareFormRef = ref()
const baseUrl = window.location.origin

// 模拟数据
const shares = ref([
  { id: 1, file: { id: 1, name: '项目报告.pdf', size: 1024000 }, shareUrl: `${baseUrl}/s/K9x2L1mP`, expireTime: new Date(Date.now() + 7 * 86400000).getTime(), createTime: Date.now(), views: 15 },
  { id: 2, file: { id: 2, name: '产品截图.png', size: 1590000 }, shareUrl: `${baseUrl}/s/p7N8r2Xq`, expireTime: null, createTime: new Date(Date.now() - 2 * 86400000).getTime(), views: 8 }
])

//const availableFiles = computed(() => fileStore.fileList)
const { isImage, isVideo, isDocument } = fileStore

// ✨✨✨ 新增：监听弹窗打开，加载真实文件列表 ✨✨✨
watch(showCreateDialog, (val) => {
  if (val) {
    fetchFilesForShare()
  }
})


// ✨✨✨ 新增：获取文件列表函数 ✨✨✨
const fetchFilesForShare = async () => {
  loadingFiles.value = true
  try {
    // 调用后端 API 获取文件
    const res = await getFileList() 
    if (res.code === 200 && res.data) {
      // 过滤掉文件夹，只允许分享文件
      realFileList.value = res.data.filter(item => !item.isFolder && !item.isDir)
    }
  } catch (e) {
    ElMessage.error('获取文件列表失败')
  } finally {
    loadingFiles.value = false
  }
}

// 3. 搜索跳转：更新 URL
const handleSearch = () => {
  router.push({ query: { ...route.query, q: searchKeyword.value } })
}

// 4. 刷新逻辑：清空 URL 参数，重置页面状态
const handleRefresh = () => {
  loading.value = true
  router.push({ query: {} }) // 清空查询参数
  setTimeout(() => { 
    loading.value = false; 
    ElMessage.success('已刷新') 
  }, 500)
}

// 5. 过滤逻辑：依赖 searchKeyword (它会随 URL 变化而变化)
const filteredShares = computed(() => {
  if (!searchKeyword.value) return shares.value
  return shares.value.filter(s => s.file.name.toLowerCase().includes(searchKeyword.value.toLowerCase()))
})

const formatTime = (t) => t ? new Date(t).toLocaleDateString('zh-CN') : '永久'
const generateShareUrl = () => {
  const chars = 'ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz0123456789'
  let result = ''
  for (let i = 0; i < 10; i++) result += chars.charAt(Math.floor(Math.random() * chars.length))
  return `${window.location.origin}/s/${result}`
}
const getExpireTime = (t) => {
  const now = Date.now()
  if(t === '1day') return now + 86400000
  if(t === '7days') return now + 7*86400000
  if(t === '30days') return now + 30*86400000
  return null
}

const loadShares = async () => {
  loading.value = true
  try { await new Promise(r => setTimeout(r, 500)); } catch (e) { ElMessage.error('加载失败') } finally { loading.value = false }
}


const createShare = async () => {
  if (!shareForm.fileId) return ElMessage.warning('请选择要分享的文件')
  creating.value = true
  try {
    await new Promise(r => setTimeout(r, 800))
    // ✨ 修改：从 realFileList 中查找选中文件
    const selectedFile = realFileList.value.find(f => f.id === shareForm.fileId)
    
    if (!selectedFile) return ElMessage.error('文件不存在')
    
    shares.value.unshift({
      id: Date.now(), 
      // 注意：后端返回的字段通常是 fileName，前端显示可能需要 name
      file: { name: selectedFile.fileName || selectedFile.name, ...selectedFile }, 
      shareUrl: generateShareUrl(),
      expireTime: getExpireTime(shareForm.expireType), 
      createTime: Date.now(), 
      views: 0
    })
    ElMessage.success('分享创建成功')
    showCreateDialog.value = false
    shareForm.fileId = ''; shareForm.expireType = '7days'; shareForm.password = ''
  } catch (e) { 
    console.error(e)
    ElMessage.error('创建失败') 
  } finally { 
    creating.value = false 
  }
}
const copyLink = async (share) => {
  try { await navigator.clipboard.writeText(share.shareUrl); ElMessage.success('链接已复制'); share.views += 1 } catch (e) { ElMessage.error('复制失败') }
}

const deleteShare = async (share) => {
  try {
    await ElMessageBox.confirm(`确定要删除分享 "${share.file.name}" 吗？`, '确认删除', { confirmButtonText: '确定', cancelButtonText: '取消', type: 'warning' })
    const index = shares.value.findIndex(s => s.id === share.id)
    if (index !== -1) { shares.value.splice(index, 1); ElMessage.success('已删除') }
  } catch (e) {}
}

onMounted(() => { loadShares(); themeStore.init() })
</script>

<style scoped>
.share-page { padding: 24px; background: var(--main-bg); min-height: calc(100vh - 64px); transition: background-color 0.3s ease; }
.operation-bar { display: flex; justify-content: space-between; align-items: center; margin-bottom: 16px; padding: 16px 24px; background: var(--card-bg); border-radius: 12px; box-shadow: var(--shadow-sm); border: 1px solid var(--border-light); transition: all 0.3s ease; }
.page-title { display: flex; align-items: center; gap: 12px; }
.title-icon { font-size: 24px; color: var(--primary); transition: color 0.3s ease; }
.title-text { font-size: 18px; font-weight: 600; color: var(--text-primary); transition: color 0.3s ease; }
.operation-right { display: flex; gap: 12px; align-items: center; }
.create-btn, .refresh-btn { border-radius: 8px; padding: 10px 20px; }
.shares-card { border-radius: 12px; border: 1px solid var(--border-light); box-shadow: var(--shadow-sm); background: var(--card-bg); transition: all 0.3s ease; }
.file-info { display: flex; align-items: center; gap: 8px; }
.file-icon { color: var(--primary); transition: color 0.3s ease; }
.file-name { font-size: 14px; color: var(--text-primary); transition: color 0.3s ease; }
.share-link { display: flex; align-items: center; gap: 8px; }
.url-text { font-size: 13px; color: var(--primary); font-family: 'Monaco', 'Menlo', monospace; flex: 1; word-break: break-all; transition: color 0.3s ease; }
.expire-time, .create-time { color: var(--text-tertiary); font-size: 13px; transition: color 0.3s ease; }
.views-count { color: var(--text-primary); font-weight: 500; transition: color 0.3s ease; }
.empty-state { margin-top: 60px; text-align: center; }
.file-option { display: flex; align-items: center; gap: 8px; }
.file-option-icon { color: var(--text-tertiary); transition: color 0.3s ease; }

/* 搜索框样式 */
.search-input :deep(.el-input-group__append) { background-color: var(--el-color-primary); color: white; border-color: var(--el-color-primary); cursor: pointer; }

/* Deep 样式优化 */
:deep(.el-table) { border-radius: 8px; background: var(--card-bg); transition: background-color 0.3s ease; }
:deep(.el-table th) { background: var(--el-fill-color-light); color: var(--text-primary); font-weight: 600; transition: all 0.3s ease; }
:deep(.el-table .el-table__row) { background: var(--card-bg); transition: background-color 0.3s; }
:deep(.el-table .el-table__row:hover) { background: var(--el-fill-color-light) !important; }
:deep(.el-table .el-table__cell) { border-bottom: 1px solid var(--border-light); transition: all 0.3s ease; }
:deep(.el-card__body) { padding: 0; background: var(--card-bg); transition: background-color 0.3s ease; }
:deep(.el-dialog) { background: var(--card-bg); border: 1px solid var(--border-light); transition: all 0.3s ease; }
:deep(.el-dialog__title) { color: var(--text-primary); transition: color 0.3s ease; }
:deep(.el-dialog__body) { color: var(--text-primary); transition: color 0.3s ease; }
:deep(.el-form-item__label) { color: var(--text-primary); transition: color 0.3s ease; }
:deep(.el-input__inner) { background: var(--el-bg-color); border-color: var(--border-medium); color: var(--text-primary); transition: all 0.3s ease; }
:deep(.el-input__inner::placeholder) { color: var(--text-disabled); }
:deep(.el-radio) { color: var(--text-primary); transition: color 0.3s ease; }
:deep(.el-empty__description) { color: var(--text-secondary); transition: color 0.3s ease; }


/* ✨✨✨ 新增：白色确认按钮样式 ✨✨✨ */
.white-confirm-btn {
  background-color: #ffffff;
  border: 1px solid #dcdfe6;
  color: #606266;
  transition: all 0.3s;
}

.white-confirm-btn:hover {
  color: #409eff;
  border-color: #c6e2ff;
  background-color: #ecf5ff;
}

</style>
