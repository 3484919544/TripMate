<template>
  <view class="profile">
    <!-- 用户信息头 -->
    <view class="profile__header">
      <view class="profile__avatar-wrap" @tap="onEditProfile">
        <image v-if="avatarUrl" :src="avatarUrl" class="profile__avatar-img" mode="aspectFill" />
        <view v-else class="profile__avatar">
          <text class="profile__avatar-text">{{ avatarText }}</text>
        </view>
      </view>
      <view class="profile__user">
        <text class="profile__nickname">{{ nickname }}</text>
        <view class="profile__username-row" @tap="onCopyAccount">
          <text class="profile__username">@{{ accountText }}</text>
          <text class="profile__username-copy">点击复制</text>
        </view>
        <text v-if="bio" class="profile__bio">{{ bio }}</text>
      </view>
      <view class="profile__edit" @tap="onEditProfile">
        <text>编辑</text>
      </view>
    </view>

    <!-- 入口列表 -->
    <view class="profile__group">
      <view class="profile__item" @tap="go('/pages/list/list')">
        <text class="profile__item-icon">🎒</text>
        <text class="profile__item-text">我的行程</text>
        <text class="profile__item-arrow">›</text>
      </view>
      <view class="profile__item" @tap="go('/pages/ai-logs/ai-logs')">
        <text class="profile__item-icon">🤖</text>
        <text class="profile__item-text">AI 使用记录</text>
        <text class="profile__item-arrow">›</text>
      </view>
      <view class="profile__item" v-if="isAdmin" @tap="go('/pages/admin-users/admin-users')">
        <text class="profile__item-icon">🛡️</text>
        <text class="profile__item-text">用户管理</text>
        <text class="profile__item-arrow">›</text>
      </view>
      <view class="profile__item" v-if="isAdmin" @tap="go('/pages/admin-feedback/admin-feedback')">
        <text class="profile__item-icon">💬</text>
        <text class="profile__item-text">反馈管理</text>
        <text class="profile__item-arrow">›</text>
      </view>
      <view class="profile__item" v-if="!isAdmin" @tap="go('/pages/feedback/feedback')">
        <text class="profile__item-icon">💬</text>
        <text class="profile__item-text">反馈</text>
        <text class="profile__item-arrow">›</text>
      </view>
    </view>

    <view class="profile__group">
      <view class="profile__item" @tap="go('/pages/settings/settings')">
        <text class="profile__item-icon">⚙️</text>
        <text class="profile__item-text">设置</text>
        <text class="profile__item-arrow">›</text>
      </view>
      <view class="profile__item profile__item--danger" @tap="onLogout">
        <text class="profile__item-icon">🚪</text>
        <text class="profile__item-text">退出登录</text>
        <text class="profile__item-arrow">›</text>
      </view>
    </view>

    <view class="profile__version">
      <text>智游 TripMate v1.0.0</text>
    </view>

    <!-- 编辑资料弹窗 -->
    <view v-if="showEdit" class="popup-mask" @tap.self="showEdit = false">
      <view class="popup-box">
        <view class="popup-title">编辑资料</view>

        <!-- 头像 -->
        <view class="edit-row edit-row--avatar">
          <view class="edit-avatar" @tap="onChooseAvatar">
            <image v-if="editForm.avatarUrl" :src="editForm.avatarUrl" class="edit-avatar-img" mode="aspectFill" />
            <text v-else class="edit-avatar-text">{{ editAvatarText }}</text>
            <view class="edit-avatar-tip" v-if="uploading">上传中...</view>
          </view>
          <text class="edit-avatar-hint">点击更换头像</text>
        </view>

        <!-- 昵称 -->
        <view class="edit-row">
          <text class="edit-label">昵称</text>
          <input class="edit-input" v-model="editForm.nickname" placeholder="请输入昵称" maxlength="50" />
        </view>

        <!-- 签名 -->
        <view class="edit-row">
          <text class="edit-label">个性签名</text>
          <textarea class="edit-textarea" v-model="editForm.bio" placeholder="一句话介绍自己" maxlength="200" :auto-height="true" />
        </view>

        <view class="popup-actions">
          <view class="btn" @tap="showEdit = false">取消</view>
          <view class="btn btn-primary" @tap="onSaveProfile" :class="{ disabled: saving }">
            {{ saving ? '保存中...' : '保存' }}
          </view>
        </view>
      </view>
    </view>
  </view>
</template>

<script setup>
import { ref, reactive, computed } from 'vue'
import { onShow } from '@dcloudio/uni-app'
import { useUserStore } from '../../store/user.js'
import { updateProfile, uploadAvatar } from '../../api/auth.js'

const userStore = useUserStore()

const showEdit = ref(false)
const saving = ref(false)
const uploading = ref(false)
const editForm = reactive({
  nickname: '',
  bio: '',
  avatarUrl: ''   // 完整可访问 URL
})

