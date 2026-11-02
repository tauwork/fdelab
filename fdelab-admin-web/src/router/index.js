import { createRouter, createWebHistory } from 'vue-router'
import { useUserStore } from '../stores/user'
import AdminLayout from '../layouts/AdminLayout.vue'
import LoginView from '../views/login/LoginView.vue'
import DashboardView from '../views/dashboard/DashboardView.vue'
import UserView from '../views/system/UserView.vue'
import TalentView from '../views/talent/TalentView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    {
      path: '/login',
      name: 'login',
      component: LoginView,
      meta: { public: true },
    },
    {
      path: '/',
      component: AdminLayout,
      children: [
        { path: '', redirect: '/dashboard' },
        { path: 'dashboard', name: 'dashboard', component: DashboardView, meta: { title: '概览' } },
        { path: 'talents', name: 'talents', component: TalentView, meta: { title: '人才' } },
        { path: 'system/users', name: 'users', component: UserView, meta: { title: '用户' } },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  const store = useUserStore()
  if (to.meta.public) {
    if (to.path === '/login' && store.token) {
      return '/dashboard'
    }
    return true
  }
  if (!store.token) {
    return '/login'
  }
  if (!store.profile) {
    try {
      await store.fetchProfile()
    } catch {
      store.clear()
      return '/login'
    }
  }
  return true
})

export default router
