<script setup>
import { reactive, ref } from 'vue'
import { useRouter } from 'vue-router'
import { apiJson } from '../api'
import { setAuth } from '../auth'
import { errorMessage } from '../utils'

const router = useRouter()
const form = reactive({ email: '', password: '' })
const error = ref('')
const loading = ref(false)

async function login() {
  loading.value = true
  error.value = ''
  try {
    const result = await apiJson('/auth/login', { method: 'POST', body: JSON.stringify(form) })
    setAuth(result.accessToken, { email: form.email })
    const profile = await apiJson('/auth/me')
    setAuth(result.accessToken, profile)
    await router.push('/')
  } catch (exception) {
    error.value = errorMessage(exception)
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <main class="login-page">
    <form class="login-card" @submit.prevent="login">
      <h1>Provision</h1>
      <p>Управление приборами учёта</p>
      <label>Email<input v-model="form.email" type="email" required autofocus /></label>
      <label>Пароль<input v-model="form.password" type="password" required /></label>
      <p v-if="error" class="message error">{{ error }}</p>
      <button :disabled="loading">{{ loading ? 'Вход…' : 'Войти' }}</button>
    </form>
  </main>
</template>
