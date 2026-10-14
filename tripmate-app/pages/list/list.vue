<template>
  <view class="list">
    <!-- 搜索 + 筛选 -->
    <view class="list__search">
      <view class="list__search-box">
        <text class="list__search-icon">🔍</text>
        <input
          v-model="keyword"
          class="list__search-input"
          placeholder="搜目的地 / 标题"
          confirm-type="search"
          @confirm="onSearch"
        />
        <text v-if="keyword" class="list__search-clear" @tap="clearSearch">✕</text>
      </view>
      <view class="list__filter" @tap="showFilter = !showFilter">
        <text>筛选</text>
      </view>
    </view>

    <!-- 筛选面板 -->
    <view v-if="showFilter" class="list__filter-panel">
      <view class="list__filter-row">
        <text class="list__filter-label">天数</text>
        <view class="list__filter-tags">
          <view
            v-for="d in dayOptions"
            :key="d.value"
            class="list__filter-tag"
            :class="{ active: filter.days === d.value }"
            @tap="filter.days = d.value"
          >{{ d.label }}</view>
        </view>
      </view>
      <view class="list__filter-actions">
        <view class="list__filter-btn" @tap="resetFilter">重置</view>
        <view class="list__filter-btn list__filter-btn--primary" @tap="onApplyFilter">应用</view>
      </view>
    </view>

    <!-- 列表 -->
    <view class="list__items">
      <TripCard
        v-for="trip in trips"
        :key="trip.id"
        :trip="trip"
        :show-delete="true"
        @tap="goDetail(trip.id)"
        @delete="onDelete(trip)"
      />
      <EmptyState
        v-if="!loading && trips.length === 0"
        icon="🧭"
        text="还没有行程"
        action-text="去创建"
        @action="goCreate"
      />
      <view v-if="loading && trips.length > 0" class="list__more">
        <text>加载中...</text>
      </view>
      <view v-if="!loading && finished && trips.length > 0" class="list__more">
        <text>— 没有更多了 —</text>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive } from 'vue'
import { onShow, onReachBottom, onPullDownRefresh } from '@dcloudio/uni-app'
import { listTrips, deleteTrip } from '../../api/trip.js'
import TripCard from '../../components/TripCard.vue'
import EmptyState from '../../components/EmptyState.vue'

const trips = ref([])
const loading = ref(false)
const finished = ref(false)
const pageNum = ref(1)
const pageSize = 10

const keyword = ref('')
const showFilter = ref(false)
const filter = reactive({ days: 0 })

const dayOptions = [
  { value: 0, label: '全部' },
  { value: 1, label: '1 天' },
  { value: 3, label: '≤3 天' },
  { value: 5, label: '≤5 天' },
  { value: 7, label: '≤7 天' }
]

onShow(() => {
  resetAndLoad()
})

onPullDownRefresh(() => {
  resetAndLoad().finally(() => uni.stopPullDownRefresh())
})

onReachBottom(() => {
  if (!loading.value && !finished.value) loadMore()
})

async function resetAndLoad() {
  pageNum.value = 1
  finished.value = false
  trips.value = []
  await loadMore()
}

async function loadMore() {
  if (loading.value) return
  loading.value = true
  try {
    const params = { pageNum: pageNum.value, pageSize, keyword: keyword.value }
    if (filter.days) params.maxDays = filter.days
    const data = await listTrips(params)
    const list = (data && (data.list || data.records)) || []
    trips.value = trips.value.concat(list)
    if (list.length < pageSize) finished.value = true
    else pageNum.value++
  } catch (e) {
    uni.showToast({ title: e.message || '加载失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function onSearch() {
  resetAndLoad()
}
function clearSearch() {
  keyword.value = ''
  resetAndLoad()
}
function resetFilter() {
  filter.days = 0
}
function onApplyFilter() {
  showFilter.value = false
  resetAndLoad()
}

function goDetail(id) {
  uni.navigateTo({ url: '/pages/detail/detail?id=' + id })
}
function goCreate() {
  uni.navigateTo({ url: '/pages/create-trip/create-trip' })
}

function onDelete(trip) {
  uni.showModal({
    title: '删除行程',
    content: '确定删除「' + (trip.title || trip.destination) + '」?',
    confirmColor: '#ff3b30',
    success: async (r) => {
      if (!r.confirm) return
      try {
        await deleteTrip(trip.id)
        uni.showToast({ title: '已删除', icon: 'success' })
        trips.value = trips.value.filter((t) => t.id !== trip.id)
      } catch (e) {
        uni.showToast({ title: e.message || '删除失败', icon: 'none' })
      }
    }
  })
}
</script>

<style lang="scss" scoped>
.list {
  min-height: 100vh;
  background: var(--bg-page);
  padding-top: 24rpx;
  &__search {
    display: flex;
    gap: 20rpx;
    padding: 0 24rpx 24rpx;
    align-items: center;
  }
  &__search-box {
    flex: 1;
    display: flex;
    align-items: center;
    gap: 12rpx;
    background: var(--bg-card);
    border-radius: 40rpx;
    padding: 0 24rpx;
    height: 72rpx;
    box-shadow: 0 2rpx 12rpx rgba(47, 111, 237, 0.05);
  }
  &__search-icon {
    font-size: var(--fs-26);
  }
  &__search-input {
    flex: 1;
    height: 72rpx;
    font-size: var(--fs-26);
  }
  &__search-clear {
    color: var(--text-muted);
    font-size: var(--fs-24);
  }
  &__filter {
    padding: 0 24rpx;
    height: 72rpx;
    line-height: 72rpx;
    background: var(--bg-card);
    border-radius: 40rpx;
    font-size: var(--fs-26);
    color: var(--brand);
  }
  &__filter-panel {
    margin: 0 24rpx 24rpx;
    background: var(--bg-card);
    border-radius: 20rpx;
    padding: 28rpx 24rpx;
  }
  &__filter-row {
    display: flex;
    flex-direction: column;
    gap: 16rpx;
  }
  &__filter-label {
    font-size: var(--fs-26);
    color: var(--text-secondary);
    font-weight: 600;
  }
  &__filter-tags {
    display: flex;
    flex-wrap: wrap;
    gap: 16rpx;
  }
  &__filter-tag {
    padding: 10rpx 28rpx;
    background: var(--bg-input);
    border: 1rpx solid var(--border-light);
    border-radius: 30rpx;
    font-size: var(--fs-24);
    color: var(--text-secondary);
    &.active {
      background: var(--brand-soft);
      border-color: var(--brand);
      color: var(--brand);
    }
  }
  &__filter-actions {
    display: flex;
    gap: 20rpx;
    margin-top: 24rpx;
  }
  &__filter-btn {
    flex: 1;
    text-align: center;
    padding: 18rpx 0;
    border-radius: 14rpx;
    font-size: var(--fs-28);
    background: var(--border-light);
    color: var(--text-secondary);
    &--primary {
      background: var(--brand);
      color: var(--text-on-brand);
      font-weight: 600;
    }
  }
  &__more {
    text-align: center;
    padding: 24rpx 0;
    font-size: var(--fs-22);
    color: var(--text-muted);
  }
}
</style>
