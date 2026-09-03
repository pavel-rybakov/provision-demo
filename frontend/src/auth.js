import { reactive } from 'vue'

const stored = localStorage.getItem('provision.auth')

export const auth = reactive(stored ? JSON.parse(stored) : {
  accessToken: null,
  id: null,
  email: null,
  fullName: null,
  role: null,
})

export function setAuth(accessToken, profile = {}) {
  auth.accessToken = accessToken
  auth.id = profile.id ?? null
  auth.email = profile.email ?? null
  auth.fullName = profile.fullName ?? null
  auth.role = profile.role ?? null
  localStorage.setItem('provision.auth', JSON.stringify({
    accessToken,
    id: auth.id,
    email: auth.email,
    fullName: auth.fullName,
    role: auth.role,
  }))
}

export function clearAuth() {
  auth.accessToken = null
  auth.id = null
  auth.email = null
  auth.fullName = null
  auth.role = null
  localStorage.removeItem('provision.auth')
}
