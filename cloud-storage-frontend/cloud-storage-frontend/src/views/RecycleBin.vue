<template>
  <div class="recycle-bin">
    <div class="operation-bar">
      <div class="operation-left">
        <el-button type="success" @click="handleBatchRestore" :disabled="selectedFiles.length === 0">
          <el-icon><RefreshLeft /></el-icon> 批量还原
        </el-button>
        <el-button type="danger" @click="handleBatchDelete" :disabled="selectedFiles.length === 0">
          <el-icon><Delete /></el-icon> 批量删除
        </el-button>
        <el-button type="danger" plain @click="handleEmpty" :disabled="recycleFiles.length === 0">
          <el-icon><DeleteFilled /></el-icon> 清空回收站
        </el-button>
      </div>
      <div class="operation-right">
        <el-input v-model="searchKeyword" placeholder="搜索回收站文件..." prefix-icon="Search" style="width: 240px;" clearable />
      </div>
    </div>

    <el-card class="file-list-card" shadow="never">
      <template #header>
        <div class="card-header">
          <span style="font-weight: bold; font-size: 16px;">回收站</span>
          <el-tag type="info" effect="plain" size="small">文件保留30天</el-tag>
        </div>
      </template>

      <el-table 
        :data="paginatedFiles" 
        v-loading="loading" 
        style="width: 100%" 
        @selection-change="handleSelectionChange"
        empty-text="回收站为空"
      >
        <el-table-column type="selection" width="50" />
        <el-table-column prop="name" label="文件名" min-width="300">
          <template #default="scope">
            <div class="file-name-cell">
              <el-icon class="file-icon" size="20" style="margin-right: 8px;">
                <Folder v-if="scope.row.isFolder" color="#e6a23c" />
                <Document v-else color="#909399" />
              </el-icon>
              <span class="file-name">{{ scope.row.name }}</span>
            </div>
          </template>
        </el-table-column>
        
        <el-table-column prop="size" label="大小" width="120" align="right">
          <template #default="scope">
            {{ scope.row.isFolder ? '-' : formatFileSize(scope.row.size) }}
          </template>
        </el-table-column>
        
        <el-table-column prop="deleteTime" label="删除时间" width="180">
          <template #default="scope">
            {{ formatTime(scope.row.deleteTime) }}
          </template>
        </el-table-column>

        <el-table-column label="剩余天数" width="120">
          <template #default="scope">
            <el-tag :type="getRemainingDays(scope.row) > 3 ? 'success' : 'danger'">
              {{ getRemainingDays(scope.row) }} 天
            </el-tag>
          </template>
        </el-table-column>
        
        <el-table-column label="操作" width="150" fixed="right">
          <template #default="scope">
            <el-button link type="primary" @click="handleRestore(scope.row)">还原</el-button>
            <el-button link type="danger" @click="handleDelete(scope.row)">删除</el-button>
          </template>
        </el-table-column>
      </el-table>

      <div class="pagination-container" v-if="recycleFiles.length > 0">
        <el-pagination
          v-model:current-page="currentPage"
          v-model:page-size="pageSize"
          :page-sizes="[10, 20, 50, 100]"
          :total="filteredData.length"
          layout="total, sizes, prev, pager, next, jumper"
        />
      </div>
    </el-card>
  </div>
</template>

<script setup>
import { ref, onMounted, computed } from 'vue'
import { ElMessage, ElMessageBox } from 'element-plus'
import { RefreshLeft, Delete, DeleteFilled, Search, Folder, Document } from '@element-plus/icons-vue'
import * as fileApi from '@/api/file'
// ✨ 引入通用工具
import { formatTime, formatFileSize, parseDate } from '@/utils/format'

const loading = ref(false)
const recycleFiles = ref([])
const selectedFiles = ref([])
const searchKeyword = ref('')
const currentPage = ref(1)
const pageSize = ref(10)

