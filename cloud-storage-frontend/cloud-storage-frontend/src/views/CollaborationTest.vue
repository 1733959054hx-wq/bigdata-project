<!-- src/views/CollaborationTest.vue -->
<template>
  <div class="collaboration-test">
    <div class="test-header">
      <h1>协同编辑测试页面</h1>
      <div class="room-info">
        <el-input v-model="roomId" placeholder="输入房间ID" style="width: 200px" />
        <el-button type="primary" @click="connectToRoom">连接房间</el-button>
        <el-button @click="disconnect">断开连接</el-button>
        <span class="connection-status" :class="{ connected: isConnected }">
          {{ isConnected ? '已连接' : '未连接' }}
        </span>
        <span class="user-count">用户数: {{ userCount }}</span>
      </div>
    </div>

    <div class="test-container">
      <!-- 文本编辑器区域 -->
      <div class="editor-section">
        <h3>文本编辑器</h3>
        <el-input
          v-model="textContent"
          type="textarea"
          :rows="10"
          placeholder="在这里输入文本，其他用户会实时看到..."
          @input="onTextChange"
        />
        <div class="text-info">
          字符数: {{ textContent.length }} | 行数: {{ textContent.split('\n').length }}
        </div>
      </div>

      <!-- 实时消息区域 -->
      <div class="messages-section">
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
          <el-input v-model="newMessage" placeholder="输入聊天消息..." @keyup.enter="sendMessage">
            <template #append>
              <el-button @click="sendMessage">发送</el-button>
            </template>
          </el-input>
        </div>
      </div>

      <!-- 用户列表区域 -->
      <div class="users-section">
        <h3>在线用户</h3>
        <div class="users-list">
          <div 
            v-for="user in onlineUsers" 
            :key="user.id"
            class="user-item"
          >
            <el-avatar :size="32" :src="user.avatar" />
            <span class="user-name">{{ user.name }}</span>
            <el-tag v-if="user.isMe" size="small">我</el-tag>
          </div>
        </div>
      </div>
    </div>

    <!-- 操作面板 -->
    <div class="action-panel">
      <el-button-group>
        <el-button @click="sendTestOperation('insert')">发送插入操作</el-button>
        <el-button @click="sendTestOperation('delete')">发送删除操作</el-button>
        <el-button @click="sendTestOperation('format')">发送格式操作</el-button>
        <el-button @click="clearMessages">清空消息</el-button>
      </el-button-group>
      
      <div class="test-rooms">
        <h4>快速测试房间:</h4>
        <el-button 
          v-for="room in testRooms" 
          :key="room"
          size="small"
          @click="switchRoom(room)"
        >
          {{ room }}
        </el-button>
      </div>
    </div>

    <!-- 调试信息 -->
    <div class="debug-info" v-if="showDebug">
      <h4 @click="showDebug = !showDebug">调试信息 (点击隐藏)</h4>
      <pre>{{ debugInfo }}</pre>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, onUnmounted } from 'vue'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const userStore = useUserStore()

// 状态管理
const roomId = ref('test-room-1')
const isConnected = ref(false)
const userCount = ref(0)
const textContent = ref('')
const newMessage = ref('')
const showDebug = ref(true)

// WebSocket 连接
let ws = null
let syncTimeout = null // 防抖定时器

// 测试数据
const messages = ref([])
const onlineUsers = ref([])
const testRooms = ['test-room-1', 'test-room-2', 'test-room-3', 'document-123']

// 调试信息
const debugInfo = computed(() => {
  return {
    连接状态: isConnected.value ? '已连接' : '未连接',
    房间ID: roomId.value,
    用户数量: userCount.value,
    在线用户: onlineUsers.value.length,
    消息数量: messages.value.length,
    当前用户: userStore.userInfo?.username || '未登录'
  }
})

// 格式化时间
const formatTime = (timestamp) => {
  return new Date(timestamp).toLocaleTimeString()
}

// 连接房间
const connectToRoom = () => {
  if (!roomId.value.trim()) {
    ElMessage.warning('请输入房间ID')
    return
  }

  try {
    // 关闭现有连接
    if (ws) {
      ws.close()
    }

    // 创建新的 WebSocket 连接
    ws = new WebSocket(`ws://localhost:1234/${roomId.value}`)
    
    ws.onopen = () => {
      isConnected.value = true
      addMessage('system', '已连接到房间: ' + roomId.value)
      ElMessage.success('连接成功！')
      
      // 发送用户加入通知
      const joinMessage = {
        type: 'user_joined',
        username: userStore.userInfo?.username || '匿名用户',
        userInfo: userStore.userInfo,
        timestamp: Date.now()
      }
      ws.send(JSON.stringify(joinMessage))
    }

    ws.onmessage = async (event) => {
      console.log('收到原始消息:', event.data)
      
      try {
        // 处理不同类型的消息数据
        let data = event.data
        
        if (data instanceof Blob) {
          data = await handleBlobData(data)
        } else if (typeof data === 'string') {
          // 已经是字符串，尝试解析 JSON
          try {
            data = JSON.parse(data)
          } catch (e) {
            // 保持为字符串
            console.log('消息是纯字符串:', data)
          }
        }
        
        handleWebSocketMessage(data)
      } catch (error) {
        console.error('处理消息失败:', error)
        addMessage('error', '处理消息时出错: ' + error.message)
      }
    }

    ws.onclose = (event) => {
      isConnected.value = false
      addMessage('system', `连接已断开 (代码: ${event.code}, 原因: ${event.reason || '无'})`)
      if (event.code !== 1000) {
        ElMessage.warning('连接异常断开')
      }
    }

    ws.onerror = (error) => {
      console.error('WebSocket错误:', error)
      ElMessage.error('连接失败: ' + error.message)
    }

  } catch (error) {
    console.error('连接房间失败:', error)
    ElMessage.error('连接失败: ' + error.message)
  }
}

