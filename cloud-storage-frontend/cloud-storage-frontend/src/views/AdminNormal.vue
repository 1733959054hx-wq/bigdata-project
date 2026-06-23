<template>
// ... (rest of template is unchanged)
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage, ElMessageBox } from 'element-plus'
import { 
  Refresh, Back, Search, Calendar, Clock, Unlock, Delete, 
  SwitchButton, CircleClose, VideoPlay, Iphone
} from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import * as adminApi from '@/api/admin'

const router = useRouter()
const userStore = useUserStore()
const loading = ref(false)
const userList = ref([])
const searchKeyword = ref('')

const resetDialogVisible = ref(false)
const newPassword = ref('')
const targetUser = ref(null)

// 加载数据
const loadUserList = async () => {
  loading.value = true
  try {
    const res = await adminApi.getUserList()
    if (res.code === 200) {
      userList.value = res.data.map(u => ({
        ...u,
        status: u.status !== undefined ? u.status : 1,
        phone: getPhone(u)
      }))
    }
  } catch (error) {
    ElMessage.error('获取数据失败')
  } finally { 
    loading.value = false 
  }
}

// 辅助：获取手机号
const getPhone = (row) => {
  return row.phone || row.mobile || row.phoneNumber || row.tel || '未绑定'
}

// 搜索
const filteredUsers = computed(() => {
  if (!searchKeyword.value) return userList.value
  const kw = searchKeyword.value.toLowerCase()
  return userList.value.filter(u => 
    (u.username && u.username.toLowerCase().includes(kw)) || 
    (String(u.phone).includes(kw)) ||
    (u.nickname && u.nickname.toLowerCase().includes(kw))
  )
})

// ✨ 核心修改：禁用/启用逻辑，确保 API 调用成功后再更新状态
const handleToggleStatus = async (row, newStatus) => {
  const action = newStatus === 1 ? '启用' : '禁用'
  const type = newStatus === 1 ? 'success' : 'warning'
  try {
    await ElMessageBox.confirm(`确定要${action}用户 "${row.username}" 吗?`, '提示', { type })
    
    // 检查是否在操作自己
    if (row.id === userStore.userInfo.id) {
        return ElMessage.error(`无法${action}自己的账号`);
    }

    // 1. 调用 API
    const res = await adminApi.updateUserStatus(row.id, newStatus)
    
    // 2. 检查 API 响应
    if (res.code === 200) {
        row.status = newStatus // ✨ 修正：API 成功后，更新本地状态
        ElMessage.success(`已${action}`)
    } else {
        ElMessage.error(res.message || `用户${action}失败`)
    }
  } catch (e) {
    if (e !== 'cancel') {
        ElMessage.error('操作取消或失败')
    }
  }
}

const handleDelete = async (row) => {
  try {
    await ElMessageBox.confirm(`确定删除用户 "${row.nickname || row.username}" 吗?`, '警告', { type: 'error' })
    const res = await adminApi.deleteUser(row.id)
    if (res.code === 200) {
        ElMessage.success('删除成功')
        loadUserList()
    } else {
        ElMessage.error(res.message || '删除失败')
    }
  } catch (e) {}
}

const openResetPassword = (row) => {
  targetUser.value = row
  newPassword.value = ''
  resetDialogVisible.value = true
}

const handleResetConfirm = async () => {
  if (!newPassword.value || newPassword.value.length < 6) return ElMessage.warning('密码至少6位')
  try {
    const res = await adminApi.adminUpdatePassword({ id: targetUser.value.id, password: newPassword.value })
    if (res.code === 200) {
        ElMessage.success('密码重置成功')
        resetDialogVisible.value = false
    } else {
        ElMessage.error(res.message || '重置失败')
    }
  } catch (e) {}
}

const goHome = () => router.push('/files')
const handleLogout = () => { userStore.logout(); router.push('/login') }

// 时间格式化
const formatTime = (time) => {
  if (!time) return '从未登录'
  return new Date(time).toLocaleString('zh-CN', {
    year: 'numeric', month: '2-digit', day: '2-digit', hour: '2-digit', minute: '2-digit'
  })
}
const isRecent = (time) => {
  if (!time) return false
  return (Date.now() - new Date(time).getTime()) < 3 * 24 * 60 * 60 * 1000
}

onMounted(() => loadUserList())
</script>

<style scoped>
/* ✨✨✨ 修复：AdminNormal 页面样式变量化 ✨✨✨ */
.admin-container { 
  background: var(--main-bg); 
  min-height: 100vh; 
  padding-bottom: 40px; 
  transition: background-color 0.3s;
}

.admin-navbar {
  background: var(--card-bg); 
  height: 60px; 
  padding: 0 24px;
  display: flex; justify-content: space-between; align-items: center;
  box-shadow: var(--shadow-sm); 
  position: sticky; top: 0; z-index: 100;
  border-bottom: 1px solid var(--border-light);
}

.system-title { font-size: 18px; font-weight: bold; color: var(--text-primary); }
.welcome-text { font-size: 14px; color: var(--text-secondary); }

.content-wrapper { padding: 24px; max-width: 1200px; margin: 0 auto; }
.operation-bar { 
  display: flex; justify-content: space-between; margin-bottom: 16px; 
  background: var(--card-bg); padding: 16px; border-radius: 8px; 
  box-shadow: var(--shadow-sm); border: 1px solid var(--border-light);
}

.user-list-card { border-radius: 8px; background-color: var(--card-bg); }
.user-info-cell { display: flex; align-items: center; gap: 12px; }
.user-detail { display: flex; flex-direction: column; justify-content: center; }
.nickname { font-weight: 600; font-size: 14px; color: var(--text-primary); }
.username { font-size: 12px; color: var(--text-tertiary); }
.phone-cell, .time-cell { display: flex; align-items: center; justify-content: center; gap: 5px; font-size: 13px; color: var(--text-secondary); }
.recent-login { color: var(--success); font-weight: bold; }

.search-input :deep(.el-input-group__append) { 
  background-color: var(--el-color-primary); 
  color: white; 
  border-color: var(--el-color-primary); 
  cursor: pointer; 
}

:deep(.el-table) { background-color: var(--card-bg); color: var(--text-primary); }
:deep(.el-table th) { background-color: var(--bg-tertiary) !important; color: var(--text-primary); }
:deep(.el-table__row:hover) { background-color: var(--bg-tertiary) !important; }
</style>