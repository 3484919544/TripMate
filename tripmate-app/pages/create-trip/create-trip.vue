<template>
  <view class="create">
    <view class="create__form">
      <!-- 出发地 -->
      <view class="field">
        <text class="field__label">出发地<text class="field__hint">(选填,用于计算交通费用)</text></text>
        <view class="field__input-wrap">
          <input
            v-model="form.origin"
            class="field__input"
            placeholder="如:上海"
            maxlength="20"
          />
          <text v-if="form.origin" class="field__clear" @tap="form.origin = ''">✕</text>
        </view>
      </view>

      <!-- 目的地 -->
      <view class="field">
        <text class="field__label">目的地</text>
        <view class="field__input-wrap">
          <input
            v-model="form.destination"
            class="field__input"
            placeholder="如:北京"
            maxlength="20"
          />
          <text v-if="form.destination" class="field__clear" @tap="form.destination = ''">✕</text>
        </view>
      </view>

      <!-- 天数 -->
      <view class="field">
        <text class="field__label">行程天数</text>
        <view class="stepper">
          <view class="stepper__btn" @tap="changeDays(-1)">−</view>
          <text class="stepper__value">{{ form.days }} 天</text>
          <view class="stepper__btn" @tap="changeDays(1)">+</view>
        </view>
      </view>

      <!-- 预算 -->
      <view class="field">
        <text class="field__label">预算(元)</text>
        <view class="field__input-wrap">
          <input
            v-model="form.budget"
            class="field__input"
            type="digit"
            placeholder="如:3000"
            maxlength="8"
          />
          <text class="field__suffix">元</text>
        </view>
      </view>

      <!-- 偏好 -->
      <view class="field">
        <text class="field__label">偏好(可多选)</text>
        <view class="tags">
          <view
            v-for="p in preferences"
            :key="p"
            class="tags__item"
            :class="{ active: isSelected(p) }"
            @tap="togglePref(p)"
          >
            <text>{{ p }}</text>
          </view>
        </view>
      </view>

      <!-- 其他想法 -->
      <view class="field">
        <text class="field__label">其他想法<text class="field__hint">(选填,纳入行程计算)</text></text>
        <textarea
          v-model="form.notes"
          class="field__textarea"
          placeholder="如:想带老人,行程要宽松;必去迪士尼;预算含机票等"
          maxlength="200"
          :auto-height="true"
        />
      </view>
    </view>

    <!-- 生成按钮 -->
    <view class="create__footer">
      <button
        class="create__btn"
        :loading="generating"
        :disabled="generating"
        @tap="onGenerate"
      >
        {{ generating ? 'AI 生成中...' : '生成行程' }}
      </button>
      <text v-if="generating" class="create__hint">通常 10 秒内返回,请稍候</text>
      <view v-if="errMsg" class="create__error">
        <text class="create__error-text">{{ errMsg }}</text>
        <text class="create__error-retry" @tap="onGenerate">重试</text>
      </view>
    </view>

    <!-- 生成中遮罩 -->
    <view v-if="generating" class="mask">
      <view class="mask__box">
        <view class="mask__loader"></view>
        <text class="mask__text">AI 正在为你规划行程...</text>
        <text class="mask__sub">{{ form.origin ? form.origin + ' → ' : '' }}{{ form.destination }} · {{ form.days }} 天</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onLoad } from '@dcloudio/uni-app'
import { generateTrip } from '../../api/trip.js'
import { useTripStore } from '../../store/trip.js'
import { validateTrip } from '../../utils/validator.js'

const tripStore = useTripStore()

const preferences = ['文化', '美食', '亲子', '自然', '购物', '休闲']
const selected = ref([])

const form = reactive({
  origin: '',
  destination: '',
  days: 3,
  budget: '',
  preference: '',
  notes: ''
})

const generating = ref(false)
const errMsg = ref('')

onLoad((options) => {
  // 从首页热门目的地快速带入
  if (options && options.destination) {
    form.destination = decodeURIComponent(options.destination)
  }
})

function isSelected(p) {
  return selected.value.includes(p)
}
function togglePref(p) {
  if (isSelected(p)) {
    selected.value = selected.value.filter((x) => x !== p)
  } else {
    selected.value.push(p)
  }
  form.preference = selected.value.join(',')
}

function changeDays(delta) {
  const next = Number(form.days) + delta
  if (next < 1) {
    uni.showToast({ title: '天数至少 1 天', icon: 'none' })
    return
  }
  if (next > 30) {
    uni.showToast({ title: '天数过多,建议不超过 30 天', icon: 'none' })
    return
  }
  form.days = next
}

async function onGenerate() {
  errMsg.value = ''
  const payload = {
    origin: form.origin || '',
    destination: form.destination,
    days: Number(form.days),
    budget: Number(form.budget),
    preference: form.preference,
    notes: form.notes || ''
  }
  const err = validateTrip(payload)
  if (err) {
    uni.showToast({ title: err, icon: 'none' })
    return
  }

  generating.value = true
  try {
    const data = await generateTrip(payload)
    if (!data || !data.dayList) {
      errMsg.value = 'AI 返回格式异常,请重试'
      return
    }
    tripStore.setCurrent(data)
    uni.navigateTo({ url: '/pages/result/result' })
  } catch (e) {
    // 区分超预算(1003)与其它错误
    if (e.code === 1003) {
      errMsg.value = '当前行程超出预算,可在结果页调整或重新生成'
    } else if (e.code === 1001 || e.code === 1002) {
      errMsg.value = e.message || 'AI 生成失败,请重试'
    } else {
      errMsg.value = e.message || '生成失败,请重试'
    }
  } finally {
    generating.value = false
  }
}
</script>