// 专门处理 Blob 数据
const handleBlobData = async (blob) => {
  try {
    const text = await blob.text()
    console.log('Blob 转换为文本:', text)
    
    // 尝试解析为 JSON
    try {
      return JSON.parse(text)
    } catch (e) {
      return text // 返回纯文本
    }
  } catch (error) {
    console.error('处理 Blob 数据失败:', error)
    return '无法解析的二进制数据'
  }
}

// 处理 WebSocket 消息
const handleWebSocketMessage = (data) => {
  console.log('收到消息:', data)
  
  // 如果是 Blob 对象，需要特殊处理
  if (data instanceof Blob) {
    handleBlobMessage(data)
    return
  }
  
  switch (data.type) {
    case 'connected':
      userCount.value = data.clientCount
      addMessage('system', `连接成功，当前房间用户数: ${data.clientCount}`)
      addCurrentUser()
      break
      
    case 'user_left':
      userCount.value = data.clientCount
      addMessage('system', `用户离开，剩余用户: ${data.clientCount}`)
      break
      
    case 'text_update':
      // 重要：更新文本内容，但不触发本地 onChange 事件
      if (data.content !== textContent.value) {
        updateTextContent(data.content, false) // false 表示不发送更新
        addMessage('update', `文档已更新 (${data.user || '其他用户'})`)
      }
      break
      
    case 'chat_message':
      addMessage('chat', `${data.user}: ${data.message}`)
      break
      
    case 'text_operation':
      handleTextOperation(data)
      break
      
    default:
      // 如果是字符串，直接显示
      if (typeof data === 'string') {
        addMessage('text', data)
      } else {
        addMessage('system', `未知消息类型: ${data.type || 'unknown'}`)
        console.log('未知消息:', data)
      }
  }
}

// 处理 Blob 消息
const handleBlobMessage = async (blob) => {
  try {
    const text = await blob.text()
    console.log('Blob 内容:', text)
    
    try {
      // 尝试解析为 JSON
      const data = JSON.parse(text)
      handleWebSocketMessage(data)
    } catch (e) {
      // 如果不是 JSON，直接显示文本
      addMessage('text', text)
    }
  } catch (error) {
    console.error('处理 Blob 消息失败:', error)
    addMessage('error', '无法处理二进制消息')
  }
}

// 处理文本操作
const handleTextOperation = (operation) => {
  let currentText = textContent.value
  
  switch (operation.operation) {
    case 'insert':
      const before = currentText.slice(0, operation.position)
      const after = currentText.slice(operation.position)
      currentText = before + operation.text + after
      break
      
    case 'delete':
      const start = operation.position
      const end = operation.position + operation.length
      currentText = currentText.slice(0, start) + currentText.slice(end)
      break
      
    default:
      console.log('未知操作:', operation.operation)
      return
  }
  
  // 更新文本内容，但不触发远程同步
  updateTextContent(currentText, false)
  addMessage('operation', `应用了 ${operation.operation} 操作 (${operation.user})`)
}

// 安全的文本更新函数
const updateTextContent = (newContent, shouldSync = true) => {
  textContent.value = newContent
  if (shouldSync) {
    syncTextToRemote()
  }
}

// 同步文本到远程
const syncTextToRemote = () => {
  if (isConnected.value && ws && ws.readyState === WebSocket.OPEN) {
    const message = {
      type: 'text_update',
      content: textContent.value,
      user: userStore.userInfo?.username || '匿名用户',
      timestamp: Date.now()
    }
    ws.send(JSON.stringify(message))
  }
}

// 文本变化处理 - 防抖版本（这是唯一的 onTextChange 函数）
const onTextChange = () => {
  // 清除之前的定时器
  if (syncTimeout) {
    clearTimeout(syncTimeout)
  }
  
  // 设置新的定时器，延迟同步以避免频繁发送
  syncTimeout = setTimeout(() => {
    syncTextToRemote()
  }, 300) // 300ms 防抖
}

// 添加当前用户到在线列表
const addCurrentUser = () => {
  const currentUser = {
    id: userStore.userInfo?.id || 'current',
    name: userStore.userInfo?.username || '匿名用户',
    avatar: userStore.userInfo?.avatar || '',
    isMe: true
  }
  
  // 检查是否已经存在
  const exists = onlineUsers.value.find(u => u.isMe)
  if (!exists) {
    onlineUsers.value.unshift(currentUser)
  }
}