// 头像完整 URL:后端返回 /uploads/xxx.jpg,前端需要补完整 origin
const avatarUrl = computed(() => {
  const u = userStore.userInfo
  if (!u || !u.avatar) return ''
  if (u.avatar.startsWith('http')) return u.avatar
  // 拼完整 URL(H5)
  // #ifdef H5
  return (typeof location !== 'undefined' ? location.origin : '') + '/api' + u.avatar
  // #endif
  // #ifndef H5
  return 'http://localhost:8090/api' + u.avatar
  // #endif
})

const nickname = computed(() => {
  const u = userStore.userInfo
  return (u && (u.nickname || u.username)) || '旅行者'
})
const bio = computed(() => {
  const u = userStore.userInfo
  return (u && u.bio) || ''
})
const username = computed(() => {
  const u = userStore.userInfo
  return (u && u.username) || ''
})
const accountText = computed(() => {
  const u = userStore.userInfo
  return (u && u.account) || '----------'
})
const onCopyAccount = () => {
  if (!accountText.value || accountText.value === '----------') {
    return uni.showToast({ title: '账号未获取,请重新登录', icon: 'none' })
  }
  uni.setClipboardData({
    data: accountText.value,
    showToast: false,
    success: () => uni.showToast({ title: '账号已复制:' + accountText.value, icon: 'none' })
  })
}
const avatarText = computed(() => {
  const n = nickname.value
  return n ? n.charAt(0).toUpperCase() : 'U'
})
const editAvatarText = computed(() => {
  const n = editForm.nickname || nickname.value
  return n ? n.charAt(0).toUpperCase() : 'U'
})
const isAdmin = computed(() => {
  const u = userStore.userInfo
  return !!(u && u.role === 'ADMIN')
})

onShow(async () => {
  if (!userStore.isLogin) {
    uni.reLaunch({ url: '/pages/login/login' })
    return
  }
  try {
    await userStore.fetchInfo()
  } catch (e) { /* ignore */ }
})

function go(url) {
  uni.navigateTo({ url })
}

function onEditProfile() {
  const u = userStore.userInfo || {}
  editForm.nickname = u.nickname || ''
  editForm.bio = u.bio || ''
  editForm.avatarUrl = avatarUrl.value  // 完整 URL
  showEdit.value = true
}

function onChooseAvatar() {
  uni.chooseImage({
    count: 1,
    sizeType: ['compressed'],
    sourceType: ['album', 'camera'],
    success: async (res) => {
      const tempPath = res.tempFilePaths[0]
      uploading.value = true
      try {
        const token = uni.getStorageSync('token') || ''
        const data = await uploadAvatar(tempPath, token)
        // data.url 是 /uploads/xxx.jpg
        editForm.avatarUrl = avatarUrlOrigin() + '/api' + data.url
        editForm._avatar = data.url  // 存后端需要的相对路径
        uni.showToast({ title: '头像已上传' })
      } catch (e) {
        uni.showToast({ title: e.message || '上传失败', icon: 'none' })
      } finally {
        uploading.value = false
      }
    }
  })
}

function avatarUrlOrigin() {
  // #ifdef H5
  return typeof location !== 'undefined' ? location.origin : ''
  // #endif
  // #ifndef H5
  return 'http://localhost:8090'
  // #endif
}

async function onSaveProfile() {
  if (saving.value) return
  saving.value = true
  try {
    const u = userStore.userInfo || {}
    const body = {
      nickname: editForm.nickname.trim() || null,
      bio: editForm.bio.trim() || null,
      avatar: editForm._avatar || u.avatar || null
    }
    // 只有改过的字段才传
    if (!body.nickname) body.nickname = null
    if (body.bio === '') body.bio = null

    const res = await updateProfile(body)
    userStore.setUserInfo({ ...u, nickname: res.nickname, bio: res.bio, avatar: res.avatar })
    uni.showToast({ title: '已保存' })
    showEdit.value = false
  } catch (e) {
    uni.showToast({ title: e.message || '保存失败', icon: 'none' })
  } finally {
    saving.value = false
  }
}

function onLogout() {
  uni.showModal({
    title: '退出登录',
    content: '确定退出当前账号?',
    success: (r) => {
      if (!r.confirm) return
      userStore.logout()
      uni.reLaunch({ url: '/pages/login/login' })
    }
  })
}
</script>

