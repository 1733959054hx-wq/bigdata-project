<template>
  <div class="profile-container">
    <div class="profile-header">
      <div class="avatar-section">
        <div class="avatar-upload">
          <el-avatar :size="100" :src="userInfo.avatar || defaultAvatar" class="user-avatar">
            {{ userInfo.nickname?.[0] || userInfo.username?.[0] }}
          </el-avatar>
          <div class="avatar-actions">
            <el-button type="primary" text @click="showAvatarDialog = true">
              更换头像
            </el-button>
          </div>
        </div>
        <div class="user-basic">
          <h2>{{ userInfo.nickname || userInfo.username }}</h2>
          <p class="user-role">{{ userInfo.role === 'admin' ? '管理员' : '普通用户' }}</p>
        </div>
      </div>
    </div>

    <el-card class="profile-card">
      <el-tabs v-model="activeTab" class="profile-tabs">
        <el-tab-pane label="基本信息" name="basic">
          <el-form :model="userForm" label-width="100px" class="basic-form">
            <el-form-item label="用户名">
              <el-input v-model="userForm.username" disabled />
            </el-form-item>
            <el-form-item label="昵称">
              <el-input v-model="userForm.nickname" />
            </el-form-item>
            <el-form-item label="邮箱">
              <el-input v-model="userForm.email" type="email" />
            </el-form-item>
            <el-form-item label="手机号">
              <el-input v-model="userForm.phone" />
            </el-form-item>
            <el-form-item label="注册时间">
              <el-input v-model="formattedRegTime" disabled />
            </el-form-item>
            <el-form-item label="最后登录">
              <el-input v-model="formattedLastLogin" disabled />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="saveBasicInfo">保存修改</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
        
        <el-tab-pane label="安全设置" name="security">
          <el-form :model="passwordForm" :rules="passwordRules" ref="passwordFormRef" label-width="100px" class="password-form">
            <el-form-item label="原密码" prop="oldPassword">
              <el-input v-model="passwordForm.oldPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="新密码" prop="newPassword">
              <el-input v-model="passwordForm.newPassword" type="password" show-password />
            </el-form-item>
            <el-form-item label="确认密码" prop="confirmPassword">
              <el-input v-model="passwordForm.confirmPassword" type="password" show-password />
            </el-form-item>
            <el-form-item>
              <el-button type="primary" @click="handleChangePassword">修改密码</el-button>
            </el-form-item>
          </el-form>
        </el-tab-pane>
      </el-tabs>
    </el-card>

    <el-dialog 
      v-model="showAvatarDialog" 
      title="更换头像" 
      width="500px"
      center
    >
      <div class="avatar-dialog">
        <div class="avatar-options">
          <div 
            v-for="(avatar, index) in defaultAvatars" 
            :key="index"
            class="avatar-option"
            :class="{ active: tempAvatar === avatar }"
            @click="selectAvatar(avatar)"
          >
            <el-avatar :size="60" :src="avatar" />
          </div>
        </div>
        
        <div class="upload-section">
          <el-upload
            class="avatar-uploader"
            action="#"
            :show-file-list="false"
            :before-upload="beforeAvatarUpload"
            :http-request="handleAvatarUpload"
          >
            <el-button type="primary" plain>
              <el-icon><Upload /></el-icon>
              上传自定义头像
            </el-button>
          </el-upload>
        </div>
      </div>
      
      <template #footer>
        <el-button @click="showAvatarDialog = false">取消</el-button>
        <el-button type="primary" @click="saveAvatar">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted } from 'vue'
import { useUserStore } from '@/stores/user'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { updateUserInfo, changePassword } from '@/api/user'
import { uploadFile } from '@/api/file' // 导入文件上传API
import { format, isValid } from 'date-fns'
import { Upload } from '@element-plus/icons-vue'

const userStore = useUserStore()
const router = useRouter()
const activeTab = ref('basic')
const defaultAvatar = 'https://cube.elemecdn.com/3/7c/3ea6beec6438fd2643bcef8fd2c3jpeg.jpeg'
const passwordFormRef = ref(null)
const showAvatarDialog = ref(false)
const tempAvatar = ref('')

const defaultAvatars = [
  'https://cube.elemecdn.com/3/7c/3ea6beec6438fd2643bcef8fd2c3jpeg.jpeg',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka',
  'https://api.dicebear.com/7.x/bottts/svg?seed=Pepper'
]

const userInfo = ref({})
const userForm = reactive({
  username: '',
  nickname: '',
  email: '',
  phone: ''
})

const passwordForm = reactive({
  oldPassword: '',
  newPassword: '',
  confirmPassword: ''
})

