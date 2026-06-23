<template>
  <div class="admin-container">
    <div class="admin-navbar">
      <div class="nav-left">
        <el-button @click="goHome" plain>
          <el-icon><Back /></el-icon> 返回前台
        </el-button>
        <span class="system-title">超级管理控制台</span>
      </div>
      <div class="nav-right">
        <span class="welcome-text">欢迎回来, {{ userStore.userInfo?.nickname || '超级管理员' }}</span>
        <el-button type="danger" plain size="small" @click="handleLogout">
          <el-icon><SwitchButton /></el-icon> 退出
        </el-button>
      </div>
    </div>

    <div class="stat-cards">
      <el-row :gutter="20">
        <el-col :span="6">
          <el-card shadow="hover" class="stat-item">
            <template #header>
              <div class="stat-header">
                <span class="stat-label">总用户数</span>
                <el-icon class="stat-icon"><User /></el-icon>
              </div>
            </template>
            <div class="stat-value">{{ totalUsers }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-item">
            <template #header>
              <div class="stat-header">
                <span class="stat-label">管理员</span>
                <el-icon class="stat-icon" color="#E6A23C"><Avatar /></el-icon>
              </div>
            </template>
            <div class="stat-value">{{ adminCount }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-item">
            <template #header>
              <div class="stat-header">
                <span class="stat-label">状态正常</span>
                <el-icon class="stat-icon" color="#67C23A"><CircleCheck /></el-icon>
              </div>
            </template>
            <div class="stat-value">{{ activeUsers }}</div>
          </el-card>
        </el-col>
        <el-col :span="6">
          <el-card shadow="hover" class="stat-item">
            <template #header>
              <div class="stat-header">
                <span class="stat-label">今日新增</span>
                <el-icon class="stat-icon" color="#409EFF"><TrendCharts /></el-icon>
              </div>
            </template>
            <div class="stat-value">{{ todayNewUsers }}</div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="chart-section">
      <el-row :gutter="20">
        <el-col :span="12">
          <el-card class="chart-card" shadow="hover">
            <template #header><span class="chart-title">用户角色分布</span></template>
            <div ref="roleChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
        <el-col :span="12">
          <el-card class="chart-card" shadow="hover">
            <template #header><span class="chart-title">用户状态分布</span></template>
            <div ref="statusChartRef" class="chart-container"></div>
          </el-card>
        </el-col>
      </el-row>
    </div>

    <div class="content-wrapper">
      <div class="operation-bar">
        <div class="operation-left">
          <el-button type="primary" @click="handleAddUser">
            <el-icon><Plus /></el-icon> 添加用户
          </el-button>
          <el-button @click="loadUserList" :loading="loading">
            <el-icon><Refresh /></el-icon> 刷新列表
          </el-button>
          
          <el-button type="warning" @click="openPublishDialog">
            <el-icon><Microphone /></el-icon> 发布公告
          </el-button>
        </div>
        <div class="operation-right">
          <el-input 
            v-model="searchKeyword" 
            placeholder="搜索用户名 / 手机号 / 昵称" 
            prefix-icon="Search" 
            style="width: 300px;" 
            clearable 
          />
        </div>
      </div>

      <el-card class="user-list-card" shadow="never">
        <el-table :data="filteredUsers" v-loading="loading" style="width: 100%" stripe border>
          <el-table-column prop="id" label="ID" width="80" align="center" sortable />
          
          <el-table-column label="用户基本信息" min-width="220">
            <template #default="scope">
              <div class="user-info-cell">
                <el-avatar 
                  :size="42" 
                  shape="square"
                  :src="scope.row.avatar || 'https://cube.elemecdn.com/3/7c/3ea6beec6438fd2643bcef8fd2c3jpeg.jpeg'" 
                />
                <div class="user-detail">
                  <div class="nickname">
                    {{ scope.row.nickname || '未设置昵称' }}
                    <el-tag v-if="scope.row.id === userStore.userInfo.id" size="small" type="success" effect="dark">我</el-tag>
                  </div>
                  <div class="username">账号: {{ scope.row.username }}</div>
                </div>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="角色" width="120" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.role === 2" type="danger" effect="dark">超级管理员</el-tag>
              <el-tag v-else-if="scope.row.role === 1" type="warning" effect="dark">管理员</el-tag>
              <el-tag v-else type="info" effect="plain">普通用户</el-tag>
            </template>
          </el-table-column>

          <el-table-column prop="email" label="邮箱" width="180" align="center">
            <template #default="scope">
              <span>{{ scope.row.email || '未绑定' }}</span>
            </template>
          </el-table-column>

          <el-table-column label="手机号" width="150" align="center">
            <template #default="scope">
              <div class="phone-cell">
                <el-icon v-if="getPhone(scope.row)"><Iphone /></el-icon>
                <span>{{ getPhone(scope.row) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="状态" width="100" align="center">
            <template #default="scope">
              <el-tag v-if="scope.row.status === 1" type="success" effect="light">
                <el-icon><Check /></el-icon> 正常
              </el-tag>
              <el-tag v-else type="danger" effect="dark">
                <el-icon><Close /></el-icon> 禁用
              </el-tag>
            </template>
          </el-table-column>
          
          <el-table-column label="最近登录" width="180" sortable prop="lastLoginTime" align="center">
            <template #default="scope">
              <div class="time-cell" :class="{ 'recent-login': isRecent(scope.row.lastLoginTime || scope.row.updateTime) }">
                <el-icon><Clock /></el-icon>
                <span>{{ formatTime(scope.row.lastLoginTime || scope.row.updateTime) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="注册时间" width="180" sortable prop="createTime" align="center">
            <template #default="scope">
              <div class="time-cell text-gray">
                <el-icon><Calendar /></el-icon>
                <span>{{ formatTime(scope.row.createTime) }}</span>
              </div>
            </template>
          </el-table-column>

          <el-table-column label="操作" width="260" fixed="right" align="center">
            <template #default="scope">
              <div class="action-buttons">
                <el-button size="small" type="primary" link @click="handleEdit(scope.row)">
                  <el-icon><Edit /></el-icon> 编辑
                </el-button>

                <template v-if="scope.row.id !== userStore.userInfo.id">
                  <el-divider direction="vertical" />
                  <el-button 
                    v-if="scope.row.status === 1" 
                    size="small" 
                    type="danger" 
                    link 
                    @click="handleToggleStatus(scope.row, 0)"
                  >
                    <el-icon><CircleClose /></el-icon> 禁用
                  </el-button>
                  <el-button 
                    v-else 
                    size="small" 
                    type="success" 
                    link 
                    @click="handleToggleStatus(scope.row, 1)"
                  >
                    <el-icon><VideoPlay /></el-icon> 启用
                  </el-button>
                </template>

                <el-divider direction="vertical" />
                
                <el-dropdown trigger="click" @command="(cmd) => handleMoreCommand(cmd, scope.row)">
                  <el-button size="small" link>
                    更多 <el-icon><ArrowDown /></el-icon>
                  </el-button>
                  <template #dropdown>
                    <el-dropdown-menu>
                      <el-dropdown-item command="resetPwd" icon="Unlock">重置密码</el-dropdown-item>
                      <el-dropdown-item command="delete" icon="Delete" style="color: #f56c6c;" :disabled="scope.row.id === userStore.userInfo.id">彻底删除</el-dropdown-item>
                    </el-dropdown-menu>
                  </template>
                </el-dropdown>
              </div>
            </template>
          </el-table-column>
        </el-table>
      </el-card>
    </div>

    <el-dialog v-model="userDialogVisible" :title="isEditMode ? '编辑用户' : '添加用户'" width="500px">
      <el-form :model="userForm" label-width="80px">
        <el-form-item label="用户名" v-if="!isEditMode">
          <el-input v-model="userForm.username" placeholder="登录账号" />
        </el-form-item>
        <el-form-item label="昵称">
          <el-input v-model="userForm.nickname" placeholder="用户展示名称" />
        </el-form-item>
        <el-form-item label="手机号">
          <el-input v-model="userForm.phone" placeholder="11位手机号" />
        </el-form-item>
        <el-form-item label="密码" v-if="!isEditMode">
          <el-input v-model="userForm.password" type="password" show-password placeholder="默认 123456" />
        </el-form-item>
        <el-form-item label="角色">
          <el-radio-group v-model="userForm.role">
            <el-radio :label="0">普通用户</el-radio>
            <el-radio :label="1">管理员</el-radio>
            <el-radio :label="2">超级管理员</el-radio>
          </el-radio-group>
        </el-form-item>
        <el-form-item label="状态" v-if="isEditMode">
          <el-radio-group v-model="userForm.status">
            <el-radio :label="1">正常</el-radio>
            <el-radio :label="0">禁用</el-radio>
          </el-radio-group>
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="userDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleSubmit">确定</el-button>
      </template>
    </el-dialog>

    <el-dialog v-model="resetDialogVisible" title="重置密码" width="400px">
      <div class="dialog-content">
        <el-alert title="操作提示" type="warning" :closable="false" show-icon style="margin-bottom: 15px">
          正在为用户 <strong>{{ targetUser?.username }}</strong> 重置密码，修改后用户需重新登录。
        </el-alert>
        <el-input v-model="resetForm.newPassword" type="password" show-password placeholder="请输入新密码" prefix-icon="Lock" />
      </div>
      <template #footer>
        <el-button @click="resetDialogVisible = false">取消</el-button>
        <el-button type="primary" @click="handleResetConfirm">确定重置</el-button>
      </template>
    </el-dialog>
    
    <el-dialog 
      v-model="publishDialogVisible" 
      title="发布系统公告" 
      width="600px"
      :close-on-click-modal="false"
    >
      <el-form :model="announcementForm" ref="announcementFormRef" label-width="80px">
        <el-form-item label="标题" prop="title" :rules="[{ required: true, message: '请输入公告标题' }]">
          <el-input v-model="announcementForm.title" placeholder="公告标题，简洁明了" maxlength="50" show-word-limit />
        </el-form-item>
        <el-form-item label="内容" prop="content" :rules="[{ required: true, message: '请输入公告内容' }]">
          <el-input 
            v-model="announcementForm.content" 
            type="textarea" 
            :rows="5" 
            placeholder="详细公告内容"
            maxlength="500"
            show-word-limit
          />
        </el-form-item>
      </el-form>
      <template #footer>
        <el-button @click="publishDialogVisible = false">取消</el-button>
        <el-button type="warning" @click="handlePublish" :loading="publishing">
          <el-icon><Promotion /></el-icon> 确认发布
        </el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, computed, nextTick, onUnmounted } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Plus, Refresh, Search, Edit, Delete, Unlock, Back, SwitchButton,
  Check, Close, CircleClose, VideoPlay, Iphone, Clock, ArrowDown, Calendar,
  User, Avatar, CircleCheck, TrendCharts, Microphone, Promotion
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import * as adminApi from '@/api/admin'
import { userRegister } from '@/api/user'
import * as echarts from 'echarts'

const router = useRouter()
const route = useRoute()
const userStore = useUserStore()
const loading = ref(false)
const userList = ref([])
const searchKeyword = ref(route.query.q || '')

// 弹窗控制
const userDialogVisible = ref(false)
const resetDialogVisible = ref(false)
const publishDialogVisible = ref(false)
const publishing = ref(false)
const isEditMode = ref(false)
const targetUser = ref(null)

const userForm = reactive({ id: null, username: '', nickname: '', phone: '', password: '', role: 0, status: 1 })
const resetForm = reactive({ userId: null, newPassword: '' })
const announcementForm = reactive({ title: '', content: '' })
const announcementFormRef = ref(null)

// 图表实例
const roleChartRef = ref(null)
const statusChartRef = ref(null)
let roleChart = null
let statusChart = null

// --- 1. 加载数据 & 统计 ---
const loadUserList = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUserList()
    if (res.code === 200) {
      userList.value = res.data.map(u => ({ ...u, status: u.status !== undefined ? u.status : 1, phone: getPhone(u) }))
      nextTick(() => initCharts())
    }
  } catch (error) { ElMessage.error('数据加载失败') } finally { loading.value = false }
}

const getPhone = (row) => row.phone || row.mobile || row.phoneNumber || row.tel || ''
const totalUsers = computed(() => userList.value.length)
const activeUsers = computed(() => userList.value.filter(u => u.status === 1).length) 
const adminCount = computed(() => userList.value.filter(u => u.role === 1 || u.role === 2).length)
const todayNewUsers = computed(() => {
  const today = new Date().toDateString()
  return userList.value.filter(u => new Date(u.createTime).toDateString() === today).length
})

const filteredUsers = computed(() => {
  if (!searchKeyword.value) return userList.value
  const kw = searchKeyword.value.toLowerCase()
  return userList.value.filter(u => 
    (u.username && u.username.toLowerCase().includes(kw)) || 
    (String(u.phone).includes(kw)) ||
    (u.nickname && u.nickname.toLowerCase().includes(kw))
  )
})

// --- 2. 图表渲染 (修复图表不可见的问题) ---
const initCharts = () => {
  if (!roleChartRef.value || !statusChartRef.value) return
  if (roleChart) roleChart.dispose()
  if (statusChart) statusChart.dispose()

  roleChart = echarts.init(roleChartRef.value)
  statusChart = echarts.init(statusChartRef.value)
  
  const superAdmin = userList.value.filter(u => u.role === 2).length
  const admin = userList.value.filter(u => u.role === 1).length
  const normal = userList.value.filter(u => u.role === 0).length
  
  roleChart.setOption({
    tooltip: { trigger: 'item' },
    legend: { bottom: '0%' },
    color: ['#F56C6C', '#E6A23C', '#409EFF'],
    series: [{ name: '角色分布', type: 'pie', radius: ['40%', '70%'], center: ['50%', '45%'], data: [{ value: superAdmin, name: '超级管理员' }, { value: admin, name: '管理员' }, { value: normal, name: '普通用户' }] }]
  })
  
  statusChart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { top: '15%', bottom: '10%', left: '10%', right: '10%' },
    xAxis: { type: 'category', data: ['正常', '禁用'] },
    yAxis: { type: 'value' },
    series: [{ data: [{ value: activeUsers.value, itemStyle: { color: '#67C23A' } }, { value: totalUsers.value - activeUsers.value, itemStyle: { color: '#F56C6C' } }], type: 'bar', barWidth: '40%' }]
  })
  
  // 关键修复：手动调用 resize 确保图表在 nextTick 后正确绘制
  roleChart.resize()
  statusChart.resize()
}

// --- 3. 交互逻辑 ---
const handleSearch = () => { router.push({ query: { ...route.query, q: searchKeyword.value } }) }
const handleRefresh = () => { router.push({ query: {} }); ElMessage.success('列表已刷新') }
const handleAddUser = () => { isEditMode.value = false; Object.assign(userForm, { username: '', nickname: '', phone: '', password: '', role: 0, status: 1 }); userDialogVisible.value = true }
const handleEdit = (row) => { isEditMode.value = true; Object.assign(userForm, { ...row }); userDialogVisible.value = true }
const handleSubmit = async () => { /* ... */ }
const handleToggleStatus = async (row, newStatus) => {
  const action = newStatus === 1 ? '启用' : '禁用';
  const type = newStatus === 1 ? 'success' : 'warning';
  
  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${row.username}" 吗?`, '提示', { type });
    
    // 检查是否在操作自己
    if (row.id === userStore.userInfo.id) {
        return ElMessage.error('无法禁用自己的账号');
    }
    
    const res = await adminApi.updateUserStatus(row.id, newStatus); 
    
    if (res.code === 200) {
        row.status = newStatus; // API 成功后，更新本地状态
        ElMessage.success(`用户已${action}`);
        initCharts(); 
    } else {
        ElMessage.error(res.message || '操作失败');
    }
  } catch (e) {
      if (e !== 'cancel') {
          ElMessage.error('操作取消或失败');
      }
  }
};
const handleDelete = async (row) => { /* ... */ }
const openResetPassword = (row) => { targetUser.value = row; resetForm.userId = row.id; resetForm.newPassword = ''; resetDialogVisible.value = true }
const handleResetConfirm = async () => { /* ... */ }
const handleMoreCommand = (cmd, row) => { if (cmd === 'resetPwd') openResetPassword(row); if (cmd === 'delete') handleDelete(row) }
const goHome = () => router.push('/files')
const handleLogout = () => { userStore.logout(); router.push('/login') }
const formatTime = (time) => { if (!time) return '从未登录'; return new Date(time).toLocaleString('zh-CN', { year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit' }) }
const isRecent = (time) => { if (!time) return false; return (Date.now() - new Date(time).getTime()) < 3 * 24 * 60 * 60 * 1000 }

const openPublishDialog = () => {
  announcementForm.title = ''
  announcementForm.content = ''
  publishDialogVisible.value = true
}

const handlePublish = async () => {
  if (!announcementFormRef.value) return
  // 假设验证逻辑在模板中
  
  publishing.value = true
  try {
    const res = await adminApi.publishAnnouncement(announcementForm)
    if (res.code === 200) {
      ElMessage.success('公告发布成功！')
      publishDialogVisible.value = false
    } else {
      ElMessage.error(res.message || '发布失败')
    }
  } catch (error) {
    ElMessage.error('发布失败，请检查网络或后端日志')
  } finally {
    publishing.value = false
  }
}

// --- 4. 生命周期 ---
onMounted(() => loadUserList())
onUnmounted(() => { if(roleChart) roleChart.dispose(); if(statusChart) statusChart.dispose() })
</script>

<style scoped>
.admin-container { 
  background: var(--main-bg);
  min-height: 100vh; 
  padding-bottom: 40px; 
  transition: background-color 0.3s;
}

.admin-navbar { 
  background: rgba(255, 255, 255, 0.8);
  backdrop-filter: blur(10px);
  height: 64px; 
  padding: 0 32px; 
  display: flex; 
  justify-content: space-between; 
  align-items: center; 
  position: sticky; 
  top: 0; 
  z-index: 100; 
  border-bottom: 1px solid var(--border-light);
}
.nav-left, .nav-right { display: flex; align-items: center; gap: 16px; }
.system-title { font-size: 20px; font-weight: 700; color: var(--text-primary); letter-spacing: -0.5px; }
.welcome-text { font-size: 14px; color: var(--text-secondary); font-weight: 500; }

.stat-cards { padding: 32px 32px 0; }
.stat-item { 
  border: none; 
  background: var(--card-bg);
  height: 100%;
  display: flex;
  align-items: center;
}
.stat-header { display: flex; justify-content: space-between; align-items: center; }
.stat-label { font-size: 14px; color: var(--text-tertiary); }
.stat-icon { font-size: 20px; background: var(--bg-tertiary); padding: 8px; border-radius: 50%; }
.stat-value { font-size: 28px; font-weight: bold; color: var(--text-primary); margin-top: 10px; }

.chart-section { padding: 24px 32px; }
.chart-card { border-radius: 8px; background-color: var(--card-bg); }
.chart-title { font-weight: 600; font-size: 16px; color: var(--text-primary); }
.chart-container { height: 320px; width: 100%; }

.content-wrapper { padding: 0 32px; }
.operation-bar { 
  display: flex; justify-content: space-between; margin-bottom: 16px; 
  background: var(--card-bg); 
  padding: 16px; border-radius: 8px; box-shadow: var(--shadow-sm);
  border: 1px solid var(--border-light);
}
.user-list-card { border-radius: 8px; background-color: var(--card-bg); }
.user-info-cell { display: flex; align-items: center; gap: 16px; }
.user-detail { display: flex; flex-direction: column; }
.nickname { font-weight: 600; font-size: 15px; color: var(--text-primary); }
.username { font-size: 13px; color: var(--text-tertiary); margin-top: 2px; }
.action-buttons { display: flex; align-items: center; justify-content: center; gap: 5px; }

/* Deep 样式修复 */
.search-input :deep(.el-input-group__append) { background-color: var(--el-color-primary); color: white; border-color: var(--el-color-primary); cursor: pointer; }
:deep(.el-card) { background-color: var(--card-bg); border-color: var(--border-light); color: var(--text-primary); }
:deep(.el-table) { --el-table-bg-color: var(--card-bg); --el-table-tr-bg-color: var(--card-bg); --el-table-header-bg-color: var(--bg-tertiary); color: var(--text-primary); }
:deep(.el-table th) { background-color: var(--bg-tertiary) !important; color: var(--text-primary); }
:deep(.el-table--striped .el-table__body tr.el-table__row--striped td) { background: var(--bg-tertiary); }
</style>