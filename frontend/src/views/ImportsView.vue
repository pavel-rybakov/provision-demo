<script setup>
import { onBeforeUnmount, ref } from 'vue'
import { api, apiJson } from '../api'
import { errorMessage, formatDate, formatDuration } from '../utils'
import PageHeader from '../components/PageHeader.vue'
import StatusMessage from '../components/StatusMessage.vue'

const file = ref(null); const current = ref(null); const lookupId = ref(''); const error = ref(''); const success = ref(''); const loading = ref(false)
const debugRowCount = ref(100)
let pollTimer
const terminalStatuses = new Set(['APPLIED', 'INVALID', 'FAILED'])
function choose(event) { file.value = event.target.files[0] }
function processingEnd(value) {
  if (value.appliedAt) return value.appliedAt
  if (value.status === 'INVALID') return value.validatedAt
  if (!terminalStatuses.has(value.status)) return new Date().toISOString()
  return null
}
function pollImport(id) {
  clearTimeout(pollTimer)
  pollTimer = setTimeout(async () => {
    try {
      current.value = await apiJson(`/admin/reading-imports/${id}`)
      if (terminalStatuses.has(current.value.status)) {
        success.value = current.value.status === 'APPLIED'
          ? 'CSV проверен и импортирован'
          : current.value.status === 'INVALID'
            ? `CSV проверен: найдено ошибок — ${current.value.invalidRows}`
            : 'Не удалось обработать CSV'
        return
      }
      pollImport(id)
    } catch (e) {
      error.value = errorMessage(e)
    }
  }, 1000)
}
onBeforeUnmount(() => clearTimeout(pollTimer))
async function importFile() {
  if (!file.value) return
  loading.value = true; error.value = ''; success.value = ''
  try {
    const body = new FormData()
    body.append('file', file.value)
    current.value = await apiJson('/admin/reading-imports', { method: 'POST', body })
    lookupId.value = current.value.id
    success.value = 'CSV загружен и передан на фоновую обработку'
    pollImport(current.value.id)
  }
  catch (e) { error.value = errorMessage(e) } finally { loading.value = false }
}
async function openImport() {
  if (!lookupId.value) return
  loading.value = true; error.value = ''; success.value = ''
  try { current.value = await apiJson(`/admin/reading-imports/${lookupId.value}`) }
  catch (e) { error.value = errorMessage(e) } finally { loading.value = false }
}
async function downloadDebugCsv() {
  loading.value = true; error.value = ''; success.value = ''
  try {
    const response = await api(`/admin/reading-imports/debug-csv?rows=${debugRowCount.value}`)
    const blob = await response.blob()
    const url = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = url
    link.download = `reading-import-debug-${debugRowCount.value}.csv`
    link.click()
    URL.revokeObjectURL(url)
  }
  catch (e) { error.value = errorMessage(e) } finally { loading.value = false }
}
</script>

<template>
  <PageHeader title="Импорт CSV" subtitle="Загрузка, проверка и атомарное применение показаний" />
  <StatusMessage :error="error" :success="success" />
  <section class="panel form-panel">
    <h2>Загрузить файл</h2><p class="hint">Колонки: meter_serial_number, measured_at, zone_t1, zone_t2, zone_t3</p>
    <div class="inline"><input type="file" accept=".csv,text/csv" @change="choose" /><button :disabled="!file || loading" @click="importFile">Импортировать</button></div>
  </section>
  <section class="panel form-panel">
    <h2>Тестовый CSV</h2><p class="hint">Генерирует от 1 до 1 000 000 строк для существующего прибора.</p>
    <div class="inline"><input v-model.number="debugRowCount" type="number" min="1" max="1000000" /><button class="secondary" :disabled="loading || debugRowCount < 1 || debugRowCount > 1000000" @click="downloadDebugCsv">Скачать CSV</button></div>
  </section>
  <section class="panel form-panel">
    <h2>История импорта</h2><div class="inline"><input v-model="lookupId" placeholder="UUID импорта" /><button class="secondary" :disabled="loading" @click="openImport">Открыть</button></div>
  </section>
  <section v-if="current" class="panel">
    <div class="summary"><div><small>Статус</small><strong>{{ current.status }}</strong></div><div><small>Всего</small><strong>{{ current.totalRows }}</strong></div><div><small>Корректно</small><strong>{{ current.validRows }}</strong></div><div><small>Ошибок</small><strong>{{ current.invalidRows }}</strong></div></div>
    <div class="import-details">
      <div><small>Загружен</small><span>{{ formatDate(current.createdAt) }}</span></div>
      <div><small>Проверен</small><span>{{ formatDate(current.validatedAt) }}</span></div>
      <div><small>Применён</small><span>{{ formatDate(current.appliedAt) }}</span></div>
    </div>
    <p class="import-meta">
      <span>{{ current.originalFilename }}</span>
      <small>Обработка: {{ formatDuration(current.createdAt, processingEnd(current)) }}</small>
    </p>
  </section>
</template>
