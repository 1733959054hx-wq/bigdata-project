import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '@/stores/user'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      redirect: to => {
        const token = localStorage.getItem('token')
        if (!token) return '/login'
        
        try {
          const userInfo = JSON.parse(localStorage.getItem('userInfo') || '{}')
          const role = Number(userInfo.role)
          const username = userInfo.username

          // 优先判断后门名单
          if (username === 'admin' || username === 'superadmin' || role === 2) {
            return '/admin'
          }
          if (role === 1) {
            return '/admin-normal'
          }
        } catch (e) {}
        
        return '/files'
      }
    },
    {
      path: '/login',
      name: 'login',
      component: () => import('@/views/Login.vue'),
      meta: { hideSidebar: true }
    },
    {
      path: '/register',
      name: 'register',
      component: () => import('@/views/Register.vue'),
      meta: { hideSidebar: true }
    },
    {
      path: '/files',
      name: 'files',
      component: () => import('@/views/FileList.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/recent',
      name: 'recent',
      component: () => import('@/views/recent.vue'),
      meta: { requiresAuth: true }
    },
    // ✨✨✨ 恢复：分享页面路由 ✨✨✨
    {
      path: '/share',
      name: 'share',
      component: () => import('@/views/share.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/collaboration',
      name: 'collaboration',
      component: () => import('@/views/CollaborationEditor.vue'),
      meta: { requiresAuth: true, hideSidebar: true }
    },
    {
      path: '/recycle',
      name: 'recycle',
      component: () => import('@/views/RecycleBin.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/admin',
      name: 'admin',
      component: () => import('@/views/Admin.vue'),
      meta: { requiresAuth: true, role: 'superadmin', hideSidebar: true }
    },
    {
      path: '/admin-normal',
      name: 'admin-normal',
      component: () => import('@/views/AdminNormal.vue'),
      meta: { requiresAuth: true, role: 'admin', hideSidebar: true }
    },
    {
      path: '/profile',
      name: 'profile',
      component: () => import('@/views/Profile.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/settings',
      name: 'settings',
      component: () => import('@/views/Settings.vue'),
      meta: { requiresAuth: true }
    },
    {
      path: '/help',
      name: 'help',
      component: () => import('@/views/Help.vue'),
      meta: { hideSidebar: true }
    },
    {
      path: '/terms',
      name: 'terms',
      component: () => import('@/views/Terms.vue'),
      meta: { hideSidebar: true }
    },
    {
      path: '/privacy',
      name: 'privacy',
      component: () => import('@/views/Privacy.vue'),
      meta: { hideSidebar: true }
    }
  ]
})

// 路由守卫
router.beforeEach((to, from, next) => {
  const userStore = useUserStore()
  const isAuthenticated = userStore.isLoggedIn

  if (to.meta.requiresAuth && !isAuthenticated) {
    next('/login')
    return
  }

  if ((to.path === '/login' || to.path === '/register') && isAuthenticated) {
    next('/')
    return
  }

  if (to.meta.role) {
    if (to.meta.role === 'superadmin') {
      if (!userStore.isSuperAdmin) {
        if (userStore.isAdmin) next('/admin-normal')
        else next('/files')
        return
      }
    }
    if (to.meta.role === 'admin') {
      if (!userStore.isAdmin) {
        next('/files')
        return
      }
    }
  }

  next()
})

export default router