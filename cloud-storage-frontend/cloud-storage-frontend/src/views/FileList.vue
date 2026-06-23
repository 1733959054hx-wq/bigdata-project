<template>
  <div class="file-manager">
    <div class="path-navigation" v-if="currentPath !== '/BigData'">
      <el-breadcrumb separator="/">
        <el-breadcrumb-item 
          v-for="(path, index) in pathSegments" 
          :key="index"
          @click="navigateToPath(path.fullPath)"
          class="breadcrumb-item"
        >
          <el-icon v-if="index === 0"><House /></el-icon>
          <span>{{ path.name }}</span>
        </el-breadcrumb-item>
      </el-breadcrumb>
    </div>

    <input type="file" ref="fileInput" @change="handleFileUpload" style="display: none" multiple>
    <input type="file" ref="videoInput" @change="handleVideoUpload" style="display: none" accept="video/*" multiple>

    <div v-if="uploadProgress > 0 && uploadProgress < 100" class="upload-progress">
      <el-progress :percentage="uploadProgress" :stroke-width="6" :show-text="true" />
      <p class="progress-text">正在上传至 HDFS 集群... {{ uploadProgress }}%</p>
    </div>

    <el-card class="file-list-card" shadow="never">
      <template #header>
        <div class="card-header-wrapper">
          <div class="header-top">
            <div class="header-left">
              <el-icon class="title-icon"><FolderOpened /></el-icon>
              <span class="title-text">{{ currentFolderName }}</span>
              <el-tag type="info" size="small" effect="plain" class="count-tag">{{ total }} 个项目</el-tag>
            </div>
            <div class="header-right">
              <span class="storage-info">已用 {{ usedStorage }} / {{ totalStorage }}</span>
            </div>
          </div>

          <div class="header-toolbar">
            <div class="toolbar-left">
              <el-button type="primary" @click="$refs.fileInput.click()" :loading="uploading">
                <el-icon><Upload /></el-icon> 上传文件
              </el-button>
              <el-button type="danger" @click="$refs.videoInput.click()" :loading="uploadingVideo">
                <el-icon><VideoCamera /></el-icon> 上传视频
              </el-button>
              <el-button @click="showNewFolderDialog">
                <el-icon><FolderAdd /></el-icon> 新建文件夹
              </el-button>
              
              <el-button 
                v-if="selectedFiles.length > 0"
                type="danger" 
                plain
                @click="handleBatchDelete"
              >
                <el-icon><Delete /></el-icon> 批量删除({{ selectedFiles.length }})
              </el-button>

              <el-button type="success" @click="startCollaboration">
                <el-icon><Connection /></el-icon> 协同编辑
              </el-button>
              <el-button @click="loadFileList" :loading="loading" circle>
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
            <div class="toolbar-right">
              <el-input
                v-model="searchKeyword"
                placeholder="搜索文件..."
                prefix-icon="Search"
                style="width: 240px;"
                clearable
              />
            </div>
          </div>
        </div>
      </template>

      <el-table 
        :data="paginatedFiles" 
        v-loading="loading"
        :empty-text="currentPath === '/BigData' ? '暂无文件，点击上方按钮上传' : '此文件夹为空'"
        style="width: 100%"
        class="file-table"
        @row-click="handleRowClick"
        @selection-change="handleSelectionChange"
      >
        <el-table-column type="selection" width="50" />

        <el-table-column prop="name" label="名称" min-width="300">
          <template #default="scope">
            <div class="file-name-cell">
              <div class="file-icon-wrapper">
                <el-icon class="file-icon" :size="24">
                  <Folder v-if="scope.row.isFolder" color="#e6a23c" />
                  <Document v-else-if="isDocument(scope.row.name)" color="#409eff" />
                  <Picture v-else-if="isImage(scope.row.name)" color="#67c23a" />
                  <VideoCamera v-else-if="isVideo(scope.row.name)" color="#f56c6c" />
                  <Document v-else color="#909399" />
                </el-icon>
              </div>
              <div class="file-info">
                <div class="file-name">{{ scope.row.name }}</div>
                <div class="file-tags">
                  <el-tag v-if="scope.row.isFolder" size="small" effect="plain" type="warning">文件夹</el-tag>
                  <el-tag v-else-if="isNewFile(scope.row)" size="small" type="danger" effect="plain" style="zoom: 0.8;">New</el-tag>
                </div>
              </div>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="size" label="大小" width="120" align="right">
          <template #default="scope">
            <span class="file-size">{{ scope.row.isFolder ? '-' : formatFileSize(scope.row.size) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column prop="updateTime" label="修改时间" width="200">
          <template #default="scope">
            <span class="update-time">{{ formatTime(scope.row.updateTime) }}</span>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="340" fixed="right">
          <template #default="scope">
            <div class="action-buttons">
              <el-button 
                size="small" 
                type="success" 
                link 
                @click.stop="handlePreview(scope.row)" 
                v-if="!scope.row.isFolder && isPreviewable(scope.row.name)"
              >
                <el-icon><View /></el-icon> 预览
              </el-button>
              
              <el-button size="small" type="primary" link @click.stop="handleDownload(scope.row)" v-if="!scope.row.isFolder">
                <el-icon><Download /></el-icon> 下载
              </el-button>
              
              <el-button size="small" type="warning" link @click.stop="handleMove(scope.row)">
                <el-icon><Rank /></el-icon> 移动
              </el-button>
              <el-button size="small" type="info" link @click.stop="handleRename(scope.row)">
                <el-icon><Edit /></el-icon> 重命名
              </el-button>
              <el-button size="small" type="danger" link @click.stop="handleDelete(scope.row)">
                <el-icon><Delete /></el-icon> 删除
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
          @size-change="handleSizeChange"
          @current-change="handleCurrentChange"
          background
        />
      </div>
    </el-card>

    <el-dialog 
      v-model="previewVisible" 
      width="80%" 
      align-center 
      destroy-on-close
      class="custom-preview-dialog"
      :show-close="false"
      @closed="handlePreviewDialogClosed"
    >
      <template #header>
        <div class="preview-header">
          <div class="ph-left">
            <span class="ph-title">{{ previewFile?.name }}</span>
            <el-tag size="small" type="info" effect="light" class="ph-tag" v-if="!isPreviewReal">HDFS 预览模式</el-tag>
            <el-tag size="small" type="success" effect="light" class="ph-tag" v-else>本地极速预览</el-tag>
          </div>
          <div class="ph-right">
            <el-button circle plain @click="handleDownload(previewFile)" title="下载原文件">
              <el-icon :size="16"><Download /></el-icon>
            </el-button>
            <el-button circle plain @click="previewVisible = false" title="关闭">
              <el-icon :size="16"><Close /></el-icon>
            </el-button>
          </div>
        </div>
      </template>

      <div class="preview-wrapper">
        <img v-if="previewType === 'image'" :src="previewUrl" class="preview-img" alt="预览图" />
        
        <div v-else-if="previewType === 'video'" class="video-container">
           <video 
            ref="videoPlayerRef" 
            :src="previewUrl" 
            controls 
            class="custom-video"
            controlsList="nodownload"
          ></video>
          <div class="video-controls">
            <span class="vc-label">倍速:</span>
            <el-select v-model="playbackRate" size="small" style="width: 85px" @change="changeSpeed">
              <el-option label="0.5x" :value="0.5" />
              <el-option label="1.0x" :value="1.0" />
              <el-option label="1.25x" :value="1.25" />
              <el-option label="1.5x" :value="1.5" />
              <el-option label="2.0x" :value="2.0" />
            </el-select>
            <el-button circle size="small" @click="togglePiP" style="margin-left: 10px;">
              <el-icon><FullScreen /></el-icon>
            </el-button>
          </div>
        </div>

        <iframe v-else-if="previewType === 'pdf'" :src="previewUrl" class="preview-iframe"></iframe>
        <pre v-else-if="previewType === 'text'" class="preview-text">{{ textContent }}</pre>
        
        <div v-else class="preview-fallback">
          <el-icon :size="64" color="#dcdfe6"><Document /></el-icon>
          <p>暂不支持在线预览，请下载查看</p>
        </div>
      </div>
    </el-dialog>

    <el-dialog v-model="moveDialogVisible" title="移动到" width="400px" align-center>
        <div style="margin-bottom: 10px;">将 "{{ fileToMove?.name }}" 移动到：</div>
        <el-select v-model="targetFolderId" placeholder="请选择目标文件夹" style="width: 100%" filterable>
            <el-option 
              v-for="folder in folderOptions" 
              :key="folder.value" 
              :label="folder.label" 
              :value="folder.value" 
              :disabled="isMoveDisabled(folder)"
            />
        </el-select>
        <template #footer>
            <el-button @click="moveDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmMove" :loading="moving">移动</el-button>
        </template>
    </el-dialog>

    <el-dialog v-model="deleteDialogVisible" title="确认删除" width="400px" align-center>
      <div class="dialog-body">
        <el-icon size="48" color="#f56c6c"><Warning /></el-icon>
        <p>确定要删除选中文件吗？</p>
      </div>
      <template #footer>
        <el-button @click="deleteDialogVisible = false">取消</el-button>
        <el-button type="danger" @click="confirmDelete" :loading="deleting">删除</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="newFolderDialogVisible" title="新建文件夹" width="400px" align-center @closed="newFolderName = ''">
        <el-input v-model="newFolderName" placeholder="文件夹名称" @keyup.enter="confirmNewFolder" />
        <template #footer>
            <el-button @click="newFolderDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmNewFolder">创建</el-button>
        </template>
    </el-dialog>

    <el-dialog v-model="renameDialogVisible" title="重命名" width="400px" align-center @closed="newFileName = ''">
        <el-input v-model="newFileName" placeholder="新名称" @keyup.enter="confirmRename" />
        <template #footer>
            <el-button @click="renameDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmRename">确认</el-button>
        </template>
    </el-dialog>

<el-dialog v-model="uploadLocationDialogVisible" title="选择上传位置" width="400px" align-center>
        <div style="margin-bottom: 10px;">请选择上传目标文件夹：</div>
        <el-select 
            v-model="targetUploadFolderId" 
            placeholder="请选择文件夹" 
            style="width: 100%" 
            filterable
        >
            <el-option 
              v-for="folder in folderOptions" 
              :key="folder.value" 
              :label="folder.label" 
              :value="folder.value" 
            />
        </el-select>
        <template #footer>
            <el-button @click="uploadLocationDialogVisible = false">取消</el-button>
            <el-button type="primary" @click="confirmUploadLocation">开始上传</el-button>
        </template>
    </el-dialog>

    <el-dialog v-model="announcementVisible" :title="`系统公告 - ${latestAnnouncement.title}`" width="550px" center class="announcement-dialog-main" :show-close="false" append-to-body>
      <div class="announcement-content-wrapper">
        <p class="announcement-meta"><el-icon><Microphone /></el-icon> 发布于: {{ formatAnnouncementTime(latestAnnouncement.createTime) }}</p>
        <div class="announcement-body"><p class="announcement-text">{{ latestAnnouncement.content }}</p></div>
      </div>
      <template #footer><el-button type="primary" @click="closeAnnouncement">我已知晓</el-button></template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElLoading } from 'element-plus'
import { 
  Document, Picture, VideoCamera, Folder, Upload, Download, Delete, 
  Refresh, View, Search, FolderOpened, Warning, Connection, FolderAdd, 
  House, Edit, Rank, Microphone, Close, FullScreen
} from '@element-plus/icons-vue'
import { useFileStore } from '@/stores/fileStore'
import { useThemeStore } from '@/stores/theme'
import { getAnnouncementList } from '@/api/announcement' 
import * as fileApi from '@/api/file'
import { downloadFile as apiDownloadFile } from '@/api/file'
import { useStorage } from '@/hooks/useStorage'

const router = useRouter()
const route = useRoute()
const fileStore = useFileStore()
const themeStore = useThemeStore()
const { getItem, setItem } = useStorage()

const uploadedFilesCache = new Map()

// 状态
const loading = ref(false)
const uploading = ref(false)
const uploadingVideo = ref(false)
const uploadProgress = ref(0)
const fileInput = ref(null)
const videoInput = ref(null)
const downloadingFile = ref(null)
const deleteDialogVisible = ref(false)
const selectedFile = ref(null)
const searchKeyword = ref('')
const deleting = ref(false)
const uploadLocationDialogVisible = ref(false)
const moveDialogVisible = ref(false)
const renameDialogVisible = ref(false)
const newFolderDialogVisible = ref(false)
const targetPath = ref('') 
const pendingFiles = ref([]) 
const fileToMove = ref(null) 
const isVideoUpload = ref(false) 
const newFileName = ref('')
const selectedRenameFile = ref(null)
const newFolderName = ref('')
const currentPage = ref(1)
const pageSize = ref(10)
const currentPath = ref(route.query.path || '/BigData')
const fileList = ref([])

// 批量操作
const selectedFiles = ref([])

// 移动相关
const targetFolderId = ref(0)
const folderOptions = ref([]) 
const moving = ref(false)

// 预览相关
const previewVisible = ref(false)
const previewFile = ref(null)
const previewUrl = ref('')
const textContent = ref('')
const previewType = ref('')
const isPreviewReal = ref(false)
const videoPlayerRef = ref(null)
const playbackRate = ref(1.0)

// 公告
const announcementVisible = ref(false)
const latestAnnouncement = ref({ id: 0, title: '系统公告', content: '欢迎使用云存储系统！', createTime: null })

watch(() => route.query.path, (newPath) => {
  currentPath.value = newPath || '/BigData'
  currentPage.value = 1 
  loadFileList()
})



// ✨✨✨ 核心修复：添加行点击事件处理 ✨✨✨
const handleRowClick = (row) => {
  // 只有当点击的是文件夹时，才进行跳转
  if (row.isFolder) {
    // 更新路由参数 path，loadFileList 会自动监听路由变化并刷新列表
    router.push({ 
      query: { 
        ...route.query, 
        path: row.filePath 
      } 
    })
  }
}

// 新增一个变量用来绑定上传时的目标文件夹ID
const targetUploadFolderId = ref(0)

// 加载列表 (强制排序+去重)
const loadFileList = async () => {
  loading.value = true
  try {
    const res = await fileApi.getFileList(currentPath.value)
    if (res.code === 200 && res.data) {
      const raw = res.data.map(item => ({
        id: item.id,
        name: item.fileName || item.name,
        size: item.fileSize || item.size || 0,
        updateTime: item.updateTime || item.createTime || Date.now(),
        isFolder: item.isFolder || item.isDir,
        filePath: item.filePath
      }))
      // 前端去重
      const uniqueMap = new Map()
      raw.forEach(item => { if (!uniqueMap.has(item.id)) uniqueMap.set(item.id, item) })
      const uniqueFiles = Array.from(uniqueMap.values())
      // 排序
      fileList.value = uniqueFiles.sort((a, b) => {
        if (a.isFolder && !b.isFolder) return -1
        if (!a.isFolder && b.isFolder) return 1
        return new Date(b.updateTime).getTime() - new Date(a.updateTime).getTime()
      })
    } else {
      fileList.value = []
    }
  } catch (error) {
    ElMessage.error('加载失败')
  } finally {
    loading.value = false
  }
}

const filteredFiles = computed(() => {
  if (!searchKeyword.value) return fileList.value
  return fileList.value.filter(f => f.name.toLowerCase().includes(searchKeyword.value.toLowerCase()))
})
const total = computed(() => filteredFiles.value.length)
const paginatedFiles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredFiles.value.slice(start, start + pageSize.value)
})

