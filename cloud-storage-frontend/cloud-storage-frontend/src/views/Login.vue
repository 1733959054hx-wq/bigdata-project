<template>
  <div class="login-container">
    <div class="bg-image"></div>
    <div class="bg-overlay"></div>
    
    <div class="login-wrapper">
      <div class="brand-section">
        <div class="brand-content">
          <div class="brand-top">
            <div class="logo-container">
              <el-icon class="logo-icon"><Cloudy /></el-icon>
            </div>
            <h1 class="brand-title">Hadoop生态<br>协同平台</h1>
          </div>
          
          <div class="brand-middle">
            <div class="feature-list">
              <div class="feature-item">
                <span class="feature-icon">⚡</span>
                <div class="feature-text">
                  <h3>极速传输</h3>
                  <p>基于HDFS的分布式存储引擎</p>
                </div>
              </div>
              <div class="feature-item">
                <span class="feature-icon">🛡️</span>
                <div class="feature-text">
                  <h3>企业级安全</h3>
                  <p>多重加密与权限隔离机制</p>
                </div>
              </div>
              <div class="feature-item">
                <span class="feature-icon">👥</span>
                <div class="feature-text">
                  <h3>实时协作</h3>
                  <p>Yjs驱动的文档协同编辑</p>
                </div>
              </div>
            </div>
          </div>

          <div class="brand-footer">
            <span>© 2024 BigData GZU Inc. All Rights Reserved.</span>
          </div>
        </div>
        
        <div class="glow-effect"></div>
      </div>

      <div class="form-section">
        <div class="form-container">
          <div class="form-header">
            <h2>欢迎回来</h2>
            <p class="sub-text">请登录您的账户以访问工作台</p>
          </div>

          <el-tabs v-model="activeTab" class="login-tabs" stretch>
            <el-tab-pane label="账号密码" name="password">
              <el-form ref="passwordLoginFormRef" :model="passwordLoginForm" :rules="rules" class="login-form" @submit.prevent="handleLogin" size="large">
                <el-form-item prop="username">
                  <el-input 
                    v-model="passwordLoginForm.username" 
                    placeholder="请输入账号" 
                    prefix-icon="User" 
                    class="premium-input"
                  />
                </el-form-item>
                <el-form-item prop="password">
                  <el-input 
                    v-model="passwordLoginForm.password" 
                    type="password" 
                    placeholder="请输入密码" 
                    prefix-icon="Lock" 
                    show-password 
                    class="premium-input"
                  />
                </el-form-item>
                
                <el-form-item prop="captchaInput">
                  <div class="captcha-row">
                    <el-input 
                      v-model="passwordLoginForm.captchaInput" 
                      placeholder="验证码" 
                      prefix-icon="MagicStick" 
                      class="premium-input captcha-input" 
                    />
                    <div class="captcha-box">
                      <CaptchaGenerator ref="passwordCaptchaRef" :width="120" :height="46" />
                    </div>
                  </div>
                </el-form-item>
                
                <div class="form-options">
                  <el-checkbox>7天内自动登录</el-checkbox>
                  <el-button type="primary" link class="forgot-btn">忘记密码?</el-button>
                </div>

                <el-button type="primary" :loading="loading" class="submit-btn" @click="handleLogin">
                  {{ loading ? '正在登录...' : '立即登录' }}
                </el-button>
              </el-form>
            </el-tab-pane>

            <el-tab-pane label="短信验证" name="sms">
              <el-form ref="smsLoginFormRef" :model="smsLoginForm" :rules="smsRules" class="login-form" size="large">
                <el-form-item prop="phone">
                  <el-input 
                    v-model="smsLoginForm.phone" 
                    placeholder="请输入手机号" 
                    prefix-icon="Iphone" 
                    class="premium-input"
                  />
                </el-form-item>
                
                <el-form-item prop="captchaInput">
                  <div class="captcha-row">
                    <el-input 
                      v-model="smsLoginForm.captchaInput" 
                      placeholder="图形验证码" 
                      prefix-icon="MagicStick" 
                      class="premium-input captcha-input" 
                    />
                    <div class="captcha-box">
                      <CaptchaGenerator ref="smsCaptchaRef" :width="120" :height="46" />
                    </div>
                  </div>
                </el-form-item>

                <el-form-item prop="smsCode">
                  <div class="sms-row">
                    <el-input 
                      v-model="smsLoginForm.smsCode" 
                      placeholder="短信验证码" 
                      prefix-icon="Message" 
                      class="premium-input sms-input"
                    />
                    <el-button class="code-btn" @click="getSmsCode" :disabled="codeLoading" plain>
                      {{ codeText }}
                    </el-button>
                  </div>
                </el-form-item>

                <el-button type="primary" class="submit-btn" @click="handleSmsLogin" :loading="loading">
                  登录 / 注册
                </el-button>
              </el-form>
            </el-tab-pane>
          </el-tabs>

          <div class="other-login-wrapper">
            <div class="divider">
              <span>其他方式登录</span>
            </div>
            
            <div class="social-login-classic">
              <div class="social-btn wechat" @click="handleWechatLogin">
                <el-icon class="social-icon"><ChatDotRound /></el-icon> 
                <span>微信登录</span>
              </div>
              <div class="social-btn qq" @click="handleQQLogin">
                <el-icon class="social-icon"><Promotion /></el-icon> 
                <span>QQ登录</span>
              </div>
            </div>
          </div>

          <div class="footer-register">
            还没有账号？ <router-link to="/register" class="register-link">免费注册</router-link>
          </div>
        </div>
      </div>
    </div>
    
    <el-dialog v-model="wechatDialogVisible" title="微信扫码登录" width="360px" center :show-close="false" class="qr-dialog">
      <div class="qrcode-container"><div class="qr-border"><img src="@/assets/wechat.png" class="qrcode-img" /></div></div>
      <div class="qrcode-tips">请使用微信扫一扫登录</div>
    </el-dialog>
    <el-dialog v-model="qqDialogVisible" title="QQ扫码登录" width="360px" center :show-close="false" class="qr-dialog">
      <div class="qrcode-container"><div class="qr-border"><img src="@/assets/qq.png" class="qrcode-img" /></div></div>
      <div class="qrcode-tips">请使用手机QQ扫一扫登录</div>
    </el-dialog>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted, watch } from 'vue'
