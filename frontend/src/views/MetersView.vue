<script setup>
import { onMounted, reactive, ref } from 'vue'
import { apiJson } from '../api'
import { emptyToNull, errorMessage, formatDate } from '../utils'
import PageHeader from '../components/PageHeader.vue'
import StatusMessage from '../components/StatusMessage.vue'

const meters = ref([]); const editingId = ref(null); const error = ref(''); const success = ref('')
const blank = () => ({ serialNumber: '', inventoryNumber: '', manufactureYear: '', transformationRatio: '', installationDate: '', sealNumber: '', antimagneticSealNumber: '', installationLocation: '', note: '', gisHousingId: '' })
const form = reactive(blank())

async function load() { try { meters.value = await apiJson('/admin/meters') } catch (e) { error.value = errorMessage(e) } }
function reset() { editingId.value = null; Object.assign(form, blank()) }
function edit(meter) { editingId.value = meter.id; Object.assign(form, Object.fromEntries(Object.keys(blank()).map(key => [key, meter[key] ?? '']))) }
function payload() { return Object.fromEntries(Object.entries(form).map(([key, value]) => [key, emptyToNull(value)])) }
async function save() {
  error.value = ''; success.value = ''
  try {
    const path = editingId.value ? `/admin/meters/${editingId.value}` : '/admin/meters'
    await apiJson(path, { method: editingId.value ? 'PUT' : 'POST', body: JSON.stringify(payload()) })
    success.value = editingId.value ? 'Прибор обновлён' : 'Прибор создан'; reset(); await load()
  } catch (e) { error.value = errorMessage(e) }
}
async function remove(meter) { if (confirm(`Удалить прибор ${meter.serialNumber}?`)) try { await apiJson(`/admin/meters/${meter.id}`, { method: 'DELETE' }); await load() } catch (e) { error.value = errorMessage(e) } }
onMounted(load)
</script>

<template>
  <PageHeader title="Приборы учёта" subtitle="Реестр электросчётчиков" />
  <StatusMessage :error="error" :success="success" />
  <section class="panel form-panel"><h2>{{ editingId ? 'Редактирование прибора' : 'Новый прибор' }}</h2>
    <form class="form-grid wide" @submit.prevent="save">
      <label>Серийный номер<input v-model="form.serialNumber" required /></label><label>Инвентарный номер<input v-model="form.inventoryNumber" required /></label>
      <label>Год изготовления<input v-model.number="form.manufactureYear" type="number" min="1800" max="9999" /></label><label>Коэффициент трансформации<input v-model="form.transformationRatio" type="number" min="0" step="0.000001" /></label>
      <label>Дата установки<input v-model="form.installationDate" type="date" /></label><label>Номер пломбы<input v-model="form.sealNumber" /></label>
      <label>Антимагнитная пломба<input v-model="form.antimagneticSealNumber" /></label><label>Место установки<input v-model="form.installationLocation" /></label>
      <label>ID в ГИС ЖКХ<input v-model="form.gisHousingId" /></label><label class="span-2">Примечание<textarea v-model="form.note" rows="2" /></label>
      <div class="form-actions span-2"><button>{{ editingId ? 'Сохранить' : 'Создать' }}</button><button v-if="editingId" type="button" class="secondary" @click="reset">Отмена</button></div>
    </form>
  </section>
  <section class="panel table-wrap"><table><thead><tr><th>Серийный №</th><th>Инвентарный №</th><th>Установка</th><th>Место</th><th></th></tr></thead>
    <tbody><tr v-for="meter in meters" :key="meter.id"><td>{{ meter.serialNumber }}</td><td>{{ meter.inventoryNumber }}</td><td>{{ meter.installationDate || '—' }}</td><td>{{ meter.installationLocation || '—' }}</td><td class="actions"><button class="link" @click="edit(meter)">Изменить</button><button class="link danger" @click="remove(meter)">Удалить</button></td></tr></tbody></table></section>
</template>