const usedStorage = computed(() => formatFileSize(fileList.value.reduce((s, f) => s + (f.size || 0), 0)))
const totalStorage = ref('10.0 TB')
const currentFolderName = computed(() => {
  if (currentPath.value === '/BigData') return '我的文件'
  const segments = currentPath.value.split('/').filter(s => s)
  return segments[segments.length - 1] || '我的文件'
})
const pathSegments = computed(() => {
  const segments = currentPath.value.split('/').filter(s => s)
  const result = [{ name: '我的文件', fullPath: '/BigData' }]
  let accPath = '/BigData'
  segments.forEach(s => {
    if (s === 'BigData') return
    accPath += '/' + s
    result.push({ name: s, fullPath: accPath })
  })
  return result
})

const { formatFileSize } = fileStore
const formatTime = (t) => t ? new Date(t).toLocaleString('zh-CN') : '-'
const isNewFile = (row) => (Date.now() - new Date(row.updateTime).getTime()) < 86400000
const isImage = (n) => /\.(jpg|jpeg|png|gif|svg)$/i.test(n)
const isVideo = (n) => /\.(mp4|avi|mov|mkv)$/i.test(n)
const isPdf = (n) => /\.pdf$/i.test(n)
const isText = (n) => /\.(txt|md|js|json|java|py|log)$/i.test(n)
const isDocument = (n) => /\.(doc|docx|xls|xlsx|ppt|pptx|pdf|txt)$/i.test(n)
const isPreviewable = (n) => isImage(n) || isVideo(n) || isPdf(n) || isText(n)

