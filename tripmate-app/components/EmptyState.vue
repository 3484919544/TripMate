<template>
  <view class="empty">
    <text class="empty__icon">{{ icon }}</text>
    <text class="empty__text">{{ text }}</text>
    <view v-if="actionText" class="empty__btn" @tap="$emit('action')">
      <text>{{ actionText }}</text>
    </view>
  </view>
</template>

<script setup>
defineProps({
  icon: { type: String, default: '🌫️' },
  text: { type: String, default: '暂无数据' },
  actionText: { type: String, default: '' }
})
defineEmits(['action'])
</script>

<style lang="scss" scoped>
.empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 120rpx 0;
  gap: 20rpx;
  &__icon {
    font-size: var(--fs-96);
  }
  &__text {
    font-size: var(--fs-26);
    color: #8a8a9e;
  }
  &__btn {
    padding: 16rpx 48rpx;
    background: #2f6fed;
    color: #fff;
    border-radius: 40rpx;
    font-size: var(--fs-26);
  }
}
</style>
