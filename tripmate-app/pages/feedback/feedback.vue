<template>
  <view class="feedback">
    <!-- 提交反馈 -->
    <view class="feedback__card">
      <view class="feedback__field">
        <text class="feedback__label">反馈内容</text>
        <textarea
          v-model="content"
          class="feedback__textarea"
          placeholder="说说哪里不合适,或给我们提建议..."
          maxlength="500"
        />
        <text class="feedback__count">{{ content.length }}/500</text>
      </view>

      <view class="feedback__field">
        <text class="feedback__label">问题类型</text>
        <view class="feedback__tags">
          <view
            v-for="t in types"
            :key="t"
            class="feedback__tag"
            :class="{ active: type === t }"
            @tap="type = t"
          >{{ t }}</view>
        </view>
      </view>
    </view>

    <view class="feedback__footer">
      <button
        class="feedback__btn"
        :loading="submitting"
        :disabled="submitting"
        @tap="onSubmit"
      >提交反馈</button>
    </view>

    <!-- 我的反馈历史 -->
    <view class="feedback__history">
      <view class="feedback__history-head">
        <text class="feedback__history-title">我的反馈</text>
        <text class="feedback__history-refresh" @tap="loadMyList">刷新</text>
      </view>

      <view v-if="myList.length === 0 && !loadingList" class="feedback__empty">
        <text>暂无反馈记录</text>
      </view>

      <view v-for="fb in myList" :key="fb.id" class="feedback__item">
        <view class="feedback__item-top">
          <text class="feedback__item-time">{{ formatTime(fb.createTime) }}</text>
          <text
            class="feedback__item-status"
            :class="fb.status === 'RESOLVED' ? 'status-done' : 'status-pending'"
          >{{ fb.status === 'RESOLVED' ? '已处理' : '待处理' }}</text>
        </view>
        <view class="feedback__item-content">{{ fb.content }}</view>
        <!-- 管理员回复 -->
        <view v-if="fb.reply" class="feedback__reply">
          <view class="feedback__reply-head">
            <text class="feedback__reply-label">管理员回复</text>
            <text v-if="fb.replyTime" class="feedback__reply-time">{{ formatTime(fb.replyTime) }}</text>
          </view>
          <text class="feedback__reply-text">{{ fb.reply }}</text>
        </view>
      </view>

      <view v-if="loadingList" class="feedback__loading">加载中...</view>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { submitFeedback, myFeedbackList } from '../../api/feedback.js'
import { validateFeedback } from '../../utils/validator.js'

const content = ref('')
const type = ref('行程不准')
const submitting = ref(false)
const myList = ref([])
const loadingList = ref(false)

const types = ['行程不准', '超预算', '内容违规', '功能建议', '其它']

onLoad(() => {})

onMounted(() => {
  loadMyList()
})

async function onSubmit() {
  const err = validateFeedback(content.value)
  if (err) {
    uni.showToast({ title: err, icon: 'none' })
    return
  }
  submitting.value = true
  try {
    await submitFeedback({ content: '[' + type.value + '] ' + content.value })
    uni.showToast({ title: '感谢反馈!', icon: 'success' })
    content.value = ''
    loadMyList()
  } catch (e) {
    uni.showToast({ title: e.message || '提交失败', icon: 'none' })
  } finally {
    submitting.value = false
  }
}

async function loadMyList() {
  loadingList.value = true
  try {
    myList.value = await myFeedbackList()
  } catch (e) {
    /* ignore */
  } finally {
    loadingList.value = false
  }
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').substring(0, 16)
}
</script>

<style lang="scss" scoped>
.feedback {
  min-height: 100vh;
  background: var(--bg-page);
  padding: 24rpx;
  &__card {
    background: var(--bg-card);
    border-radius: 24rpx;
    padding: 32rpx;
    box-shadow: 0 4rpx 24rpx rgba(47, 111, 237, 0.06);
  }
  &__field { margin-bottom: 32rpx; &:last-child { margin-bottom: 0; } }
  &__label { display: block; font-size: var(--fs-28); color: var(--text-primary); font-weight: 600; margin-bottom: 16rpx; }
  &__textarea { width: 100%; height: 240rpx; background: var(--bg-input); border: 1rpx solid var(--border-light); border-radius: 14rpx; padding: 20rpx; font-size: var(--fs-28); line-height: 1.5; color: var(--text-primary); }
  &__count { display: block; text-align: right; font-size: var(--fs-22); color: var(--text-muted); margin-top: 8rpx; }
  &__tags { display: flex; flex-wrap: wrap; gap: 20rpx; }
  &__tag { padding: 14rpx 32rpx; background: var(--bg-input); border: 1rpx solid var(--border-light); border-radius: 40rpx; font-size: var(--fs-26); color: var(--text-secondary); &.active { background: var(--brand-soft); border-color: var(--brand); color: var(--brand); font-weight: 600; } }
  &__footer { margin-top: 32rpx; }
  &__btn { width: 100%; height: 96rpx; line-height: 96rpx; background: linear-gradient(135deg, var(--brand), #5a8af5); color: var(--text-on-brand); font-size: var(--fs-32); border-radius: 18rpx; font-weight: 600; &[disabled] { opacity: 0.7; } }

  // 历史
  &__history { margin-top: 40rpx; }
  &__history-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 20rpx; padding: 0 8rpx; }
  &__history-title { font-size: var(--fs-30); font-weight: 700; color: var(--text-primary); }
  &__history-refresh { font-size: var(--fs-24); color: var(--brand); }
  &__empty { text-align: center; padding: 60rpx 0; color: var(--text-muted); font-size: var(--fs-26); }
  &__loading { text-align: center; padding: 20rpx; color: var(--text-muted); font-size: var(--fs-24); }

  &__item { background: var(--bg-card); border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
  &__item-top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 10rpx; }
  &__item-time { font-size: var(--fs-22); color: var(--text-muted); }
  &__item-status { font-size: var(--fs-22); padding: 4rpx 16rpx; border-radius: 20rpx; }
  &__item-status.status-pending { background: #fff3cd; color: #ff9500; }
  &__item-status.status-done { background: var(--brand-soft); color: var(--brand); }
  &__item-content { font-size: var(--fs-28); color: var(--text-primary); line-height: 1.5; }

  &__reply { margin-top: 16rpx; padding: 16rpx 20rpx; background: var(--brand-soft); border-radius: 12rpx; border-left: 6rpx solid var(--brand); }
  &__reply-head { display: flex; justify-content: space-between; align-items: center; margin-bottom: 8rpx; }
  &__reply-label { font-size: var(--fs-24); color: var(--brand); font-weight: 600; }
  &__reply-time { font-size: var(--fs-20); color: var(--text-muted); }
  &__reply-text { font-size: var(--fs-26); color: var(--text-primary); line-height: 1.5; }
}
</style>