const passwordRules = {
  oldPassword: [{ required: true, message: '请输入原密码', trigger: 'blur' }],
  newPassword: [
    { required: true, message: '请输入新密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, message: '请确认密码', trigger: 'blur' },
    { 
      validator: (rule, value, callback) => {
        if (value !== passwordForm.newPassword) {
          callback(new Error('两次输入的密码不一致'))
        } else {
          callback()
        }
      },
      trigger: 'blur'
    }
  ]
}

const formattedRegTime = computed(() => {
  if (!userInfo.value.createTime) return '未知'
  const date = new Date(userInfo.value.createTime)
  return isValid(date) ? format(date, 'yyyy-MM-dd HH:mm:ss') : '未知'
})

const formattedLastLogin = computed(() => {
  if (!userInfo.value.lastLogin) return '从未登录'
  const date = new Date(userInfo.value.lastLogin)
  return isValid(date) ? format(date, 'yyyy-MM-dd HH:mm:ss') : '未知'
})

const selectAvatar = (avatar) => {
  tempAvatar.value = avatar
}

const beforeAvatarUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt5M = file.size / 1024 / 1024 < 5

  if (!isJPGOrPNG) {
    ElMessage.error('头像必须是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt5M) {
    ElMessage.error('头像大小不能超过 5MB!')
    return false
  }
  return true
}

// ✨✨✨ 真实上传头像 ✨✨✨
const handleAvatarUpload = async (options) => {
  const file = options.file
  const loadingMsg = ElMessage.info({ message: '正在上传头像...', duration: 0 })
  
  try {
    const res = await uploadFile(file)
    loadingMsg.close()
    
    if (res.code === 200) {
      // 假设后端返回文件 ID，我们拼接下载链接作为头像 URL
      // 注意：这里假设后端地址是 localhost:8080，如果部署到线上需要改
      const avatarUrl = `http://localhost:8080/api/file/download/${res.data.id}`
      tempAvatar.value = avatarUrl
      ElMessage.success('头像上传成功，点击确定保存')
    } else {
      ElMessage.error('上传失败: ' + res.message)
    }
  } catch (e) {
    loadingMsg.close()
    ElMessage.error('上传异常，请检查网络')
  }
}

const saveAvatar = async () => {
  if (!tempAvatar.value) {
    ElMessage.warning('请选择或上传头像')
    return
  }

  try {
    const res = await updateUserInfo({
      avatar: tempAvatar.value
    })

    if (res.code === 200) {
      if (userStore.userInfo) {
        userStore.userInfo.avatar = tempAvatar.value
        localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
      }
      userInfo.value.avatar = tempAvatar.value
      ElMessage.success('头像更新成功')
      showAvatarDialog.value = false
      tempAvatar.value = ''
    } else {
      ElMessage.error(res.message || '保存失败')
    }
  } catch (error) {
    ElMessage.error('头像更新失败')
  }
}

onMounted(async () => {
  if (userStore.userInfo) {
    userInfo.value = { ...userStore.userInfo }
    userForm.username = userInfo.value.username || ''
    userForm.nickname = userInfo.value.nickname || ''
    userForm.email = userInfo.value.email || ''
    userForm.phone = userInfo.value.phone || ''
    tempAvatar.value = userInfo.value.avatar || ''
  }
})

const saveBasicInfo = async () => {
  try {
    const res = await updateUserInfo({
      nickname: userForm.nickname,
      email: userForm.email,
      phone: userForm.phone
    })
    
    if (res.code === 200) {
      if (userStore.userInfo) {
        Object.assign(userStore.userInfo, {
          nickname: userForm.nickname,
          email: userForm.email,
          phone: userForm.phone
        })
        localStorage.setItem('userInfo', JSON.stringify(userStore.userInfo))
      }
      userInfo.value = { ...userStore.userInfo }
      ElMessage.success('信息更新成功')
    } else {
      ElMessage.error(res.message || '更新失败')
    }
  } catch (error) {
    ElMessage.error('更新用户信息失败')
  }
}

const handleChangePassword = async () => {
  if (!passwordFormRef.value) return
  try {
    await passwordFormRef.value.validate()
    const res = await changePassword({
      oldPassword: passwordForm.oldPassword,
      newPassword: passwordForm.newPassword
    })
    if (res.code === 200) {
      ElMessage.success('密码修改成功，请重新登录')
      passwordForm.oldPassword = ''
      passwordForm.newPassword = ''
      passwordForm.confirmPassword = ''
      setTimeout(() => {
        userStore.logout()
        router.push('/login')
      }, 1000)
    } else {
      ElMessage.error(res.message || '修改失败')
    }
  } catch (error) {
    if (error.name !== 'Error') ElMessage.error('表单验证失败')
  }
}
</script>

<style scoped>
.profile-container { padding: 24px; max-width: 800px; margin: 0 auto; }
.profile-header { margin-bottom: 24px; padding: 20px; background: #fff; border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); }
.avatar-section { display: flex; align-items: center; gap: 20px; }
.avatar-upload { text-align: center; margin-right: 20px; }
.user-avatar { border: 4px solid #f0f7ff; margin-bottom: 10px; }
.avatar-actions { display: flex; justify-content: center; }
.user-basic { flex: 1; }
.user-role { color: #8c8c8c; margin: 5px 0 0 0; }
.profile-card { border-radius: 12px; box-shadow: 0 2px 8px rgba(0, 0, 0, 0.08); border: none; }
.profile-tabs { padding: 20px; }
.basic-form, .password-form { margin-top: 20px; }
.avatar-dialog { padding: 10px 0; }
.avatar-options { display: grid; grid-template-columns: repeat(4, 1fr); gap: 15px; margin-bottom: 20px; }
.avatar-option { cursor: pointer; padding: 5px; border-radius: 8px; transition: all 0.3s ease; display: flex; justify-content: center; }
.avatar-option:hover { background-color: #f5f5f5; }
.avatar-option.active { background-color: #e6f7ff; border: 2px solid #1890ff; }
.upload-section { text-align: center; padding: 20px 0; border-top: 1px solid #f0f0f0; }
.avatar-uploader { display: inline-block; }
@media (max-width: 768px) {
  .avatar-section { flex-direction: column; text-align: center; }
  .avatar-upload { margin-right: 0; margin-bottom: 20px; }
  .avatar-options { grid-template-columns: repeat(2, 1fr); }
}
</style>