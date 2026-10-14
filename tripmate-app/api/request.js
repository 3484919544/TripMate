/**
 * 网络请求封装
 * - 统一前缀 /api(H5 dev 由 manifest.json 代理到后端 8080;生产由 Nginx 代理)
 * - 自动携带 Authorization: Bearer <token>
 * - 统一处理后端返回 { code, message, data }
 * - 401 自动清 token 并跳登录
 * - 业务错误(非 200 code)reject Error,带 code 字段,由页面自行提示
 */

const BASE_URL = '/api'
const TIMEOUT = 30000

function getToken() {
  return uni.getStorageSync('token') || ''
}

function redirectToLogin() {
  uni.removeStorageSync('token')
  try {
    uni.reLaunch({ url: '/pages/login/login' })
  } catch (e) {
    /* ignore */
  }
}

export function request(options) {
  const { url, method = 'GET', data = {}, header = {} } = options
  const token = getToken()
  if (token) header['Authorization'] = 'Bearer ' + token

  return new Promise((resolve, reject) => {
    uni.request({
      url: BASE_URL + url,
      method,
      data,
      timeout: TIMEOUT,
      header: Object.assign({ 'Content-Type': 'application/json' }, header),
      success: (res) => {
        const body = res.data || {}
        const code = body.code
        // 网络层 + 业务层都成功
        if (res.statusCode === 200 && code === 200) {
          resolve(body.data === undefined ? null : body.data)
          return
        }
        // 未登录 / token 失效
        if (code === 401 || res.statusCode === 401) {
          redirectToLogin()
          const err = new Error(body.message || '未登录或登录已过期')
          err.code = 401
          reject(err)
          return
        }
        // 其它业务错误
        const err = new Error(body.message || '请求失败(' + res.statusCode + ')')
        err.code = code || res.statusCode
        reject(err)
      },
      fail: (e) => {
        const err = new Error(
          (e && e.errMsg && e.errMsg.indexOf('timeout') > -1
            ? '请求超时,请检查网络'
            : '网络异常,请检查网络连接'))
        err.code = -1
        reject(err)
      }
    })
  })
}

/**
 * 便捷方法,自动 toast 业务错误(适合大多数列表/详情类调用)
 * 返回 Promise<data>;失败时 toast 并 reject
 */
export function withToast(promise, defaultMsg) {
  return promise.catch((err) => {
    uni.showToast({ title: err.message || defaultMsg || '操作失败', icon: 'none' })
    throw err
  })
}
