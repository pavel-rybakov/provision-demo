<script setup>
import { onMounted, reactive, ref } from 'vue'
import { apiJson } from '../api'
import { emptyToNull, errorMessage, formatDate } from '../utils'
import PageHeader from '../components/PageHeader.vue'
import StatusMessage from '../components/StatusMessage.vue'

const readings = ref([]); const editingId = ref(null); const error = ref(''); const success = ref('')
const blank = () => ({ electricityMeterId: '', measuredAt: '', zoneT1: '', zoneT2: '', zoneT3: '' })
const form = reactive(blank())
async function load() { try { readings.value = await apiJson('/meter-readings') } catch (e) { error.value = errorMessage(e) } }
function reset() { editingId.value = null; Object.assign(form, blank()) }
function toLocalInput(value) { if (!value) return ''; const date = new Date(value); return new Date(date.getTime() - date.getTimezoneOffset() * 60000).toISOString().slice(0, 16) }
function edit(reading) { editingId.value = reading.id; Object.assign(form, { electricityMeterId: reading.electricityMeterId, measuredAt: toLocalInput(reading.measuredAt), zoneT1: reading.zoneT1, zoneT2: reading.zoneT2 ?? '', zoneT3: reading.zoneT3 ?? '' }) }
function payload() {
  const value = { measuredAt: new Date(form.measuredAt).toISOString(), zoneT1: form.zoneT1, zoneT2: emptyToNull(form.zoneT2), zoneT3: emptyToNull(form.zoneT3) }
  if (!editingId.value) value.electricityMeterId = form.electricityMeterId
  return value
}
async function save() {
  error.value = ''; success.value = ''
  try {
    const path = editingId.value ? `/meter-readings/${editingId.value}` : '/meter-readings'
    await apiJson(path, { method: editingId.value ? 'PUT' : 'POST', body: JSON.stringify(payload()) })
    success.value = editingId.value ? 'Показание обновлено' : 'Показание создано'; reset(); await load()
  } catch (e) { error.value = errorMessage(e) }
}
async function remove(reading) { if (confirm('Удалить показание?')) try { await apiJson(`/meter-readings/${reading.id}`, { method: 'DELETE' }); await load() } catch (e) { error.value = errorMessage(e) } }
onMounted(load)
</script>

<template>
  <PageHeader title="Показания" subtitle="Ручной ввод и история показаний" />
  <StatusMessage :error="error" :success="success" />
  <section class="panel form-panel"><h2>{{ editingId ? 'Редактирование показания' : 'Новое показание' }}</h2>
    <form class="form-grid" @submit.prevent="save">
      <label>UUID прибора<input v-model="form.electricityMeterId" :disabled="!!editingId" required /></label>
      <label>Дата и время<input v-model="form.measuredAt" type="datetime-local" required /></label>
      <label>Зона T1<input v-model="form.zoneT1" type="number" min="0" step="0.000001" required /></label>
      <label>Зона T2<input v-model="form.zoneT2" type="number" min="0" step="0.000001" /></label>
      <label>Зона T3<input v-model="form.zoneT3" type="number" min="0" step="0.000001" /></label>
      <div class="form-actions"><button>{{ editingId ? 'Сохранить' : 'Создать' }}</button><button v-if="editingId" type="button" class="secondary" @click="reset">Отмена</button></div>
    </form>
  </section>
  <section class="panel table-wrap"><table><thead><tr><th>Прибор</th><th>Время</th><th>T1</th><th>T2</th><th>T3</th><th>Источник</th><th></th></tr></thead>
    <tbody><tr v-for="reading in readings" :key="reading.id"><td class="mono">{{ reading.electricityMeterId }}</td><td>{{ formatDate(reading.measuredAt) }}</td><td>{{ reading.zoneT1 }}</td><td>{{ reading.zoneT2 ?? '—' }}</td><td>{{ reading.zoneT3 ?? '—' }}</td><td><span class="badge">{{ reading.sourceType }}</span></td><td class="actions"><button class="link" @click="edit(reading)">Изменить</button><button class="link danger" @click="remove(reading)">Удалить</button></td></tr></tbody></table></section>
</template>
