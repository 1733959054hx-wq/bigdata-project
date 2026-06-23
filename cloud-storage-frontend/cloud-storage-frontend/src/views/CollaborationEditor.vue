<template>
  <div class="collaboration-editor">
    <!-- 房间设置界面 -->
    <div v-if="!currentRoom" class="room-setup">
      <div class="setup-container">
        <div class="setup-header">
          <el-icon class="header-icon"><Connection /></el-icon>
          <h1>协同编辑空间</h1>
          <p>与团队成员实时协作编辑文档</p>
        </div>

        <div class="setup-options">
          <!-- 创建新房间 -->
          <div class="setup-card">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><Plus /></el-icon>
                  <span>创建新房间</span>
                </div>
              </template>
              <div class="card-content">
                <el-form :model="newRoomForm" :rules="roomRules" ref="newRoomFormRef">
                  <el-form-item prop="name">
                    <el-input
                      v-model="newRoomForm.name"
                      placeholder="输入房间名称"
                      prefix-icon="OfficeBuilding"
                    />
                  </el-form-item>
                  <el-form-item>
                    <el-input
                      v-model="newRoomForm.description"
                      placeholder="房间描述（可选）"
                      type="textarea"
                      :rows="2"
                    />
                  </el-form-item>
                  <el-button type="primary" @click="createNewRoom" class="create-btn">
                    <el-icon><Plus /></el-icon>
                    创建房间
                  </el-button>
                </el-form>
              </div>
            </el-card>
          </div>

          <!-- 加入现有房间 -->
          <div class="setup-card">
            <el-card shadow="hover">
              <template #header>
                <div class="card-header">
                  <el-icon><UserFilled /></el-icon>
                  <span>加入房间</span>
                </div>
              </template>
              <div class="card-content">
                <el-form :model="joinRoomForm" :rules="roomRules" ref="joinRoomFormRef">
                  <el-form-item prop="roomId">
                    <el-input
                      v-model="joinRoomForm.roomId"
                      placeholder="输入房间ID"
                      prefix-icon="Key"
                    />
                  </el-form-item>
                  <el-button type="success" @click="joinExistingRoom" class="join-btn">
                    <el-icon><UserFilled /></el-icon>
                    加入房间
                  </el-button>
                </el-form>
              </div>
            </el-card>
          </div>
        </div>

        <!-- 快速开始 -->
        <div class="quick-start">
          <el-divider>快速开始</el-divider>
          <p>选择一个预设房间快速开始协作</p>
          <div class="quick-buttons">
            <el-button
              v-for="room in quickRooms"
              :key="room.id"
              @click="quickStart(room.id)"
              class="quick-btn"
            >
              {{ room.name }}
            </el-button>
          </div>
        </div>

        <!-- 返回文件列表 -->
        <div class="back-section">
          <el-button @click="goBack" class="btn-back">
            <el-icon><ArrowLeft /></el-icon>
            返回文件列表
          </el-button>
        </div>
      </div>
    </div>

    <!-- 编辑界面 -->
    <div v-else class="editor-interface">
      <!-- 编辑器头部 -->
      <div class="editor-header">
        <div class="header-left">
          <h2>{{ currentRoom.name }}</h2>
          <p class="room-description">{{ currentRoom.description }}</p>
          <!-- 当前编辑文件显示 -->
          <div v-if="currentEditingFile" class="current-file-info">
            <el-tag type="primary" size="small">
              <el-icon><Document /></el-icon>
              正在编辑: {{ currentEditingFile.name }}
            </el-tag>
            <el-text type="info" size="small">
              最后保存: {{ formatTime(currentEditingFile.updateTime) }}
            </el-text>
          </div>
        </div>
        
        <div class="header-center">
          <div class="online-users-display">
            <div class="users-count">
              <span class="users-icon">👥</span>
              <span class="users-text">在线用户: {{ onlineUsersCount }}</span>
            </div>
          </div>
        </div>

        <div class="header-right">
          <el-button @click="createNewFile" class="btn-primary">
            <el-icon><Plus /></el-icon>
            新建文件
          </el-button>
          <el-button @click="saveCurrentFile" class="btn-save" :loading="saving">
            <el-icon><Document /></el-icon>
            保存文件 (Ctrl+S)
          </el-button>
          <el-button @click="toggleFileBrowser" class="btn-secondary">
            <el-icon><FolderOpened /></el-icon>
            {{ showFileBrowser ? '隐藏文件' : '浏览文件' }}
          </el-button>
          <el-button @click="copyRoomLink" class="btn-secondary">
            <el-icon><Link /></el-icon>
            复制房间链接
          </el-button>
          <el-button @click="leaveRoom" class="btn-danger">
            <el-icon><Close /></el-icon>
            离开房间
          </el-button>
        </div>
      </div>

      <!-- 主要内容区域 -->
      <div class="main-content">
        <!-- 文件浏览器侧边栏 -->
        <div v-if="showFileBrowser" class="file-browser-sidebar">
          <div class="sidebar-header">
            <h3>HBase 文件</h3>
            <div class="header-actions">
              <el-button 
                size="small" 
                @click="createNewFile" 
                type="primary"
                text
              >
                <el-icon><Plus /></el-icon>
                新建
              </el-button>
              <el-button 
                size="small" 
                @click="refreshFileList" 
                :loading="loadingFiles"
                text
              >
                <el-icon><Refresh /></el-icon>
              </el-button>
            </div>
          </div>
          
          <div class="file-search">
            <el-input
              v-model="fileSearchKeyword"
              placeholder="搜索HBase文件..."
              prefix-icon="Search"
              size="small"
              clearable
            />
          </div>

          <div class="file-list-container">
            <!-- 新建文件选项 -->
            <div 
              class="file-item new-file-item"
              @click="createNewFile"
            >
              <div class="file-icon">
                <el-icon><Plus /></el-icon>
              </div>
              <div class="file-info">
                <div class="file-name">新建文件...</div>
                <div class="file-meta">点击创建新文件</div>
              </div>
            </div>
            
            <!-- HBase文件列表 -->
            <div class="hbase-files-section">
              <div 
                v-for="file in filteredHBaseFiles" 
                :key="file.id"
                class="file-item hbase-file"
                :class="{ 'active': currentEditingFile?.id === file.id }"
                @click="handleHBaseFileSelect(file)"
              >
                <div class="file-icon">
                  <el-icon><Document /></el-icon>
                </div>
                <div class="file-info">
                  <div class="file-name">{{ file.name }}</div>
                  <div class="file-meta">
                    <span class="file-size" v-if="file.size">
                      {{ formatFileSize(file.size) }}
                    </span>
                    <span class="file-time" v-if="file.updateTime">
                      {{ formatTime(file.updateTime) }}
                    </span>
                    <span class="file-source">HBase存储</span>
                    <el-tag size="mini" type="success">远程</el-tag>
                  </div>
                </div>
                <div class="file-actions">
                  <el-button
                    size="small"
                    type="danger"
                    text
                    @click.stop="deleteHBaseFile(file)"
                    class="delete-btn"
                  >
                    <el-icon><Delete /></el-icon>
                  </el-button>
                </div>
              </div>
            </div>
            
            <div v-if="filteredHBaseFiles.length === 0" class="empty-files">
              <el-empty description="暂无HBase文件" :image-size="80" />
            </div>
          </div>
        </div>

        <!-- 编辑器区域 -->
        <div class="editor-section" :class="{ 'with-sidebar': showFileBrowser }">
          <div class="editor-container">
            <!-- 未选择文件时的提示 -->
            <div v-if="!currentEditingFile" class="no-file-selected">
              <el-empty description="请创建一个新文件或从左侧选择文件开始编辑">
                <div class="action-buttons">
                  <el-button type="primary" @click="createNewFile">
                    <el-icon><Plus /></el-icon>
                    新建文件
                  </el-button>
                  <el-button @click="showFileBrowser = true" v-if="!showFileBrowser">
                    <el-icon><FolderOpened /></el-icon>
                    浏览文件
                  </el-button>
                </div>
              </el-empty>
            </div>
            
            <!-- 编辑器 -->
            <div v-else id="quill-editor" class="quill-editor"></div>
          </div>
        </div>

        <!-- 在线用户侧边栏 -->
        <div class="sidebar">
          <!-- 在线用户列表 -->
          <div class="sidebar-section">
            <h3>在线用户</h3>
            <div class="users-list">
              <div 
                v-for="user in onlineUsers" 
                :key="user.id"
                class="user-item"
                :class="{ 'current-user': user.isMe }"
              >
                <el-avatar :size="32" :src="user.avatar" class="user-avatar">
                  {{ user.name.charAt(0).toUpperCase() }}
                </el-avatar>
                <div class="user-info">
                  <span class="user-name">{{ user.name }}</span>
                  <span class="user-status">{{ user.isMe ? '我' : '在线' }}</span>
                </div>
                <el-tag v-if="user.isMe" size="small" type="success">我</el-tag>
              </div>
            </div>
          </div>

          <!-- 实时消息 -->
          <div class="sidebar-section">
            <h3>实时消息</h3>
            <div class="messages-container">
              <div 
                v-for="(msg, index) in messages" 
                :key="index"
                class="message"
                :class="msg.type"
              >
                <span class="timestamp">{{ formatTime(msg.timestamp) }}</span>
                <span class="content">{{ msg.content }}</span>
              </div>
            </div>
            <div class="message-input">
              <el-input 
                v-model="newMessage" 
                placeholder="输入聊天消息..." 
                @keyup.enter="sendMessage"
                size="small"
              >
                <template #append>
                  <el-button @click="sendMessage" size="small">
                    <el-icon><Promotion /></el-icon>
                  </el-button>
                </template>
              </el-input>
            </div>
          </div>
        </div>
      </div>

      <!-- 状态栏 -->
      <div class="status-bar">
        <span class="connection-status" :class="connectionStatusClass">
          连接状态: {{ connectionStatus }}
        </span>
        <span>房间ID: {{ currentRoom.id }}</span>
        <span v-if="currentEditingFile">文件: {{ currentEditingFile.name }}</span>
        <span v-if="lastSavedTime">最后保存: {{ formatTime(lastSavedTime) }}</span>
        <span v-if="hasUnsavedChanges" class="unsaved-changes">
          <el-icon><Warning /></el-icon>
          未保存的更改
        </span>
      </div>
    </div>

   <!-- 新建文件对话框 -->
