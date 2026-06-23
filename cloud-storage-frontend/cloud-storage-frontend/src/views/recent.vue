<template>
  <div class="recent-page">
    <div class="operation-bar">
      <div class="operation-left">
        <div class="page-title">
          <el-icon class="title-icon"><Clock /></el-icon>
          <span class="title-text">最近浏览</span>
          <el-tag type="info" size="small" effect="plain" v-if="!loading">{{ total }} 个文件</el-tag>
        </div>
      </div>
      <div class="operation-right">
        <el-input
          v-model="searchKeyword"
          placeholder="搜索文件名..."
          prefix-icon="Search"
          style="width: 240px;"
          clearable
          class="search-input"
        />
        <el-button @click="loadRecentFiles" :loading="loading" class="refresh-btn">
          <el-icon><Refresh /></el-icon> 刷新
        </el-button>
      </div>
    </div>

    <div class="time-filter">
      <el-radio-group v-model="timeRange">
        <el-radio-button label="all">全部</el-radio-button>
        <el-radio-button label="today">今天</el-radio-button>
        <el-radio-button label="week">本周</el-radio-button>
        <el-radio-button label="month">本月</el-radio-button>
      </el-radio-group>
    </div>

    <el-card class="file-list-card" shadow="never">
      <el-table 
        :data="paginatedFiles" 
        v-loading="loading"
        empty-text="暂无最近文件"
        style="width: 100%"
        class="file-table"
      >
        <el-table-column prop="name" label="文件名" min-width="300">
          <template #default="scope">
            <div class="file-name-cell" @click="handlePreview(scope.row)">
              <div class="file-icon-wrapper">
                <el-icon class="file-icon" :size="24">
                  <component :is="getFileIconComponent(scope.row.name)" :color="getFileIconColor(scope.row.name)" />
                </el-icon>
              </div>
              <div class="file-info">
                <div class="file-name">{{ scope.row.name }}</div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="size" label="大小" width="120" align="right">
          <template #default="scope">
            <span class="file-size">{{ formatFileSizeSafe(scope.row.size) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="uploadTime" label="最近浏览时间" width="220" sortable>
          <template #default="scope">
            <div class="time-info">
              <span class="update-time">{{ formatTime(scope.row.uploadTime) }}</span>
              <span class="time-ago" style="font-size: 12px; color: #909399; margin-left: 8px;" v-if="scope.row.uploadTime">
                {{ getTimeAgo(scope.row.uploadTime) }}
              </span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button size="small" type="success" @click.stop="handlePreview(scope.row)" link>
                <el-icon><View /></el-icon> 预览
              </el-button>
              <el-button size="small" type="primary" @click.stop="handleDownload(scope.row)" link>
                <el-icon><Download /></el-icon> 下载
              </el-button>
            </div>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" v-if="filteredFiles.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="total"
          layout="total, sizes, prev, pager, next, jumper"
          class="custom-pagination"
        />
      </div>
    </el-card>

    <el-dialog 
      v-model="previewVisible" 
      :title="`预览 - ${previewFile?.name}`" 
      width="80%" 
      top="5vh"
      destroy-on-close
    >
      <div class="preview-wrapper">
        <img v-if="previewType === 'image'" :src="previewUrl" style="max-width:100%; max-height:70vh; object-fit:contain;" />
        <video v-else-if="previewType === 'video'" :src="previewUrl" controls autoplay style="width:100%; max-height:70vh;"></video>
        <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" style="width:100%; height:70vh; border:none;"></iframe>
        <pre v-else-if="previewType === 'text'" style="max-height:70vh;overflow:auto;padding:20px;background:#f5f5f5;width:100%;">{{ textContent }}</pre>
        <div v-else class="fallback-preview">
          <el-icon :size="64" color="#909399"><Document /></el-icon>
          <p>暂不支持在线预览</p>
        </div>
      </div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage } from 'element-plus'
import { Document, Picture, VideoCamera, Download, Refresh, View, Search, Clock, Tickets } from '@element-plus/icons-vue'
import { useFileStore } from '@/stores/fileStore'
import * as fileApi from '@/api/file'
import { formatTime, parseDate } from '@/utils/format'

const fileStore = useFileStore()
const loading = ref(false)
const searchKeyword = ref('')
const timeRange = ref('all')
const currentPage = ref(1)
const pageSize = ref(20)
const fileList = ref([])

const previewVisible = ref(false)
const previewFile = ref(null)
const previewUrl = ref('')
const textContent = ref('')
const previewType = ref('unknown')

const loadRecentFiles = async () => {
  loading.value = true
  try {
    const res = await fileApi.getFileList('/BigData')
    if (res.code === 200 && res.data) {
      const rawFiles = res.data.map(item => {
        const dateObj = parseDate(item.updateTime || item.createTime)
        return {
          id: item.id,
          name: item.fileName,
          size: item.fileSize,
          uploadTime: dateObj ? dateObj.getTime() : 0,
          type: item.fileType,
          filePath: item.filePath,
          isFolder: item.isFolder
        }
      })
      
      // ✨✨✨ 前端去重 + 强制按时间倒序 ✨✨✨
      const uniqueMap = new Map()
      rawFiles.forEach(f => {
        if (!uniqueMap.has(f.id)) uniqueMap.set(f.id, f)
      })
      const uniqueFiles = Array.from(uniqueMap.values())

      fileList.value = uniqueFiles
        .filter(f => !f.isFolder)
        .sort((a, b) => b.uploadTime - a.uploadTime)
        
    } else {
      fileList.value = []
    }
  } catch (error) {
    fileList.value = []
  } finally {
    loading.value = false
  }
}

// 筛选
const filteredFiles = computed(() => {
  let files = fileList.value
  if (searchKeyword.value) {
    files = files.filter(f => f.name.toLowerCase().includes(searchKeyword.value.toLowerCase()))
  }
  const now = Date.now()
  const oneDay = 86400000
  if (timeRange.value === 'today') {
    const startOfDay = new Date(new Date().setHours(0,0,0,0)).getTime()
    files = files.filter(f => f.uploadTime >= startOfDay)
  } else if (timeRange.value === 'week') {
    files = files.filter(f => (now - f.uploadTime) < 7 * oneDay)
  } else if (timeRange.value === 'month') {
    files = files.filter(f => (now - f.uploadTime) < 30 * oneDay)
  }
  return files
})

const paginatedFiles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredFiles.value.slice(start, start + pageSize.value)
})
const total = computed(() => filteredFiles.value.length)

