<template>
  <view class="day-card">
    <view class="day-card__head">
      <view class="day-card__day">Day {{ day.dayIndex }}</view>
      <text class="day-card__theme ellipsis">{{ day.theme || '今日行程' }}</text>
      <text class="day-card__cost">{{ formatMoney(day.cost) }}</text>
    </view>

    <view class="day-card__slots">
      <view class="slot">
        <view class="slot__badge slot__badge--am">上午</view>
        <text class="slot__text">{{ day.morning || '自由安排' }}</text>
      </view>
      <view class="slot">
        <view class="slot__badge slot__badge--pm">下午</view>
        <text class="slot__text">{{ day.afternoon || '自由安排' }}</text>
      </view>
      <view class="slot">
        <view class="slot__badge slot__badge--ev">晚上</view>
        <text class="slot__text">{{ day.evening || '自由安排' }}</text>
      </view>
    </view>

    <view v-if="day.tips" class="day-card__tips">
      <text class="day-card__tips-label">💡 提示</text>
      <text class="day-card__tips-text">{{ day.tips }}</text>
    </view>

    <view v-if="editable" class="day-card__edit" @tap="$emit('edit', day)">
      <text>编辑</text>
    </view>
  </view>
</template>

<script setup>
import { formatMoney } from '../utils/format.js'

defineProps({
  day: { type: Object, required: true },
  editable: { type: Boolean, default: false }
})
defineEmits(['edit'])
</script>

<style lang="scss" scoped>
.day-card {
  background: var(--bg-card);
  border-radius: 20rpx;
  padding: 28rpx;
  margin: 0 24rpx 24rpx;
  box-shadow: 0 4rpx 24rpx rgba(47, 111, 237, 0.06);
  &__head {
    display: flex;
    align-items: center;
    gap: 16rpx;
  }
  &__day {
    font-size: var(--fs-24);
    color: var(--text-on-brand);
    background: var(--brand);
    padding: 6rpx 18rpx;
    border-radius: 20rpx;
    font-weight: 600;
  }
  &__theme {
    flex: 1;
    font-size: var(--fs-30);
    font-weight: 600;
    color: var(--text-primary);
  }
  &__cost {
    font-size: var(--fs-28);
    color: #ff6b35;
    font-weight: 600;
  }
  &__slots {
    margin-top: 24rpx;
    display: flex;
    flex-direction: column;
    gap: 18rpx;
  }
  .slot {
    display: flex;
    align-items: flex-start;
    gap: 16rpx;
    &__badge {
      flex-shrink: 0;
      width: 80rpx;
      text-align: center;
      font-size: var(--fs-22);
      padding: 4rpx 0;
      border-radius: 12rpx;
      &--am {
        color: var(--warning);
        background: #fff5e6;
      }
      &--pm {
        color: var(--brand);
        background: var(--brand-soft);
      }
      &--ev {
        color: #7b5cff;
        background: #f0eaff;
      }
    }
    &__text {
      flex: 1;
      font-size: var(--fs-26);
      color: var(--text-primary);
      line-height: 1.6;
    }
  }
  &__tips {
    margin-top: 20rpx;
    padding: 16rpx 20rpx;
    background: var(--bg-input);
    border-radius: 12rpx;
    display: flex;
    flex-direction: column;
    gap: 6rpx;
  }
  &__tips-label {
    font-size: var(--fs-24);
    color: var(--warning);
    font-weight: 600;
  }
  &__tips-text {
    font-size: var(--fs-24);
    color: var(--text-secondary);
    line-height: 1.5;
  }
  &__edit {
    margin-top: 18rpx;
    text-align: right;
    font-size: var(--fs-24);
    color: var(--brand);
  }
}
</style>
