import axios from 'axios'
import { ElMessage } from 'element-plus'
import { useUserStore } from '@/stores/user'

const request = axios.create({
  // 指向你本地后端
  baseURL: 'http://localhost:8080/api', 
  timeout: 10000
})

// 请求拦截器
request.interceptors.request.use(config => {
  const userStore = useUserStore()
  if (userStore.token) {
    config.headers.token = userStore.token
  }
  return config
})

// 响应拦截器
request.interceptors.response.use(
  response => {
    // ✨✨✨ 核心修复：如果是文件流 (blob)，直接返回，不检查 code ✨✨✨
    if (response.config.responseType === 'blob' || response.data instanceof Blob) {
      return response.data
    }

    const res = response.data
    if (res.code !== 200) {
      if (!response.config.skipError) {
        ElMessage.error(res.message || '请求失败')
      }
      return Promise.reject(new Error(res.message || 'Error'))
    }
    return res
  },
  error => {
    console.error('API Error:', error)
    
    if (error.config && error.config.skipError) {
      return Promise.reject(error)
    }

    let msg = '请求失败'
    if (error.response) {
      if (error.response.status === 403) msg = '权限不足'
      if (error.response.status === 401) msg = '未登录或 Token 过期'
      if (error.response.status === 404) msg = '资源未找到'
    }
    ElMessage.error(msg)
    return Promise.reject(error)
  }
)

export default request