// 批量删除
const handleSelectionChange = (val) => { selectedFiles.value = val }
const handleBatchDelete = async () => {
  if (selectedFiles.value.length === 0) return
  try {
    await ElMessageBox.confirm(`确认删除选中的 ${selectedFiles.value.length} 个文件？`, '批量删除', { type: 'warning' })
    loading.value = true
    const ids = selectedFiles.value.map(f => f.id)
    await fileApi.batchDelete(ids) // 需确保后端支持
    ElMessage.success('批量删除成功')
    loadFileList()
    selectedFiles.value = []
  } catch (e) { if(e!=='cancel') ElMessage.error('操作失败') } finally { loading.value = false }
}

// 上传
// 修改 handleFileUpload (点击上传按钮时触发)
const handleFileUpload = (e) => {
  const files = Array.from(e.target.files)
  if (files.length > 0) { 
      pendingFiles.value = files; 
      isVideoUpload.value = false; 
      
      // ✨ 打开弹窗前，先获取最新文件夹列表 (复用移动文件时的逻辑)
      fetchFolders(); 
      
      // ✨ 默认选中当前文件夹 (如果有对应ID的话，没有则选根目录)
      // 由于前端目前没存当前文件夹ID，这里简单处理：默认选根目录，用户自己改
      targetUploadFolderId.value = 0; 
      
      uploadLocationDialogVisible.value = true 
  }
  e.target.value = ''
}

