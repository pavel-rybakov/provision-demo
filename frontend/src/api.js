import { auth, clearAuth } from './auth'

const API_PREFIX = '/api/v1'

export async function api(path, options = {}) {
  const headers = new Headers(options.headers)
  if (auth.accessToken) headers.set('Authorization', `Bearer ${auth.accessToken}`)
  if (options.body && !(options.body instanceof FormData)) headers.set('Content-Type', 'application/json')

  const response = await fetch(`${API_PREFIX}${path}`, { ...options, headers })
  if (response.status === 401) clearAuth()
  if (!response.ok) {
    let message = `Ошибка ${response.status}`
    try {
      const problem = await response.json()
      message = problem.detail || problem.title || message
    } catch {
      // Response does not contain JSON Problem Details.
    }
    throw new Error(message)
  }
  if (response.status === 204) return null
  return response
}

export async function apiJson(path, options = {}) {
  const response = await api(path, options)
  return response?.json()
}
