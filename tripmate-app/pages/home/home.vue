<template>
  <view class="home">
    <!-- 顶部欢迎 -->
    <view class="home__header">
      <view class="home__header-inner">
        <view class="home__greet">
          <text class="home__hi">你好,{{ nickname }} 👋</text>
          <text class="home__sub">今天想去哪儿?</text>
        </view>
        <view class="home__avatar" @tap="goProfile">
          <image v-if="avatarUrl" :src="avatarUrl" class="home__avatar-img" mode="aspectFill" />
          <text v-else class="home__avatar-text">{{ avatarText }}</text>
        </view>
      </view>
    </view>

    <!-- 主入口 -->
    <view class="home__actions">
      <view class="home__action home__action--primary" @tap="goCreate">
        <view class="home__action-icon">✈️</view>
        <view class="home__action-body">
          <text class="home__action-title">创建行程</text>
          <text class="home__action-desc">输入目的地 · AI 秒生成</text>
        </view>
        <text class="home__action-arrow">›</text>
      </view>
      <view class="home__action home__action--sub" @tap="goList">
        <view class="home__action-icon">🎒</view>
        <view class="home__action-body">
          <text class="home__action-title">我的行程</text>
          <text class="home__action-desc">查看历史 · 调整复制</text>
        </view>
        <text class="home__action-arrow">›</text>
      </view>
    </view>

    <!-- 最近行程 -->
    <view class="home__section">
      <view class="home__section-head">
        <text class="home__section-title">最近行程</text>
        <text class="home__section-more" @tap="goList">查看全部 ›</text>
      </view>
      <TripCard
        v-for="trip in recentTrips"
        :key="trip.id"
        :trip="trip"
        @tap="goDetail(trip.id)"
      />
      <EmptyState
        v-if="!loadingRecent && recentTrips.length === 0"
        icon="🧭"
        text="还没有行程,去创建第一个吧"
        action-text="创建行程"
        @action="goCreate"
      />
    </view>

    <!-- 热门目的地 -->
    <view class="home__section">
      <view class="home__section-head">
        <text class="home__section-title">热门目的地</text>
      </view>
      <view class="home__dests">
        <view
          v-for="d in hotDestinations"
          :key="d.name"
          class="home__dest"
          @tap="quickCreate(d)"
        >
          <text class="home__dest-emoji">{{ d.emoji }}</text>
          <text class="home__dest-name">{{ d.name }}</text>
          <text class="home__dest-tag">{{ d.tag }}</text>
        </view>
      </view>
    </view>

    <view class="home__bottom-space"></view>
  </view>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../store/user.js'
import { listTrips, getHotDestinations } from '../../api/trip.js'
import TripCard from '../../components/TripCard.vue'
import EmptyState from '../../components/EmptyState.vue'

const userStore = useUserStore()

const recentTrips = ref([])
const loadingRecent = ref(false)

const nickname = computed(() => {
  const u = userStore.userInfo
  return (u && (u.nickname || u.username)) || '旅行者'
})
const avatarText = computed(() => {
  const n = nickname.value
  return n ? n.charAt(0).toUpperCase() : 'U'
})
const avatarUrl = computed(() => {
  const u = userStore.userInfo
  if (!u || !u.avatar) return ''
  if (u.avatar.startsWith('http')) return u.avatar
  // #ifdef H5
  return (typeof location !== 'undefined' ? location.origin : '') + '/api' + u.avatar
  // #endif
  // #ifndef H5
  return 'http://localhost:8090/api' + u.avatar
  // #endif
})

// 热门目的地(本地兜底数据,接口可用时由后端覆盖)
const hotDestinations = ref([
  { name: '北京', emoji: '🏯', tag: '文化' },
  { name: '成都', emoji: '🐼', tag: '美食' },
  { name: '杭州', emoji: '🌿', tag: '自然' },
  { name: '西安', emoji: '🏛️', tag: '历史' },
  { name: '三亚', emoji: '🏖️', tag: '休闲' },
  { name: '厦门', emoji: '🌊', tag: '文艺' }
])

// 常用目的地 emoji 映射,未命中则用地球
const destEmojis = {
  北京: '🏯', 上海: '🌃', 广州: '🗼', 深圳: '🏙️', 成都: '🐼', 杭州: '🌿',
  西安: '🏛️', 南京: '🏰', 武汉: '🌉', 重庆: '🌶️', 三亚: '🏖️', 厦门: '🌊',
  青岛: '🍺', 大连: '⛵', 苏州: '🏮', 桂林: '⛰️', 丽江: '🏔️', 大理: '⛅',
  西藏: '🏔️', 新疆: '🐪', 哈尔滨: '❄️', 长沙: '🎆', 香港: '🎡', 澳门: '🎡'
}

// 备用图标池:映射表未命中或图标已被占用时按序取用,保证同屏图标不重复
const fallbackEmojis = ['🌍', '🎆', '🏜️', '⛵', '⛺', '🌋', '🛕', '🎠', '🚡', '🌾', '🐻', '🌴', '🗺️', '🧭']

// 给目的地列表分配互不相同的图标
function assignEmojis(list) {
  const used = new Set()
  return list.map(x => {
    let emoji = destEmojis[x.name]
    if (!emoji || used.has(emoji)) {
      emoji = fallbackEmojis.find(e => !used.has(e)) || '🌍'
    }
    used.add(emoji)
    return { ...x, emoji }
  })
}