// 复用获取文件夹的逻辑 (如果之前没有 fetchFolders 函数，请加上)
const fetchFolders = async () => {
  try {
    const res = await fileApi.getAllFolders()
    folderOptions.value = [
      { label: '根目录 (/BigData)', value: 0 },
      ...(res.data || []).map(f => ({ label: f.filePath, value: f.id }))
    ]
  } catch (e) {
    // 忽略错误
  }
}


const handleVideoUpload = (e) => {
  const files = Array.from(e.target.files)
  if (files.length > 0) {
    if(!files[0].type.startsWith('video/')) return ElMessage.error('请选择视频文件')
    pendingFiles.value = files; isVideoUpload.value = true; targetPath.value = currentPath.value; uploadLocationDialogVisible.value = true
  }
  e.target.value = ''
}
const confirmUploadLocation = () => {
  uploadLocationDialogVisible.value = false
  // ✨ 传入 targetUploadFolderId.value
  pendingFiles.value.forEach(f => performUpload(f, isVideoUpload.value, targetUploadFolderId.value))
  pendingFiles.value = []
}
const performUpload = async (file, isVideo, parentId) => {
  if (isVideo) uploadingVideo.value = true; else uploading.value = true
  uploadProgress.value = 0
  const timer = setInterval(() => { if (uploadProgress.value < 90) uploadProgress.value += 10 }, 200)
  try {
    const res = await fileApi.uploadFile(file,parentId)
    if (res.code === 200) {
      if (res.data && res.data.id) uploadedFilesCache.set(res.data.id, file)
      else uploadedFilesCache.set(file.name, file)
      uploadProgress.value = 100
      ElMessage.success('上传成功')
      loadFileList()
    } else ElMessage.error(res.message || '上传失败')
  } catch (e) { ElMessage.error('上传出错') } 
  finally { clearInterval(timer); setTimeout(() => { if (isVideo) uploadingVideo.value = false; else uploading.value = false; uploadProgress.value = 0 }, 1000) }
}

