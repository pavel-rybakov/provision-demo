<script setup>
import { onMounted, reactive, ref } from 'vue'
import { apiJson } from '../api'
import { errorMessage } from '../utils'
import PageHeader from '../components/PageHeader.vue'
import StatusMessage from '../components/StatusMessage.vue'

const accounts = ref([])
const editingId = ref(null)
const error = ref('')
const success = ref('')
const form = reactive({ email: '', fullName: '', password: '', role: 'MANAGER' })

async function load() {
  try { accounts.value = await apiJson('/admin/accounts') }
  catch (exception) { error.value = errorMessage(exception) }
}

function reset() {
  editingId.value = null
  Object.assign(form, { email: '', fullName: '', password: '', role: 'MANAGER' })
}

function edit(account) {
  editingId.value = account.id
  Object.assign(form, { email: account.email, fullName: account.fullName, password: '', role: account.role })
}

async function save() {
  error.value = ''; success.value = ''
  try {
    const path = editingId.value ? `/admin/accounts/${editingId.value}` : '/admin/accounts'
    await apiJson(path, { method: editingId.value ? 'PUT' : 'POST', body: JSON.stringify(form) })
    success.value = editingId.value ? 'Пользователь обновлён' : 'Пользователь создан'
    reset(); await load()
  } catch (exception) { error.value = errorMessage(exception) }
}

async function remove(account) {
  if (!confirm(`Удалить ${account.email}?`)) return
  try { await apiJson(`/admin/accounts/${account.id}`, { method: 'DELETE' }); await load() }
  catch (exception) { error.value = errorMessage(exception) }
}

onMounted(load)
</script>

<template>
  <PageHeader title="Пользователи" subtitle="Учётные записи администраторов и менеджеров" />
  <StatusMessage :error="error" :success="success" />
  <section class="panel form-panel">
    <h2>{{ editingId ? 'Редактирование' : 'Новый пользователь' }}</h2>
    <form class="form-grid" @submit.prevent="save">
      <label>Email<input v-model="form.email" type="email" required /></label>
      <label>ФИО<input v-model="form.fullName" required /></label>
      <label>Пароль<input v-model="form.password" type="password" minlength="8" maxlength="72" required /></label>
      <label>Роль<select v-model="form.role"><option>ADMIN</option><option>MANAGER</option></select></label>
      <div class="form-actions"><button>{{ editingId ? 'Сохранить' : 'Создать' }}</button><button v-if="editingId" type="button" class="secondary" @click="reset">Отмена</button></div>
    </form>
  </section>
  <section class="panel table-wrap">
    <table><thead><tr><th>Email</th><th>ФИО</th><th>Роль</th><th></th></tr></thead>
      <tbody><tr v-for="account in accounts" :key="account.id"><td>{{ account.email }}</td><td>{{ account.fullName }}</td><td><span class="badge">{{ account.role }}</span></td><td class="actions"><button class="link" @click="edit(account)">Изменить</button><button class="link danger" @click="remove(account)">Удалить</button></td></tr></tbody>
    </table>
  </section>
</template>