<style lang="scss" scoped>
.profile {
  min-height: 100vh;
  background: var(--bg-page);
  padding-bottom: 60rpx;
  &__header {
    background: linear-gradient(135deg, var(--brand), #4a7bf5);
    padding: 60rpx 40rpx 50rpx;
    display: flex;
    align-items: center;
    gap: 24rpx;
  }
  &__avatar-wrap {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    overflow: hidden;
  }
  &__avatar {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
    background: rgba(255, 255, 255, 0.25);
    display: flex;
    align-items: center;
    justify-content: center;
  }
  &__avatar-img {
    width: 120rpx;
    height: 120rpx;
    border-radius: 50%;
  }
  &__avatar-text {
    color: var(--text-on-brand);
    font-size: var(--fs-48);
    font-weight: 700;
  }
  &__user {
    flex: 1;
    display: flex;
    flex-direction: column;
    gap: 6rpx;
  }
  &__nickname {
    font-size: var(--fs-36);
    font-weight: 700;
    color: var(--text-on-brand);
  }
  &__username {
    font-size: var(--fs-24);
    color: rgba(255, 255, 255, 0.85);
  }
  &__username-row {
    display: flex;
    align-items: center;
    gap: 10rpx;
  }
  &__username-copy {
    font-size: var(--fs-20);
    color: rgba(255, 255, 255, 0.6);
    border: 1rpx solid rgba(255, 255, 255, 0.4);
    border-radius: 8rpx;
    padding: 2rpx 10rpx;
  }
  &__bio {
    font-size: var(--fs-22);
    color: rgba(255, 255, 255, 0.7);
    margin-top: 4rpx;
  }
  &__edit {
    padding: 10rpx 24rpx;
    background: rgba(255, 255, 255, 0.2);
    border-radius: 30rpx;
    font-size: var(--fs-24);
    color: var(--text-on-brand);
  }
  &__group {
    margin: 24rpx 24rpx 0;
    background: var(--bg-card);
    border-radius: 20rpx;
    overflow: hidden;
    box-shadow: 0 4rpx 16rpx rgba(47, 111, 237, 0.06);
  }
  &__item {
    display: flex;
    align-items: center;
    gap: 20rpx;
    padding: 32rpx;
    border-bottom: 1rpx solid var(--border-light);
    &:last-child { border-bottom: none; }
    &-icon { font-size: var(--fs-36); width: 48rpx; text-align: center; }
    &-text { flex: 1; font-size: var(--fs-30); color: var(--text-primary); }
    &-arrow { font-size: var(--fs-36); color: var(--text-muted); }
    &--danger .profile__item-text { color: var(--danger); }
  }
  &__version {
    text-align: center;
    margin-top: 60rpx;
    font-size: var(--fs-22);
    color: var(--text-muted);
  }
}

/* 编辑弹窗 */
.popup-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.popup-box { background: var(--bg-card); border-radius: 24rpx; padding: 40rpx; width: 620rpx; max-height: 85vh; overflow-y: auto; }
.popup-title { font-size: var(--fs-32); font-weight: 600; margin-bottom: 30rpx; text-align: center; color: var(--text-primary); }
.popup-actions { display: flex; gap: 20rpx; margin-top: 30rpx; }
.popup-actions .btn { flex: 1; text-align: center; padding: 18rpx 0; border-radius: 12rpx; font-size: var(--fs-28); background: var(--bg-input); color: var(--text-secondary); }
.popup-actions .btn-primary { background: var(--brand); color: var(--text-on-brand); }
.popup-actions .btn.disabled { opacity: 0.6; }

.edit-row { margin-bottom: 24rpx; }
.edit-row--avatar { display: flex; flex-direction: column; align-items: center; gap: 12rpx; margin-bottom: 32rpx; }
.edit-label { display: block; font-size: var(--fs-26); color: var(--text-muted); margin-bottom: 12rpx; }
.edit-input { width: 100%; height: 72rpx; background: var(--bg-input); border: 1rpx solid var(--border-light); border-radius: 12rpx; padding: 0 20rpx; font-size: var(--fs-28); color: var(--text-primary); box-sizing: border-box; }
.edit-textarea { width: 100%; min-height: 100rpx; background: var(--bg-input); border: 1rpx solid var(--border-light); border-radius: 12rpx; padding: 18rpx 20rpx; font-size: var(--fs-28); color: var(--text-primary); box-sizing: border-box; }
.edit-avatar { width: 140rpx; height: 140rpx; border-radius: 50%; background: var(--brand-soft); display: flex; align-items: center; justify-content: center; position: relative; overflow: hidden; border: 4rpx solid var(--brand); }
.edit-avatar-img { width: 140rpx; height: 140rpx; border-radius: 50%; }
.edit-avatar-text { font-size: var(--fs-52); font-weight: 700; color: var(--brand); }
.edit-avatar-tip { position: absolute; bottom: 0; left: 0; right: 0; background: rgba(0,0,0,0.5); color: #fff; font-size: var(--fs-18); padding: 6rpx 0; text-align: center; }
.edit-avatar-hint { font-size: var(--fs-24); color: var(--text-muted); }
</style>