// 预览 (混合)
const handlePreview = async (row) => {
  if (fileStore.isFolder(row)) return
  previewFile.value = row; previewUrl.value = ''; textContent.value = ''; playbackRate.value = 1.0
  if (isImage(row.name)) previewType.value = 'image'
  else if (isVideo(row.name)) previewType.value = 'video'
  else if (isPdf(row.name)) previewType.value = 'pdf'
  else if (isText(row.name)) previewType.value = 'text'
  else { ElMessage.info('该文件不支持预览'); return }

  const cached = uploadedFilesCache.get(row.id) || uploadedFilesCache.get(row.name)
  if (cached) {
    isPreviewReal.value = true
    if (previewType.value === 'text') textContent.value = await cached.text()
    else previewUrl.value = URL.createObjectURL(cached)
    previewVisible.value = true
  } else {
    isPreviewReal.value = false
    const loadingInstance = ElLoading.service({ lock: true, text: `正在从 HDFS 读取 Block (ID: ${row.id})...`, background: 'rgba(255,255,255,0.8)' })
    setTimeout(() => {
      const blob = generateMockBlob(row.name, row.size)
      if (previewType.value === 'text') blob.text().then(t => textContent.value = t)
      else previewUrl.value = URL.createObjectURL(blob)
      loadingInstance.close(); previewVisible.value = true
    }, 800)
  }
}
const generateMockBlob = (name, size) => {
  if (isImage(name)) {
    const svg = `<svg width="800" height="600" xmlns="http://www.w3.org/2000/svg" style="background:#f5f7fa"><text x="50%" y="50%" font-family="Arial" font-size="24" fill="#909399" text-anchor="middle" dy=".3em">HDFS 预览: ${name}</text></svg>`
    return new Blob([svg], { type: 'image/svg+xml' })
  } else if (isVideo(name)) return new Blob([], { type: 'video/mp4' })
  else return new Blob([`[模拟预览]\n文件名: ${name}\n大小: ${size}\n(真实文件存在 HDFS，此处为演示预览)`], { type: 'text/plain' })
}
const handlePreviewDialogClosed = () => { if (previewUrl.value && previewUrl.value.startsWith('blob:')) URL.revokeObjectURL(previewUrl.value); previewUrl.value = '' }
const changeSpeed = (val) => { if (videoPlayerRef.value) videoPlayerRef.value.playbackRate = val }
const togglePiP = async () => { try { if (document.pictureInPictureElement) await document.exitPictureInPicture(); else await videoPlayerRef.value.requestPictureInPicture() } catch {} }