import { useRouter } from 'vue-router'
import { ElMessage } from 'element-plus'
import { User, Lock, Cloudy, Iphone, Message, ChatDotRound, Promotion, MagicStick } from '@element-plus/icons-vue'
import { useUserStore } from '@/stores/user'
import CaptchaGenerator from '@/components/CaptchaGenerator.vue' 

const router = useRouter()
const userStore = useUserStore()

const passwordLoginFormRef = ref(null)
const smsLoginFormRef = ref(null)
const passwordCaptchaRef = ref(null)
const smsCaptchaRef = ref(null)

const loading = ref(false)
const activeTab = ref('password')
const wechatDialogVisible = ref(false)
const qqDialogVisible = ref(false)

const codeText = ref('获取验证码')
const codeLoading = ref(false)
let codeTimer = null; 

const passwordLoginForm = reactive({ username: '', password: '', captchaInput: '' })
const smsLoginForm = reactive({ phone: '', captchaInput: '', smsCode: '' }) 

const validateCaptcha = (rule, value, callback) => {
    let currentCaptchaRef = activeTab.value === 'password' ? passwordCaptchaRef.value : smsCaptchaRef.value;
    if (!value) {
        callback(new Error('请输入验证码'));
    } else if (value.toLowerCase() !== currentCaptchaRef.getCaptchaCode().toLowerCase()) {
        currentCaptchaRef.refreshCaptcha(); 
        callback(new Error('验证码错误'));
    } else {
        callback();
    }
};

const rules = {
  username: [{ required: true, message: '请输入账号', trigger: 'blur' }],
  password: [{ required: true, message: '请输入密码', trigger: 'blur' }],
  captchaInput: [{ required: true, validator: validateCaptcha, trigger: 'blur' }]
}

