import { defineStore } from 'pinia'
import { ref, watch } from 'vue'

export const useSettingsStore = defineStore('settings', () => {
  const isDark = ref(uni.getStorageSync('pref_dark') === 'true')
  const notifyOn = ref(uni.getStorageSync('pref_notify') !== 'false')
  // 兼容历史存储的 'small',统一归一化为 'normal'
  const storedFont = uni.getStorageSync('pref_font')
  const fontSize = ref(['normal', 'large', 'xl'].includes(storedFont) ? storedFont : 'normal')

  // 初始化:给 body 加 data-theme 属性,全局 CSS 变量据此切换
  function applyTheme() {
    // #ifdef H5
    if (typeof document !== 'undefined') {
      document.documentElement.setAttribute('data-theme', isDark.value ? 'dark' : 'light')
    }
    // #endif
  }

  // 初始化:给 body 加 data-font 属性,全局字号变量据此切换
  function applyFont() {
    // #ifdef H5
    if (typeof document !== 'undefined') {
      document.documentElement.setAttribute('data-font', fontSize.value)
    }
    // #endif
  }

  // 启动时立即应用
  applyTheme()
  applyFont()

  function toggleDark(val) {
    isDark.value = !!val
    uni.setStorageSync('pref_dark', String(isDark.value))
    applyTheme()
  }

  function setFont(key) {
    fontSize.value = key
    uni.setStorageSync('pref_font', key)
    applyFont()
  }

  function setNotify(val) {
    notifyOn.value = !!val
    uni.setStorageSync('pref_notify', String(notifyOn.value))
  }

  return { isDark, notifyOn, fontSize, toggleDark, setFont, setNotify, applyTheme, applyFont }
})