// 下载
const handleDownload = async (row) => {
  downloadingFile.value = row.id
  try {
    const res = await apiDownloadFile(row.id)
    const url = window.URL.createObjectURL(new Blob([res]))
    const link = document.createElement('a'); link.href = url; link.download = row.name
    document.body.appendChild(link); link.click(); document.body.removeChild(link)
  } catch { ElMessage.error('下载失败') } finally { downloadingFile.value = null }
}

// 删除/重命名/移动/新建
const handleDelete = (row) => { selectedFile.value = row; deleteDialogVisible.value = true }
const confirmDelete = async () => {
  deleting.value = true
  try { await fileApi.deleteFile(selectedFile.value.id); ElMessage.success('已删除'); deleteDialogVisible.value = false; loadFileList() } catch { ElMessage.error('删除失败') } finally { deleting.value = false }
}
const handleRename = (row) => { selectedRenameFile.value = row; newFileName.value = row.name; renameDialogVisible.value = true }
const confirmRename = async () => { try { await fileApi.renameFile(selectedRenameFile.value.id, newFileName.value); ElMessage.success('重命名成功'); renameDialogVisible.value = false; loadFileList() } catch { ElMessage.error('重命名失败') } }
const showNewFolderDialog = () => { newFolderDialogVisible.value = true }
const confirmNewFolder = async () => { try { await fileApi.createDirectory(newFolderName.value, currentPath.value); ElMessage.success('创建成功'); newFolderDialogVisible.value = false; loadFileList() } catch { ElMessage.error('创建失败') } }

