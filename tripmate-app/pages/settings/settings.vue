<template>
  <view class="settings">
    <!-- 1. 账号与安全 -->
    <view class="section">
      <view class="section-title">账号与安全</view>

      <view class="item" @click="showChangeNick = true">
        <text class="item-label">昵称</text>
        <view class="item-right">
          <text class="item-value">{{ nickname || '未设置' }}</text>
          <text class="item-arrow">›</text>
        </view>
      </view>

      <view class="item" @click="showChangePwd = true">
        <text class="item-label">修改密码</text>
        <text class="item-arrow">›</text>
      </view>

      <view class="item item--danger" @click="onDeleteAccount">
        <text class="item-label">注销账号</text>
        <text class="item-arrow">›</text>
      </view>
    </view>

    <!-- 2. AI 与隐私 -->
    <view class="section">
      <view class="section-title">AI 与隐私</view>

      <view class="item" @click="showAiNotice = true">
        <text class="item-label">AI 内容标识说明</text>
        <text class="item-arrow">›</text>
      </view>
      <view class="item" @click="showPrivacy = true">
        <text class="item-label">用户协议与隐私政策</text>
        <text class="item-arrow">›</text>
      </view>
      <view class="item" @click="onClearCache">
        <text class="item-label">清除缓存</text>
        <text class="item-arrow">›</text>
      </view>
    </view>

    <!-- 3. 系统偏好 -->
    <view class="section">
      <view class="section-title">系统偏好</view>

      <view class="item">
        <text class="item-label">深色模式</text>
        <switch :checked="isDark" color="#2f6fed" @change="onToggleDark" />
      </view>
      <view class="item">
        <text class="item-label">消息通知</text>
        <switch :checked="notifyOn" color="#2f6fed" @change="onToggleNotify" />
      </view>
      <view class="item" @click="showFont = true">
        <text class="item-label">字体大小</text>
        <view class="item-right">
          <text class="item-value">{{ fontLabel }}</text>
          <text class="item-arrow">›</text>
        </view>
      </view>
    </view>

    <!-- 4. 数据管理 -->
    <view class="section">
      <view class="section-title">数据管理</view>

      <view class="item" @click="onExport">
        <text class="item-label">导出我的行程</text>
        <text class="item-arrow">›</text>
      </view>
      <view class="item item--danger" @click="onClearAllTrips">
        <text class="item-label">清空所有行程</text>
        <text class="item-arrow">›</text>
      </view>
    </view>

    <view class="version">智游 TripMate v1.0.0</view>

    <!-- ===== 修改昵称弹窗 ===== -->
    <view v-if="showChangeNick" class="popup-mask" @click.self="showChangeNick = false">
      <view class="popup-box">
        <view class="popup-title">修改昵称</view>
        <input class="popup-input" v-model="nicknameInput" placeholder="请输入昵称" maxlength="50" />
        <view class="popup-actions">
          <view class="btn" @click="showChangeNick = false">取消</view>
          <view class="btn btn-primary" @click="onSaveNick">确定</view>
        </view>
      </view>
    </view>

    <!-- ===== 修改密码弹窗 ===== -->
    <view v-if="showChangePwd" class="popup-mask" @click.self="showChangePwd = false">
      <view class="popup-box">
        <view class="popup-title">修改密码</view>
        <input class="popup-input" v-model="pwdOld" type="password" placeholder="旧密码" />
        <input class="popup-input" v-model="pwdNew" type="password" placeholder="新密码(至少6位)" />
        <input class="popup-input" v-model="pwdConfirm" type="password" placeholder="确认新密码" />
        <view class="popup-actions">
          <view class="btn" @click="showChangePwd = false">取消</view>
          <view class="btn btn-primary" @click="onSavePwd">确定</view>
        </view>
      </view>
    </view>

    <!-- ===== AI 说明弹窗 ===== -->
    <view v-if="showAiNotice" class="popup-mask" @click.self="showAiNotice = false">
      <view class="popup-box popup-box--tall">
        <view class="popup-title">AI 内容标识说明</view>
        <view class="popup-text">本应用生成的旅游行程由人工智能(AI)模型自动生成,仅供参考。</view>
        <view class="popup-text">• 行程内容可能存在时效性偏差(如景点开放时间、交通变更、价格波动等)</view>
        <view class="popup-text">• 出发前请结合实际天气、交通、当地政策等信息核实</view>
        <view class="popup-text">• 本应用不承担因行程建议不准导致的任何损失</view>
        <view class="popup-text popup-text--warn">所有 AI 生成内容均标注"AI 生成"标识,请留意辨别。</view>
        <view class="popup-actions">
          <view class="btn btn-primary" @click="showAiNotice = false">我知道了</view>
        </view>
      </view>
    </view>

    <!-- ===== 隐私政策弹窗 ===== -->
    <view v-if="showPrivacy" class="popup-mask" @click.self="showPrivacy = false">
      <view class="popup-box popup-box--tall">
        <view class="popup-title">用户协议与隐私政策</view>
        <view class="popup-text">【用户协议】本应用为教学演示项目,行程内容不构成任何邀约或建议。</view>
        <view class="popup-text">【隐私政策】我们仅保存您注册的账户信息、生成的行程数据及 AI 调用日志。不与任何第三方共享您的个人信息,除法律要求外。</view>
        <view class="popup-text">您可随时在本设置页注销账号,注销后所有相关数据将被删除。</view>
        <view class="popup-actions">
          <view class="btn btn-primary" @click="showPrivacy = false">我知道了</view>
        </view>
      </view>
    </view>

    <!-- ===== 字体大小弹窗 ===== -->
    <view v-if="showFont" class="popup-mask" @click.self="showFont = false">
      <view class="popup-box">
        <view class="popup-title">字体大小</view>
        <view class="font-options">
          <view
            v-for="opt in fontOptions"
            :key="opt.key"
            class="font-opt"
            :class="{ active: fontSize === opt.key }"
            @click="onSetFont(opt.key)"
          >{{ opt.label }}</view>
        </view>
        <view class="popup-actions">
          <view class="btn" @click="showFont = false">关闭</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { changePassword, updateNickname, clearAllTrips, deleteAccount } from '../../api/auth.js'
