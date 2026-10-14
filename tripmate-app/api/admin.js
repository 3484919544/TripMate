import { request } from './request.js'

// AI 调用日志列表(分页)
export const listAiLogs = (params) =>
  request({ url: '/admin/ai-logs', method: 'GET', data: params })

// ===== 用户管理(仅管理员) =====

// 用户列表(分页,可按用户名/昵称搜索)
export const listUsers = (params) =>
  request({ url: '/admin/user/list', method: 'GET', data: params })

// 管理员创建用户(可指定 role=USER/ADMIN)
export const createUser = (data) =>
  request({ url: '/admin/user', method: 'POST', data })

// 修改用户(重置密码/昵称/角色)
export const updateUser = (id, data) =>
  request({ url: `/admin/user/${id}`, method: 'PUT', data })

// 删除用户
export const deleteUser = (id) =>
  request({ url: `/admin/user/${id}`, method: 'DELETE' })

