<template>
  <view class="trip-card" @tap="$emit('tap')">
    <view class="trip-card__head">
      <text class="trip-card__title ellipsis">{{ trip.title || trip.destination + ' 行程' }}</text>
      <text class="trip-card__status" :class="statusClass">{{ statusText }}</text>
    </view>

    <view class="trip-card__meta">
      <view class="trip-card__meta-item">
        <text class="trip-card__meta-label">目的地</text>
        <text class="trip-card__meta-value">{{ trip.destination }}</text>
      </view>
      <view class="trip-card__meta-item">
        <text class="trip-card__meta-label">天数</text>
        <text class="trip-card__meta-value">{{ trip.days }} 天</text>
      </view>
      <view class="trip-card__meta-item">
        <text class="trip-card__meta-label">花费</text>
        <text class="trip-card__meta-value">{{ formatMoney(trip.totalCost) }}</text>
      </view>
    </view>

    <view v-if="trip.preference" class="trip-card__tags">
      <text
        v-for="p in prefList"
        :key="p"
        class="trip-card__tag"
      >{{ p }}</text>
    </view>

    <view class="trip-card__foot">
      <text class="trip-card__time">{{ formatDate(trip.createTime) }}</text>
      <view v-if="showDelete" class="trip-card__del" @tap.stop="$emit('delete')">
        <text>删除</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { computed } from 'vue'
import { formatMoney, formatDate } from '../utils/format.js'

const props = defineProps({
  trip: { type: Object, required: true },
  showDelete: { type: Boolean, default: false }
})
defineEmits(['tap', 'delete'])

const prefList = computed(() => {
  const p = props.trip.preference
  if (!p) return []
  return Array.isArray(p) ? p : String(p).split(',').filter(Boolean)
})

const statusText = computed(() => {
  const s = props.trip.status || 'GENERATED'
  return s === 'DRAFT' ? '草稿' : '已生成'
})
const statusClass = computed(() => {
  const s = props.trip.status || 'GENERATED'
  return s === 'DRAFT' ? 'is-draft' : 'is-done'
})
</script>

<style lang="scss" scoped>
.trip-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 28rpx;
  margin: 0 24rpx 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(47, 111, 237, 0.06);
  &__head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    gap: 16rpx;
  }
  &__title {
    flex: 1;
    font-size: var(--fs-32);
    font-weight: 600;
    color: var(--text-primary);
  }
  &__status {
    font-size: var(--fs-22);
    padding: 4rpx 16rpx;
    border-radius: 20rpx;
    &.is-done {
      color: var(--brand);
      background: var(--brand-soft);
    }
    &.is-draft {
      color: var(--warning);
      background: #fff5e6;
    }
  }
  &__meta {
    display: flex;
    margin-top: 20rpx;
    gap: 24rpx;
  }
  &__meta-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6rpx;
  }
  &__meta-label {
    font-size: var(--fs-22);
    color: var(--text-muted);
  }
  &__meta-value {
    font-size: var(--fs-28);
    color: var(--text-primary);
    font-weight: 500;
  }
  &__tags {
    display: flex;
    flex-wrap: wrap;
    gap: 12rpx;
    margin-top: 18rpx;
  }
  &__tag {
    font-size: var(--fs-22);
    color: var(--brand);
    background: var(--brand-soft);
    padding: 4rpx 16rpx;
    border-radius: 20rpx;
  }
  &__foot {
    display: flex;
    align-items: center;
    justify-content: space-between;
    margin-top: 18rpx;
    padding-top: 18rpx;
    border-top: 1rpx solid var(--border-light);
  }
  &__time {
    font-size: var(--fs-22);
    color: var(--text-muted);
  }
  &__del {
    font-size: var(--fs-24);
    color: var(--danger);
    padding: 4rpx 16rpx;
  }
}
</style>
