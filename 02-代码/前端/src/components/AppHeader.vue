<template>
  <header class="app-header">
    <div class="header-inner">
      <router-link :to="homePath" class="brand">
        <span class="brand-mark">果</span>
        <span class="brand-name">水果交易智能管理系统</span>
      </router-link>

      <nav class="nav" v-if="user.isLoggedIn">
        <template v-if="user.role === 'USER'">
          <router-link to="/home" class="nav-item">首页</router-link>
          <router-link to="/category" class="nav-item">全部商品</router-link>
          <router-link to="/orders" class="nav-item">我的订单</router-link>
          <router-link to="/after-sales" class="nav-item">我的售后</router-link>
        </template>
        <template v-else-if="user.role === 'MERCHANT'">
          <router-link to="/merchant/dashboard" class="nav-item">工作台</router-link>
          <router-link to="/merchant/products" class="nav-item">商品管理</router-link>
          <router-link to="/merchant/orders" class="nav-item">订单管理</router-link>
        </template>
        <template v-else-if="user.role === 'ADMIN'">
          <router-link to="/admin/dashboard" class="nav-item">工作台</router-link>
          <router-link to="/admin/merchants" class="nav-item">商家审核</router-link>
          <router-link to="/admin/overview" class="nav-item">数据总览</router-link>
        </template>
      </nav>

      <div class="header-right">
        <template v-if="user.isLoggedIn">
          <router-link v-if="user.role === 'USER'" to="/cart" class="icon-btn cart-btn">
            <span class="icon">车</span>
            <span class="cart-label">购物车</span>
            <span v-if="app.cartCount > 0" class="badge">{{ app.cartCount }}</span>
          </router-link>
          <div class="user-menu">
            <span class="avatar">{{ avatarText }}</span>
            <span class="user-name">{{ displayName }}</span>
            <span class="role-tag">{{ roleText }}</span>
            <button class="logout-btn" @click="onLogout">退出</button>
          </div>
        </template>
        <template v-else>
          <router-link to="/login" class="btn btn-outline btn-sm">登录</router-link>
          <router-link to="/register" class="btn btn-primary btn-sm">注册</router-link>
        </template>
      </div>
    </div>
  </header>
</template>

<script setup>
import { computed } from 'vue'
import { useRouter } from 'vue-router'
import { useUserStore, useAppStore } from '../store'
import { ROLE_TEXT } from '../utils/enums'
import { logout } from '../api/auth'
import { notifySuccess } from '../utils/request'

const router = useRouter()
const user = useUserStore()
const app = useAppStore()

const homePath = computed(() => {
  if (user.role === 'MERCHANT') return '/merchant/dashboard'
  if (user.role === 'ADMIN') return '/admin/dashboard'
  return '/home'
})

const roleText = computed(() => ROLE_TEXT[user.role] || '')
const displayName = computed(() => user.realName || user.username || '用户')
const avatarText = computed(() => (displayName.value || '用').slice(0, 1))

async function onLogout() {
  try {
    await logout()
  } catch (e) {
    // 忽略退出接口错误，仍清理本地状态
  }
  user.logout()
  app.setCartCount(0)
  app.setUnreadWarnings(0)
  notifySuccess('已退出登录')
  router.push('/login')
}
</script>

<style scoped>
.app-header {
  background: #fff;
  border-bottom: 1px solid var(--border);
  position: sticky;
  top: 0;
  z-index: 100;
}
.header-inner {
  max-width: 1320px;
  margin: 0 auto;
  height: 64px;
  padding: 0 20px;
  display: flex;
  align-items: center;
  gap: 32px;
}
.brand {
  display: flex;
  align-items: center;
  gap: 10px;
  font-weight: 700;
  white-space: nowrap;
}
.brand-mark {
  width: 36px;
  height: 36px;
  border-radius: 10px;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
}
.brand-name {
  font-size: 17px;
  color: var(--text);
}
.nav {
  display: flex;
  align-items: center;
  gap: 6px;
  flex: 1;
}
.nav-item {
  padding: 8px 14px;
  border-radius: 8px;
  color: var(--text-secondary);
  font-weight: 500;
  transition: all 0.2s;
}
.nav-item:hover {
  color: var(--primary);
  background: var(--primary-light);
}
.nav-item.router-link-active {
  color: var(--primary);
  background: var(--primary-light);
}
.header-right {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-left: auto;
}
.icon-btn {
  position: relative;
  display: flex;
  align-items: center;
  gap: 6px;
  padding: 8px 10px;
  color: var(--text-secondary);
  border-radius: 8px;
}
.icon-btn:hover {
  background: var(--primary-light);
  color: var(--primary);
}
.icon {
  width: 22px;
  height: 22px;
  border: 2px solid currentColor;
  border-radius: 6px;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  font-size: 10px;
  font-weight: 700;
}
.badge {
  position: absolute;
  top: 0;
  right: 0;
  min-width: 18px;
  height: 18px;
  padding: 0 5px;
  border-radius: 9px;
  background: var(--danger);
  color: #fff;
  font-size: 11px;
  line-height: 18px;
  text-align: center;
}
.user-menu {
  display: flex;
  align-items: center;
  gap: 8px;
}
.avatar {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: var(--primary-light);
  color: var(--primary-dark);
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 700;
}
.user-name {
  font-weight: 500;
  max-width: 100px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.role-tag {
  font-size: 12px;
  color: var(--primary);
  background: var(--primary-light);
  padding: 2px 8px;
  border-radius: 999px;
}
.logout-btn {
  border: none;
  background: none;
  color: var(--text-secondary);
  padding: 6px;
  font-size: 13px;
}
.logout-btn:hover {
  color: var(--danger);
}
@media (max-width: 900px) {
  .nav,
  .user-name,
  .role-tag {
    display: none;
  }
  .brand-name {
    font-size: 14px;
  }
}
</style>