// 加载数据
const loadData = async () => {
  loading.value = true
  try {
    const res = await fileApi.getRecycleBinList()
    if (res.code === 200 && res.data) {
      recycleFiles.value = res.data.map(item => ({
        id: item.id,
        name: item.fileName,
        size: item.fileSize,
        isFolder: item.isFolder,
        // ✨ 使用万能解析器，防止 Spring Boot 数组格式报错
        deleteTime: parseDate(item.deleteTime) 
      }))
    } else {
      recycleFiles.value = []
    }
  } catch (e) {
    ElMessage.error('加载回收站失败')
  } finally {
    loading.value = false
  }
}

// 计算剩余天数 (30天有效期)
const getRemainingDays = (row) => {
  if (!row.deleteTime) return 30
  const now = new Date()
  const diff = now.getTime() - row.deleteTime.getTime()
  const daysPassed = Math.floor(diff / (1000 * 60 * 60 * 24))
  return Math.max(0, 30 - daysPassed)
}

// 单个还原
const handleRestore = async (row) => {
  try {
    const res = await fileApi.restoreFile(row.id)
    if (res.code === 200) {
      ElMessage.success('还原成功')
      loadData()
    } else {
      ElMessage.error(res.message || '还原失败')
    }
  } catch (e) { ElMessage.error('还原失败') }
}

// 单个删除
const handleDelete = (row) => {
  ElMessageBox.confirm('彻底删除无法恢复，确认删除？', '警告', { type: 'warning' })
    .then(async () => {
      try {
        const res = await fileApi.deleteFile(row.id)
        if (res.code === 200) {
          ElMessage.success('删除成功')
          loadData()
        } else {
          ElMessage.error(res.message || '删除失败')
        }
      } catch (e) { ElMessage.error('删除失败') }
    })
}

// 清空回收站
const handleEmpty = () => {
  ElMessageBox.confirm('确认清空回收站？所有文件将永久丢失！', '严重警告', { type: 'error' })
    .then(async () => {
      try {
        await fileApi.emptyTrash()
        ElMessage.success('已清空')
        loadData()
      } catch (e) { ElMessage.error('清空失败') }
    })
}

// 批量还原
const handleBatchRestore = async () => {
  let successCount = 0
  for (const f of selectedFiles.value) {
    try {
      const res = await fileApi.restoreFile(f.id)
      if (res.code === 200) successCount++
    } catch (e) {}
  }
  if (successCount > 0) {
    ElMessage.success(`成功还原 ${successCount} 个文件`)
    loadData()
  } else {
    ElMessage.warning('操作未完成')
  }
}

// 批量删除
const handleBatchDelete = async () => {
  const ids = selectedFiles.value.map(f => f.id)
  try {
    const res = await fileApi.batchDelete(ids)
    if (res.code === 200) {
      ElMessage.success('批量删除完成')
      loadData()
    } else {
      ElMessage.error(res.message || '批量删除失败')
    }
  } catch (e) {
    ElMessage.error('批量删除失败')
  }
}

const handleSelectionChange = (val) => selectedFiles.value = val

// 搜索过滤与分页
const filteredData = computed(() => {
  if (!searchKeyword.value) return recycleFiles.value
  return recycleFiles.value.filter(f => f.name.toLowerCase().includes(searchKeyword.value.toLowerCase()))
})

const paginatedFiles = computed(() => {
  const start = (currentPage.value - 1) * pageSize.value
  return filteredData.value.slice(start, start + pageSize.value)
})

onMounted(loadData)
</script>

<style scoped>
.recycle-bin { padding: 24px; background: var(--main-bg); min-height: 100vh; }
.operation-bar { display: flex; justify-content: space-between; margin-bottom: 16px; background: var(--card-bg); padding: 16px; border-radius: 12px; border: 1px solid var(--border-light); }
.file-list-card { border-radius: 12px; border: 1px solid var(--border-light); }
.file-name-cell { display: flex; align-items: center; gap: 10px; }
.card-header { display: flex; justify-content: space-between; align-items: center; }
.pagination-container { padding: 20px; display: flex; justify-content: center; }
</style>