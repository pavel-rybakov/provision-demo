<script setup>
import { ref } from 'vue'
import { apiJson } from '../api'
import { errorMessage, formatDate } from '../utils'
import PageHeader from '../components/PageHeader.vue'
import StatusMessage from '../components/StatusMessage.vue'

const file = ref(null); const current = ref(null); const lookupId = ref(''); const error = ref(''); const success = ref(''); const loading = ref(false)
function choose(event) { file.value = event.target.files[0] }
async function upload() {
  if (!file.value) return
  loading.value = true; error.value = ''; success.value = ''
  try { const body = new FormData(); body.append('file', file.value); current.value = await apiJson('/admin/reading-imports', { method: 'POST', body }); lookupId.value = current.value.id; success.value = 'CSV загружен в staging' }
  catch (e) { error.value = errorMessage(e) } finally { loading.value = false }
}
async function action(name) {
  if (!lookupId.value) return
  loading.value = true; error.value = ''; success.value = ''
  try { current.value = await apiJson(`/admin/reading-imports/${lookupId.value}${name ? `/${name}` : ''}`, { method: name ? 'POST' : 'GET' }); success.value = name === 'validate' ? 'Проверка завершена' : name === 'apply' ? 'Импорт применён' : '' }
  catch (e) { error.value = errorMessage(e) } finally { loading.value = false }
}
</script>

<template>
  <PageHeader title="Импорт CSV" subtitle="Загрузка, проверка и атомарное применение показаний" />
  <StatusMessage :error="error" :success="success" />
  <section class="panel form-panel">
    <h2>Загрузить файл</h2><p class="hint">Колонки: meter_serial_number, measured_at, zone_t1, zone_t2, zone_t3</p>
    <div class="inline"><input type="file" accept=".csv,text/csv" @change="choose" /><button :disabled="!file || loading" @click="upload">Загрузить</button></div>
  </section>
  <section class="panel form-panel">
    <h2>Операции импорта</h2><div class="inline"><input v-model="lookupId" placeholder="UUID импорта" /><button class="secondary" :disabled="loading" @click="action('')">Открыть</button><button :disabled="loading" @click="action('validate')">Проверить</button><button :disabled="loading || current?.status !== 'READY'" @click="action('apply')">Применить</button></div>
  </section>
  <section v-if="current" class="panel">
    <div class="summary"><div><small>Статус</small><strong>{{ current.status }}</strong></div><div><small>Всего</small><strong>{{ current.totalRows }}</strong></div><div><small>Корректно</small><strong>{{ current.validRows }}</strong></div><div><small>Ошибок</small><strong>{{ current.invalidRows }}</strong></div></div>
    <p class="hint">{{ current.originalFilename }} · {{ formatDate(current.createdAt) }}</p>
    <div class="table-wrap"><table><thead><tr><th>Строка</th><th>Серийный №</th><th>Прибор</th><th>Ошибка</th></tr></thead><tbody><tr v-for="row in current.rows" :key="row.rowNumber"><td>{{ row.rowNumber }}</td><td>{{ row.meterSerialNumber }}</td><td class="mono">{{ row.electricityMeterId || '—' }}</td><td :class="{ 'text-danger': row.validationError }">{{ row.validationError || '—' }}</td></tr></tbody></table></div>
  </section>
</template>
