<template>
  <view class="result" v-if="trip">
    <!-- 标题区 -->
    <view class="result__head">
      <view class="result__head-row">
        <view class="result__head-left">
          <text class="result__title">{{ trip.title }}</text>
          <view class="result__head-meta">
            <AiBadge />
            <text class="result__dest">{{ trip.destination }} · {{ trip.days }} 天</text>
          </view>
        </view>
      </view>

      <view class="result__budget">
        <view class="result__budget-item">
          <text class="result__budget-label">预算</text>
          <text class="result__budget-value">{{ formatMoney(trip.budget) }}</text>
        </view>
        <view class="result__budget-sep"></view>
        <view class="result__budget-item">
          <text class="result__budget-label">预计花费</text>
          <text class="result__budget-value" :class="{ over: trip.overBudget }">
            {{ formatMoney(trip.totalCost) }}
          </text>
        </view>
      </view>
    </view>

    <BudgetAlert
      :budget="trip.budget"
      :total-cost="trip.totalCost"
      :over-budget="trip.overBudget"
    />

    <!-- 每日安排 -->
    <view class="result__days">
      <DayPlanCard
        v-for="(day, idx) in trip.dayList"
        :key="idx"
        :day="day"
        :editable="editing"
        @edit="onEditDay(day)"
      />
    </view>

    <!-- AI 免责声明 -->
    <view class="result__disclaimer">
      <text>* 以上内容由 AI 生成,仅供参考。景点开放时间、票价、交通请以实际为准。</text>
    </view>

    <!-- 操作区 -->
    <view class="result__actions">
      <view v-if="!editing" class="result__action-btn" @tap="editing = true">
        <text>✏️ 调整</text>
      </view>
      <view v-else class="result__action-btn result__action-btn--primary" @tap="onFinishEdit">
        <text>✓ 完成调整</text>
      </view>
      <view class="result__action-btn result__action-btn--primary" :class="{ disabled: saving }" @tap="onSave">
        <text>{{ saving ? '保存中...' : '💾 保存' }}</text>
      </view>
      <view class="result__action-btn" @tap="onRegenerate">
        <text>🔄 重新生成</text>
      </view>
      <view class="result__action-btn" @tap="onCopy">
        <text>📋 复制</text>
      </view>
      <view class="result__action-btn" @tap="onFeedback">
        <text>👎 不合适</text>
      </view>
    </view>

    <view class="result__bottom-space"></view>

    <!-- 编辑单日弹窗 -->
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
          <view class="modal__btn modal__btn--primary" @tap="onSaveDay">确定</view>
        </view>
      </view>
    </view>
  </view>

  <view v-else class="result-empty">
    <EmptyState icon="🗺️" text="没有可显示的行程" action-text="去创建" @action="goCreate" />
  </view>
</template>

<script setup>
import { ref, computed, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { useTripStore } from '../../store/trip.js'
import { saveTrip } from '../../api/trip.js'
import { submitFeedback } from '../../api/feedback.js'
import { formatMoney, tripToText } from '../../utils/format.js'
import AiBadge from '../../components/AiBadge.vue'
import BudgetAlert from '../../components/BudgetAlert.vue'
import DayPlanCard from '../../components/DayPlanCard.vue'
import EmptyState from '../../components/EmptyState.vue'

const tripStore = useTripStore()
const trip = computed(() => tripStore.current)

const editing = ref(false)
const saving = ref(false)

const editDayVisible = ref(false)
const editDay = reactive({})

function onEditDay(day) {
  Object.assign(editDay, day) // 浅拷贝字段
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
  // 重算总花费
  recalcCost()
  editDayVisible.value = false
}
function recalcCost() {
  const t = trip.value
  if (!t) return
  t.totalCost = (t.dayList || []).reduce((s, d) => s + (Number(d.cost) || 0), 0)
  t.overBudget = t.totalCost > Number(t.budget)
}
function onFinishEdit() {
  recalcCost()
  editing.value = false
  uni.showToast({ title: '已更新,记得保存', icon: 'none' })
}

async function onSave() {
  if (saving.value) return
  saving.value = true
  try {
    const data = await saveTrip({ ...trip.value })
    uni.showToast({ title: '保存成功', icon: 'success' })
    // 保存后跳详情
    const id = (data && (data.id || data)) || trip.value.id
    if (id) {
      setTimeout(() => {
        uni.redirectTo({ url: '/pages/detail/detail?id=' + id })
      }, 600)
    }
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

function onRegenerate() {
  uni.showModal({
    title: '重新生成',
    content: '当前结果将被覆盖,确定吗?',
    success: (r) => {
      if (r.confirm) {
        uni.redirectTo({ url: '/pages/create-trip/create-trip' })
      }
    }
  })
}

function onCopy() {
  const text = tripToText(trip.value)
  uni.setClipboardData({
    data: text,
    success: () => uni.showToast({ title: '已复制,可粘贴给朋友', icon: 'none' })
  })
}

function onFeedback() {
  uni.navigateTo({ url: '/pages/feedback/feedback?from=result' })
}

function goCreate() {
  uni.redirectTo({ url: '/pages/create-trip/create-trip' })
}
</script>

<style lang="scss" scoped>
.result {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 40rpx;

  &__head {
    background: var(--bg-card);
    padding: 32rpx 24rpx 28rpx;
    margin: 0 0 24rpx;
  }
  &__head-row {
    display: flex;
    align-items: flex-start;
    justify-content: space-between;
  }
  &__title {
    font-size: var(--fs-38);
    font-weight: 700;
    color: var(--text-primary);
    line-height: 1.4;
  }
  &__head-meta {
    display: flex;
    align-items: center;
    gap: 12rpx;
    margin-top: 14rpx;
  }
  &__dest {
    font-size: var(--fs-24);
    color: var(--text-muted);
  }
  &__budget {
    display: flex;
    align-items: center;
    margin-top: 24rpx;
    background: var(--bg-input);
    border-radius: 16rpx;
    padding: 24rpx 0;
  }
  &__budget-item {
    flex: 1;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;
  }
  &__budget-label {
    font-size: var(--fs-22);
    color: var(--text-muted);
  }
  &__budget-value {
    font-size: var(--fs-36);
    font-weight: 700;
    color: var(--text-primary);
    &.over {
      color: var(--danger);
    }
  }
  &__budget-sep {
    width: 1rpx;
    height: 60rpx;
    background: var(--border-light);
  }
  &__disclaimer {
    margin: 0 24rpx 24rpx;
    padding: 20rpx 24rpx;
    background: #fff8e6;
    border-radius: 14rpx;
    font-size: var(--fs-22);
    color: #9a7a2e;
    line-height: 1.5;
  }
  &__actions {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    padding: 0 24rpx;
  }
  &__action-btn {
    flex: 1;
    min-width: 200rpx;
    text-align: center;
    padding: 22rpx 0;
    background: var(--bg-card);
    border-radius: 16rpx;
    font-size: var(--fs-28);
    color: var(--text-primary);
    box-shadow: 0 4rpx 16rpx rgba(47, 111, 237, 0.06);
    &--primary {
      background: var(--brand);
      color: var(--text-on-brand);
      font-weight: 600;
    }
    &.disabled {
      opacity: 0.6;
    }
  }
  &__bottom-space {
    height: 40rpx;
  }
}
.result-empty {
  min-height: 100vh;
  display: flex;
  align-items: center;
  justify-content: center;
  background: var(--bg-page);
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