<style lang="scss" scoped>
.create {
  min-height: 100vh;
  background: var(--bg-page);
  display: flex;
  flex-direction: column;
  padding: 24rpx 24rpx 0;

  &__form {
    flex: 1;
    background: var(--bg-card);
    border-radius: 24rpx;
    padding: 12rpx 32rpx 40rpx;
    box-shadow: 0 4rpx 24rpx rgba(47, 111, 237, 0.06);
  }
  &__footer {
    padding: 24rpx 0 40rpx;
  }
  &__btn {
    width: 100%;
    height: 96rpx;
    line-height: 96rpx;
    background: linear-gradient(135deg, var(--brand), #5a8af5);
    color: var(--text-on-brand);
    font-size: var(--fs-34);
    border-radius: 18rpx;
    font-weight: 600;
    &[disabled] {
      opacity: 0.7;
    }
  }
  &__hint {
    display: block;
    text-align: center;
    margin-top: 16rpx;
    font-size: var(--fs-22);
    color: var(--text-muted);
  }
  &__error {
    margin-top: 20rpx;
    padding: 20rpx 24rpx;
    background: #fff5f3;
    border-radius: 14rpx;
    display: flex;
    align-items: center;
    justify-content: space-between;
    &-text {
      flex: 1;
      font-size: var(--fs-24);
      color: var(--danger);
    }
    &-retry {
      font-size: var(--fs-26);
      color: var(--brand);
      font-weight: 600;
      padding-left: 20rpx;
    }
  }
}
.field {
  padding: 24rpx 0;
  border-bottom: 1rpx solid var(--border-light);
  &:last-child {
    border-bottom: none;
  }
  &__label {
    display: block;
    font-size: var(--fs-28);
    color: var(--text-primary);
    font-weight: 600;
    margin-bottom: 18rpx;
  }
  &__hint {
    font-size: var(--fs-22);
    color: var(--text-muted);
    font-weight: 400;
    margin-left: 10rpx;
  }
  &__input-wrap {
    display: flex;
    align-items: center;
    background: var(--bg-input);
    border: 1rpx solid var(--border-light);
    border-radius: 14rpx;
    padding: 0 24rpx;
    height: 88rpx;
  }
  &__input {
    flex: 1;
    height: 88rpx;
    font-size: var(--fs-30);
  }
  &__suffix {
    font-size: var(--fs-26);
    color: var(--text-muted);
  }
  &__clear {
    color: var(--text-muted);
    font-size: var(--fs-28);
    padding: 0 8rpx;
  }
  &__textarea {
    width: 100%;
    min-height: 120rpx;
    font-size: var(--fs-28);
    background: var(--bg-input);
    border: 1rpx solid var(--border-light);
    border-radius: 14rpx;
    padding: 18rpx 24rpx;
    box-sizing: border-box;
    color: var(--text-primary);
    line-height: 1.5;
  }
}
.stepper {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: var(--bg-input);
  border: 1rpx solid var(--border-light);
  border-radius: 14rpx;
  padding: 8rpx 24rpx;
  height: 88rpx;
  &__btn {
    width: 64rpx;
    height: 64rpx;
    line-height: 64rpx;
    text-align: center;
    background: var(--bg-card);
    border-radius: 14rpx;
    font-size: var(--fs-40);
    color: var(--brand);
    font-weight: 600;
  }
  &__value {
    font-size: var(--fs-30);
    font-weight: 600;
    color: var(--text-primary);
  }
}
.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 20rpx;
  &__item {
    padding: 14rpx 32rpx;
    background: var(--bg-input);
    border: 1rpx solid var(--border-light);
    border-radius: 40rpx;
    font-size: var(--fs-26);
    color: var(--text-secondary);
    &.active {
      background: var(--brand-soft);
      border-color: var(--brand);
      color: var(--brand);
      font-weight: 600;
    }
  }
}
.mask {
  position: fixed;
  inset: 0;
  background: rgba(20, 20, 40, 0.55);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 999;
  &__box {
    width: 70%;
    background: var(--bg-card);
    border-radius: 28rpx;
    padding: 60rpx 40rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 20rpx;
  }
  &__loader {
    width: 80rpx;
    height: 80rpx;
    border: 6rpx solid var(--brand-soft);
    border-top-color: var(--brand);
    border-radius: 50%;
    animation: spin 0.9s linear infinite;
  }
  &__text {
    font-size: var(--fs-30);
    font-weight: 600;
    color: var(--text-primary);
  }
  &__sub {
    font-size: var(--fs-24);
    color: var(--text-muted);
  }
}
@keyframes spin {
  to {
    transform: rotate(360deg);
  }
}
</style>
