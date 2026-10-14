<template>
  <view class="detail" v-if="trip">
    <view class="detail__head">
      <text class="detail__title">{{ trip.title }}</text>
      <view class="detail__meta">
        <AiBadge />
        <text class="detail__meta-text">{{ trip.destination }} · {{ trip.days }} 天 · {{ formatDate(trip.createTime) }}</text>
      </view>
      <view v-if="trip.preference" class="detail__tags">
        <text v-for="p in prefList" :key="p" class="detail__tag">{{ p }}</text>
      </view>
    </view>

    <BudgetAlert
      :budget="trip.budget"
      :total-cost="trip.totalCost"
      :over-budget="trip.overBudget"
    />

    <view class="detail__summary">
      <view class="detail__summary-item">
        <text class="detail__summary-label">预算</text>
        <text class="detail__summary-value">{{ formatMoney(trip.budget) }}</text>
      </view>
      <view class="detail__summary-item">
        <text class="detail__summary-label">预计花费</text>
        <text class="detail__summary-value" :class="{ over: trip.overBudget }">{{ formatMoney(trip.totalCost) }}</text>
      </view>
      <view class="detail__summary-item">
        <text class="detail__summary-label">剩余</text>
        <text class="detail__summary-value" :class="{ over: trip.overBudget }">
          {{ formatMoney(trip.budget - trip.totalCost) }}
        </text>
      </view>
    </view>

    <view class="detail__days">
      <DayPlanCard
        v-for="(day, idx) in trip.dayList"
        :key="idx"
        :day="day"
        :editable="editing"
        @edit="onEditDay(day)"
      />
    </view>

    <view class="detail__actions">
      <view class="detail__action-btn" @tap="toggleEdit">
        <text>{{ editing ? '✓ 完成编辑' : '✏️ 编辑' }}</text>
      </view>
      <view class="detail__action-btn detail__action-btn--danger" @tap="onDelete">
        <text>🗑 删除</text>
      </view>
      <view class="detail__action-btn" @tap="onCopy">
        <text>📋 复制</text>
      </view>
    </view>

    <view class="detail__bottom-space"></view>

    <!-- 编辑单日弹窗(复用结果页逻辑) -->
    <view v-if="editDayVisible" class="modal">
      <view class="modal__box">
        <text class="modal__title">编辑 Day {{ editDay.dayIndex }}</text>
        <view class="modal__field">
          <text class="modal__label">主题</text>
          <input v-model="editDay.theme" class="modal__input" />
        </view>
        <view class="modal__field">
          <text class="modal__label">上午</text>
          <textarea v-model="editDay.morning" class="modal__textarea" />
        </view>
        <view class="modal__field">
          <text class="modal__label">下午</text>
          <textarea v-model="editDay.afternoon" class="modal__textarea" />
        </view>
        <view class="modal__field">
          <text class="modal__label">晚上</text>
          <textarea v-model="editDay.evening" class="modal__textarea" />
        </view>
        <view class="modal__field">
          <text class="modal__label">花费</text>
          <input v-model="editDay.cost" type="digit" class="modal__input" />
        </view>
        <view class="modal__field">
          <text class="modal__label">提示</text>
          <textarea v-model="editDay.tips" class="modal__textarea" />
        </view>
        <view class="modal__btns">
          <view class="modal__btn" @tap="editDayVisible = false">取消</view>
          <view class="modal__btn modal__btn--primary" @tap="onSaveDay">保存</view>
        </view>
      </view>
    </view>
  </view>

  <view v-else class="detail-loading">
    <text>加载中...</text>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onLoad, onShow } from '@dcloudio/uni-app'
import { getTrip, updateTrip, deleteTrip } from '../../api/trip.js'
import { formatMoney, formatDate, tripToText } from '../../utils/format.js'
import AiBadge from '../../components/AiBadge.vue'
import BudgetAlert from '../../components/BudgetAlert.vue'
import DayPlanCard from '../../components/DayPlanCard.vue'

const trip = ref(null)
const editing = ref(false)
const editDayVisible = ref(false)
const editDay = reactive({})
let tripId = null

onLoad((options) => {
  tripId = options && options.id
  if (!tripId) {
    uni.showToast({ title: '缺少行程 ID', icon: 'none' })
    return
  }
  loadTrip()
})

onShow(() => {
  if (tripId && trip.value) loadTrip(true)
})

async function loadTrip(silent) {
  try {
    const data = await getTrip(tripId)
    trip.value = data
  } catch (e) {
    if (!silent) uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  }
}

const prefList = computed(() => {
  const p = trip.value && trip.value.preference
  if (!p) return []
  return Array.isArray(p) ? p : String(p).split(',').filter(Boolean)
})

function onEditDay(day) {
  Object.keys(day).forEach((k) => (editDay[k] = day[k]))
  editDay._ref = day
  editDayVisible.value = true
}