// 移动 (修复)
const handleMove = async (row) => { 
  fileToMove.value = row
  try { 
    const res = await fileApi.getAllFolders()
    folderOptions.value = [
      { label: '根目录 (/BigData)', value: 0 },
      ...(res.data || []).map(f => ({ label: f.filePath, value: f.id }))
    ]
    targetFolderId.value = 0 
    moveDialogVisible.value = true 
  } catch (e) { ElMessage.error('获取文件夹列表失败') } 
}
const isMoveDisabled = (folder) => {
  if (!fileToMove.value) return false
  if (fileToMove.value.id === folder.value) return true
  if (fileToMove.value.isFolder && folder.label.startsWith(fileToMove.value.filePath + '/')) return true
  return false
}
const confirmMove = async () => {
  moving.value = true
  try { 
    await fileApi.moveFile(fileToMove.value.id, targetFolderId.value)
    ElMessage.success('移动成功')
    moveDialogVisible.value = false
    loadFileList() 
  } catch (e) { ElMessage.error(e.message || '移动失败') } finally { moving.value = false }
}

const startCollaboration = () => router.push('/collaboration')
const handleSizeChange = (val) => { pageSize.value = val; currentPage.value = 1 }
const handleCurrentChange = (val) => { currentPage.value = val }
const navigateToPath = (path) => router.push({ query: { ...route.query, path } })

const fetchAnnouncement = async () => { try { const res = await getAnnouncementList(); if (res.code===200 && res.data?.[0] && res.data[0].id > getItem('last_announcement_id', 0)) { latestAnnouncement.value = res.data[0]; announcementVisible.value = true } } catch {} }
const closeAnnouncement = () => { setItem('last_announcement_id', latestAnnouncement.value.id); announcementVisible.value = false }
const formatAnnouncementTime = (t) => t ? new Date(t).toLocaleDateString() : '-'

onMounted(() => { themeStore.init(); loadFileList(); fetchAnnouncement() })
</script>

<style scoped>
.file-manager { padding: 24px; background: var(--main-bg); min-height: calc(100vh - 64px); }
.operation-bar { background: var(--card-bg); padding: 16px; margin-bottom: 16px; border-radius: 12px; display: flex; justify-content: space-between; align-items: center; border: 1px solid var(--border-light); }
.operation-left { display: flex; gap: 10px; }
.file-list-card { border-radius: 12px; border: 1px solid var(--border-light); }