const smsRules = {
  phone: [
    { required: true, message: '请输入手机号', trigger: 'blur' },
    { pattern: /^1[3-9]\d{9}$/, message: '格式不正确', trigger: 'blur' }
  ],
  captchaInput: [{ required: true, validator: validateCaptcha, trigger: 'blur' }],
  smsCode: [{ required: true, message: '请输入验证码', trigger: 'blur' }]
}

const handleLogin = async () => {
  if (!passwordLoginFormRef.value) return
  await passwordLoginFormRef.value.validate(async (valid) => {
    if (valid) {
      passwordCaptchaRef.value.refreshCaptcha(); 
      performLogin({
        username: passwordLoginForm.username,
        password: passwordLoginForm.password,
        loginType: 'password'
      })
    }
  })
}

const getSmsCode = async () => {
  const valid = await smsLoginFormRef.value.validateField(['phone', 'captchaInput']).catch(() => false);
  if (!valid) return;
  if (codeLoading.value) return; 

  codeLoading.value = true;
  ElMessage.success('验证码：123456'); 
  smsLoginForm.smsCode = ''; 

  let count = 60;
  codeText.value = `${count}s`;
  codeTimer = setInterval(() => {
    count--;
    codeText.value = `${count}s`;
    if (count <= 0) {
      clearInterval(codeTimer);
      codeText.value = '获取验证码';
      codeLoading.value = false;
      smsCaptchaRef.value.refreshCaptcha(); 
    }
  }, 1000);
};

const handleSmsLogin = async () => {
  if (!smsLoginFormRef.value) return
  await smsLoginFormRef.value.validate(async (valid) => {
    if (valid) {
      smsCaptchaRef.value.refreshCaptcha();
      performLogin({ 
        phone: smsLoginForm.phone, 
        smsCode: smsLoginForm.smsCode, 
        loginType: 'sms'
      })
    }
  })
}

const handleWechatLogin = () => {
  wechatDialogVisible.value = true
  setTimeout(() => {
    wechatDialogVisible.value = false
    ElMessage.success('微信授权成功')
    performLogin({ username: 'wechat_vip', loginType: 'wechat' })
  }, 1500)
}

const handleQQLogin = () => {
  qqDialogVisible.value = true
  setTimeout(() => {
    qqDialogVisible.value = false
    ElMessage.success('QQ授权成功')
    performLogin({ username: 'qq_vip', loginType: 'qq' })
  }, 1500)
}

const performLogin = async (payload) => {
  loading.value = true
  try {
    await userStore.login(payload)
    ElMessage.success('登录成功')
    if (userStore.isSuperAdmin) router.push('/admin')
    else if (userStore.isAdmin) router.push('/admin-normal')
    else router.push('/files')
  } catch (error) {
    if (activeTab.value === 'password') passwordCaptchaRef.value?.refreshCaptcha();
    else smsCaptchaRef.value?.refreshCaptcha();
  } finally {
    loading.value = false
  }
}

watch(activeTab, () => {
  if (activeTab.value === 'password') passwordCaptchaRef.value?.refreshCaptcha();
  else smsCaptchaRef.value?.refreshCaptcha();
  passwordLoginFormRef.value?.clearValidate();
  smsLoginFormRef.value?.clearValidate();
});

onMounted(() => {
    if (passwordCaptchaRef.value) passwordCaptchaRef.value.refreshCaptcha();
})
</script>

<style scoped>
/* 容器：使用高质量灰白调科技图 */
.login-container { 
  min-height: 100vh;
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  position: relative;
  /* ✨✨✨ 更换为明亮、高级的灰白几何/建筑背景 ✨✨✨ */
  background-image: url('/2.jpg');
  background-size: cover;
  background-position: center;
  font-family: 'Inter', system-ui, -apple-system, sans-serif;
}

.bg-overlay {
  position: absolute;
  top: 0; left: 0; width: 100%; height: 100%;
  /* ✨✨✨ 使用亮白色半透明遮罩，营造通透感 ✨✨✨ */
  background: rgba(255, 255, 255, 0.4); 
  backdrop-filter: blur(8px); /* 增强磨砂感 */
  z-index: 0;
}

