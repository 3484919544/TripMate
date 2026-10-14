<template>
  <view class="ai-badge">
    <text class="ai-badge__icon">✨</text>
    <text class="ai-badge__text">AI 生成</text>
  </view>
</template>

<script setup>
defineProps({
  size: { type: String, default: 'normal' }
})
</script>

<style lang="scss" scoped>
.ai-badge {
  display: inline-flex;
  align-items: center;
  gap: 4rpx;
  padding: 4rpx 14rpx;
  background: linear-gradient(135deg, #2f6fed, #6f8ef5);
  color: #fff;
  border-radius: 20rpx;
  font-size: var(--fs-20);
  line-height: 1.6;
  &__icon {
    font-size: var(--fs-22);
  }
}
</style>
