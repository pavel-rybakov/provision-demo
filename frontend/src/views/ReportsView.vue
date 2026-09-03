<script setup>
import { ref } from 'vue'
import { api } from '../api'
import { errorMessage } from '../utils'
import PageHeader from '../components/PageHeader.vue'
import StatusMessage from '../components/StatusMessage.vue'

const email = ref(''); const loading = ref(false); const error = ref(''); const success = ref('')
async function generate() {
  loading.value = true; error.value = ''; success.value = ''
  try {
    const query = email.value ? `?send_to_email=${encodeURIComponent(email.value)}` : ''
    const response = await api(`/admin/reports/latest-readings${query}`, { method: 'POST' })
    const blob = await response.blob(); const url = URL.createObjectURL(blob); const link = document.createElement('a')
    link.href = url; link.download = `latest-meter-readings-${new Date().toISOString().slice(0, 10)}.csv`; link.click(); URL.revokeObjectURL(url)
    success.value = email.value ? `Отчёт скачан и отправлен на ${email.value}` : 'Отчёт скачан'
  } catch (e) { error.value = errorMessage(e) } finally { loading.value = false }
}
</script>

<template>
  <PageHeader title="Отчёты" subtitle="Последние показания всех приборов" />
  <StatusMessage :error="error" :success="success" />
  <section class="panel report-card"><div class="report-icon">CSV</div><div><h2>Последние показания</h2><p>Файл содержит каждый прибор и его последнее зарегистрированное показание.</p></div>
    <form @submit.prevent="generate"><label>Email для дополнительной отправки<input v-model="email" type="email" placeholder="Необязательно" /></label><button :disabled="loading">{{ loading ? 'Формирование…' : 'Сформировать и скачать' }}</button></form>
  </section>
</template>
