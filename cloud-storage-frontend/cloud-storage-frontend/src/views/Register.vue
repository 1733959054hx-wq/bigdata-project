<template>
  <div class="register-container">
    <div class="register-box">
      <div class="register-header">
        <div class="logo">
          <el-icon :size="40" color="#409eff"><Cloudy /></el-icon>
        </div>
        <h2>创建新账号</h2>
        <p class="subtitle">加入我们，开启云端之旅</p>
      </div>

      <el-form
        ref="registerFormRef"
        :model="registerForm"
        :rules="registerRules"
        class="register-form"
        size="large"
      >
        <div class="form-avatar-section">
          <el-avatar :size="80" :src="registerForm.avatar" class="current-avatar">
            {{ registerForm.nickname?.[0] || registerForm.username?.[0] || 'user' }}
          </el-avatar>
          <el-button type="primary" link @click="openAvatarDialog">
            选择头像
          </el-button>
        </div>

        <el-form-item prop="username">
          <el-input 
            v-model="registerForm.username" 
            placeholder="用户名 (用于登录)"
            prefix-icon="User"
          />
        </el-form-item>

        <el-form-item prop="password">
          <el-input 
            v-model="registerForm.password" 
            type="password"
            placeholder="密码 (不少于6位)"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="confirmPassword">
          <el-input 
            v-model="registerForm.confirmPassword" 
            type="password"
            placeholder="确认密码"
            prefix-icon="Lock"
            show-password
          />
        </el-form-item>

        <el-form-item prop="nickname">
          <el-input 
            v-model="registerForm.nickname" 
            placeholder="昵称 (选填)"
            prefix-icon="Postcard"
          />
        </el-form-item>

        <el-form-item prop="phone">
          <el-input 
            v-model="registerForm.phone" 
            placeholder="手机号 (选填)"
            prefix-icon="Iphone"
          />
        </el-form-item>

        <el-form-item prop="email">
          <el-input 
            v-model="registerForm.email" 
            placeholder="邮箱 (选填)"
            prefix-icon="Message"
          />
        </el-form-item>

        <el-form-item class="agreement-item">
          <el-checkbox v-model="agreed">
            我已阅读并同意 <el-button link type="primary">服务条款</el-button> 和 <el-button link type="primary">隐私政策</el-button>
          </el-checkbox>
        </el-form-item>

        <el-button 
          type="primary" 
          class="register-btn" 
          :loading="loading"
          @click="handleRegister"
        >
          立即注册
        </el-button>

        <div class="login-link">
          已有账号？ <router-link to="/login">去登录</router-link>
        </div>
      </el-form>
    </div>

    <el-dialog 
      v-model="showAvatarDialog" 
      title="选择头像" 
      width="500px"
      center
      append-to-body
    >
      <div class="avatar-dialog-content">
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
        <el-button type="primary" @click="confirmAvatar">确定</el-button>
      </template>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Iphone, Postcard, Message, Cloudy, Upload } from '@element-plus/icons-vue'
import { userRegister } from '@/api/user'

const router = useRouter()
const registerFormRef = ref(null)
const loading = ref(false)
const agreed = ref(false)

// ✨✨✨ 头像相关状态 ✨✨✨
const showAvatarDialog = ref(false)
const tempAvatar = ref('')
// 默认头像列表 (与个人中心保持一致)
const defaultAvatars = [
  'https://cube.elemecdn.com/3/7c/3ea6beec6438fd2643bcef8fd2c3jpeg.jpeg',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Felix',
  'https://api.dicebear.com/7.x/avataaars/svg?seed=Aneka',
  'https://api.dicebear.com/7.x/bottts/svg?seed=Pepper'
]

const registerForm = reactive({
  username: '',
  password: '',
  confirmPassword: '',
  nickname: '',
  phone: '',
  email: '',
  avatar: defaultAvatars[0] // 默认选中第一个头像
})

// 初始化临时头像
onMounted(() => {
  tempAvatar.value = registerForm.avatar
})

const validatePass2 = (rule, value, callback) => {
  if (value === '') {
    callback(new Error('请再次输入密码'))
  } else if (value !== registerForm.password) {
    callback(new Error('两次输入密码不一致!'))
  } else {
    callback()
  }
}

const registerRules = {
  username: [
    { required: true, message: '请输入用户名', trigger: 'blur' },
    { min: 3, max: 20, message: '长度在 3 到 20 个字符', trigger: 'blur' }
  ],
  password: [
    { required: true, message: '请输入密码', trigger: 'blur' },
    { min: 6, message: '密码长度不能少于6位', trigger: 'blur' }
  ],
  confirmPassword: [
    { required: true, validator: validatePass2, trigger: 'blur' }
  ]
}

// ✨✨✨ 头像操作相关方法 (复制自 Profile.vue) ✨✨✨
const openAvatarDialog = () => {
  // 打开弹窗时，将临时头像重置为当前表单选中的头像
  tempAvatar.value = registerForm.avatar
  showAvatarDialog.value = true
}

const selectAvatar = (avatar) => {
  tempAvatar.value = avatar
}

const beforeAvatarUpload = (file) => {
  const isJPGOrPNG = file.type === 'image/jpeg' || file.type === 'image/png'
  const isLt2M = file.size / 1024 / 1024 < 2

  if (!isJPGOrPNG) {
    ElMessage.error('头像必须是 JPG 或 PNG 格式!')
    return false
  }
  if (!isLt2M) {
    ElMessage.error('头像大小不能超过 2MB!')
    return false
  }
  return true
}

