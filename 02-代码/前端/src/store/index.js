import { defineStore } from 'pinia'

// 从 localStorage 安全读取
function readJSON(key, fallback) {
  try {
    const raw = localStorage.getItem(key)
    return raw ? JSON.parse(raw) : fallback
  } catch (e) {
    return fallback
  }
}

// 用户认证状态：token / role / merchantId / userId
export const useUserStore = defineStore('user', {
  state: () => ({
    token: '',
    userId: null,
    role: '',
    merchantId: null,
    username: '',
    realName: '',
    userInfo: null
  }),
  getters: {
    isLoggedIn: (state) => !!state.token,
    isUser: (state) => state.role === 'USER',
    isMerchant: (state) => state.role === 'MERCHANT',
    isAdmin: (state) => state.role === 'ADMIN'
  },
  actions: {
    // 登录成功后保存会话
    setLogin(data) {
      this.token = data.token || ''
      this.userId = data.userId || null
      this.role = data.role || ''
      this.merchantId = data.merchantId || null
      this.username = data.username || ''
      this.realName = data.realName || ''
      if (data.userInfo) this.userInfo = data.userInfo
      try {
        localStorage.setItem('ft_token', this.token)
        localStorage.setItem(
          'ft_user',
          JSON.stringify({
            userId: this.userId,
            role: this.role,
            merchantId: this.merchantId,
            username: this.username,
            realName: this.realName
          })
        )
      } catch (e) {
        // ignore
      }
    },
    // 刷新页面后从本地恢复会话
    restore() {
      try {
        this.token = localStorage.getItem('ft_token') || ''
        const u = readJSON('ft_user', null)
        if (u) {
          this.userId = u.userId || null
          this.role = u.role || ''
          this.merchantId = u.merchantId || null
          this.username = u.username || ''
          this.realName = u.realName || ''
        }
      } catch (e) {
        // ignore
      }
    },
    logout() {
      this.token = ''
      this.userId = null
      this.role = ''
      this.merchantId = null
      this.username = ''
      this.realName = ''
      this.userInfo = null
      try {
        localStorage.removeItem('ft_token')
        localStorage.removeItem('ft_user')
      } catch (e) {
        // ignore
      }
    }
  }
})

// 全局 UI 状态：购物车数量、未读预警数
export const useAppStore = defineStore('app', {
  state: () => ({
    cartCount: 0,
    unreadWarnings: 0
  }),
  actions: {
    setCartCount(n) {
      this.cartCount = Number(n) || 0
    },
    setUnreadWarnings(n) {
      this.unreadWarnings = Number(n) || 0
    }
  }
})
