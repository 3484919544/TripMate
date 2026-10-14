<template>
  <view class="admin-users">
    <!-- 搜索栏 -->
    <view class="search-bar">
      <input
        class="search-input"
        v-model="keyword"
        placeholder="输入用户名或昵称搜索..."
        confirm-type="search"
        @confirm="onSearch"
      />
      <text v-if="keyword" class="clear-btn" @click="onClear">✕</text>
      <text class="search-btn" @click="onSearch">搜索</text>
    </view>

    <!-- 新建按钮 -->
    <view class="add-row">
      <view class="add-btn" @click="openCreate">+ 新建用户</view>
    </view>

    <!-- 用户列表 -->
    <view class="list">
      <view v-for="u in list" :key="u.id" class="card">
        <view class="card-head">
          <text class="uname">{{ u.username }}</text>
          <text
            class="role-tag"
            :class="u.role === 'ADMIN' ? 'role-admin' : 'role-user'"
          >{{ u.role === 'ADMIN' ? '管理员' : '普通用户' }}</text>
        </view>
        <view class="card-meta">账号:{{ u.account || '-' }}</view>
        <view class="card-meta">昵称:{{ u.nickname || '-' }}</view>
        <view class="card-meta">ID:{{ u.id }}</view>
        <view class="card-actions">
          <view class="btn btn-primary" @click="openEdit(u)">编辑</view>
          <view class="btn btn-danger" @click="onDel(u)">删除</view>
        </view>
      </view>
      <view v-if="!loading && list.length === 0" class="empty-wrap">
        <text class="empty-text">暂无用户(或接口调用失败)</text>
      </view>
      <view v-if="errorMsg" class="error-wrap">
        <text class="error-text">{{ errorMsg }}</text>
      </view>
    </view>

    <!-- 新建弹窗 -->
    <view v-if="showCreate" class="popup-mask" @click.self="showCreate = false">
      <view class="popup-box">
        <view class="popup-title">新建用户</view>
        <view class="form-row">
          <text class="form-label">用户名</text>
          <input class="form-input" v-model="form.username" placeholder="3-20 字" />
        </view>
        <view class="form-row">
          <text class="form-label">密码</text>
          <input class="form-input" v-model="form.password" placeholder="6-30 字" password />
        </view>
        <view class="form-row">
          <text class="form-label">昵称</text>
          <input class="form-input" v-model="form.nickname" placeholder="可选" />
        </view>
        <view class="form-row">
          <text class="form-label">角色</text>
          <view class="role-picker">
            <view class="role-opt" :class="{ active: form.role === 'USER' }" @click="form.role = 'USER'">普通用户</view>
            <view class="role-opt" :class="{ active: form.role === 'ADMIN' }" @click="form.role = 'ADMIN'">管理员</view>
          </view>
        </view>
        <view class="popup-actions">
          <view class="btn" @click="showCreate = false">取消</view>
          <view class="btn btn-primary" @click="onCreate">确定</view>
        </view>
      </view>
    </view>

    <!-- 编辑弹窗 -->
    <view v-if="showEdit" class="popup-mask" @click.self="showEdit = false">
      <view class="popup-box">
        <view class="popup-title">编辑用户 · {{ editForm.username }}</view>
        <view class="form-row">
          <text class="form-label">新密码</text>
          <input class="form-input" v-model="editForm.password" placeholder="留空不改" password />
        </view>
        <view class="form-row">
          <text class="form-label">昵称</text>
          <input class="form-input" v-model="editForm.nickname" placeholder="空不改" />
        </view>
        <view class="form-row">
          <text class="form-label">角色</text>
          <view class="role-picker">
            <view class="role-opt" :class="{ active: editForm.role === 'USER' }" @click="editForm.role = 'USER'">普通用户</view>
            <view class="role-opt" :class="{ active: editForm.role === 'ADMIN' }" @click="editForm.role = 'ADMIN'">管理员</view>
          </view>
        </view>
        <view class="popup-actions">
          <view class="btn" @click="showEdit = false">取消</view>
          <view class="btn btn-primary" @click="onEdit">保存</view>
        </view>
      </view>
    </view>
  </view>
</template>

<script>
import { listUsers, createUser, updateUser, deleteUser } from '../../api/admin.js'

