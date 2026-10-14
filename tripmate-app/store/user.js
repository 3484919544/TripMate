import { defineStore } from 'pinia'
import { ref, computed } from 'vue'
import * as authApi from '../api/auth.js'
import { request } from '../api/request.js'

export const useUserStore = defineStore('user', () => {
  const token = ref(uni.getStorageSync('token') || '')
  const userInfo = ref(uni.getStorageSync('userInfo') || null)

  const isLogin = computed(() => !!token.value)
  const isAdmin = computed(() => userInfo.value && userInfo.value.role === 'ADMIN')

  function setToken(t) {
    token.value = t
    uni.setStorageSync('token', t)
  }

  function setUserInfo(u) {
    userInfo.value = u
    uni.setStorageSync('userInfo', u)
  }

  /** 一键设置登录态(登录接口返回后调用) */
  function setLogin(t, user) {
    setToken(t)
    if (user) setUserInfo(user)
  }

  /** 旧版 login 包装 */
  async function login(form) {
    const data = await authApi.login(form)
    setLogin(data.token, data.user)
    return data
  }

  async function register(form) {
    return authApi.register(form)
  }

  async function fetchInfo() {
    const info = await request({ url: '/user/info', method: 'GET' })
    setUserInfo(info)
    return info
  }

  function logout() {
    token.value = ''
    userInfo.value = null
    uni.removeStorageSync('token')
    uni.removeStorageSync('userInfo')
  }

  function restore() {
    token.value = uni.getStorageSync('token') || ''
    userInfo.value = uni.getStorageSync('userInfo') || null
  }

  return {
    token,
    userInfo,
    isLogin,
    isAdmin,
    setToken,
    setUserInfo,
    setLogin,
    login,
    register,
    fetchInfo,
    logout,
    restore
  }
})
