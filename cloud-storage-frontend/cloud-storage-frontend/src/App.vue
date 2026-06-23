<template>
  <el-config-provider :locale="zhCn">
    <div class="app-container">
      <aside v-show="shouldShowSidebar" class="app-sidebar">
        <div class="logo-container">
          <el-icon class="logo-icon" :size="32" color="#409eff"><Cloudy /></el-icon>
          <span class="logo-text">云存储</span>
        </div>
        
        <el-menu
          :default-active="activeMenu"
          class="sidebar-menu"
          router
          :collapse="isCollapse"
        >
          <el-menu-item index="/files">
            <el-icon><FolderOpened /></el-icon>
            <template #title>我的文件</template>
          </el-menu-item>
          <el-menu-item index="/recent">
            <el-icon><Clock /></el-icon>
            <template #title>最近浏览</template>
          </el-menu-item>
          <el-menu-item index="/share">
            <el-icon><Share /></el-icon>
            <template #title>我的分享</template>
          </el-menu-item>
          <el-menu-item index="/recycle">
            <el-icon><Delete /></el-icon>
            <template #title>回收站</template>
          </el-menu-item>

          <div v-if="userStore.isAdmin">
            <el-divider content-position="center">管理</el-divider>
            <el-menu-item v-if="userStore.isSuperAdmin" index="/admin">
              <el-icon><Setting /></el-icon>
              <template #title>系统管理</template>
            </el-menu-item>
            <el-menu-item index="/admin-normal">
              <el-icon><User /></el-icon>
              <template #title>用户管理</template>
            </el-menu-item>
          </div>
        </el-menu>

        <div class="user-profile">
          <el-dropdown trigger="click" @command="handleCommand">
            <div class="avatar-wrapper">
              <el-avatar :size="36" :src="userStore.getUserAvatar" />
              <span class="username" v-if="!isCollapse">{{ userStore.userInfo?.nickname || userStore.userInfo?.username }}</span>
              <el-icon class="el-icon--right" v-if="!isCollapse"><CaretBottom /></el-icon>
            </div>
            <template #dropdown>
              <el-dropdown-menu>
                <el-dropdown-item command="profile">个人中心</el-dropdown-item>
                <el-dropdown-item command="settings">设置</el-dropdown-item>
                <el-dropdown-item divided command="logout" style="color: #f56c6c;">退出登录</el-dropdown-item>
              </el-dropdown-menu>
            </template>
          </el-dropdown>
        </div>
      </aside>

      <main class="app-main" :class="{ 'full-width': !shouldShowSidebar }">
        <router-view v-slot="{ Component, route }">
          <transition name="fade" mode="out-in">
            <component :is="Component" :key="route.fullPath" />
          </transition>
        </router-view>
      </main>
    </div>
  </el-config-provider>
</template>

<script setup>
import { ref, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useUserStore } from '@/stores/user'
import zhCn from 'element-plus/dist/locale/zh-cn.mjs'
import { 
  FolderOpened, Clock, Delete, 
  Setting, User, CaretBottom, Cloudy, Share 
} from '@element-plus/icons-vue'

const route = useRoute()
const router = useRouter()
const userStore = useUserStore()
const isCollapse = ref(false)

const activeMenu = computed(() => {
  if (route.path.startsWith('/files')) return '/files'
  return route.path
})

const shouldShowSidebar = computed(() => {
  if (!userStore.isLoggedIn) return false
  const hiddenPaths = ['/login', '/register', '/404']
  if (hiddenPaths.includes(route.path)) return false
  if (route.meta.hideSidebar) return false
  return true
})

const handleCommand = (command) => {
  switch (command) {
    case 'profile': router.push('/profile'); break
    case 'settings': router.push('/settings'); break
    case 'logout': userStore.logout(); router.push('/login'); break
  }
}
</script>

<style scoped>
/* ✨✨✨ 核心修复：使用 CSS 变量替换硬编码颜色 ✨✨✨ */
.app-container {
  display: flex;
  height: 100vh;
  width: 100vw;
  overflow: hidden;
  background-color: var(--main-bg); /* 替换 #f5f7fa */
  color: var(--text-primary);
  transition: background-color 0.3s, color 0.3s;
}

.app-sidebar {
  width: 240px;
  background-color: var(--sidebar-bg); /* 替换 #fff */
  border-right: 1px solid var(--border-light); /* 替换 #e4e7ed */
  display: flex;
  flex-direction: column;
  transition: width 0.3s, background-color 0.3s, border-color 0.3s;
  flex-shrink: 0;
  z-index: 1000;
}

.logo-container {
  height: 60px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 10px;
  border-bottom: 1px solid var(--border-light); /* 替换 #f0f0f0 */
}

.logo-text {
  font-size: 18px;
  font-weight: 600;
  color: var(--text-primary); /* 替换 #303133 */
}

.sidebar-menu {
  flex: 1;
  border-right: none;
  overflow-y: auto;
  background-color: transparent; /* 让菜单背景透明，跟随 sidebar */
}

.user-profile {
  padding: 16px;
  border-top: 1px solid var(--border-light); /* 替换 #f0f0f0 */
}

.avatar-wrapper {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
  padding: 8px;
  border-radius: 8px;
  transition: background-color 0.3s;
}

.avatar-wrapper:hover {
  background-color: var(--sidebar-hover-bg); /* 替换 #f5f7fa */
}

.username {
  font-size: 14px;
  color: var(--text-secondary); /* 替换 #606266 */
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.app-main {
  flex: 1;
  overflow-x: hidden;
  overflow-y: auto;
  position: relative;
  background-color: var(--main-bg); /* 替换 #f5f7fa */
  transition: background-color 0.3s;
}

.app-main.full-width {
  width: 100vw;
}

.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.2s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}
</style>