// 错误码 -> 提示文案
export const ERROR_MESSAGES = {
  40001: '参数校验失败，请检查输入',
  40002: '请求格式错误',
  40003: '缺少必填参数',
  40101: '请先登录',
  40102: '登录已过期，请重新登录',
  40103: '账号或密码错误',
  40301: '权限不足，无法访问',
  40302: '账号已被禁用，请联系平台',
  40401: '数据不存在',
  40402: '接口不存在',
  40501: '请求方式不支持',
  40901: '数据已存在',
  40902: '操作冲突，请重试',
  42201: '业务规则校验失败',
  42901: '操作过于频繁，请稍后再试',
  50001: '系统繁忙，请稍后重试',
  50002: '数据服务异常',
  50003: '文件操作异常'
}

export function getErrorMessage(code, message) {
  if (message) return message
  return ERROR_MESSAGES[code] || '操作失败，请稍后重试'
}

// 轻量消息提示（避免引入额外 UI 库）
let messageContainer = null
function ensureContainer() {
  if (messageContainer && document.body.contains(messageContainer)) return messageContainer
  messageContainer = document.createElement('div')
  messageContainer.className = 'ft-message-container'
  document.body.appendChild(messageContainer)
  return messageContainer
}

export function showMessage(text, type = 'info', duration = 2500) {
  if (typeof document === 'undefined') return
  const container = ensureContainer()
  const el = document.createElement('div')
  el.className = 'ft-message ft-message-' + type
  el.textContent = text
  container.appendChild(el)
  setTimeout(() => {
    el.classList.add('ft-message-leave')
    setTimeout(() => el.remove(), 300)
  }, duration)
}

export const notifySuccess = (text) => showMessage(text, 'success')
export const notifyError = (text) => showMessage(text, 'error')
export const notifyWarning = (text) => showMessage(text, 'warning')
export const notifyInfo = (text) => showMessage(text, 'info')