// 根据所有用户的行程动态更新热门目的地(热度最高前 6)
async function loadHotDests() {
  try {
    const data = await getHotDestinations()
    const list = (Array.isArray(data) ? data : []).filter(x => x && x.name)
    if (list.length === 0) return // 无数据时保留本地兜底
    hotDestinations.value = assignEmojis(list.slice(0, 6)).map(x => ({
      name: x.name,
      emoji: x.emoji,
      tag: (x.cnt || 0) + ' 人去过'
    }))
  } catch (e) { /* 静默失败,保留本地兜底 */ }
}

onShow(async () => {
  // 未登录跳登录
  if (!userStore.isLogin) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  // 拉取最新用户信息(头像/昵称可能刚改过)
  try { await userStore.fetchInfo() } catch (e) { /* ignore */ }
  loadRecent()
  loadHotDests()
})

async function loadRecent() {
  loadingRecent.value = true
  try {
    const data = await listTrips({ pageNum: 1, pageSize: 3 })
    recentTrips.value = (data && (data.list || data.records)) || []
  } catch (e) {
    // 静默失败,首页不弹错
    recentTrips.value = []
  } finally {
    loadingRecent.value = false
  }
}

function goCreate() {
  uni.navigateTo({ url: '/pages/create-trip/create-trip' })
}
function goList() {
  uni.navigateTo({ url: '/pages/list/list' })
}
function goProfile() {
  uni.navigateTo({ url: '/pages/profile/profile' })
}
function goDetail(id) {
  uni.navigateTo({ url: '/pages/detail/detail?id=' + id })
}
function quickCreate(d) {
  uni.navigateTo({
    url: '/pages/create-trip/create-trip?destination=' + encodeURIComponent(d.name)
  })
}
</script>

<style lang="scss" scoped>
.home {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 40rpx;
  &__header {
    background: linear-gradient(135deg, var(--brand), #4a7bf5);
    padding: 60rpx 32rpx 80rpx;
  }
  &__header-inner {
    display: flex;
    align-items: center;
    justify-content: space-between;
  }
  &__greet {
    display: flex;
    flex-direction: column;
    gap: 8rpx;
  }
  &__hi {
    font-size: var(--fs-38);
    font-weight: 700;
    color: var(--text-on-brand);
  }
  &__sub {
    font-size: var(--fs-26);
    color: rgba(255, 255, 255, 0.9);
  }
  &__avatar {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
    overflow: hidden;
  }
  &__avatar-img {
    width: 80rpx;
    height: 80rpx;
    border-radius: 50%;
  }
  &__avatar-text {
    color: var(--text-on-brand);
    font-size: var(--fs-32);
    font-weight: 700;
  }
  &__actions {
    margin: -50rpx 24rpx 0;
    display: flex;
    flex-direction: column;
    gap: 20rpx;
  }
  &__action {
    display: flex;
    align-items: center;
    gap: 20rpx;
    background: var(--bg-card);
    border-radius: 20rpx;
    padding: 28rpx 32rpx;
    box-shadow: 0 6rpx 24rpx rgba(47, 111, 237, 0.08);
    &--primary {
      background: linear-gradient(135deg, var(--brand), #5a8af5);
      .home__action-title,
      .home__action-desc,
      .home__action-arrow {
        color: var(--text-on-brand);
      }
      .home__action-icon {
        background: rgba(255, 255, 255, 0.2);
      }
    }
    &-icon {
      width: 80rpx;
      height: 80rpx;
      border-radius: 20rpx;
      background: var(--brand-soft);
      display: flex;
      align-items: center;
      justify-content: center;
      font-size: var(--fs-40);
    }
    &-body {
      flex: 1;
      display: flex;
      flex-direction: column;
      gap: 4rpx;
    }
    &-title {
      font-size: var(--fs-30);
      font-weight: 600;
      color: var(--text-primary);
    }
    &-desc {
      font-size: var(--fs-24);
      color: var(--text-muted);
    }
    &-arrow {
      font-size: var(--fs-40);
      color: var(--text-muted);
    }
  }
  &__section {
    margin-top: 40rpx;
    padding: 0 4rpx;
  }
  &__section-head {
    display: flex;
    align-items: center;
    justify-content: space-between;
    padding: 0 24rpx;
    margin-bottom: 20rpx;
  }
  &__section-title {
    font-size: var(--fs-32);
    font-weight: 700;
    color: var(--text-primary);
  }
  &__section-more {
    font-size: var(--fs-24);
    color: var(--brand);
  }
  &__dests {
    display: flex;
    flex-wrap: wrap;
    gap: 20rpx;
    padding: 0 24rpx;
  }
  &__dest {
    width: calc((100% - 20rpx) / 2);
    background: var(--bg-card);
    border-radius: 20rpx;
    padding: 24rpx;
    display: flex;
    flex-direction: column;
    align-items: center;
    gap: 8rpx;
    box-shadow: 0 4rpx 16rpx rgba(47, 111, 237, 0.06);
    &-emoji {
      font-size: var(--fs-56);
    }
    &-name {
      font-size: var(--fs-28);
      font-weight: 600;
      color: var(--text-primary);
    }
    &-tag {
      font-size: var(--fs-22);
      color: var(--brand);
      background: var(--brand-soft);
      padding: 2rpx 14rpx;
      border-radius: 16rpx;
    }
  }
  &__bottom-space {
    height: 40rpx;
  }
}
</style>
