export const emptyToNull = (value) => value === '' ? null : value
export const formatDate = (value) => value ? new Date(value).toLocaleString('ru-RU') : '—'

export function errorMessage(error) {
  return error instanceof Error ? error.message : 'Неизвестная ошибка'
}
