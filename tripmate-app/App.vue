<script setup>
import { onLaunch } from '@dcloudio/uni-app'
import { useUserStore } from './store/user.js'
import { useSettingsStore } from './store/settings.js'

onLaunch(() => {
  const userStore = useUserStore()
  userStore.restore()

  // 初始化全局主题与字号
  const settings = useSettingsStore()
  settings.applyTheme()
  settings.applyFont()
})
</script>

<style>
/* ===== CSS 变量:浅色默认 ===== */
page {
  --bg-page: #f5f6fa;
  --bg-card: #ffffff;
  --bg-input: #f5f6fa;
  --text-primary: #1a1a2e;
  --text-secondary: #555555;
  --text-muted: #999999;
  --border-light: #f0f0f5;
  --border-medium: #e0e0e8;
  --brand: #2f6fed;
  --brand-soft: #e8f0ff;
  --danger: #ff3b30;
  --success: #34c759;
  --warning: #ff9500;
  --text-on-brand: #ffffff;

  background-color: var(--bg-page);
  color: var(--text-primary);
  font-size: var(--fs-28);
  font-family: -apple-system, BlinkMacSystemFont, 'Segoe UI', 'PingFang SC',
    'Microsoft YaHei', sans-serif;

  /* ===== 字号变量:标准(默认) ===== */
  --fs-18: 18rpx;
  --fs-20: 20rpx;
  --fs-22: 22rpx;
  --fs-24: 24rpx;
  --fs-26: 26rpx;
  --fs-28: 28rpx;
  --fs-30: 30rpx;
  --fs-32: 32rpx;
  --fs-34: 34rpx;
  --fs-36: 36rpx;
  --fs-38: 38rpx;
  --fs-40: 40rpx;
  --fs-42: 42rpx;
  --fs-44: 44rpx;
  --fs-48: 48rpx;
  --fs-52: 52rpx;
  --fs-56: 56rpx;
  --fs-96: 96rpx;
}

/* ===== CSS 变量:深色 ===== */
html[data-theme='dark'] page,
page.theme-dark,
.theme-dark {
  --bg-page: #0f0f1a;
  --bg-card: #1e1e2e;
  --bg-input: #2a2a3a;
  --text-primary: #e8e8f0;
  --text-secondary: #b0b0c0;
  --text-muted: #808090;
  --border-light: #2a2a3a;
  --border-medium: #3a3a4a;
  --brand: #4a80ff;
  --brand-soft: #1a2a4a;
  --danger: #ff6b6b;
  --success: #30d158;
  --warning: #ff9f0a;
  --text-on-brand: #ffffff;
}

view, text, button, input, textarea, scroll-view {
  box-sizing: border-box;
}
button::after { border: none; }

/* ===== 通用工具类 ===== */
.flex { display: flex; }
.flex-center { display: flex; align-items: center; justify-content: center; }
.flex-between { display: flex; align-items: center; justify-content: space-between; }
.ellipsis { overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }

/* ===== 覆盖常见组件硬编码颜色 ===== */
/* 卡片/列表容器 */
view[class*="card"], view[class*="section"], view[class*="group"] {
  background-color: var(--bg-card) !important;
}
/* 通用 item 行 */
view[class*="item"] {
  background-color: var(--bg-card);
  border-bottom-color: var(--border-light);
}
/* 文字颜色兜底 */
view, text {
  color: var(--text-primary);
}
/* 弹窗遮罩 */
view[class*="popup-mask"] {
  background: rgba(0, 0, 0, 0.6);
}
/* 弹窗框体 */
view[class*="popup-box"] {
  background: var(--bg-card);
  color: var(--text-primary);
}
/* 输入框 */
input {
  background: var(--bg-input) !important;
  color: var(--text-primary);
}
/* 分割线 */
view[class*="divider"] {
  background: var(--border-light);
}
/* 导航栏(H5 自定义) */
.nav-bar, .header-bar {
  background: var(--brand) !important;
}
/* 按钮 */
view[class*="btn"] {
  background: var(--brand);
  color: var(--text-on-brand);
}
view[class*="btn-primary"] {
  background: var(--brand) !important;
  color: var(--text-on-brand) !important;
}
view[class*="btn"][class*="cancel"] {
  background: var(--bg-input);
  color: var(--text-secondary);
}

/* 深色模式下修正部分颜色 */
html[data-theme='dark'] {
  color-scheme: dark;
}

/* ===== 字号:大(约放大15%) ===== */
html[data-font='large'] page {
  --fs-18: 21rpx;
  --fs-20: 23rpx;
  --fs-22: 25rpx;
  --fs-24: 28rpx;
  --fs-26: 30rpx;
  --fs-28: 32rpx;
  --fs-30: 35rpx;
  --fs-32: 37rpx;
  --fs-34: 39rpx;
  --fs-36: 41rpx;
  --fs-38: 44rpx;
  --fs-40: 46rpx;
  --fs-42: 48rpx;
  --fs-44: 51rpx;
  --fs-48: 55rpx;
  --fs-52: 60rpx;
  --fs-56: 64rpx;
  --fs-96: 110rpx;
}

/* ===== 字号:特大(约放大30%) ===== */
html[data-font='xl'] page {
  --fs-18: 23rpx;
  --fs-20: 26rpx;
  --fs-22: 29rpx;
  --fs-24: 31rpx;
  --fs-26: 34rpx;
  --fs-28: 36rpx;
  --fs-30: 39rpx;
  --fs-32: 42rpx;
  --fs-34: 44rpx;
  --fs-36: 47rpx;
  --fs-38: 49rpx;
  --fs-40: 52rpx;
  --fs-42: 55rpx;
  --fs-44: 57rpx;
  --fs-48: 62rpx;
  --fs-52: 68rpx;
  --fs-56: 73rpx;
  --fs-96: 125rpx;
}
</style>