const handleAvatarUpload = (options) => {
  const file = options.file
  const reader = new FileReader()
  reader.onload = (e) => {
    // 将读取到的 Base64 设置为临时头像
    tempAvatar.value = e.target.result
    ElMessage.success('图片读取成功，点击确定应用')
  }
  reader.readAsDataURL(file)
}

// 确认选择头像
const confirmAvatar = () => {
  if (tempAvatar.value) {
    registerForm.avatar = tempAvatar.value
    showAvatarDialog.value = false
    ElMessage.success('已选择新头像')
  }
}

// 注册提交
const handleRegister = async () => {
  if (!registerFormRef.value) return
  
  if (!agreed.value) {
    ElMessage.warning('请先同意服务条款和隐私政策')
    return
  }
  
  try {
    await registerFormRef.value.validate()
    loading.value = true
    
    // 调用注册接口，包含 avatar 字段
    const res = await userRegister({
      username: registerForm.username,
      password: registerForm.password,
      nickname: registerForm.nickname,
      phone: registerForm.phone,
      email: registerForm.email,
      avatar: registerForm.avatar // 提交选中的头像
    })
    
    if (res.code === 200) {
      ElMessage.success('注册成功，请登录')
      router.push('/login')
    } else {
      ElMessage.error(res.message || '注册失败')
    }
  } catch (error) {
    console.error('注册错误:', error)
    if (error.name !== 'Error') {
       ElMessage.error('请检查表单填写是否正确')
    }
  } finally {
    loading.value = false
  }
}
</script>

<style scoped>
.register-container {
  height: 100vh;
  display: flex;
  justify-content: center;
  align-items: center;
  background: linear-gradient(135deg, #f0f7ff 0%, #e6f7ff 100%);
  position: relative;
  overflow: hidden;
}

.register-container::before {
  content: '';
  position: absolute;
  width: 100%;
  height: 100%;
  background-image: url("data:image/svg+xml,%3Csvg width='60' height='60' viewBox='0 0 60 60' xmlns='http://www.w3.org/2000/svg'%3E%3Cg fill='none' fill-rule='evenodd'%3E%3Cg fill='%23409eff' fill-opacity='0.05'%3E%3Cpath d='M36 34v-4h-2v4h-4v2h4v4h2v-4h4v-2h-4zm0-30V0h-2v4h-4v2h4v4h2V6h4V4h-4zM6 34v-4H4v4H0v2h4v4h2v-4h4v-2H6zM6 4V0H4v4H0v2h4v4h2V6h4V4H6z'/%3E%3C/g%3E%3C/g%3E%3C/svg%3E");
}

.register-box {
  width: 440px;
  padding: 40px;
  background: rgba(255, 255, 255, 0.95);
  border-radius: 16px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.08);
  backdrop-filter: blur(10px);
  position: relative;
  z-index: 1;
}

.register-header {
  text-align: center;
  margin-bottom: 32px;
}

.logo {
  margin-bottom: 16px;
  display: inline-block;
  padding: 12px;
  background: #f0f7ff;
  border-radius: 12px;
}

.register-header h2 {
  margin: 0 0 8px;
  font-size: 24px;
  color: #303133;
  font-weight: 600;
}

.subtitle {
  margin: 0;
  color: #909399;
  font-size: 14px;
}

/* ✨✨✨ 头像选择区域样式 ✨✨✨ */
.form-avatar-section {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 24px;
}

.current-avatar {
  border: 4px solid #f0f7ff;
  box-shadow: 0 2px 8px rgba(0,0,0,0.05);
  margin-bottom: 8px;
}

.register-form .el-input {
  --el-input-hover-border-color: #409eff;
  --el-input-focus-border-color: #409eff;
}

:deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #dcdfe6 inset;
  padding: 8px 12px;
  background: #fcfcfc;
  border-radius: 8px;
}

:deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 1px #409eff inset !important;
  background: #fff;
}

.agreement-item {
  margin-bottom: 24px;
}

.register-btn {
  width: 100%;
  padding: 12px;
  font-size: 16px;
  font-weight: 500;
  border-radius: 8px;
  height: auto;
  background: linear-gradient(135deg, #409eff, #36cfc9);
  border: none;
  transition: all 0.3s;
}

.register-btn:hover:not(.is-loading) {
  transform: translateY(-1px);
  box-shadow: 0 4px 12px rgba(64, 158, 255, 0.3);
  background: linear-gradient(135deg, #66b1ff, #5cdbd3);
}

.login-link {
  text-align: center;
  margin-top: 16px;
  color: #606266;
  font-size: 14px;
}

.login-link a {
  color: #409eff;
  text-decoration: none;
  font-weight: 500;
  margin-left: 4px;
}

.login-link a:hover {
  text-decoration: underline;
}

/* ✨✨✨ 头像弹窗样式 (与 Profile.vue 一致) ✨✨✨ */
.avatar-dialog-content {
  padding: 10px 0;
}

.avatar-options {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 15px;
  margin-bottom: 20px;
  justify-items: center;
}

.avatar-option {
  cursor: pointer;
  padding: 5px;
  border-radius: 8px;
  transition: all 0.3s ease;
  display: flex;
  justify-content: center;
  align-items: center;
  border: 2px solid transparent;
}

.avatar-option:hover {
  background-color: #f5f5f5;
}

.avatar-option.active {
  background-color: #e6f7ff;
  border-color: #1890ff;
}

.upload-section {
  text-align: center;
  padding: 20px 0;
  border-top: 1px solid #f0f0f0;
}

.avatar-uploader {
  display: inline-block;
}
</style>