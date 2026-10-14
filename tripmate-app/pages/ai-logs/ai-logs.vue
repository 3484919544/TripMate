<template>
  <view class="logs">
    <view class="logs__list">
      <view v-for="log in logs" :key="log.id" class="log-card">
        <view class="log-card__head">
          <text class="log-card__model">{{ log.model || 'AI' }}</text>
          <text
            class="log-card__status"
            :class="log.status === 'SUCCESS' ? 'is-ok' : 'is-fail'"
          >{{ log.status === 'SUCCESS' ? '成功' : '失败' }}</text>
        </view>
        <view class="log-card__row">
          <text class="log-card__label">时间</text>
          <text class="log-card__value">{{ formatDateTime(log.requestTime) }}</text>
        </view>
        <view class="log-card__row">
          <text class="log-card__label">耗时</text>
          <text class="log-card__value">{{ duration(log) }}</text>
        </view>
        <view v-if="log.tokens" class="log-card__row">
          <text class="log-card__label">Tokens</text>
          <text class="log-card__value">{{ log.tokens }}</text>
        </view>
        <view v-if="log.prompt" class="log-card__prompt">
          <text class="log-card__prompt-text">{{ log.prompt }}</text>
        </view>
        <view v-if="log.errorMsg" class="log-card__error">
          <text>{{ log.errorMsg }}</text>
        </view>
      </view>

      <EmptyState
        v-if="!loading && logs.length === 0"
        icon="🤖"
        text="还没有 AI 调用记录"
      />
      <view v-if="loading" class="logs__more">
        <text>加载中...</text>
      </view>
      <view v-if="!loading && finished && logs.length > 0" class="logs__more">
        <text>— 没有更多了 —</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref } from 'vue'
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { listAiLogs } from '../../api/admin.js'
import { formatDateTime } from '../../utils/format.js'
import EmptyState from '../../components/EmptyState.vue'

const logs = ref([])
const loading = ref(false)
const finished = ref(false)
const pageNum = ref(1)
const pageSize = 15

onShow(() => {
  resetAndLoad()
})
onPullDownRefresh(() => {
  resetAndLoad().finally(() => uni.stopPullDownRefresh())
})
onReachBottom(() => {
  if (!loading.value && !finished.value) loadMore()
})

async function resetAndLoad() {
  pageNum.value = 1
  finished.value = false
  logs.value = []
  await loadMore()
}

async function loadMore() {
  if (loading.value) return
  loading.value = true
  try {
    const data = await listAiLogs({ pageNum: pageNum.value, pageSize })
    const list = (data && (data.list || data.records)) || []
    logs.value = logs.value.concat(list)
    if (list.length < pageSize) finished.value = true
    else pageNum.value++
  } catch (e) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function duration(log) {
  if (!log.requestTime) return '-'
  const start = new Date(log.requestTime).getTime()
  const end = log.responseTime ? new Date(log.responseTime).getTime() : 0
  if (!start || !end) return '-'
  return Math.max(end - start, 0) / 1000 + ' 秒'
}
</script>

<style lang="scss" scoped>
.logs {
  min-height: 100vh;
  background: var(--bg-page);
  padding: 24rpx 0;
  &__list {
    padding: 0 4rpx;
  }
  &__more {
    text-align: center;
    padding: 24rpx 0;
    font-size: var(--fs-22);
    color: var(--text-muted);
  }
}
.log-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 28rpx;
  margin: 0 24rpx 24rpx;
  box-shadow: 0 4rpx 16rpx rgba(47, 111, 237, 0.06);
  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-bottom: 16rpx;
  }
  &__model {
    font-size: var(--fs-28);
    font-weight: 600;
    color: var(--brand);
  }
  &__status {
    font-size: var(--fs-22);
    padding: 4rpx 16rpx;
    border-radius: 16rpx;
    &.is-ok {
      color: var(--success);
      background: #e9f9ef;
    }
    &.is-fail {
      color: var(--danger);
      background: #fff0ef;
    }
  }
  &__row {
    display: flex;
    justify-content: space-between;
    padding: 8rpx 0;
  }
  &__label {
    font-size: var(--fs-24);
    color: var(--text-muted);
  }
  &__value {
    font-size: var(--fs-24);
    color: var(--text-primary);
  }
  &__prompt {
    margin-top: 16rpx;
    padding: 16rpx 20rpx;
    background: var(--bg-input);
    border-radius: 12rpx;
    max-height: 200rpx;
    overflow: hidden;
  }
  &__prompt-text {
    font-size: var(--fs-22);
    color: var(--text-secondary);
    line-height: 1.5;
  }
  &__error {
    margin-top: 16rpx;
    padding: 16rpx 20rpx;
    background: #fff5f3;
    border-radius: 12rpx;
    font-size: var(--fs-22);
    color: var(--danger);
    line-height: 1.5;
  }
}
</style>
