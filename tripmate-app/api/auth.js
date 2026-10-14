import { request } from './request.js'

export const fetchCaptcha = () =>
  request({ url: '/auth/captcha', method: 'GET' })

export const login = ({ account, password, captcha, captchaKey }) =>
  request({ url: '/auth/login', method: 'POST', data: { account, password, captcha, captchaKey } })

export const register = ({ phone, username, password, confirmPassword }) =>
  request({ url: '/auth/register', method: 'POST', data: { phone, username, password, confirmPassword } })

// ===== 用户资料 =====

export const updateProfile = (data) =>
  request({ url: '/user/profile', method: 'PUT', data })

export const changePassword = (oldPassword, newPassword) =>
  request({ url: '/user/password', method: 'POST', data: { oldPassword, newPassword } })

export const updateNickname = (nickname) =>
  request({ url: '/user/nickname', method: 'PUT', data: { nickname } })

export const clearAllTrips = () =>
  request({ url: '/user/trips', method: 'DELETE' })

export const deleteAccount = () =>
  request({ url: '/user', method: 'DELETE' })

// ===== 公开接口(无需登录) =====

export const resetPassword = (phone, captcha, captchaKey, newPassword, confirmPassword) =>
  request({ url: '/user/reset-password', method: 'POST', data: { phone, captcha, captchaKey, newPassword, confirmPassword } })

export const checkPhoneExists = (phone) =>
  request({ url: '/user/check-phone', method: 'GET', data: { phone } })

/** 头像上传(用 uni.uploadFile,不走 request.js) */
export function uploadAvatar(filePath, token) {
  return new Promise((resolve, reject) => {
    uni.uploadFile({
      url: (typeof location !== 'undefined' ? location.origin : 'http://localhost:5173') + '/api/upload/avatar',
      filePath,
      name: 'file',
      header: token ? { 'Authorization': 'Bearer ' + token } : {},
      success: (res) => {
        try {
          const body = JSON.parse(res.data)
          if (body.code === 200) resolve(body.data)
          else reject(new Error(body.message || '上传失败'))
        } catch (e) { reject(new Error('上传响应解析失败')) }
      },
      fail: (e) => reject(new Error(e.errMsg || '上传失败'))
    })
  })
}