function onSaveDay() {
  const ref = editDay._ref
  if (ref) {
    ref.theme = editDay.theme
    ref.morning = editDay.morning
    ref.afternoon = editDay.afternoon
    ref.evening = editDay.evening
    ref.cost = Number(editDay.cost) || 0
    ref.tips = editDay.tips
  }
  recalc()
  editDayVisible.value = false
  // 编辑后自动保存
  persistUpdate()
}

function recalc() {
  const t = trip.value
  if (!t) return
  t.totalCost = (t.dayList || []).reduce((s, d) => s + (Number(d.cost) || 0), 0)
  t.overBudget = t.totalCost > Number(t.budget)
}

function toggleEdit() {
  if (editing.value) {
    persistUpdate()
  }
  editing.value = !editing.value
}

async function persistUpdate() {
  try {
    await updateTrip(tripId, trip.value)
    uni.showToast({ title: '已保存', icon: 'success' })
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  }
}

function onDelete() {
  uni.showModal({
    title: '删除行程',
    content: '删除后不可恢复,确定删除?',
    confirmColor: '#ff3b30',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await deleteTrip(tripId)
        uni.showToast({ title: '已删除', icon: 'success' })
        setTimeout(() => uni.navigateBack(), 500)
      } catch (e) {
        uni.showToast({ title: e.message || '删除失败', icon: 'none' })
      }
    }
  })
}

function onCopy() {
  uni.setClipboardData({
    data: tripToText(trip.value),
    success: () => uni.showToast({ title: '已复制', icon: 'none' })
  })
}
</script>

<style lang="scss" scoped>
.detail {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 40rpx;
  &__head {
    background: var(--bg-card);
    padding: 32rpx 24rpx;
    margin-bottom: 24rpx;
  }
  &__title {
    font-size: var(--fs-38);
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.4;
  }
  &__meta {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-top: 14rpx;
  }
  &__meta-text {
    font-size: var(--fs-24);
    color: var(--text-muted);
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
    border-radius: 16rpx;
  }
  &__summary {
    display: flex;
    margin: 0 24rpx 24rpx;
    background: var(--bg-card);
    border-radius: 16rpx;
    padding: 24rpx 0;
    box-shadow: 0 4rpx 16rpx rgba(47, 111, 237, 0.06);
  }
  &__summary-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;
  }
  &__summary-label {
    font-size: var(--fs-22);
    color: var(--text-muted);
  }
  &__summary-value {
    font-size: var(--fs-32);
    font-weight: 700;
    color: var(--text-primary);
    &.over {
      color: var(--danger);
    }
  }
  &__actions {
    display: flex;
    gap: 20rpx;
    padding: 0 24rpx;
  }
  &__action-btn {
    flex: 1;
    text-align: center;
    padding: 22rpx 0;
    background: var(--bg-card);
    border-radius: 16rpx;
    font-size: var(--fs-28);
    color: var(--text-primary);
    box-shadow: 0 4rpx 16rpx rgba(47, 111, 237, 0.06);
    &--danger {
      color: var(--danger);
    }
  }
  &__bottom-space {
    height: 40rpx;
  }
}
.detail-loading {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  color: var(--text-muted);
}
.modal {
  position: fixed;
  inset: 0;
  background: rgba(0, 0, 0, 0.5);
  z-index: 1000;
  display: flex;
  align-items: flex-end;
  justify-content: center;
  &__box {
    width: 100%;
    max-height: 85vh;
    overflow-y: auto;
    background: var(--bg-card);
    border-radius: 28rpx 28rpx 0 0;
    padding: 32rpx 32rpx calc(32rpx + env(safe-area-inset-bottom));
  }
  &__title {
    font-size: var(--fs-34);
    font-weight: 700;
    color: var(--text-primary);
    margin-bottom: 24rpx;
    display: block;
  }
  &__field {
    margin-bottom: 20rpx;
  }
  &__label {
    display: block;
    font-size: var(--fs-24);
    color: var(--text-secondary);
    margin-bottom: 10rpx;
  }
  &__input {
    width: 100%;
    height: 80rpx;
    background: var(--bg-input);
    border-radius: 12rpx;
    padding: 0 20rpx;
    font-size: var(--fs-28);
  }
  &__textarea {
    width: 100%;
    height: 120rpx;
    background: var(--bg-input);
    border-radius: 12rpx;
    padding: 16rpx 20rpx;
    font-size: var(--fs-28);
  }
  &__btns {
    display: flex;
    gap: 20rpx;
    margin-top: 12rpx;
  }
  &__btn {
    flex: 1;
    text-align: center;
    padding: 22rpx 0;
    border-radius: 14rpx;
    font-size: var(--fs-30);
    background: var(--border-light);
    color: var(--text-secondary);
    &--primary {
      background: var(--brand);
      color: var(--text-on-brand);
      font-weight: 600;
    }
  }
}
</style>
