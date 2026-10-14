<template>
  <view class="login">
    <!-- 顶部品牌区 -->
    <view class="login__hero">
      <view class="login__logo">智游</view>
      <text class="login__title">智游 TripMate</text>
      <text class="login__subtitle">10 秒生成一份能直接照着走的行程</text>
    </view>

    <view class="login__card">
      <!-- 切换 -->
      <view class="login__tabs">
        <view
          class="login__tab"
          :class="{ active: mode === 'login' }"
          @tap="switchMode('login')"
        >登录</view>
        <view
          class="login__tab"
          :class="{ active: mode === 'register' }"
          @tap="switchMode('register')"
        >注册</view>
      </view>

      <!-- 登录表单 -->
      <view v-if="mode === 'login'" class="form">
        <view class="form__item">
          <text class="form__label">10位账号</text>
          <input v-model="loginForm.account" class="form__input" placeholder="请输入10位数字账号" maxlength="10" type="number" />
        </view>
        <view class="form__item">
          <text class="form__label">密码</text>
          <input v-model="loginForm.password" class="form__input" :password="true" placeholder="请输入密码" maxlength="50" />
        </view>
        <view class="form__item">
          <text class="form__label">验证码</text>
          <view class="captcha-row">
            <input v-model="loginForm.captcha" class="form__input captcha-input" placeholder="请输入验证码" maxlength="4" />
            <image
              v-if="captchaImage"
              :src="captchaImage"
              class="captcha-img"
              @tap="loadCaptcha"
              mode="aspectFit"
            />
            <view v-else class="captcha-img captcha-img--loading" @tap="loadCaptcha">加载中...</view>
          </view>
        </view>

        <button class="form__btn" :loading="loading" :disabled="loading" @tap="onLogin">登录</button>
        <view class="form__tip-row">
          <text class="form__tip" @tap="showResetPwd = true">忘记密码?</text>
          <text class="form__tip" @tap="mode = 'register'">没有账号?注册</text>
        </view>
      </view>

      <!-- 注册表单 -->
      <view v-else class="form">
        <view class="form__item">
          <text class="form__label">手机号</text>
          <input v-model="regForm.phone" class="form__input" placeholder="请输入11位手机号" maxlength="11" type="number" />
        </view>
        <view class="form__item">
          <text class="form__label">用户名</text>
          <input v-model="regForm.username" class="form__input" placeholder="2-20位,登录后可改昵称" maxlength="20" />
        </view>
        <view class="form__item">
          <text class="form__label">密码</text>
          <input v-model="regForm.password" class="form__input" :password="true" placeholder="至少6位" maxlength="50" />
        </view>
        <view class="form__item">
          <text class="form__label">确认密码</text>
          <input v-model="regForm.confirmPassword" class="form__input" :password="true" placeholder="再次输入密码" maxlength="50" />
        </view>

        <button class="form__btn" :loading="loading" :disabled="loading" @tap="onRegister">注册</button>
        <view class="form__tip">
          <text>注册成功后系统将分配一个10位数字账号</text>
        </view>
      </view>
    </view>

    <!-- 注册成功弹窗 -->
    <view v-if="showAccountPopup" class="popup-mask" @tap.self="showAccountPopup = false">
      <view class="popup-box">
        <view class="popup-title">🎉 注册成功!</view>
        <view class="popup-sub">系统已为您生成账号,请妥善保存:</view>
        <view class="popup-account">{{ registeredAccount }}</view>
        <view class="popup-note">此账号为您登录的唯一凭证,忘记后无法找回</view>
        <view class="popup-actions">
          <view class="btn" @tap="copyAccount">复制账号</view>
          <view class="btn btn-primary" @tap="goLogin">去登录</view>
        </view>
      </view>
    </view>

    <!-- 忘记密码弹窗 -->
    <view v-if="showResetPwd" class="popup-mask" @tap.self="showResetPwd = false">
      <view class="popup-box">
        <view class="popup-title">找回密码</view>
        <view class="popup-sub">输入注册时的手机号,重置密码</view>

        <view class="form__item" style="margin-bottom:16rpx">
          <text class="form__label">手机号</text>
          <input v-model="resetForm.phone" class="form__input" placeholder="请输入11位手机号" maxlength="11" type="number" />
        </view>
        <view class="form__item" style="margin-bottom:16rpx">
          <text class="form__label">验证码</text>
          <view class="captcha-row">
            <input v-model="resetForm.captcha" class="form__input captcha-input" placeholder="请输入验证码" maxlength="4" />
            <image v-if="resetCaptchaImage" :src="resetCaptchaImage" class="captcha-img" @tap="loadResetCaptcha" mode="aspectFit" />
          </view>
        </view>
        <view class="form__item" style="margin-bottom:16rpx">
          <text class="form__label">新密码</text>
          <input v-model="resetForm.newPassword" class="form__input" :password="true" placeholder="至少6位" maxlength="50" />
        </view>
        <view class="form__item" style="margin-bottom:24rpx">
          <text class="form__label">确认新密码</text>
          <input v-model="resetForm.confirmPassword" class="form__input" :password="true" placeholder="再次输入" maxlength="50" />
        </view>

        <view class="popup-actions">
          <view class="btn" @tap="showResetPwd = false">取消</view>
          <view class="btn btn-primary" :class="{ disabled: resetLoading }" @tap="onResetPassword">
            {{ resetLoading ? '提交中...' : '重置密码' }}
          </view>
        </view>
      </view>
    </view>

    <view class="login__footer">
      <text>AI 生成内容仅供参考,请结合实际出行</text>
    </view>
  </view>
