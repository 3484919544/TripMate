<template>
  <view class="admin-fb">
    <!-- 状态筛选 -->
    <view class="tabs">
      <view
        class="tabs__item"
        :class="{ active: filter === 'ALL' }"
        @tap="filter = 'ALL'; loadList()"
      >全部</view>
      <view
        class="tabs__item"
        :class="{ active: filter === 'PENDING' }"
        @tap="filter = 'PENDING'; loadList()"
      >待处理({{ pendingCount }})</view>
      <view
        class="tabs__item"
        :class="{ active: filter === 'RESOLVED' }"
        @tap="filter = 'RESOLVED'; loadList()"
      >已处理</view>
    </view>

    <!-- 列表 -->
    <view v-if="filteredList.length === 0" class="empty">
      <text>暂无反馈</text>
    </view>

    <view v-for="fb in filteredList" :key="fb.id" class="fb-item">
      <view class="fb-item__top">
        <text class="fb-item__user">用户 #{{ fb.userId }}</text>
        <text
          class="fb-item__status"
          :class="fb.status === 'RESOLVED' ? 'done' : 'pending'"
        >{{ fb.status === 'RESOLVED' ? '已处理' : '待处理' }}</text>
      </view>
      <text class="fb-item__time">{{ formatTime(fb.createTime) }}</text>
      <view class="fb-item__content">{{ fb.content }}</view>

      <!-- 已有回复 -->
      <view v-if="fb.reply" class="fb-reply">
        <text class="fb-reply__label">管理员回复:</text>
        <text class="fb-reply__text">{{ fb.reply }}</text>
        <text class="fb-reply__time">{{ formatTime(fb.replyTime) }}</text>
      </view>

      <view class="fb-item__actions">
        <view v-if="fb.status !== 'RESOLVED'" class="btn btn-primary" @tap="openReply(fb)">回复</view>
        <view class="btn btn-danger" @tap="onDelete(fb)">删除</view>
      </view>
    </view>

    <!-- 回复弹窗 -->
    <view v-if="replyingFb" class="popup-mask" @tap.self="replyingFb = null">
      <view class="popup-box">
        <view class="popup-title">回复反馈 #{{ replyingFb.id }}</view>
        <view class="popup-sub">用户:{{ replyingFb.content }}</view>
        <textarea
          class="popup-textarea"
          v-model="replyText"
          placeholder="请输入回复内容..."
          maxlength="500"
          :auto-height="true"
        />
        <view class="popup-actions">
          <view class="btn" @tap="replyingFb = null">取消</view>
          <view class="btn btn-primary" @tap="onSubmitReply">提交回复</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { adminFeedbackList, adminReplyFeedback, adminDeleteFeedback } from '../../api/feedback.js'

const list = ref([])
const filter = ref('ALL')
const replyingFb = ref(null)
const replyText = ref('')

const filteredList = computed(() => {
  if (filter.value === 'ALL') return list.value
  return list.value.filter(f => f.status === filter.value)
})
const pendingCount = computed(() => list.value.filter(f => f.status !== 'RESOLVED').length)

onMounted(() => loadList())

async function loadList() {
  try {
    list.value = await adminFeedbackList()
  } catch (e) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  }
}

function openReply(fb) {
  replyingFb.value = fb
  replyText.value = fb.reply || ''
}

async function onSubmitReply() {
  if (!replyText.value.trim()) {
    return uni.showToast({ title: '回复内容不能为空', icon: 'none' })
  }
  try {
    await adminReplyFeedback(replyingFb.value.id, replyText.value.trim())
    uni.showToast({ title: '已回复' })
    replyingFb.value = null
    loadList()
  } catch (e) {
    uni.showToast({ title: e.message || '回复失败', icon: 'none' })
  }
}

function onDelete(fb) {
  uni.showModal({
    title: '删除反馈',
    content: '确定删除这条反馈?此操作不可撤销。',
    confirmColor: '#ff3b30',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await adminDeleteFeedback(fb.id)
        uni.showToast({ title: '已删除' })
        loadList()
      } catch (e) {
        uni.showToast({ title: e.message || '删除失败', icon: 'none' })
      }
    }
  })
}

function formatTime(t) {
  if (!t) return ''
  return String(t).replace('T', ' ').substring(0, 16)
}
</script>

<style lang="scss" scoped>
.admin-fb { min-height: 100vh; background: var(--bg-page); padding: 20rpx; }

.tabs { display: flex; gap: 16rpx; padding: 0 8rpx 20rpx; }
.tabs__item { padding: 14rpx 28rpx; background: var(--bg-card); border-radius: 30rpx; font-size: var(--fs-26); color: var(--text-secondary); border: 1rpx solid var(--border-light); }
.tabs__item.active { background: var(--brand); color: var(--text-on-brand); border-color: var(--brand); font-weight: 600; }

.empty { text-align: center; padding: 80rpx 0; color: var(--text-muted); font-size: var(--fs-26); }

.fb-item { background: var(--bg-card); border-radius: 16rpx; padding: 24rpx; margin-bottom: 16rpx; }
.fb-item__top { display: flex; justify-content: space-between; align-items: center; margin-bottom: 6rpx; }
.fb-item__user { font-size: var(--fs-24); color: var(--text-muted); }
.fb-item__status { font-size: var(--fs-22); padding: 4rpx 16rpx; border-radius: 20rpx; }
.fb-item__status.pending { background: #fff3cd; color: #ff9500; }
.fb-item__status.done { background: var(--brand-soft); color: var(--brand); }
.fb-item__time { font-size: var(--fs-22); color: var(--text-muted); display: block; margin-bottom: 10rpx; }
.fb-item__content { font-size: var(--fs-28); color: var(--text-primary); line-height: 1.5; }

.fb-reply { margin-top: 16rpx; padding: 16rpx 20rpx; background: var(--brand-soft); border-radius: 12rpx; border-left: 6rpx solid var(--brand); display: flex; flex-direction: column; gap: 6rpx; }
.fb-reply__label { font-size: var(--fs-22); color: var(--brand); font-weight: 600; }
.fb-reply__text { font-size: var(--fs-26); color: var(--text-primary); line-height: 1.4; }
.fb-reply__time { font-size: var(--fs-20); color: var(--text-muted); align-self: flex-end; }

.fb-item__actions { display: flex; gap: 16rpx; margin-top: 20rpx; }
.btn { padding: 14rpx 32rpx; border-radius: 12rpx; font-size: var(--fs-26); background: var(--bg-input); color: var(--text-secondary); }
.btn-primary { background: var(--brand); color: var(--text-on-brand); }
.btn-danger { background: #fff5f3; color: var(--danger); }

.popup-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.popup-box { background: var(--bg-card); border-radius: 24rpx; padding: 40rpx; width: 620rpx; }
.popup-title { font-size: var(--fs-30); font-weight: 600; margin-bottom: 12rpx; color: var(--text-primary); text-align: center; }
.popup-sub { font-size: var(--fs-24); color: var(--text-muted); background: var(--bg-input); padding: 12rpx 16rpx; border-radius: 10rpx; margin-bottom: 20rpx; }
.popup-textarea { width: 100%; min-height: 140rpx; background: var(--bg-input); border: 1rpx solid var(--border-light); border-radius: 12rpx; padding: 16rpx 20rpx; font-size: var(--fs-28); color: var(--text-primary); box-sizing: border-box; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 24rpx; }
.popup-actions .btn { flex: 1; text-align: center; padding: 18rpx 0; border-radius: 12rpx; font-size: var(--fs-28); }
.popup-actions .btn-primary { background: var(--brand); color: var(--text-on-brand); }
</style>