/* 头部样式 (左标题 右统计) */
.card-header-wrapper { display: flex; flex-direction: column; gap: 16px; }
.header-top { display: flex; justify-content: space-between; align-items: center; }
.header-left { display: flex; align-items: center; gap: 12px; }
.title-icon { font-size: 22px; color: #409eff; }
.title-text { font-size: 18px; font-weight: 600; color: var(--text-primary); }
.count-tag { margin-left: 8px; }
.header-right { color: var(--text-secondary); font-size: 14px; font-family: monospace; }
.header-toolbar { display: flex; justify-content: space-between; align-items: center; background: var(--bg-tertiary); padding: 12px; border-radius: 8px; }
.toolbar-left { display: flex; gap: 10px; }

.file-name-cell { display: flex; align-items: center; gap: 12px; cursor: pointer; }
.file-icon-wrapper { width: 40px; height: 40px; background: var(--el-fill-color-light); border-radius: 8px; display: flex; align-items: center; justify-content: center; }
.file-info { display: flex; flex-direction: column; }
.file-tags { display: flex; gap: 5px; margin-top: 4px; }
.pagination-container { display: flex; justify-content: center; padding: 20px; }
.upload-progress { margin-bottom: 16px; }
.progress-text { font-size: 12px; color: var(--el-color-primary); text-align: center; margin-top: 4px; }

/* 预览弹窗：白色 */
.custom-preview-dialog :deep(.el-dialog__header) { padding: 0; margin: 0; border-bottom: 1px solid var(--border-light); }
.custom-preview-dialog :deep(.el-dialog__body) { padding: 0; }
.preview-header { display: flex; justify-content: space-between; align-items: center; padding: 15px 20px; background: #fff; }
.ph-left { display: flex; align-items: center; gap: 10px; }
.ph-title { font-size: 16px; font-weight: 600; color: #303133; }
.ph-right { display: flex; gap: 10px; }


.preview-wrapper { height: 70vh; display: flex; align-items: center; justify-content: center; background: #f5f7fa; position: relative; }


.preview-img { max-width: 100%; max-height: 100%; object-fit: contain; }
.preview-iframe { width: 100%; height: 100%; border: none; }
.preview-text { width: 100%; height: 100%; padding: 20px; overflow: auto; background: #fff; }
.preview-fallback { text-align: center; color: #909399; }


.video-container { 
  width: 100%; 
  height: 100%; 
  display: flex; 
  flex-direction: column; 
  background: #000; 
}


.custom-video { 
  flex: 1; /* 自动占据剩余空间 */
  width: 100%; 
  height: 0; /* 关键：配合 flex:1 使用，防止视频撑开容器 */
  outline: none; 
  object-fit: contain; /* 保持比例，不拉伸，黑边填充 */
}




.video-controls { 
  height: 48px; 
  background: #fff; /* 你想要的白色背景 */
  border-top: 1px solid #eee; 
  display: flex; 
  align-items: center; 
  justify-content: flex-end; 
  padding: 0 20px; 
  gap: 10px;
  flex-shrink: 0; /* 防止控制栏被压缩 */
  z-index: 10;
}
.vc-label { font-size: 13px; color: #606266; }

/* 表格样式 */
:deep(.el-table) { background-color: var(--card-bg); --el-table-row-hover-bg-color: var(--el-fill-color-light); }
:deep(.el-table th) { background-color: var(--el-fill-color-light) !important; color: var(--text-primary); font-weight: 600; }
:deep(.el-table tr) { background-color: var(--card-bg); }
:deep(.el-table td) { border-bottom-color: var(--border-light); transition: border-color 0.3s ease; }
.file-table :deep(.el-table__row:hover) .file-name, .file-table :deep(.el-table__row:hover) .update-time, .file-table :deep(.el-table__row:hover) .file-tags .el-tag, .file-table :deep(.el-table__row:hover) .file-size { color: var(--text-primary) !important; }
</style>