// 封装localStorage操作，方便设置管理
export const useStorage = () => {
  const getItem = (key, defaultValue = null) => {
    try {
      const value = localStorage.getItem(key)
      return value ? JSON.parse(value) : defaultValue
    } catch (error) {
      console.error(`获取存储${key}失败:`, error)
      return defaultValue
    }
  }

  const setItem = (key, value) => {
    try {
      localStorage.setItem(key, JSON.stringify(value))
    } catch (error) {
      console.error(`设置存储${key}失败:`, error)
    }
  }

  return { getItem, setItem }
}