<el-dialog
  v-model="showNewFileDialog"
  title="新建文件"
  width="400px"
  :before-close="handleNewFileDialogClose"
>
  <el-form :model="newFileForm" :rules="newFileRules" ref="newFileFormRef">
    <el-form-item label="文件名" prop="name">
      <el-input
        v-model="newFileForm.name"
        placeholder="请输入文件名"
        @keyup.enter="confirmCreateNewFile"
      />
    </el-form-item>
  </el-form>
  <template #footer>
    <span class="dialog-footer">
      <el-button @click="cancelCreateNewFile">取消</el-button>
      <el-button type="primary" @click="confirmCreateNewFile">
        创建
      </el-button>
    </span>
  </template>
</el-dialog>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, onMounted, onUnmounted, computed, nextTick } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Connection, Plus, OfficeBuilding, UserFilled, Key, 
  Link, Close, Promotion, Document, FolderOpened,
  Refresh, Search, Check, Warning, Folder, Picture, VideoCamera,
  ArrowLeft, Delete
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import { useFileStore } from '@/stores/fileStore'

// 导入 Yjs 相关模块
import * as Y from 'yjs'
import { QuillBinding } from 'y-quill'
import { WebsocketProvider } from 'y-websocket'
import Quill from 'quill'
import QuillCursors from 'quill-cursors'
import 'quill/dist/quill.snow.css'

// 注册光标模块
Quill.register('modules/cursors', QuillCursors)

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const fileStore = useFileStore()

// 定义接口（保持您的现有接口）
interface RoomInfo {
  id: string
  name: string
  description: string
}

interface OnlineUser {
  id: string | number
  name: string
  avatar: string
  isMe: boolean
  color?: string
}

interface Message {
  type: string
  content: string
  timestamp: number
}

interface RoomForm {
  name: string
  description: string
}

interface JoinForm {
  roomId: string
}

interface UserInfo {
  id?: string | number
  username?: string
  nickname?: string
  email?: string
  avatar?: string
  token?: string
  [key: string]: any
}

interface FileItem {
  id: string | number
  name: string
  size: number
  updateTime: number
  type: string
  deleted: boolean
  deleteTime: number | null
  originalPath: string
  realFile?: File
  content?: string
  isFolder?: boolean
  remoteId?: string
}

interface NewFileForm {
  name: string
}

interface HBaseFile {
  id: string
  name: string
  size?: number
  updateTime?: number
  content?: string
  type?: string
}

// Refs（保持您的现有 refs）
const connectionStatus = ref('未连接')
const onlineUsersCount = ref(1)
const newMessage = ref('')
const lastSavedTime = ref<number | null>(null)
const saving = ref(false)
const showFileBrowser = ref(true)
const loadingFiles = ref(false)
const fileSearchKeyword = ref('')
const hasUnsavedChanges = ref(false)
const showNewFileDialog = ref(false)
const newFileFormRef = ref()
const newRoomFormRef = ref()
const joinRoomFormRef = ref()

// HBase文件列表
const hbaseFiles = ref<HBaseFile[]>([])
const loadingHBaseFiles = ref(false)

// 新建文件表单
const newFileForm = reactive<NewFileForm>({
  name: '',
})

