import { request } from './request.js'

/**
 * 行程数据结构(TripVO):
 * {
 *   id?, title, destination, days, budget, totalCost, overBudget,
 *   preference, status?, createTime?,
 *   dayList: [{ dayIndex, theme, morning, afternoon, evening, cost, tips }]
 * }
 */

// AI 生成行程(返回 TripVO,未保存)
export const generateTrip = (data) =>
  request({ url: '/trip/generate', method: 'POST', data })

// 保存行程(返回 { id })
export const saveTrip = (data) =>
  request({ url: '/trip/save', method: 'POST', data })

// 行程列表(分页 + 关键词)
export const listTrips = (params) =>
  request({ url: '/trip/list', method: 'GET', data: params })

// 行程详情
export const getTrip = (id) =>
  request({ url: '/trip/' + id, method: 'GET' })

// 修改行程
export const updateTrip = (id, data) =>
  request({ url: '/trip/' + id, method: 'PUT', data })

// 删除行程
export const deleteTrip = (id) =>
  request({ url: '/trip/' + id, method: 'DELETE' })

// 热门目的地(简单配置接口,可降级为本地数据)
export const getHotDestinations = () =>
  request({ url: '/destination/hot', method: 'GET' }).catch(() => null)