// 工具函数
const { isImage, isVideo, isDocument, formatFileSize: storeFormatSize } = fileStore
const formatFileSizeSafe = (s) => { try { return storeFormatSize(s) } catch { return '0 B' } }

const getTimeAgo = (timestamp) => {
  if (!timestamp) return ''
  const diff = Date.now() - timestamp
  if (diff < 60000) return '刚刚'
  if (diff < 3600000) return Math.floor(diff / 60000) + '分钟前'
  if (diff < 86400000) return Math.floor(diff / 3600000) + '小时前'
  if (diff < 604800000) return Math.floor(diff / 86400000) + '天前'
  return ''
}

const isNew = (t) => (Date.now() - t) < 3 * 86400000
const getFileIconComponent = (n) => isImage(n) ? Picture : (isDocument(n) ? Document : (isVideo(n) ? VideoCamera : Tickets))
const getFileIconColor = (n) => isImage(n) ? '#67c23a' : (isDocument(n) ? '#409eff' : (isVideo(n) ? '#f56c6c' : '#909399'))

const handleDownload = async (file) => {
  try {
    const res = await fileApi.downloadFile(file.id)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a'); link.href = url; link.setAttribute('download', file.name)
    document.body.appendChild(link); link.click(); document.body.removeChild(link)
  } catch { ElMessage.error('下载失败') }
}

const handlePreview = async (row) => {
  previewFile.value = row; previewType.value = 'unknown'; previewUrl.value = ''; textContent.value = ''
  if (isImage(row.name)) {
    previewType.value = 'image'
    previewUrl.value = 'https://images.unsplash.com/photo-1498050108023-c5249f4df085'
  } else if (isDocument(row.name)) {
    previewType.value = 'text'
    textContent.value = '这是最近浏览的文档快照...'
  } else {
    previewVisible.value = true; return
  }
  previewVisible.value = true
}

onMounted(() => { loadRecentFiles() })
</script>

<style scoped>
.recent-page { padding: 24px; background: var(--main-bg); min-height: 100vh; }
.operation-bar { background: var(--card-bg); padding: 16px 24px; margin-bottom: 16px; border-radius: 12px; display: flex; justify-content: space-between; align-items: center; border: 1px solid var(--border-light); }
.page-title { display: flex; align-items: center; gap: 12px; }
.title-icon { font-size: 24px; color: #409eff; } 
.title-text { font-size: 18px; font-weight: 600; color: var(--text-primary); }
.time-filter { margin-bottom: 16px; }
.file-list-card { border-radius: 12px; border: 1px solid var(--border-light); }
.file-name-cell { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.file-info { display: flex; flex-direction: column; }
.file-tags { display: flex; align-items: center; gap: 5px; margin-top: 2px; }
.time-info { display: flex; align-items: center; }
.pagination-container { display: flex; justify-content: center; padding: 20px; }
.preview-wrapper { height: 70vh; display: flex; justify-content: center; align-items: center; background: #f5f7fa; }
.fallback-preview { text-align: center; color: #909399; }
.search-input :deep(.el-input-group__append) { background-color: var(--el-color-primary); color: white; border-color: var(--el-color-primary); cursor: pointer; }
</style>