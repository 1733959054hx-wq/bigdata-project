// src/stores/fileStore.js
import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'

export const useFileStore = defineStore('file', () => {
  // 1. 定义默认模拟数据
  const now = Date.now()
  const day = 24 * 60 * 60 * 1000

  const defaultMockFiles = [
    {
      id: 1,
      name: '项目报告.pdf',
      size: 1024000,
      updateTime: now,
      type: 'document',
      deleted: false,
      deleteTime: null,
      originalPath: '/我的文件'
    },
    {
      id: 2,
      name: '产品截图.png',
      size: 1590000,
      updateTime: now - day,
      type: 'image',
      deleted: false,
      deleteTime: null,
      originalPath: '/我的文件'
    },
    {
      id: 3,
      name: '新建文件夹',
      size: 0, // 文件夹初始占位大小
      updateTime: now,
      type: 'folder',
      deleted: false,
      deleteTime: null,
      originalPath: '/我的文件',
      isFolder: true
    }
  ]

  // 2. 初始化状态
  const STORAGE_KEY = 'cloud_storage_files_v3' // 升级版本号以重置缓存
  
  const initFiles = () => {
    const saved = localStorage.getItem(STORAGE_KEY)
    if (saved) {
      try {
        return JSON.parse(saved)
      } catch (e) {
        console.error('读取缓存失败', e)
      }
    }
    return defaultMockFiles
  }

  const allFiles = ref(initFiles())

  // 3. 监听变化并自动保存
  watch(allFiles, (newVal) => {
    localStorage.setItem(STORAGE_KEY, JSON.stringify(newVal))
  }, { deep: true })

  const fileUrlMap = ref(new Map())

  const fileList = computed(() => {
    return allFiles.value.filter(file => !file.deleted)
  })

  const recycleFiles = computed(() => {
    return allFiles.value.filter(file => file.deleted).map(file => ({
      ...file,
      remainingDays: calculateRemainingDays(file.deleteTime)
    }))
  })

  const calculateRemainingDays = (deleteTime) => {
    if (!deleteTime) return 30
    const daysSinceDelete = Math.floor((Date.now() - deleteTime) / (24 * 60 * 60 * 1000))
    return Math.max(0, 30 - daysSinceDelete)
  }

  const isFileNameExists = (fileName, excludeFileId = null) => {
    return allFiles.value.some(
      file => file.name === fileName && 
              file.id !== excludeFileId && 
              !file.deleted
    )
  }

  // ✨✨✨ 核心修复：精确计算文件夹大小 ✨✨✨
  const getFolderSize = (folder) => {
    if (!folder || (!folder.isFolder && folder.type !== 'folder')) return folder.size || 0
    
    // 1. 获取该文件夹的完整绝对路径
    let parentPath = folder.originalPath
    // 归一化处理：如果父路径是 '/'，统一视为 '/我的文件'
    if (parentPath === '/') parentPath = '/我的文件'
    
    const folderFullPath = `${parentPath}/${folder.name}`
    
    // 2. 遍历所有文件进行累加
    return allFiles.value.reduce((total, file) => {
      // 排除已删除的文件和文件夹自身
      if (file.deleted || file.id === folder.id) return total
      
      // 排除其他文件夹（通常只计算文件大小，如果要包含子文件夹大小，需递归，这里简化为只算文件）
      if (file.isFolder || file.type === 'folder') return total

      const filePath = file.originalPath || ''
      
      // ✨ 关键修复：严格匹配路径 ✨
      // 条件1: 文件直接在这个文件夹里 (originalPath === folderFullPath)
      // 条件2: 文件在子文件夹里 (originalPath 以 folderFullPath + '/' 开头)
      // 这样避免了 "Test" 匹配到 "Test2" 的问题
      const isChild = filePath === folderFullPath || filePath.startsWith(folderFullPath + '/')
      
      if (isChild) {
        return total + (file.size || 0)
      }
      
      return total
    }, 0)
  }



  // 上传文件
  const uploadFile = (file) => {
    const newFile = {
      id: Date.now(),
      name: file.name,
      size: file.size,
      updateTime: Date.now(),
      type: getFileType(file.name),
      deleted: false,
      deleteTime: null,
      originalPath: '/我的文件',
      realFile: file
    }
    allFiles.value.unshift(newFile)
    
    if (file) {
      const fileUrl = URL.createObjectURL(file)
      fileUrlMap.value.set(newFile.id, fileUrl)
    }
    
    return newFile
  }

  const createFolder = (folderName) => {
    if (isFileNameExists(folderName)) {
      throw new Error(`文件夹 "${folderName}" 已存在`)
    }

    const newFolder = {
      id: Date.now(),
      name: folderName,
      size: 0,
      updateTime: Date.now(),
      type: 'folder',
      deleted: false,
      deleteTime: null,
      originalPath: '/我的文件',
      isFolder: true
    }
    allFiles.value.unshift(newFolder)
    return newFolder
  }

  const createFile = (fileName, content = '') => {
    const newFile = {
      id: Date.now(),
      name: fileName,
      size: new Blob([content]).size,
      updateTime: Date.now(),
      type: getFileType(fileName),
      deleted: false,
      deleteTime: null,
      originalPath: '/我的文件',
      content: content
    }
    allFiles.value.unshift(newFile)
    return newFile
  }

  const addExternalFile = (fileData) => {
    if (allFiles.value.some(file => file.id === fileData.id)) return false
    
    allFiles.value.unshift({
      ...fileData,
      deleted: false,
      deleteTime: null
    })
    return true
  }

  const updateFileContent = (fileId, newContent) => {
    const fileIndex = allFiles.value.findIndex(file => file.id === fileId)
    if (fileIndex !== -1) {
      const file = allFiles.value[fileIndex]
      allFiles.value[fileIndex] = {
        ...file,
        content: newContent,
        size: new Blob([newContent]).size,
        updateTime: Date.now()
      }
      return true
    }
    return false
  }

  const renameFile = (fileId, newName) => {
    const fileIndex = allFiles.value.findIndex(file => file.id === fileId)
    if (fileIndex !== -1) {
      const oldPath = allFiles.value[fileIndex].originalPath + '/' + allFiles.value[fileIndex].name
      const newPath = allFiles.value[fileIndex].originalPath + '/' + newName
      
      // 如果是文件夹，还需要更新它里面所有文件的路径
      if (isFolder(allFiles.value[fileIndex])) {
        allFiles.value.forEach(f => {
          if (f.originalPath && f.originalPath.startsWith(oldPath)) {
            f.originalPath = f.originalPath.replace(oldPath, newPath)
          }
        })
      }

      allFiles.value[fileIndex] = {
        ...allFiles.value[fileIndex],
        name: newName,
        updateTime: Date.now(),
        type: isFolder(allFiles.value[fileIndex]) ? 'folder' : getFileType(newName)
      }
      return true
    }
    return false
  }

  const deleteFile = (fileId) => {
    const fileIndex = allFiles.value.findIndex(file => file.id === fileId)
    if (fileIndex !== -1) {
      allFiles.value[fileIndex] = {
        ...allFiles.value[fileIndex],
        deleted: true,
        deleteTime: Date.now()
      }
    }
  }

  const restoreFile = (fileId) => {
    const fileIndex = allFiles.value.findIndex(file => file.id === fileId)
    if (fileIndex !== -1) {
      allFiles.value[fileIndex] = {
        ...allFiles.value[fileIndex],
        deleted: false,
        deleteTime: null
      }
    }
  }

  const permanentDelete = (fileId) => {
    const fileIndex = allFiles.value.findIndex(file => file.id === fileId)
    if (fileIndex !== -1) {
      if (fileUrlMap.value.has(fileId)) {
        URL.revokeObjectURL(fileUrlMap.value.get(fileId))
        fileUrlMap.value.delete(fileId)
      }
      allFiles.value.splice(fileIndex, 1)
    }
  }

  const emptyRecycleBin = () => {
    const deletedFiles = allFiles.value.filter(file => file.deleted)
    deletedFiles.forEach(file => {
      if (fileUrlMap.value.has(file.id)) {
        URL.revokeObjectURL(fileUrlMap.value.get(file.id))
        fileUrlMap.value.delete(file.id)
      }
    })
    allFiles.value = allFiles.value.filter(file => !file.deleted)
  }

// ✨✨✨ 新增：内存文件缓存 (不存 LocalStorage，刷新即焚，保证性能) ✨✨✨
  const fileCache = new Map()

  // 缓存真实文件流
  const cacheFile = (id, fileObj) => {
    fileCache.set(id, fileObj)
    console.log(`📦 [FileStore] 文件已缓存到内存: ${id}`)
  }

  // 获取缓存的文件
  const getCachedFile = (id) => {
    return fileCache.get(id)
  }

  // 判断是否有缓存
  const hasCache = (id) => {
    return fileCache.has(id)
  }

  const getFileType = (fileName) => {
    if (!fileName) return 'other'
    if (/\.(jpg|jpeg|png|gif|bmp|webp|svg)$/i.test(fileName)) return 'image'
    if (/\.(mp4|avi|mov|wmv|flv|mkv)$/i.test(fileName)) return 'video'
    if (/\.(pdf|doc|docx|txt|ppt|pptx|xls|xlsx)$/i.test(fileName)) return 'document'
    if (/\.(txt|md|js|jsx|ts|tsx|vue|css|scss|less|html|htm|xml|json|yml|yaml|ini|conf|log)$/i.test(fileName)) return 'text'
    return 'other'
  }

  const formatFileSize = (bytes) => {
    if (!bytes || bytes === 0) return '0 B'
    const k = 1024
    const sizes = ['B', 'KB', 'MB', 'GB']
    const i = Math.floor(Math.log(bytes) / Math.log(k))
    return parseFloat((bytes / Math.pow(k, i)).toFixed(2)) + ' ' + sizes[i]
  }

  const isImage = (fileName) => /\.(jpg|jpeg|png|gif|bmp|webp|svg)$/i.test(fileName)
  const isVideo = (fileName) => /\.(mp4|avi|mov|wmv|flv|mkv)$/i.test(fileName)
  const isDocument = (fileName) => /\.(pdf|doc|docx|txt|ppt|pptx|xls|xlsx)$/i.test(fileName)
  const isPdf = (fileName) => /\.pdf$/i.test(fileName)
  const isText = (fileName) => /\.(txt|md|js|jsx|ts|tsx|vue|css|scss|less|html|htm|xml|json|yml|yaml|ini|conf|log)$/i.test(fileName)
  const isOfficeDocument = (fileName) => /\.(doc|docx|xls|xlsx|ppt|pptx)$/i.test(fileName)
  const isFolder = (file) => file.isFolder || file.type === 'folder'
  const isPreviewable = (fileName, file) => {
    if (isFolder(file)) return false
    return isImage(fileName) || isDocument(fileName) || isText(fileName) || isVideo(fileName)
  }

  const getOrCreateFileUrl = async (file) => {
    if (fileUrlMap.value.has(file.id)) return fileUrlMap.value.get(file.id)
    if (file.realFile) {
      const url = URL.createObjectURL(file.realFile)
      fileUrlMap.value.set(file.id, url)
      return url
    }
    return ''
  }

  const getTextFileContent = async (file) => {
    if (file.realFile) {
      return await new Promise((resolve) => {
        const reader = new FileReader()
        reader.onload = e => resolve(e.target.result)
        reader.readAsText(file.realFile)
      })
    }
    return file.content || `模拟内容: ${file.name}`
  }

  return {
    allFiles,
    fileList,
    recycleFiles,
    uploadFile,
    createFolder,
    createFile,
    updateFileContent,
    renameFile,
    deleteFile,
    restoreFile,
    permanentDelete,
    emptyRecycleBin,
    getFileType,
    getOrCreateFileUrl,
    getTextFileContent,
    isImage,
    isVideo,
    isDocument,
    isPdf,
    isText,
    isOfficeDocument,
    isFolder,
    isPreviewable,
    formatFileSize,
    isFileNameExists,
    addExternalFile,
    getFolderSize,
    cacheFile,      // 导出新方法
    getCachedFile,  // 导出新方法
    hasCache,       // 导出新方法
    isImage, isVideo, isPdf, isText, formatFileSize // 工具函数
  }
})