// Yjs 相关变量
let ydoc: Y.Doc | null = null
let provider: WebsocketProvider | null = null
let binding: QuillBinding | null = null
let quill: Quill | null = null

// 计算属性
const connectionStatusClass = computed(() => {
  if (connectionStatus.value.includes('已连接')) return 'connected'
  if (connectionStatus.value.includes('连接中')) return 'connecting'
  if (connectionStatus.value.includes('断开') || connectionStatus.value.includes('错误')) return 'disconnected'
  return 'disconnected'
})

// HBase文件过滤
const filteredHBaseFiles = computed(() => {
  if (fileSearchKeyword.value) {
    return hbaseFiles.value.filter(file => 
      file.name.toLowerCase().includes(fileSearchKeyword.value.toLowerCase())
    )
  }
  return hbaseFiles.value
})

// 房间状态
const currentRoom = ref<RoomInfo | null>(null)
const onlineUsers = ref<OnlineUser[]>([])
const messages = ref<Message[]>([])
const currentEditingFile = ref<FileItem | null>(null)

// 表单数据
const newRoomForm = reactive<RoomForm>({
  name: '',
  description: ''
})

const joinRoomForm = reactive<JoinForm>({
  roomId: ''
})

// 文件名映射工具函数
const FILE_NAME_MAP_KEY = 'hbase_file_name_map'

// 获取文件名映射
const getFileNameMap = (): Map<string, string> => {
  try {
    const stored = localStorage.getItem(FILE_NAME_MAP_KEY)
    if (stored) {
      return new Map(JSON.parse(stored))
    }
  } catch (error) {
    console.error('读取文件名映射失败:', error)
  }
  return new Map()
}

// 保存文件名映射
const saveFileNameMap = (map: Map<string, string>) => {
  try {
    localStorage.setItem(FILE_NAME_MAP_KEY, JSON.stringify(Array.from(map.entries())))
  } catch (error) {
    console.error('保存文件名映射失败:', error)
  }
}

// 快速开始房间
const quickRooms = ref([
  { id: 'meeting-room', name: '会议记录' },
  { id: 'brainstorming', name: '头脑风暴' },
  { id: 'document-edit', name: '文档编辑' },
  { id: 'code-review', name: '代码审查' }
])

// 当前用户信息
const currentUser = computed(() => {
  const userInfo = userStore.userInfo as UserInfo | null
  
  if (!userInfo) {
    const randomId = Math.random().toString(36).substring(2, 8)
    return {
      id: randomId,
      name: `用户_${randomId}`,
      avatar: ''
    }
  }
  
  return {
    id: userInfo?.id || 'anonymous',
    name: userInfo?.username || userInfo?.nickname || '匿名用户',
    avatar: userInfo?.avatar || ''
  }
})

// 规则验证
const roomRules = {
  name: [
    { required: true, message: '请输入房间名称', trigger: 'blur' },
    { min: 2, max: 20, message: '房间名称长度为2-20个字符', trigger: 'blur' }
  ],
  roomId: [
    { required: true, message: '请输入房间ID', trigger: 'blur' }
  ]
}

const newFileRules = {
  name: [
    { required: true, message: '请输入文件名', trigger: 'blur' },
    { min: 1, max: 100, message: '文件名长度为1-100个字符', trigger: 'blur' }
  ]
}

// ========== Yjs 协同编辑核心实现 ==========

// 初始化 Quill 编辑器
const initQuillEditor = () => {
  nextTick(() => {
    const editorElement = document.getElementById('quill-editor')
    if (!editorElement) {
      console.error('编辑器容器未找到')
      return
    }
    
    editorElement.innerHTML = ''
    
    quill = new Quill(editorElement, {
      theme: 'snow',
      modules: {
        toolbar: [
          [{ 'header': [1, 2, 3, 4, 5, 6, false] }],
          [{ 'font': [] }],
          [{ 'size': ['small', false, 'large', 'huge'] }],
          ['bold', 'italic', 'underline', 'strike'],
          [{ 'color': [] }, { 'background': [] }],
          [{ 'script': 'sub'}, { 'script': 'super' }],
          [{ 'list': 'ordered'}, { 'list': 'bullet' }],
          [{ 'indent': '-1'}, { 'indent': '+1' }],
          [{ 'align': [] }],
          ['blockquote', 'code-block'],
          ['link', 'image', 'video'],
          ['clean']
        ],
        cursors: {
          transformOnTextChange: true,
          selectionChangeSource: 'api',
          hideDelayMs: 3000,
          hideSpeedMs: 300
        },
        history: {
          delay: 1000,
          maxStack: 500,
          userOnly: true
        }
      },
      placeholder: '开始编辑您的文档...'
    })

    // 监听文本变化用于保存提示
    quill.on('text-change', (delta, oldDelta, source) => {
      if (source === 'user') {
        hasUnsavedChanges.value = true
      }
    })

    // 初始化 Yjs 协同编辑
    initYjsCollaboration()
  })
}

// 初始化 Yjs 协同编辑
const initYjsCollaboration = () => {
  if (!quill || !currentRoom.value) {
    console.error('初始化 Yjs 失败: Quill 或房间信息缺失')
    return
  }

  try {
    // 清理之前的连接
    if (binding) {
      binding.destroy()
      binding = null
    }
    if (provider) {
      provider.destroy()
      provider = null
    }
    if (ydoc) {
      ydoc.destroy()
      ydoc = null
    }

    // 创建 Yjs 文档
    ydoc = new Y.Doc()
    
    // 创建 WebSocket 提供者 - 使用 Yjs 的协议
    const roomName = `room-${currentRoom.value.id}`
    provider = new WebsocketProvider(
      'ws://localhost:1234', 
      roomName, 
      ydoc
    )

    // 获取共享文本
    const ytext = ydoc.getText('quill')
    
    // 绑定 Quill 和 Yjs - 这是关键！
    binding = new QuillBinding(ytext, quill, provider.awareness)

    // 设置用户信息（用于光标显示）
    const userColor = getRandomColor()
    provider.awareness.setLocalStateField('user', {
      name: currentUser.value.name,
      color: userColor,
      avatar: currentUser.value.avatar
    })

    // 监听连接状态
    provider.on('status', (event: any) => {
      console.log('Yjs 连接状态:', event.status)
      switch (event.status) {
        case 'connected':
          connectionStatus.value = '已连接'
          break
        case 'connecting':
          connectionStatus.value = '连接中...'
          break
        case 'disconnected':
          connectionStatus.value = '连接断开'
          break
      }
    })

    // 监听同步状态
    provider.on('sync', (isSynced: boolean) => {
      console.log('Yjs 同步状态:', isSynced)
      if (isSynced) {
        addMessage('system', '文档同步完成')
      }
    })

    // 监听用户状态变化
    provider.awareness.on('change', () => {
      updateOnlineUsers()
    })

    // 监听文档变化（用于保存提示）
    ydoc.on('update', () => {
      hasUnsavedChanges.value = true
    })

    console.log('Yjs 协同编辑已初始化')
    
  } catch (error) {
    console.error('初始化 Yjs 协同编辑失败:', error)
    ElMessage.error('协同编辑初始化失败')
  }
}