</template>

<script setup>
import { ref, onMounted, watch } from 'vue'
import { useUserStore } from '../../store/user.js'
import { fetchCaptcha, login as loginApi, register as registerApi, resetPassword } from '../../api/auth.js'

const userStore = useUserStore()

const mode = ref('login')
const loading = ref(false)

// 登录
const loginForm = ref({ account: '', password: '', captcha: '', captchaKey: '' })
const captchaImage = ref('')

// 注册
const regForm = ref({ phone: '', username: '', password: '', confirmPassword: '' })

// 注册成功弹窗
const showAccountPopup = ref(false)
const registeredAccount = ref('')

// 忘记密码
const showResetPwd = ref(false)
const resetLoading = ref(false)
const resetForm = ref({ phone: '', captcha: '', captchaKey: '', newPassword: '', confirmPassword: '' })
const resetCaptchaImage = ref('')

watch(showResetPwd, (v) => {
  if (v) loadResetCaptcha()
})

onMounted(() => {
  if (userStore.isLogin) {
    uni.reLaunch({ url: '/pages/home/home' })
    return
  }
  loadCaptcha()
})

async function loadCaptcha() {
  try {
    const data = await fetchCaptcha()
    loginForm.value.captchaKey = data.captchaKey
    captchaImage.value = data.captchaImage
  } catch (e) {
    uni.showToast({ title: '验证码加载失败', icon: 'none' })
  }
}

function switchMode(m) {
  mode.value = m
  if (m === 'login') loadCaptcha()
}

async function onLogin() {
  const f = loginForm.value
  if (!f.account || f.account.length !== 10) return uni.showToast({ title: '请输入10位账号', icon: 'none' })
  if (!f.password) return uni.showToast({ title: '请输入密码', icon: 'none' })
  if (!f.captcha) return uni.showToast({ title: '请输入验证码', icon: 'none' })

  loading.value = true
  try {
    const resp = await loginApi({ ...f })
    userStore.setLogin(resp.token, resp.user)
    uni.showToast({ title: '登录成功', icon: 'success' })
    setTimeout(() => uni.reLaunch({ url: '/pages/home/home' }), 500)
  } catch (e) {
    uni.showToast({ title: e.message || '登录失败', icon: 'none' })
    loadCaptcha() // 刷新验证码
    f.captcha = ''
  } finally {
    loading.value = false
  }
}

async function onRegister() {
  const f = regForm.value
  if (!/^1[3-9]\d{9}$/.test(f.phone)) return uni.showToast({ title: '手机号格式不正确', icon: 'none' })
  if (!f.username || f.username.length < 2) return uni.showToast({ title: '用户名至少2位', icon: 'none' })
  if (!f.password || f.password.length < 6) return uni.showToast({ title: '密码至少6位', icon: 'none' })
  if (f.password !== f.confirmPassword) return uni.showToast({ title: '两次密码不一致', icon: 'none' })

  loading.value = true
  try {
    const result = await registerApi({ ...f })
    registeredAccount.value = result.account
    showAccountPopup.value = true
    // 清空注册表单
    regForm.value = { phone: '', username: '', password: '', confirmPassword: '' }
  } catch (e) {
    uni.showToast({ title: e.message || '注册失败', icon: 'none' })
  } finally {
    loading.value = false
  }
}

function copyAccount() {
  uni.setClipboardData({
    data: registeredAccount.value,
    success: () => uni.showToast({ title: '已复制', icon: 'success' })
  })
}

function goLogin() {
  showAccountPopup.value = false
  mode.value = 'login'
  loginForm.value.account = registeredAccount.value
  loginForm.value.password = ''
  loadCaptcha()
}

async function loadResetCaptcha() {
  try {
    const data = await fetchCaptcha()
    resetForm.value.captchaKey = data.captchaKey
    resetCaptchaImage.value = data.captchaImage
  } catch (e) {
    uni.showToast({ title: '验证码加载失败', icon: 'none' })
  }
}