/* --- 登录主卡片 --- */
.login-wrapper {
  position: relative;
  z-index: 1;
  width: 1100px;  /* 宽敞布局 */
  height: 720px;  /* 高度充足 */
  background: #ffffff;
  border-radius: 24px;
  /* 更柔和、高级的投影 */
  box-shadow: 0 30px 60px -10px rgba(0, 0, 0, 0.1), 0 10px 20px -5px rgba(0, 0, 0, 0.04);
  display: flex;
  overflow: hidden;
  border: 1px solid rgba(255, 255, 255, 0.8); /* 增加边框质感 */
}

/* --- 左侧品牌区 --- */
.brand-section {
  flex: 0.9;
  /* 保持深海蓝渐变，对比度高，显高级 */
  background: linear-gradient(160deg, #2070d2 0%, #1451a8 100%);
  position: relative;
  padding: 60px;
  color: #fff;
  overflow: hidden;
}

.brand-content {
  position: relative;
  z-index: 2;
  height: 100%;
  display: flex;
  flex-direction: column;
  justify-content: space-between; 
}

/* 顶部 */
.brand-top { }

.logo-container {
  width: 60px; height: 60px;
  background: rgba(255, 255, 255, 0.15);
  border-radius: 14px;
  display: flex; align-items: center; justify-content: center;
  margin-bottom: 24px;
  backdrop-filter: blur(4px);
  border: 1px solid rgba(255, 255, 255, 0.1);
}
.logo-icon { font-size: 32px; color: #fff; }

.brand-title {
  font-size: 36px; 
  font-weight: 700;
  line-height: 1.3;
  margin: 0;
  letter-spacing: 1px;
  text-shadow: 0 4px 8px rgba(0,0,0,0.1);
}

/* 中间 */
.brand-middle {
  flex: 1;
  display: flex;
  align-items: center; 
}

.feature-list {
  display: flex;
  flex-direction: column;
  gap: 36px; 
}

.feature-item {
  display: flex;
  align-items: flex-start;
  gap: 20px;
}

.feature-icon { 
  font-size: 22px; 
  background: rgba(255,255,255,0.1);
  width: 44px; height: 44px;
  border-radius: 50%;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  border: 1px solid rgba(255,255,255,0.1);
}

.feature-text h3 { 
  font-size: 18px; 
  margin: 0 0 6px 0; 
  font-weight: 600; 
  letter-spacing: 0.5px;
}

.feature-text p { 
  margin: 0; 
  font-size: 14px; 
  opacity: 0.85; 
  line-height: 1.6; 
}

/* 底部 */
.brand-footer {
  font-size: 13px; 
  opacity: 0.6; 
  border-top: 1px solid rgba(255,255,255,0.1);
  padding-top: 24px;
}

/* 装饰 */
.glow-effect {
  position: absolute;
  top: -50%; left: -50%;
  width: 200%; height: 200%;
  background: radial-gradient(circle, rgba(255,255,255,0.05) 0%, transparent 60%);
  pointer-events: none;
  z-index: 1;
}

/* --- 右侧表单区 --- */
.form-section {
  flex: 1.1;
  padding: 60px 80px; 
  display: flex;
  flex-direction: column;
  justify-content: center;
  background: #fff;
}

.form-container {
  width: 100%;
  max-width: 400px;
  margin: 0 auto;
}

.form-header { margin-bottom: 32px; }
.form-header h2 { font-size: 28px; color: #111827; margin: 0 0 8px; font-weight: 700; }
.sub-text { color: #6b7280; font-size: 14px; }

/* Tabs */
:deep(.el-tabs__nav-wrap::after) { height: 1px; background-color: #e5e7eb; }
:deep(.el-tabs__item) { font-size: 16px; color: #6b7280; padding: 12px 0; }
:deep(.el-tabs__item.is-active) { color: #305787; font-weight: 600; }
:deep(.el-tabs__active-bar) { background-color: #305787; height: 3px; }

.login-form { margin-top: 28px; }

/* 输入框 */
.premium-input :deep(.el-input__wrapper) {
  box-shadow: 0 0 0 1px #d1d5db;
  border-radius: 8px;
  background: #f9fafb;
  padding: 12px 15px; 
  transition: all 0.2s;
}
.premium-input :deep(.el-input__wrapper:hover) { box-shadow: 0 0 0 1px #438ee8; background: #fff; }
.premium-input :deep(.el-input__wrapper.is-focus) {
  box-shadow: 0 0 0 2px rgba(48, 87, 135, 0.2);
  border-color: #1670dc;
  background: #fff;
}
.premium-input :deep(.el-input__inner) { font-size: 15px; color: #1f2937; height: 24px; }
.premium-input :deep(.el-input__prefix) { font-size: 18px; color: #9ca3af; }

/* 验证码 */
.captcha-row, .sms-row { display: flex; gap: 16px; }
.code-btn {
  border: 1px solid #d1d5db;
  color: #4b5563;
  border-radius: 8px;
  padding: 0 20px;
  font-weight: 500;
  background: #fff;
  height: 50px; 
}
.code-btn:hover:not(.is-disabled) { border-color: #305787; color: #305787; background: #f0f5ff; }

/* 辅助选项 */
.form-options { display: flex; justify-content: space-between; align-items: center; margin: 16px 0 32px; }
.forgot-btn { color: #305787; font-size: 14px; }

/* 登录按钮 */
.submit-btn {
  width: 100%;
  height: 50px;
  font-size: 16px;
  font-weight: 600;
  border-radius: 8px;
  background: #3689ef;
  border: none;
  box-shadow: 0 4px 6px -1px rgba(48, 87, 135, 0.2);
  transition: all 0.2s;
  letter-spacing: 1px;
}
.submit-btn:hover {
  background: #2e7ee1;
  box-shadow: 0 10px 15px -3px rgba(48, 87, 135, 0.3);
  transform: translateY(-2px);
}

/* 社交登录 */
.divider { display: flex; align-items: center; color: #9ca3af; font-size: 13px; margin: 40px 0 24px; }
.divider::before, .divider::after { content: ''; flex: 1; height: 1px; background: #e5e7eb; }
.divider span { padding: 0 16px; }

.social-login-classic { display: flex; gap: 20px; }
.social-btn {
  flex: 1;
  height: 44px;
  border-radius: 8px;
  display: flex; align-items: center; justify-content: center; gap: 10px;
  cursor: pointer; font-size: 14px; font-weight: 500;
  border: 1px solid #e5e7eb;
  background: #fff; color: #374151;
  transition: all 0.2s;
}
.social-btn:hover { background: #f9fafb; border-color: #d1d5db; transform: translateY(-2px); box-shadow: 0 4px 6px rgba(0,0,0,0.05); }
.social-icon { font-size: 20px; }
.wechat .social-icon { color: #07c160; }
.qq .social-icon { color: #12b7f5; }

.footer-register { text-align: center; margin-top: 32px; font-size: 15px; color: #6b7280; }
.register-link { color: #305787; text-decoration: none; font-weight: 600; margin-left: 6px; }
.register-link:hover { text-decoration: underline; }

/* 扫码弹窗 */
.qr-dialog :deep(.el-dialog) { border-radius: 16px; }
.qrcode-container { display: flex; justify-content: center; margin: 24px 0; }
.qr-border { padding: 12px; border: 1px solid #f0f0f0; border-radius: 12px; box-shadow: 0 4px 12px rgba(0,0,0,0.05); }
.qrcode-img { width: 180px; height: 180px; display: block; }
.qrcode-tips { text-align: center; color: #666; font-size: 14px; margin-bottom: 24px; }

/* 响应式 */
@media (max-width: 1200px) {
  .login-wrapper { width: 95%; height: 700px; }
  .brand-section { padding: 40px; }
  .form-section { padding: 40px; }
}

@media (max-width: 900px) {
  .login-wrapper { width: 100%; height: 100vh; border-radius: 0; flex-direction: column; }
  .brand-section { display: none; }
  .form-section { padding: 40px 20px; }
  .form-container { max-width: 100%; }
}
</style>