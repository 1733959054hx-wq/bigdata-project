import request from './request'

const ROOT_PATH = '/BigData'

export const getFileList = (path = ROOT_PATH) => {
  return request({ url: '/directory/list', method: 'get', params: { path } })
}

export const createDirectory = (directoryName, parentId = null) => {
  return request({ url: '/directory/create', method: 'post', data: { directoryName, parentId } })
}

// ✨✨✨ 修复：修正URL路径（去掉重复的/api）并移除 Content-Type ✨✨✨
export const uploadFile = (file, parentId = 0) => {
  const formData = new FormData()
  formData.append('file', file)
  // 确保 parentId 转为字符串或数字
  formData.append('parentId', parentId || 0) 
  
  return request({
    url: '/file/upload', // ✅ 这里改成了 /file/upload，配合 baseURL 自动变成 /api/file/upload
    method: 'post',
    data: formData
    // ✅ 千万不要手动写 Content-Type，浏览器会自动处理
  })
}

export const downloadFile = (fileId) => {
  return request({
    url: `/file/download/${fileId}`,
    method: 'get',
    responseType: 'blob',
    skipError: true 
  })
}

export const renameFile = (id, newName) => {
  return request({ url: '/file/rename', method: 'put', data: { id: Number(id), newName: String(newName) } })
}

export const deleteFile = (fileId) => {
  return request({ url: `/trash/${fileId}`, method: 'delete' })
}

export const getAllFolders = () => {
  return request({ url: '/file/folders', method: 'get' })
}

export const moveFile = (fileId, targetParentId) => {
  return request({ url: '/file/move', method: 'post', data: { fileId, targetParentId } })
}

// 回收站接口
export const restoreFile = (fileId, targetParentId = null) => request({ url: `/trash/restore/${fileId}`, method: 'post', params: { targetParentId } })
export const emptyTrash = () => request({ url: '/trash/empty', method: 'delete' })
export const batchDelete = (fileIds) => request({ url: '/trash/batch', method: 'delete', data: fileIds })
export const getRecycleBinList = () => request({ url: '/trash/list', method: 'get' })