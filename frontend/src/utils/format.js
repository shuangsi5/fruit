// 格式化工具：金额、日期、数字

// 金额展示：加 ¥ 符号，保留两位小数
export function formatMoney(value) {
  if (value === null || value === undefined || value === '') return '¥0.00'
  const num = Number(value)
  if (Number.isNaN(num)) return '¥0.00'
  return '¥' + num.toFixed(2)
}

// 纯数字金额（不带符号），保留两位小数
export function formatAmount(value) {
  if (value === null || value === undefined || value === '') return '0.00'
  const num = Number(value)
  if (Number.isNaN(num)) return '0.00'
  return num.toFixed(2)
}

function pad(n) {
  return n < 10 ? '0' + n : String(n)
}

// 日期时间：yyyy-MM-dd HH:mm:ss
export function formatDateTime(value) {
  if (!value) return '-'
  if (value instanceof Date) {
    return (
      value.getFullYear() +
      '-' + pad(value.getMonth() + 1) +
      '-' + pad(value.getDate()) +
      ' ' + pad(value.getHours()) +
      ':' + pad(value.getMinutes()) +
      ':' + pad(value.getSeconds())
    )
  }
  // 兼容 "2026-11-21T10:00:00" 与 "2026-11-21 10:00:00"
  const str = String(value)
  if (str.includes(' ')) return str.slice(0, 19)
  if (str.includes('T')) return str.replace('T', ' ').slice(0, 19)
  return str
}

// 日期：yyyy-MM-dd
export function formatDate(value) {
  if (!value) return '-'
  const str = String(value)
  if (str.length >= 10) return str.slice(0, 10)
  return str
}

// 今天的 yyyy-MM-dd（本地时区）
export function today() {
  const d = new Date()
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
}

// N 天前的日期
export function daysAgo(n) {
  const d = new Date()
  d.setDate(d.getDate() - n)
  return d.getFullYear() + '-' + pad(d.getMonth() + 1) + '-' + pad(d.getDate())
}
