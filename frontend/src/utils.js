export const emptyToNull = (value) => value === '' ? null : value
export const formatDate = (value) => value ? new Date(value).toLocaleString('ru-RU') : '—'

export function formatDuration(start, end) {
  if (!start || !end) return '—'
  const milliseconds = new Date(end).getTime() - new Date(start).getTime()
  if (!Number.isFinite(milliseconds) || milliseconds < 0) return '—'
  if (milliseconds < 1000) return `${milliseconds} мс`

  const totalSeconds = Math.floor(milliseconds / 1000)
  const hours = Math.floor(totalSeconds / 3600)
  const minutes = Math.floor(totalSeconds % 3600 / 60)
  const seconds = totalSeconds % 60
  return [hours && `${hours} ч`, minutes && `${minutes} мин`, `${seconds} с`]
    .filter(Boolean)
    .join(' ')
}

export function errorMessage(error) {
  return error instanceof Error ? error.message : 'Неизвестная ошибка'
}
