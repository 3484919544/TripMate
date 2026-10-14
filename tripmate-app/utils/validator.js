/**
 * 前端校验:返回错误消息字符串,通过则返回 null
 */

export function validateTrip(form) {
  const { destination, days, budget, preference } = form
  if (!destination || !String(destination).trim()) return '请输入目的地'
  const d = Number(days)
  if (!days || isNaN(d) || d <= 0) return '天数必须大于 0'
  if (d > 30) return '天数过多,建议不超过 30 天'
  const b = Number(budget)
  if (!budget || isNaN(b) || b <= 0) return '预算必须大于 0'
  if (b > 1000000) return '预算数额过大,请检查'
  if (!preference || !preference.length) return '请至少选择一个偏好'
  return null
}

export function validateLogin(form) {
  const { username, password } = form
  if (!username || !username.trim()) return '请输入用户名'
  if (!password || password.length < 6) return '密码至少 6 位'
  return null
}

export function validateFeedback(content) {
  if (!content || !String(content).trim()) return '请输入反馈内容'
  if (String(content).length > 500) return '反馈内容不超过 500 字'
  return null
}
