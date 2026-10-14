import { request } from './request.js'

// 提交反馈
export const submitFeedback = (data) =>
  request({ url: '/feedback', method: 'POST', data })

// 我的反馈历史(含管理员回复)
export const myFeedbackList = () =>
  request({ url: '/feedback/my', method: 'GET' })

// ===== 管理员接口 =====

// 全部反馈列表
export const adminFeedbackList = () =>
  request({ url: '/admin/feedback/list', method: 'GET' })

// 管理员回复反馈
export const adminReplyFeedback = (id, reply) =>
  request({ url: '/admin/feedback/' + id + '/reply', method: 'PUT', data: { reply } })

// 管理员删除反馈
export const adminDeleteFeedback = (id) =>
  request({ url: '/admin/feedback/' + id, method: 'DELETE' })
