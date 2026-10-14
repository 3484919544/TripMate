import { defineStore } from 'pinia'
import { ref } from 'vue'

/**
 * 行程临时 store:用于跨页传递 AI 生成的大 JSON(URL 传参会超限)
 * create-trip 生成成功 -> setCurrent -> 跳 result 页读取 current
 */
export const useTripStore = defineStore('trip', () => {
  const current = ref(null) // 当前生成但未保存的行程 TripVO

  function setCurrent(trip) {
    current.value = trip
  }

  function getCurrent() {
    return current.value
  }

  function clear() {
    current.value = null
  }

  return { current, setCurrent, getCurrent, clear }
})
