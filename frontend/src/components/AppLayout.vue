<script setup>
import { useRouter } from 'vue-router'
import { api } from '../api'
import { auth, clearAuth } from '../auth'

const router = useRouter()

async function logout() {
  try {
    await api('/auth/logout', { method: 'POST' })
  } finally {
    clearAuth()
    await router.push('/login')
  }
}
</script>

<template>
  <div class="shell">
    <aside class="sidebar">
      <div class="brand">Provision</div>
      <nav>
        <RouterLink v-if="auth.role === 'ADMIN'" to="/accounts">Пользователи</RouterLink>
        <RouterLink v-if="auth.role === 'ADMIN'" to="/meters">Приборы</RouterLink>
        <RouterLink to="/readings">Показания</RouterLink>
        <RouterLink v-if="auth.role === 'ADMIN'" to="/imports">Импорт CSV</RouterLink>
        <RouterLink v-if="auth.role === 'ADMIN'" to="/reports">Отчёты</RouterLink>
      </nav>
      <div class="user-block">
        <small>Вы вошли как</small>
        <span>{{ auth.fullName || auth.email }}</span>
        <small>{{ auth.role }}</small>
        <button class="secondary" @click="logout">Выйти</button>
      </div>
    </aside>
    <main class="content"><RouterView /></main>
  </div>
</template>
