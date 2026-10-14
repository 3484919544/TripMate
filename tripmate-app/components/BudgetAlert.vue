<template>
  <view v-if="overBudget" class="budget-alert">
    <text class="budget-alert__icon">⚠️</text>
    <view class="budget-alert__body">
      <text class="budget-alert__title">超出预算提醒</text>
      <text class="budget-alert__desc">
        预计花费 {{ formatMoney(totalCost) }} 已超出预算 {{ formatMoney(budget) }},
        可点击"重新生成"或手动调整。
      </text>
    </view>
  </view>
</template>

<script setup>
import { formatMoney } from '../utils/format.js'

defineProps({
  budget: { type: Number, default: 0 },
  totalCost: { type: Number, default: 0 },
  overBudget: { type: Boolean, default: false }
})
</script>

<style lang="scss" scoped>
.budget-alert {
  display: flex;
  align-items: flex-start;
  gap: 12rpx;
  padding: 20rpx 24rpx;
  margin: 0 24rpx 24rpx;
  background: #fff5f3;
  border: 1rpx solid #ffd9cc;
  border-radius: 16rpx;
  &__icon {
    font-size: var(--fs-30);
    line-height: 1.4;
  }
  &__body {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6rpx;
  }
  &__title {
    font-size: var(--fs-26);
    font-weight: 600;
    color: #ff3b30;
  }
  &__desc {
    font-size: var(--fs-24);
    color: #7a4a42;
    line-height: 1.5;
  }
}
</style>