// 更新在线用户列表
const updateOnlineUsers = () => {
  if (!provider) return

  const states = provider.awareness.getStates()
  const users: OnlineUser[] = []

  states.forEach((state: any, clientId: number) => {
    if (state.user) {
      const isMe = clientId === provider?.awareness.clientID
      users.push({
        id: clientId,
        name: state.user.name,
        avatar: state.user.avatar || '',
        color: state.user.color,
        isMe: isMe
      })
    }
  })

  onlineUsers.value = users
  onlineUsersCount.value = users.length
}

// 生成随机颜色用于光标
const getRandomColor = () => {
  const colors = [
    '#FF6B6B', '#4ECDC4', '#45B7D1', '#96CEB4', 
    '#FFEAA7', '#DDA0DD', '#98D8C8', '#F7DC6F',
    '#BB8FCE', '#85C1E9', '#F8C471', '#82E0AA'
  ]
  return colors[Math.floor(Math.random() * colors.length)]
}

// ========== 文件操作（修改为使用 Yjs） ==========

// 文件操作
const refreshFileList = async () => {
  loadingFiles.value = true
  try {
    await fetchHBaseFiles()
    ElMessage.success('HBase文件列表已刷新')
  } catch (error) {
    console.error('刷新文件列表失败:', error)
    ElMessage.error('刷新失败')
  } finally {
    loadingFiles.value = false
  }
}

const toggleFileBrowser = () => {
  showFileBrowser.value = !showFileBrowser.value
}

// 获取HBase文件列表 
const fetchHBaseFiles = async () => {
  loadingHBaseFiles.value = true
  try {
    console.log('🌐 获取HBase文件列表...')
    const response = await fetch('http://localhost:8080/api/file/hbase/files')
    
    if (response.ok) {
      const fileIds = await response.json()
      console.log('📄 获取到的HBase文件ID列表:', fileIds)
      
      const fileNameMap = getFileNameMap()
      const filesWithDetails: HBaseFile[] = []
      
      for (const fileId of fileIds) {
        try {
          // 从localStorage映射表中获取文件名
          const fileName = fileNameMap.get(fileId) || fileId
          
          // 获取文件内容
          const contentResponse = await fetch(`http://localhost:8080/api/file/content/${fileId}`)
          let content = ''
          let fileSize = 0
          
          if (contentResponse.ok) {
            content = await contentResponse.text()
            fileSize = content.length
          }
          
          filesWithDetails.push({
            id: fileId,
            name: fileName,
            size: fileSize,
            updateTime: Date.now(),
            content: content,
            type: 'txt'
          })
        } catch (error) {
          console.error(`获取文件 ${fileId} 内容失败:`, error)
          const fileNameMap = getFileNameMap()
          const fileName = fileNameMap.get(fileId) || fileId
          filesWithDetails.push({
            id: fileId,
            name: fileName,
            type: 'txt'
          })
        }
      }
      
      hbaseFiles.value = filesWithDetails
      console.log('✅ 获取HBase文件列表成功:', filesWithDetails)
    } else {
      throw new Error('获取HBase文件列表失败')
    }
  } catch (error) {
    console.error('❌ 获取HBase文件列表失败:', error)
    ElMessage.error('获取HBase文件列表失败')
  } finally {
    loadingHBaseFiles.value = false
  }
}

// 新建文件
const createNewFile = async () => {
  if (hasUnsavedChanges.value && currentEditingFile.value) {
    try {
      await ElMessageBox.confirm(
        `文件 "${currentEditingFile.value.name}" 有未保存的更改。是否保存？`,
        '未保存的更改',
        {
          confirmButtonText: '保存',
          cancelButtonText: '不保存',
          type: 'warning'
        }
      )
      await saveCurrentFile()
    } catch (error) {
      // 用户选择不保存，继续创建新文件
    }
  }
  
  newFileForm.name = ''
  showNewFileDialog.value = true
  
  nextTick(() => {
    const input = document.querySelector('.el-dialog .el-input__inner') as HTMLInputElement
    if (input) {
      input.focus()
    }
  })
}

