import { createRouter, createWebHistory } from 'vue-router'
import { auth, clearAuth, setAuth } from './auth'
import { apiJson } from './api'
import AppLayout from './components/AppLayout.vue'
import LoginView from './views/LoginView.vue'
import AccountsView from './views/AccountsView.vue'
import MetersView from './views/MetersView.vue'
import ReadingsView from './views/ReadingsView.vue'
import ImportsView from './views/ImportsView.vue'
import ReportsView from './views/ReportsView.vue'

const router = createRouter({
  history: createWebHistory(),
  routes: [
    { path: '/login', component: LoginView, meta: { public: true } },
    {
      path: '/',
      component: AppLayout,
      children: [
        { path: '', redirect: () => auth.role === 'MANAGER' ? '/readings' : '/accounts' },
        { path: 'accounts', component: AccountsView, meta: { admin: true } },
        { path: 'meters', component: MetersView, meta: { admin: true } },
        { path: 'readings', component: ReadingsView },
        { path: 'imports', component: ImportsView, meta: { admin: true } },
        { path: 'reports', component: ReportsView, meta: { admin: true } },
      ],
    },
  ],
})

router.beforeEach(async (to) => {
  if (!to.meta.public && !auth.accessToken) return '/login'
  if (auth.accessToken && !auth.role) {
    try {
      const profile = await apiJson('/auth/me')
      setAuth(auth.accessToken, profile)
    } catch {
      clearAuth()
      return '/login'
    }
  }
  if (to.path === '/login' && auth.accessToken) return '/'
  if (to.meta.admin && auth.role !== 'ADMIN') return '/readings'
})

export default router
