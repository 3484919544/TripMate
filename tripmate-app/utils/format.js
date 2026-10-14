/**
 * 格式化工具
 */

export function formatMoney(n) {
  const num = Number(n || 0)
  return '¥' + num.toFixed(2).replace(/\B(?=(\d{3})+(?!\d))/g, ',')
}

export function formatDate(str) {
  if (!str) return ''
  const d = new Date(str)
  if (isNaN(d.getTime())) return str
  const pad = (n) => (n < 10 ? '0' + n : '' + n)
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
}

export function formatDateTime(str) {
  if (!str) return ''
  const d = new Date(str)
  if (isNaN(d.getTime())) return str
  const pad = (n) => (n < 10 ? '0' + n : '' + n)
  return (
    d.getFullYear() +
    '-' +
    pad(d.getMonth() + 1) +
    '-' +
    pad(d.getDate()) +
    ' ' +
    pad(d.getHours()) +
    ':' +
    pad(d.getMinutes())
  )
}

// 行程文本复制(用于"发给朋友")
export function tripToText(trip) {
  if (!trip) return ''
  const lines = []
  lines.push('【' + (trip.title || '行程') + '】')
  lines.push('目的地:' + trip.destination + ' / ' + trip.days + ' 天')
  lines.push('预算:' + formatMoney(trip.budget) + ' / 预计花费:' + formatMoney(trip.totalCost))
  ;(trip.dayList || []).forEach((day) => {
    lines.push('')
    lines.push('Day ' + day.dayIndex + (day.theme ? ' ' + day.theme : ''))
    lines.push('  上午:' + (day.morning || '-'))
    lines.push('  下午:' + (day.afternoon || '-'))
    lines.push('  晚上:' + (day.evening || '-'))
    lines.push('  花费:' + formatMoney(day.cost))
    if (day.tips) lines.push('  提示:' + day.tips)
  })
  lines.push('')
  lines.push('—— 来自「智游 TripMate」')
  return lines.join('\n')
}