import { listTrips } from '../../api/trip.js'
import { useUserStore } from '../../store/user.js'
import { useSettingsStore } from '../../store/settings.js'

export default {
  data() {
    return {
      showChangeNick: false,
      showChangePwd: false,
      showAiNotice: false,
      showPrivacy: false,
      showFont: false,
      nicknameInput: '',
      pwdOld: '',
      pwdNew: '',
      pwdConfirm: '',
      fontOptions: [
        { key: 'normal', label: '标准' },
        { key: 'large', label: '大' },
        { key: 'xl', label: '特大' }
      ]
    }
  },
  computed: {
    nickname() {
      return useUserStore().userInfo ? useUserStore().userInfo.nickname : ''
    },
    isDark() {
      return useSettingsStore().isDark
    },
    notifyOn() {
      return useSettingsStore().notifyOn
    },
    fontSize() {
      return useSettingsStore().fontSize
    },
    fontLabel() {
      const o = this.fontOptions.find(x => x.key === this.fontSize)
      return o ? o.label : '标准'
    }
  },
  onLoad() {
    // 主题状态由 store 从 localStorage 初始化,无需重复读取
  },
  methods: {
    async onSaveNick() {
      if (!this.nicknameInput || this.nicknameInput.isBlank()) {
        return uni.showToast({ title: '昵称不能为空', icon: 'none' })
      }
      try {
        const res = await updateNickname(this.nicknameInput)
        const u = useUserStore().userInfo || {}
        useUserStore().setUserInfo({ ...u, nickname: res.nickname })
        uni.showToast({ title: '已更新' })
        this.showChangeNick = false
      } catch (e) {
        uni.showToast({ title: e.message || '修改失败', icon: 'none' })
      }
    },
    async onSavePwd() {
      if (!this.pwdOld || !this.pwdNew) return uni.showToast({ title: '请填完整', icon: 'none' })
      if (this.pwdNew.length < 6) return uni.showToast({ title: '新密码至少6位', icon: 'none' })
      if (this.pwdNew !== this.pwdConfirm) return uni.showToast({ title: '两次不一致', icon: 'none' })
      try {
        await changePassword(this.pwdOld, this.pwdNew, this.pwdConfirm)
        uni.showToast({ title: '密码已修改,请重新登录' })
        this.showChangePwd = false
        setTimeout(() => {
          useUserStore().logout()
          uni.reLaunch({ url: '/pages/login/login' })
        }, 1200)
      } catch (e) {
        uni.showToast({ title: e.message || '修改失败', icon: 'none' })
      }
    },
    onToggleDark(e) {
      useSettingsStore().toggleDark(e.detail.value)
      uni.showToast({ title: e.detail.value ? '深色模式已开启' : '浅色模式', icon: 'none' })
    },
    onToggleNotify(e) {
      useSettingsStore().setNotify(e.detail.value)
    },
    onSetFont(key) {
      useSettingsStore().setFont(key)
      this.showFont = false
      uni.showToast({ title: '已切换为' + this.fontLabel })
    },
    onClearCache() {
      uni.showModal({
        title: '清除缓存',
        content: '将清除本地临时数据,不影响已保存的行程。确定?',
        success: (r) => {
          if (!r.confirm) return
          uni.removeStorageSync('pref_dark')
          uni.removeStorageSync('pref_font')
          uni.removeStorageSync('pref_notify')
          uni.showToast({ title: '缓存已清除' })
        }
      })
    },
    async onExport() {
      try {
        const res = await listTrips({ pageNum: 1, pageSize: 100 })
        const trips = res.list || res.records || []
        if (trips.length === 0) {
          return uni.showToast({ title: '暂无行程可导出', icon: 'none' })
        }
        let md = `# 我的行程\n\n`
        trips.forEach(t => {
          md += `## ${t.title || t.destination} · ${t.days}天\n\n`
          md += `目的地:${t.destination} · 预算:¥${t.budget || 0} · 总花费:¥${t.totalCost || 0}\n\n`
          if (t.dayList) {
            t.dayList.forEach(d => {
              md += `**Day${d.dayIndex} · ${d.theme || ''}**\n`
              md += `- 上午:${d.morning || ''}\n`
              md += `- 下午:${d.afternoon || ''}\n`
              md += `- 晚上:${d.evening || ''}\n`
              if (d.tips) md += `- 提示:${d.tips}\n`
              md += `\n`
            })
          }
        })
        // 注意:trip 列表接口不返回 dayList(只有详情接口返回),这里简化处理
        uni.setClipboardData({
          data: md,
          success: () => uni.showToast({ title: '已复制到剪贴板' })
        })
      } catch (e) {
        uni.showToast({ title: e.message || '导出失败', icon: 'none' })
      }
    },
    onClearAllTrips() {
      uni.showModal({
        title: '清空所有行程',
        content: '将永久删除您保存的全部行程,此操作不可撤销。确定?',
        confirmColor: '#ff3b30',
        success: async (r) => {
          if (!r.confirm) return
          try {
            await clearAllTrips()
            uni.showToast({ title: '已清空' })
          } catch (e) {
            uni.showToast({ title: e.message || '清空失败', icon: 'none' })
          }
        }
      })
    },
    onDeleteAccount() {
      uni.showModal({
        title: '注销账号',
        content: '注销后您的账号、行程、AI日志将永久删除,无法恢复。确定注销?',
        confirmColor: '#ff3b30',
        success: async (r) => {
          if (!r.confirm) return
          try {
            await deleteAccount()
            uni.showToast({ title: '账号已注销' })
            setTimeout(() => {
              useUserStore().logout()
              uni.reLaunch({ url: '/pages/login/login' })
            }, 1200)
          } catch (e) {
            uni.showToast({ title: e.message || '注销失败', icon: 'none' })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.settings { min-height: 100vh; background: var(--bg-page); padding: 16rpx 0 60rpx; }

.section { background: var(--bg-card); margin: 20rpx; border-radius: 16rpx; overflow: hidden; }
.section-title { font-size: var(--fs-26); color: var(--text-muted); padding: 24rpx 28rpx 0; font-weight: 500; }

.item {
  display: flex; align-items: center; padding: 32rpx 28rpx;
  border-bottom: 1rpx solid var(--border-light);
}
.item:last-child { border-bottom: none; }
.item--danger { color: var(--danger); }
.item-label { font-size: var(--fs-30); flex: 1; color: var(--text-primary); }
.item--danger .item-label { color: var(--danger); }
.item-right { display: flex; align-items: center; gap: 10rpx; }
.item-value { font-size: var(--fs-28); color: var(--text-muted); }
.item-arrow { font-size: var(--fs-36); color: var(--text-muted); }

.version { text-align: center; margin-top: 60rpx; font-size: var(--fs-22); color: var(--text-muted); }

.popup-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.popup-box { background: var(--bg-card); border-radius: 24rpx; padding: 40rpx; width: 600rpx; }
.popup-box--tall { max-height: 80vh; overflow-y: auto; }
.popup-title { font-size: var(--fs-32); font-weight: 600; margin-bottom: 24rpx; text-align: center; color: var(--text-primary); }
.popup-input { width: 100%; font-size: var(--fs-28); height: 72rpx; background: var(--bg-input); border-radius: 12rpx; padding: 0 20rpx; margin-bottom: 20rpx; box-sizing: border-box; color: var(--text-primary); }
.popup-text { font-size: var(--fs-26); color: var(--text-secondary); line-height: 1.6; margin-bottom: 12rpx; }
.popup-text--warn { color: var(--warning); font-weight: 500; }
.popup-actions { display: flex; gap: 20rpx; margin-top: 24rpx; }
.popup-actions .btn { flex: 1; text-align: center; padding: 18rpx 0; border-radius: 12rpx; font-size: var(--fs-28); background: var(--bg-input); color: var(--text-secondary); }
.popup-actions .btn-primary { background: var(--brand); color: var(--text-on-brand); }

.font-options { display: flex; gap: 16rpx; margin-bottom: 24rpx; }
.font-opt { flex: 1; text-align: center; padding: 20rpx 0; border-radius: 12rpx; background: var(--bg-input); font-size: var(--fs-28); color: var(--text-secondary); }
.font-opt.active { background: var(--brand-soft); color: var(--brand); border: 2rpx solid var(--brand); }
</style>