// 确认创建新文件 - 增强版本
const confirmCreateNewFile = async () => {
  if (!newFileFormRef.value) return
  
  try {
    await newFileFormRef.value.validate()
    
    const fileName = newFileForm.name
    console.log('🔄 开始创建文件:', fileName)
    
    // 生成文件ID
    const fileId = generateFileId()
    console.log('📝 生成文件ID:', fileId)
    
    // 创建文件内容
    const fileContent = `新建文件内容 - 创建于 ${new Date().toLocaleString()}`
    
    // 调用创建文件接口
    const response = await fetch(`http://localhost:8080/api/file/content/${fileId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'text/plain'
      },
      body: fileContent
    })
    
    console.log('📡 创建接口响应状态:', response.status)
    
    if (response.ok) {
      // 保存文件名映射到localStorage
      const fileNameMap = getFileNameMap()
      fileNameMap.set(fileId, fileName)
      saveFileNameMap(fileNameMap)
      
      // 创建HBase文件对象
      const newHBaseFile: HBaseFile = {
        id: fileId,
        name: fileName,
        size: fileContent.length,
        updateTime: Date.now(),
        content: fileContent,
        type: 'txt'
      }
      
      // 添加到HBase文件列表
      hbaseFiles.value.unshift(newHBaseFile)
      
      // 设置为当前编辑文件
      currentEditingFile.value = {
        id: fileId,
        name: fileName,
        size: fileContent.length,
        updateTime: Date.now(),
        type: 'txt',
        deleted: false,
        deleteTime: null,
        originalPath: '/HBase文件',
        content: fileContent,
        remoteId: fileId
      } as FileItem
      
      // 确保编辑器已初始化
      if (!quill) {
        initQuillEditor()
      }
      
      // 等待编辑器就绪
      await nextTick()
      
      // 使用 Yjs 设置文档内容
      if (ydoc && quill) {
        console.log('🔄 使用 Yjs 设置新文件内容...')
        const ytext = ydoc.getText('quill')
        ytext.delete(0, ytext.length) // 清空现有内容
        ytext.insert(0, fileContent) // 插入新内容
        
        // 确保 Quill 显示正确
        setTimeout(() => {
          if (quill && quill.getText() !== fileContent) {
            quill.setText(fileContent)
          }
        }, 100)
      }
      
      hasUnsavedChanges.value = false
      showNewFileDialog.value = false
      addMessage('system', `创建新文件: ${fileName}`)
      ElMessage.success('文件创建成功')
      
    } else {
      const errorText = await response.text()
      throw new Error(`创建文件失败: ${response.status} ${errorText}`)
    }
  } catch (error) {
    console.error('❌ 创建文件失败:', error)
    ElMessage.error('创建文件失败')
  }
}

const cancelCreateNewFile = () => {
  showNewFileDialog.value = false
}

const handleNewFileDialogClose = (done: () => void) => {
  ElMessageBox.confirm('确定要取消创建文件吗？', '提示', {
    confirmButtonText: '确定',
    cancelButtonText: '取消',
    type: 'warning'
  }).then(() => {
    done()
  }).catch(() => {
    // 取消关闭
  })
}

const generateFileId = () => {
  const timestamp = Date.now()
  const uniqueId = timestamp % 100000
  return `doc_${String(uniqueId).padStart(3, '0')}`
}

// 处理HBase文件选择 - 切换文件时不保存直接切换
const handleHBaseFileSelect = async (file: HBaseFile) => {
  // 直接丢弃未保存的更改，不弹出确认对话框
  if (hasUnsavedChanges.value && currentEditingFile.value) {
    console.log(`丢弃文件 "${currentEditingFile.value.name}" 的未保存更改`)
    addMessage('system', `已丢弃文件 "${currentEditingFile.value.name}" 的未保存更改`)
  }
  
  try {
    // 获取文件内容
    const response = await fetch(`http://localhost:8080/api/file/content/${file.id}`)
    if (response.ok) {
      const content = await response.text()
      
      // 更新文件内容
      file.content = content
      
      const tempFile: FileItem = {
        id: file.id,
        name: file.name,
        size: content.length,
        updateTime: file.updateTime || Date.now(),
        type: file.type || 'txt',
        deleted: false,
        deleteTime: null,
        originalPath: '/HBase文件',
        content: content,
        remoteId: file.id
      }
      
      currentEditingFile.value = tempFile
      
      nextTick(() => {
        if (quill) {
          quill.root.innerHTML = content
          quill.enable(true)
          hasUnsavedChanges.value = false
        } else {
          initQuillEditor()
          const checkEditor = setInterval(() => {
            if (quill) {
              quill.root.innerHTML = content
              quill.enable(true)
              hasUnsavedChanges.value = false
              clearInterval(checkEditor)
            }
          }, 100)
        }
      })
      
      addMessage('system', `开始编辑文件: ${file.name}`)
    } else {
      throw new Error('获取文件内容失败')
    }
  } catch (error) {
    console.error('加载文件失败:', error)
    ElMessage.error('加载文件失败，请重试')
  }
}

// 保存文件
const saveCurrentFile = async () => {
  if (!currentEditingFile.value || !quill) {
    ElMessage.warning('没有正在编辑的文件')
    return
  }
  
  const content = quill.root.innerHTML
  
  saving.value = true
  try {
    const fileId = currentEditingFile.value.id
    
    const response = await fetch(`http://localhost:8080/api/file/content/${fileId}`, {
      method: 'POST',
      headers: {
        'Content-Type': 'text/plain'
      },
      body: content
    })
    
    console.log('📡 保存接口响应状态:', response.status)
    
    if (response.ok) {
      // 更新HBase文件列表中的文件内容
      const fileIndex = hbaseFiles.value.findIndex(f => f.id === fileId)
      if (fileIndex !== -1) {
        hbaseFiles.value[fileIndex].content = content
        hbaseFiles.value[fileIndex].size = content.length
        hbaseFiles.value[fileIndex].updateTime = Date.now()
      }
      
      lastSavedTime.value = Date.now()
      hasUnsavedChanges.value = false
      ElMessage.success(`文件 "${currentEditingFile.value.name}" 已保存`)
      addMessage('system', `文件已保存: ${currentEditingFile.value.name}`)
      
      if ( ws) {
        const message = {
          type: 'file_saved',
          fileName: currentEditingFile.value.name,
          user: currentUser.value.name,
          timestamp: Date.now()
        }
        ws.send(JSON.stringify(message))
      }
    } else {
      throw new Error('保存到远程失败')
    }
  } catch (error: any) {
    console.error('保存文件失败:', error)
    ElMessage.error(error.message || '保存文件失败，请重试')
  } finally {
    saving.value = false
  }
}

// 删除HBase文件
const deleteHBaseFile = async (file: HBaseFile) => {
  try {
    await ElMessageBox.confirm(
      `确定要删除文件 "${file.name}" 吗？此操作不可恢复。`,
      '删除文件',
      {
        confirmButtonText: '确定删除',
        cancelButtonText: '取消',
        type: 'warning',
        confirmButtonClass: 'el-button--danger'
      }
    )
    
    console.log('🗑️ 开始删除文件:', file.name)
    
    const response = await fetch(`http://localhost:8080/api/file/content/${file.id}`, {
      method: 'DELETE'
    })
    
    console.log('📡 删除接口响应状态:', response.status)
    
    if (response.ok) {
      // 从localStorage映射表中删除
      const fileNameMap = getFileNameMap()
      fileNameMap.delete(file.id)
      saveFileNameMap(fileNameMap)
      
      // 从HBase文件列表中移除
      hbaseFiles.value = hbaseFiles.value.filter(f => f.id !== file.id)
      
      // 如果删除的是当前编辑的文件，清空编辑器
      if (currentEditingFile.value?.id === file.id) {
        currentEditingFile.value = null
        if (quill && ydoc) {
          const ytext = ydoc.getText('quill')
          ytext.delete(0, ytext.length) // 清空 Yjs 文档
        }
      }
      
      ElMessage.success(`文件 "${file.name}" 已删除`)
      addMessage('system', `文件已删除: ${file.name}`)
      
    } else {
      const errorText = await response.text()
      throw new Error(`删除文件失败: ${response.status} ${errorText}`)
    }
  } catch (error: any) {
    if (error === 'cancel') {
      return
    }
    console.error('❌ 删除文件失败:', error)
    ElMessage.error(error.message || '删除文件失败，请重试')
  }
}

// ========== 房间操作 ==========

// 房间操作
const generateRoomId = () => {
  return Math.random().toString(36).substring(2, 10)
}

const createNewRoom = async () => {
  if (!newRoomFormRef.value) return
  
  try {
    await newRoomFormRef.value.validate()
    
    const roomId = generateRoomId()
    currentRoom.value = {
      id: roomId,
      name: newRoomForm.name || '未命名房间',
      description: newRoomForm.description || '协同编辑房间'
    }

    connectToRoom(roomId)
    addMessage('system', `创建房间: ${currentRoom.value.name}`)
    refreshFileList()
    ElMessage.success(`房间 "${currentRoom.value.name}" 创建成功`)
    
  } catch (error) {
    console.error('创建房间失败:', error)
  }
}

const joinExistingRoom = async () => {
  if (!joinRoomFormRef.value) return
  
  try {
    await joinRoomFormRef.value.validate()
    
    currentRoom.value = {
      id: joinRoomForm.roomId,
      name: `房间 ${joinRoomForm.roomId}`,
      description: '通过房间ID加入的房间'
    }

    connectToRoom(joinRoomForm.roomId)
    addMessage('system', `加入房间: ${joinRoomForm.roomId}`)
    refreshFileList()
    ElMessage.success(`成功加入房间: ${joinRoomForm.roomId}`)
    
  } catch (error) {
    console.error('加入房间失败:', error)
  }
}

const quickStart = (roomId: string) => {
  const room = quickRooms.value.find(r => r.id === roomId)
  currentRoom.value = {
    id: roomId,
    name: room?.name || roomId,
    description: '快速开始房间'
  }

  connectToRoom(roomId)
  addMessage('system', `快速加入房间: ${roomId}`)
  refreshFileList()
  ElMessage.success(`快速加入房间: ${room?.name || roomId}`)
}

const connectToRoom = (roomId: string) => {
  console.log(`🔗 连接到房间: ${roomId}`)
  
  // 先初始化编辑器
  initQuillEditor()
  
  // 设置聊天 WebSocket
  setupChatWebSocket(roomId)
  
  connectionStatus.value = '连接中...'
  addMessage('system', '正在连接到协同编辑房间...')
  
  // 延迟检查连接状态
  setTimeout(() => {
    if (connectionStatus.value === '连接中...') {
      console.log('⚠️ 连接可能超时，检查网络和服务器状态')
    }
  }, 5000)
}

// 离开房间
const leaveRoom = async () => {
  try {
    if (hasUnsavedChanges.value && currentEditingFile.value) {
      try {
        await ElMessageBox.confirm(
          `文件 "${currentEditingFile.value.name}" 有未保存的更改。是否保存？`,
          '未保存的更改',
          {
            confirmButtonText: '保存并离开',
            cancelButtonText: '直接离开',
            type: 'warning'
          }
        )
        await saveCurrentFile()
      } catch (error) {
        // 用户选择不保存
      }
    }
    
    await ElMessageBox.confirm('确定要离开房间吗？', '提示', {
      confirmButtonText: '确定',
      cancelButtonText: '取消',
      type: 'warning'
    })
    
    // 清理 Yjs 资源
    if (binding) {
      binding.destroy()
      binding = null
    }
    if (provider) {
      provider.destroy()
      provider = null
    }
    if (ydoc) {
      ydoc.destroy()
      ydoc = null
    }
    if (quill) {
      quill = null
    }
    
    currentRoom.value = null
    currentEditingFile.value = null
    onlineUsers.value = []
    messages.value = []
    connectionStatus.value = '未连接'
    
    ElMessage.success('已离开房间')
    
  } catch (error) {
    // 用户取消
  }
}

const goBack = () => {
  router.push('/files')
}

// ========== 聊天功能（保持原有 WebSocket） ==========

// WebSocket 连接（仅用于聊天）
let ws: WebSocket | null = null

const setupChatWebSocket = (roomId: string) => {
  if (ws) {
    ws.close()
  }

  try {
    ws = new WebSocket(`ws://localhost:1234/ws/${roomId}`)
    
    ws.onopen = () => {
      addMessage('system', '聊天连接已建立')
      
      const joinMessage = {
        type: 'user_joined',
        username: currentUser.value.name,
        userInfo: currentUser.value,
        timestamp: Date.now()
      }
      ws?.send(JSON.stringify(joinMessage))
    }

    ws.onmessage = async (event) => {
      try {
        let data = event.data
        if (data instanceof Blob) {
          data = await data.text()
        }
        
        if (typeof data === 'string') {
          try {
            data = JSON.parse(data)
          } catch (e) {
            // 保持为字符串
          }
        }
        
        // 只处理聊天相关消息
        handleChatMessage(data)
      } catch (error) {
        console.error('处理聊天消息失败:', error)
      }
    }

    ws.onclose = () => {
      addMessage('system', '聊天连接已断开')
    }

    ws.onerror = (error) => {
      console.error('聊天 WebSocket 错误:', error)
    }

  } catch (error) {
    console.error('建立聊天连接失败:', error)
  }
}

// 处理聊天消息
const handleChatMessage = (data: any) => {
  if (typeof data === 'object') {
    switch (data.type) {
      case 'chat_message':
        addMessage('chat', `${data.user}: ${data.message}`)
        break
      case 'user_joined':
        if (data.userInfo && data.userInfo.id !== currentUser.value.id) {
          addMessage('system', `用户 ${data.username} 加入了房间`)
        }
        break
      case 'user_left':
        addMessage('system', `用户离开，剩余用户: ${data.clientCount || 1}`)
        break
    }
  }
}

// 发送聊天消息
const sendMessage = () => {
  if (!newMessage.value.trim() || !ws) return
  
  const message = {
    type: 'chat_message',
    message: newMessage.value,
    user: currentUser.value.name,
    timestamp: Date.now()
  }
  
  ws.send(JSON.stringify(message))
  newMessage.value = ''
}

// ========== 工具函数 ==========

// 工具函数
const formatFileSize = (bytes: number) => {
  if (!bytes || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}

const formatTime = (timestamp: number) => {
  if (!timestamp) return '-'
  return new Date(timestamp).toLocaleTimeString()
}

const addMessage = (type: string, content: string) => {
  messages.value.push({
    type,
    content,
    timestamp: Date.now()
  })
  
  if (messages.value.length > 100) {
    messages.value = messages.value.slice(-50)
  }
}

const copyRoomLink = () => {
  if (!currentRoom.value) return
  
  const link = `${window.location.origin}/collaboration?roomId=${currentRoom.value.id}`
  navigator.clipboard.writeText(link).then(() => {
    ElMessage.success('房间链接已复制到剪贴板！')
  }).catch(() => {
    ElMessage.error('复制失败')
  })
}

// 添加快捷键监听
const setupKeyboardShortcuts = () => {
  const handleKeyDown = (event: KeyboardEvent) => {
    if ((event.ctrlKey || event.metaKey) && event.key === 's') {
      event.preventDefault()
      saveCurrentFile()
    }
    
    if ((event.ctrlKey || event.metaKey) && event.key === 'n') {
      event.preventDefault()
      createNewFile()
    }
  }
  
  document.addEventListener('keydown', handleKeyDown)
  
  return () => {
    document.removeEventListener('keydown', handleKeyDown)
  }
}

const setupAutoSave = () => {
  const autoSaveInterval = setInterval(() => {
    if (hasUnsavedChanges.value && currentEditingFile.value) {
      saveCurrentFile()
    }
  }, 30000)
  
  return () => clearInterval(autoSaveInterval)
}

// 生命周期
onMounted(() => {
  const urlRoomId = route.query.roomId as string
  if (urlRoomId) {
    joinRoomForm.roomId = urlRoomId
    joinExistingRoom()
  }
  
  const cleanupKeyboardShortcuts = setupKeyboardShortcuts()
  const cleanupAutoSave = setupAutoSave()
  
  onUnmounted(() => {
    cleanupKeyboardShortcuts()
    cleanupAutoSave()
    
    // 清理 Yjs 资源
    if (binding) binding.destroy()
    if (provider) provider.destroy()
    if (ydoc) ydoc.destroy()
    
    // 清理 WebSocket
    if (ws) ws.close()
  })
})
</script>

<style scoped>
.collaboration-editor {
  height: 100vh;
  display: flex;
  flex-direction: column;
  background: #f8f9fa;
}

.header-actions {
  display: flex;
  gap: 0.5rem;
}

/* 新建文件项样式 */
.new-file-item {
  background: #f0f9ff;
  border: 1px dashed #409eff;
  margin-bottom: 0.5rem;
}

.new-file-item:hover {
  background: #e6f7ff;
  border-color: #1677ff;
}

.new-file-item .file-icon {
  background: #409eff;
  color: white;
}

/* 操作按钮组 */
.action-buttons {
  display: flex;
  gap: 1rem;
  justify-content: center;
  margin-top: 1rem;
}

/* 危险按钮样式 */
.btn-danger {
  background: #f56c6c;
  color: white;
}

.btn-danger:hover {
  background: #e65c5c;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(245, 108, 108, 0.3);
}

/* 对话框样式 */
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 0.75rem;
}

/* 房间设置界面样式 */
.room-setup {
  flex: 1;
  display: flex;
  align-items: center;
  justify-content: center;
  padding: 2rem;
  background: linear-gradient(135deg, #f2f2f2 0%, #e0dde2 100%);
}

.setup-container {
  max-width: 900px;
  width: 100%;
  background: white;
  border-radius: 12px;
  padding: 2.5rem;
  box-shadow: 0 10px 30px rgba(0, 0, 0, 0.2);
}

.setup-header {
  text-align: center;
  margin-bottom: 2.5rem;
}

.header-icon {
  font-size: 3rem;
  color: #409eff;
  margin-bottom: 1rem;
}

.setup-header h1 {
  margin: 0 0 0.5rem 0;
  color: #303133;
  font-size: 2rem;
  font-weight: 700;
}

.setup-header p {
  margin: 0;
  color: #606266;
  font-size: 1.1rem;
}

.setup-options {
  display: grid;
  grid-template-columns: 1fr 1fr;
  gap: 2rem;
  margin-bottom: 2rem;
}

.setup-card {
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  transition: transform 0.2s ease, box-shadow 0.2s ease;
}

.setup-card:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
}

.card-header {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-weight: 600;
  color: #303133;
}

.card-content {
  padding: 1rem 0;
}

.create-btn, .join-btn {
  width: 100%;
  margin-top: 1rem;
}

.quick-start {
  text-align: center;
}

.quick-buttons {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1rem;
  margin-top: 1rem;
}

.quick-btn {
  padding: 0.75rem;
}

.back-section {
  text-align: center;
  margin-top: 2rem;
}

.back-btn {
  color: #606266;
  border: none;
}

/* 编辑界面样式 */
.editor-interface {
  height: 100vh;
  display: flex;
  flex-direction: column;
}

.editor-header {
  padding: 1rem 1.5rem;
  background: #fff;
  border-bottom: 1px solid #e4e7ed;
  box-shadow: 0 2px 4px rgba(0, 0, 0, 0.1);
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.header-left h2 {
  margin: 0 0 0.25rem 0;
  color: #303133;
  font-size: 1.25rem;
  font-weight: 600;
}

.room-description {
  margin: 0;
  color: #909399;
  font-size: 0.875rem;
}

/* 当前文件信息 */
.current-file-info {
  margin-top: 0.5rem;
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.online-users-display {
  display: flex;
  align-items: center;
}

.users-count {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.5rem 1rem;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  color: white;
  border-radius: 20px;
  font-weight: 600;
  font-size: 0.95rem;
  box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  animation: pulse-glow 2s infinite;
}

.users-icon {
  font-size: 1.1rem;
}

.users-text {
  white-space: nowrap;
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 2px 8px rgba(102, 126, 234, 0.3);
  }
  50% {
    box-shadow: 0 2px 12px rgba(102, 126, 234, 0.5);
  }
}

.header-right {
  display: flex;
  gap: 0.75rem;
}

.btn-primary, .btn-secondary, .btn-back, .btn-save {
  padding: 0.625rem 1.25rem;
  border: none;
  border-radius: 6px;
  cursor: pointer;
  font-size: 0.875rem;
  font-weight: 500;
  transition: all 0.2s ease;
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.btn-primary {
  background: #409eff;
  color: white;
}

.btn-primary:hover {
  background: #337ecc;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(64, 158, 255, 0.3);
}

.btn-secondary {
  background: #909399;
  color: white;
}

.btn-secondary:hover {
  background: #75787c;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(144, 147, 153, 0.3);
}

.btn-save {
  background: #67c23a;
  color: white;
}

.btn-save:hover {
  background: #5daf34;
  transform: translateY(-1px);
  box-shadow: 0 4px 8px rgba(103, 194, 58, 0.3);
}

.btn-back {
  background: #909399;
  color: white;
  border: none;
}

.btn-back:hover {
  background: #75787c;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

/* 文件浏览器侧边栏 */
.file-browser-sidebar {
  width: 320px;
  background: #fff;
  border-right: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 1rem 1.25rem;
  border-bottom: 1px solid #e4e7ed;
}

.sidebar-header h3 {
  margin: 0;
  color: #303133;
  font-size: 1rem;
  font-weight: 600;
}

.file-search {
  padding: 0.75rem 1.25rem;
  border-bottom: 1px solid #e4e7ed;
}

.file-list-container {
  flex: 1;
  overflow-y: auto;
  padding: 0.5rem;
}

.file-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.75rem;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  border: 1px solid transparent;
}

.file-item:hover {
  background: #f5f7fa;
  border-color: #e4e7ed;
}

.file-item.active {
  background: #f0f7ff;
  border-color: #409eff;
}

.file-item.folder {
  cursor: not-allowed;
  opacity: 0.6;
}

.file-icon {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 32px;
  height: 32px;
  background: #f5f5f5;
  border-radius: 6px;
  color: #409eff;
}

.file-item.folder .file-icon {
  color: #ffa940;
}

.file-info {
  flex: 1;
  min-width: 0;
}

.file-name {
  font-weight: 500;
  color: #303133;
  font-size: 0.875rem;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.file-meta {
  display: flex;
  gap: 0.5rem;
  margin-top: 0.25rem;
}

.file-size, .file-time {
  font-size: 0.75rem;
  color: #909399;
}

.file-status {
  color: #67c23a;
}

.empty-files {
  padding: 2rem 1rem;
  text-align: center;
}

/* 编辑器区域 */
.editor-section {
  flex: 1;
  padding: 1.5rem;
  background: #f8f9fa;
}

.editor-section.with-sidebar {
  margin-left: 0;
}

.editor-container {
  height: 100%;
  background: #fff;
  border: 1px solid #e4e7ed;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.no-file-selected {
  height: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  background: #fafbfc;
}

.quill-editor {
  height: 100%;
}

/* 在线用户侧边栏 */
.sidebar {
  width: 320px;
  background: #fff;
  border-left: 1px solid #e4e7ed;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-section {
  padding: 1.25rem;
  border-bottom: 1px solid #e4e7ed;
}

.sidebar-section h3 {
  margin: 0 0 1rem 0;
  color: #303133;
  font-size: 1rem;
  font-weight: 600;
}

/* 用户列表 */
.users-list {
  max-height: 200px;
  overflow-y: auto;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.5rem 0;
}

.user-item.current-user {
  background: #f0f7ff;
  margin: 0 -1rem;
  padding: 0.5rem 1rem;
  border-radius: 6px;
}

.user-avatar {
  background: #409eff;
}

.user-info {
  flex: 1;
  display: flex;
  flex-direction: column;
}

.user-name {
  font-weight: 500;
  color: #303133;
  font-size: 0.875rem;
}

.user-status {
  font-size: 0.75rem;
  color: #909399;
}

/* 消息区域 */
.messages-container {
  height: 200px;
  overflow-y: auto;
  margin-bottom: 1rem;
  border: 1px solid #e4e7ed;
  border-radius: 6px;
  padding: 0.75rem;
  background: #fafbfc;
}

.message {
  margin-bottom: 0.5rem;
  padding: 0.5rem;
  border-radius: 4px;
  font-size: 0.8125rem;
}

.message.system {
  background: #f0f9ff;
  color: #409eff;
  border-left: 3px solid #409eff;
}

.message.chat {
  background: #f6ffed;
  color: #52c41a;
  border-left: 3px solid #52c41a;
}

.message.update {
  background: #fff7e6;
  color: #fa8c16;
  border-left: 3px solid #fa8c16;
}

.message.operation {
  background: #f9f0ff;
  color: #722ed1;
  border-left: 3px solid #722ed1;
}

.timestamp {
  display: block;
  font-size: 0.75rem;
  color: #909399;
  margin-bottom: 0.25rem;
}

.message-input {
  margin-top: 0.75rem;
}

/* 状态栏 */
.status-bar {
  padding: 0.75rem 1.5rem;
  background: #fff;
  border-top: 1px solid #e4e7ed;
  display: flex;
  justify-content: space-between;
  align-items: center;
  font-size: 0.8125rem;
  color: #606266;
  box-shadow: 0 -2px 4px rgba(0, 0, 0, 0.05);
}

.status-bar span {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.status-bar span:not(:last-child)::after {
  content: "•";
  margin-left: 1rem;
  color: #dcdfe6;
}

/* 未保存更改提示 */
.unsaved-changes {
  color: #e6a23c;
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

/* 连接状态指示器 */
.connection-status {
  display: inline-flex;
  align-items: center;
  gap: 0.375rem;
}

.connection-status::before {
  content: "";
  width: 0.5rem;
  height: 0.5rem;
  border-radius: 50%;
  background: currentColor;
}

.connection-status.connected::before {
  background: #67c23a;
}

.connection-status.connecting::before {
  background: #e6a23c;
  animation: pulse 1.5s infinite;
}

.connection-status.disconnected::before {
  background: #f56c6c;
}

@keyframes pulse {
  0%, 100% {
    opacity: 1;
  }
  50% {
    opacity: 0.5;
  }
}

/* 响应式设计 */
@media (max-width: 768px) {
  .setup-options {
    grid-template-columns: 1fr;
  }
  
  .quick-buttons {
    grid-template-columns: 1fr;
  }
  
  .editor-header {
    flex-direction: column;
    gap: 1rem;
    text-align: center;
  }
  
  .header-right {
    justify-content: center;
    flex-wrap: wrap;
  }
  
  .main-content {
    flex-direction: column;
  }
  
  .file-browser-sidebar {
    width: 100%;
    height: 300px;
  }
  
  .sidebar {
    width: 100%;
    height: 300px;
  }
  
  .status-bar {
    flex-direction: column;
    gap: 0.5rem;
    text-align: center;
  }
  
  .status-bar span:not(:last-child)::after {
    display: none;
  }
}

@media (max-width: 480px) {
  .room-setup {
    padding: 1rem;
  }
  
  .setup-container {
    padding: 1.5rem;
  }
  
  .header-right {
    flex-direction: column;
    width: 100%;
  }
  
  .btn-primary, .btn-secondary, .btn-back, .btn-save {
    justify-content: center;
    width: 100%;
  }
}
/* 文件操作按钮 */
.file-actions {
  display: flex;
  align-items: center;
  gap: 0.25rem;
}

.delete-btn {
  color: #f56c6c;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.file-item:hover .delete-btn {
  opacity: 1;
}

.file-item.active .delete-btn {
  opacity: 1;
}

.delete-btn:hover {
  color: #e65c5c;
  background: rgba(245, 108, 108, 0.1);
}
/* HBase文件相关样式 */
.hbase-files-section {
  margin-top: 1rem;
}

.section-divider {
  margin: 1rem 0;
}

.divider-text {
  color: #67c23a;
  font-size: 0.75rem;
  font-weight: 600;
}

.hbase-file {
  border-left: 3px solid #67c23a;
}

.hbase-file .file-icon {
  background: #f0f9ff;
  color: #67c23a;
}

.hbase-tag {
  margin-left: 0.5rem;
}

.file-source {
  font-size: 0.7rem;
  color: #909399;
  margin-right: 0.5rem;
}
/* 增强光标样式 */
.ql-cursor {
  z-index: 1000;
}

.ql-cursor .ql-cursor-caret {
  border-left: 2px solid;
  margin-left: -1px;
}

.ql-cursor .ql-cursor-flag {
  background: currentColor;
  color: white;
  padding: 2px 6px;
  border-radius: 3px;
  font-size: 12px;
  white-space: nowrap;
  pointer-events: none;
}

/* 在线用户列表中的颜色指示 */
.user-color-indicator {
  width: 12px;
  height: 12px;
  border-radius: 50%;
  display: inline-block;
  margin-right: 8px;
}

/* 连接状态指示器 */
.connection-status.connected {
  color: #67c23a;
}

.connection-status.connecting {
  color: #e6a23c;
}

.connection-status.disconnected {
  color: #f56c6c;
}
</style>