// 添加消息
const addMessage = (type, content) => {
  messages.value.push({
    type,
    content,
    timestamp: Date.now()
  })
  
  // 限制消息数量，避免内存泄漏
  if (messages.value.length > 100) {
    messages.value = messages.value.slice(-50)
  }
}

// 发送聊天消息
const sendMessage = () => {
  if (!newMessage.value.trim()) return
  
  if (isConnected.value && ws) {
    const message = {
      type: 'chat_message',
      message: newMessage.value,
      user: userStore.userInfo?.username || '匿名用户',
      timestamp: Date.now()
    }
    ws.send(JSON.stringify(message))
    newMessage.value = ''
  } else {
    ElMessage.warning('请先连接到房间')
  }
}

// 发送测试操作
const sendTestOperation = (operationType) => {
  if (!isConnected.value || !ws) {
    ElMessage.warning('请先连接到房间')
    return
  }

  const operations = {
    insert: {
      type: 'text_operation',
      operation: 'insert',
      position: Math.min(textContent.value.length, 10),
      text: '[测试插入]',
      user: userStore.userInfo?.username || '测试用户'
    },
    delete: {
      type: 'text_operation', 
      operation: 'delete',
      position: Math.max(0, textContent.value.length - 5),
      length: 3,
      user: userStore.userInfo?.username || '测试用户'
    },
    format: {
      type: 'format_operation',
      operation: 'bold',
      range: { start: 0, end: 5 },
      user: userStore.userInfo?.username || '测试用户'
    }
  }

  const operation = operations[operationType]
  if (operation) {
    ws.send(JSON.stringify(operation))
    addMessage('operation', `发送了 ${operationType} 操作`)
  }
}

// 切换房间
const switchRoom = (newRoomId) => {
  roomId.value = newRoomId
  connectToRoom()
}

// 断开连接
const disconnect = () => {
  if (ws) {
    ws.close()
    ws = null
  }
  isConnected.value = false
  onlineUsers.value = onlineUsers.value.filter(user => user.isMe) // 只保留自己
  addMessage('system', '已断开连接')
}

// 清空消息
const clearMessages = () => {
  messages.value = []
}

// 生命周期
onMounted(() => {
  // 自动连接默认房间
  setTimeout(() => {
    connectToRoom()
  }, 1000)
})

onUnmounted(() => {
  disconnect()
  // 清理定时器
  if (syncTimeout) {
    clearTimeout(syncTimeout)
  }
})
</script>

<style scoped>
.collaboration-test {
  padding: 20px;
  max-width: 1200px;
  margin: 0 auto;
}

.test-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding-bottom: 20px;
  border-bottom: 1px solid #eaeaea;
}

.room-info {
  display: flex;
  align-items: center;
  gap: 10px;
}

.connection-status {
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.connection-status.connected {
  background: #f0f9ff;
  color: #1890ff;
}

.connection-status:not(.connected) {
  background: #fff2f0;
  color: #ff4d4f;
}

.user-count {
  font-weight: bold;
  color: #52c41a;
}

.test-container {
  display: grid;
  grid-template-columns: 2fr 1fr 1fr;
  gap: 20px;
  margin-bottom: 20px;
}

.editor-section, .messages-section, .users-section {
  border: 1px solid #eaeaea;
  border-radius: 8px;
  padding: 16px;
  background: white;
}

.editor-section h3, .messages-section h3, .users-section h3 {
  margin-top: 0;
  margin-bottom: 16px;
  color: #1890ff;
}

.text-info {
  margin-top: 10px;
  font-size: 12px;
  color: #666;
}

.messages-container {
  height: 300px;
  overflow-y: auto;
  border: 1px solid #f0f0f0;
  border-radius: 4px;
  padding: 10px;
  margin-bottom: 10px;
  background: #fafafa;
}

.message {
  margin-bottom: 8px;
  padding: 4px 8px;
  border-radius: 4px;
  font-size: 12px;
}

.message.system {
  background: #e6f7ff;
  color: #1890ff;
}

.message.chat {
  background: #f6ffed;
  color: #52c41a;
}

.message.update {
  background: #fff7e6;
  color: #fa8c16;
}

.message.operation {
  background: #f9f0ff;
  color: #722ed1;
}

.timestamp {
  color: #999;
  margin-right: 8px;
}

.user-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 8px;
  border-bottom: 1px solid #f0f0f0;
}

.user-item:last-child {
  border-bottom: none;
}

.user-name {
  flex: 1;
}

.action-panel {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-bottom: 20px;
  padding: 16px;
  background: #fafafa;
  border-radius: 8px;
}

.test-rooms {
  display: flex;
  align-items: center;
  gap: 10px;
}

.test-rooms h4 {
  margin: 0;
  margin-right: 10px;
}

.debug-info {
  background: #f5f5f5;
  border-radius: 8px;
  padding: 16px;
  font-family: 'Courier New', monospace;
}

.debug-info h4 {
  margin-top: 0;
  cursor: pointer;
  color: #1890ff;
}

.debug-info pre {
  margin: 0;
  white-space: pre-wrap;
  word-wrap: break-word;
}
</style>