export default {
  data() {
    return {
      keyword: '',
      list: [],
      total: 0,
      loading: false,
      errorMsg: '',
      showCreate: false,
      showEdit: false,
      form: { username: '', password: '', nickname: '', role: 'USER' },
      editForm: { id: null, username: '', password: '', nickname: '', role: 'USER' }
    }
  },
  onShow() {
    console.log('[admin-users] onShow triggered, calling load()')
    this.load()
  },
  onPullDownRefresh() {
    this.load(true)
  },
  methods: {
    async load(reset = false) {
      this.loading = true
      this.errorMsg = ''
      console.log('[admin-users] load() called, keyword=', this.keyword)
      try {
        const res = await listUsers({
          pageNum: 1,
          pageSize: 50,
          keyword: this.keyword
        })
        console.log('[admin-users] API response:', JSON.stringify(res).slice(0, 300))
        // request.js 已解包 Result.data,所以 res 就是 { list, total }
        this.list = res.list || res.records || []
        this.total = res.total || 0
        console.log('[admin-users] list=', this.list.length, 'total=', this.total)
        if (this.list.length === 0) {
          this.errorMsg = '接口返回空列表'
        }
      } catch (e) {
        console.error('[admin-users] load FAILED:', e)
        this.errorMsg = '加载失败: ' + (e.message || '未知错误')
        uni.showToast({ title: this.errorMsg, icon: 'none' })
      } finally {
        this.loading = false
        if (reset) uni.stopPullDownRefresh()
      }
    },
    onSearch() { this.load() },
    onClear() { this.keyword = ''; this.load() },
    openCreate() {
      this.form = { username: '', password: '', nickname: '', role: 'USER' }
      this.showCreate = true
    },
    async onCreate() {
      if (!this.form.username || !this.form.password) {
        return uni.showToast({ title: '用户名/密码必填', icon: 'none' })
      }
      try {
        await createUser(this.form)
        uni.showToast({ title: '创建成功' })
        this.showCreate = false
        this.load()
      } catch (e) {
        uni.showToast({ title: e.message || '创建失败', icon: 'none' })
      }
    },
    openEdit(u) {
      this.editForm = { id: u.id, username: u.username, password: '', nickname: u.nickname || '', role: u.role }
      this.showEdit = true
    },
    async onEdit() {
      try {
        await updateUser(this.editForm.id, {
          password: this.editForm.password || null,
          nickname: this.editForm.nickname || null,
          role: this.editForm.role
        })
        uni.showToast({ title: '已保存' })
        this.showEdit = false
        this.load()
      } catch (e) {
        uni.showToast({ title: e.message || '保存失败', icon: 'none' })
      }
    },
    onDel(u) {
      uni.showModal({
        title: '确认删除',
        content: `删除用户 ${u.username}?`,
        success: async (r) => {
          if (!r.confirm) return
          try {
            await deleteUser(u.id)
            uni.showToast({ title: '已删除' })
            this.load()
          } catch (e) {
            uni.showToast({ title: e.message || '删除失败', icon: 'none' })
          }
        }
      })
    }
  }
}
</script>

<style lang="scss" scoped>
.admin-users { min-height: 100vh; background: var(--bg-page); padding: 24rpx; box-sizing: border-box; }

.search-bar {
  display: flex; align-items: center; background: var(--bg-card); border-radius: 16rpx;
  padding: 16rpx 20rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04);
}
.search-input { flex: 1; font-size: var(--fs-28); height: 60rpx; line-height: 60rpx; }
.clear-btn { font-size: var(--fs-28); color: var(--text-muted); padding: 0 12rpx; }
.search-btn { font-size: var(--fs-28); color: var(--brand); padding: 0 12rpx; font-weight: 500; }

.add-row { display: flex; justify-content: flex-end; margin-bottom: 20rpx; }
.add-btn { background: var(--brand); color: var(--text-on-brand); font-size: var(--fs-28); padding: 14rpx 32rpx; border-radius: 30rpx; font-weight: 500; }

.card { background: var(--bg-card); border-radius: 16rpx; padding: 28rpx; margin-bottom: 20rpx; box-shadow: 0 2rpx 8rpx rgba(0,0,0,0.04); }
.card-head { display: flex; align-items: center; justify-content: space-between; margin-bottom: 12rpx; }
.uname { font-size: var(--fs-32); font-weight: 600; color: var(--text-primary); }
.role-tag { font-size: var(--fs-22); padding: 4rpx 16rpx; border-radius: 20rpx; }
.role-admin { background: #ffeaea; color: var(--danger); }
.role-user { background: #e8f0ff; color: var(--brand); }
.card-meta { font-size: var(--fs-24); color: var(--text-secondary); margin-top: 6rpx; }
.card-actions { display: flex; gap: 20rpx; margin-top: 20rpx; }

.empty-wrap, .error-wrap { padding: 100rpx 0; text-align: center; }
.empty-text, .error-text { font-size: var(--fs-28); color: var(--text-muted); }
.error-text { color: var(--danger); }

.btn { font-size: var(--fs-26); padding: 10rpx 28rpx; border-radius: 8rpx; background: var(--border-light); color: var(--text-primary); }
.btn-primary { background: var(--brand); color: var(--text-on-brand); }
.btn-danger { background: #fff0ef; color: var(--danger); }

.popup-mask { position: fixed; inset: 0; background: rgba(0,0,0,0.5); display: flex; align-items: center; justify-content: center; z-index: 999; }
.popup-box { background: var(--bg-card); border-radius: 24rpx; padding: 40rpx; width: 600rpx; max-height: 80vh; overflow-y: auto; }
.popup-title { font-size: var(--fs-32); font-weight: 600; margin-bottom: 32rpx; text-align: center; color: var(--text-primary); }
.form-row { display: flex; align-items: center; margin-bottom: 24rpx; gap: 16rpx; }
.form-label { font-size: var(--fs-28); width: 120rpx; color: var(--text-secondary); flex-shrink: 0; }
.form-input { flex: 1; font-size: var(--fs-28); height: 64rpx; background: var(--bg-input); border-radius: 12rpx; padding: 0 20rpx; box-sizing: border-box; }
.role-picker { display: flex; gap: 16rpx; flex: 1; }
.role-opt { flex: 1; text-align: center; padding: 14rpx 0; border-radius: 12rpx; background: var(--bg-input); font-size: var(--fs-26); color: var(--text-secondary); }
.role-opt.active { background: var(--brand-soft); color: var(--brand); border: 2rpx solid var(--brand); }
.popup-actions { display: flex; gap: 20rpx; margin-top: 32rpx; }
.popup-actions .btn { flex: 1; text-align: center; padding: 18rpx 0; border-radius: 12rpx; font-size: var(--fs-28); }
</style>