async function onResetPassword() {
  const f = resetForm.value
  if (!/^1[3-9]\d{9}$/.test(f.phone)) return uni.showToast({ title: '手机号格式不正确', icon: 'none' })
  if (!f.captcha) return uni.showToast({ title: '请输入验证码', icon: 'none' })
  if (!f.newPassword || f.newPassword.length < 6) return uni.showToast({ title: '新密码至少6位', icon: 'none' })
  if (f.newPassword !== f.confirmPassword) return uni.showToast({ title: '两次密码不一致', icon: 'none' })

  resetLoading.value = true
  try {
    await resetPassword(f.phone, f.captcha, f.captchaKey, f.newPassword, f.confirmPassword)
    uni.showToast({ title: '密码已重置,请登录', icon: 'none' })
    showResetPwd.value = false
    resetForm.value = { phone: '', captcha: '', captchaKey: '', newPassword: '', confirmPassword: '' }
    // 刷新登录页验证码
    loadCaptcha()
    loginForm.value.password = ''
  } catch (e) {
    uni.showToast({ title: e.message || '重置失败', icon: 'none' })
    loadResetCaptcha()
    resetForm.value.captcha = ''
  } finally {
    resetLoading.value = false
  }
}
</script>

<style lang="scss" scoped>
.login {
  min-height: 100vh;
  background: linear-gradient(180deg, var(--brand) 0%, #4a7bf5 40%, var(--bg-page) 40%, var(--bg-page) 100%);
  display: flex;
  flex-direction: column;
  align-items: center;
  padding: 0 48rpx;

  &__hero { width: 100%; padding: 120rpx 0 50rpx; display: flex; flex-direction: column; align-items: center; gap: 14rpx; }
  &__logo { width: 110rpx; height: 110rpx; border-radius: 28rpx; background: var(--bg-card); color: var(--brand); font-size: var(--fs-44); font-weight: 800; display: flex; align-items: center; justify-content: center; box-shadow: 0 10rpx 30rpx rgba(0,0,0,0.15); }
  &__title { font-size: var(--fs-42); font-weight: 700; color: var(--text-on-brand); }
  &__subtitle { font-size: var(--fs-24); color: rgba(255,255,255,0.9); }
  &__card { width: 100%; background: var(--bg-card); border-radius: 28rpx; padding: 36rpx 32rpx; box-shadow: 0 10rpx 40rpx rgba(47,111,237,0.12); }
  &__tabs { display: flex; background: var(--bg-page); border-radius: 14rpx; padding: 6rpx; margin-bottom: 28rpx; }
  &__tab { flex: 1; text-align: center; padding: 16rpx 0; font-size: var(--fs-28); color: var(--text-muted); border-radius: 12rpx; transition: all 0.2s; &.active { background: var(--bg-card); color: var(--brand); font-weight: 600; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.06); } }
  &__footer { margin-top: 40rpx; font-size: var(--fs-22); color: var(--text-muted); }
}

.form {
  &__item { margin-bottom: 22rpx; }
  &__label { display: block; font-size: var(--fs-24); color: var(--text-secondary); margin-bottom: 10rpx; }
  &__input { width: 100%; height: 84rpx; background: var(--bg-input); border: 1rpx solid var(--border-light); border-radius: 14rpx; padding: 0 22rpx; font-size: var(--fs-28); box-sizing: border-box; }
  &__btn { width: 100%; height: 88rpx; line-height: 88rpx; background: var(--brand); color: var(--text-on-brand); font-size: var(--fs-32); border-radius: 16rpx; font-weight: 600; margin-top: 12rpx; &[disabled] { opacity: 0.6; } }
  &__tip { margin-top: 14rpx; font-size: var(--fs-24); color: var(--brand); }
}

.form__tip-row { display: flex; justify-content: space-between; margin-top: 14rpx; padding: 0 4rpx; }

.captcha-row { display: flex; gap: 16rpx; align-items: center; }
.captcha-input { flex: 1; }
.captcha-img { width: 200rpx; height: 84rpx; border-radius: 12rpx; border: 1rpx solid var(--border-light); background: var(--bg-input); }
.captcha-img--loading { display: flex; align-items: center; justify-content: center; font-size: var(--fs-22); color: var(--text-muted); }

.popup-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.popup-box { background: var(--bg-card); border-radius: 24rpx; padding: 44rpx; width: 620rpx; text-align: center; }
.popup-title { font-size: var(--fs-36); font-weight: 700; color: var(--text-primary); margin-bottom: 12rpx; }
.popup-sub { font-size: var(--fs-26); color: var(--text-secondary); margin-bottom: 24rpx; }
.popup-account { font-size: var(--fs-44); font-weight: 800; color: var(--brand); letter-spacing: 6rpx; padding: 24rpx 0; background: var(--brand-soft); border-radius: 14rpx; margin-bottom: 16rpx; }
.popup-note { font-size: var(--fs-22); color: #ff9500; margin-bottom: 28rpx; }
.popup-actions { display: flex; gap: 20rpx; }
.popup-actions .btn { flex: 1; padding: 20rpx 0; border-radius: 14rpx; font-size: var(--fs-28); background: var(--bg-input); color: var(--text-secondary); }
.popup-actions .btn-primary { background: var(--brand); color: var(--text-on-brand); }
</style>
