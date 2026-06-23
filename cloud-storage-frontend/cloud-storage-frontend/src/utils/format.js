// src/utils/format.js

/**
 * 万能时间解析函数
 * 能处理：
 * 1. 时间戳 (1678888888000)
 * 2. ISO字符串 ("2023-11-25T10:00:00")
 * 3. Spring Boot 数组 ([2023, 11, 25, 10, 0, 0])
 */
export const parseDate = (val) => {
  if (!val) return null // 关键修复：没值就是没值，不要给当前时间
  
  // 处理 Spring Boot 数组格式 [yyyy, MM, dd, HH, mm, ss]
  if (Array.isArray(val)) {
    // JS Date 的月份是 0-11，所以 val[1] 需要减 1
    return new Date(val[0], val[1] - 1, val[2], val[3] || 0, val[4] || 0, val[5] || 0)
  }
  
  // 处理时间戳或字符串
  const date = new Date(val)
  // 检查是否是无效日期
  if (isNaN(date.getTime())) return null
  
  return date
}

/**
 * 格式化时间显示 (YYYY-MM-DD HH:mm)
 */
export const formatTime = (val) => {
  const date = parseDate(val)
  if (!date) return '-' // 如果无效，显示横杠
  
  return date.toLocaleString('zh-CN', {
    year: 'numeric', 
    month: '2-digit', 
    day: '2-digit',
    hour: '2-digit', 
    minute: '2-digit',
    hour12: false
  }).replace(/\//g, '-')
}

/**
 * 格式化文件大小 (自动转换 B, KB, MB, GB)
 */
export const formatFileSize = (bytes) => {
  if (bytes === null || bytes === undefined || bytes === 0) return '0 B'
  const k = 1024
  const sizes = ['B', 'KB', 'MB', 'GB', 'TB']
  const i = Math.floor(Math.log(bytes) / Math.log(k))
  return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
}