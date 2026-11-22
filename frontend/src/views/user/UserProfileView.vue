<template>
  <div class="page-container">
    <h2 class="page-title">个人中心</h2>

    <div class="profile-layout">
      <div class="card profile-card">
        <div class="profile-avatar">{{ avatarText }}</div>
        <div class="profile-name">{{ displayName }}</div>
        <div class="profile-role">普通用户</div>
        <div class="profile-stats">
          <router-link to="/orders" class="stat-item">
            <div class="stat-num">{{ stat.orders }}</div>
            <div class="stat-label">订单</div>
          </router-link>
          <router-link to="/cart" class="stat-item">
            <div class="stat-num">{{ app.cartCount }}</div>
            <div class="stat-label">购物车</div>
          </router-link>
          <router-link to="/after-sales" class="stat-item">
            <div class="stat-num">{{ stat.afterSales }}</div>
            <div class="stat-label">售后</div>
          </router-link>
        </div>
      </div>

      <div class="card info-card">
        <h3>账户信息</h3>
        <div class="info-row"><span class="info-label">用户名</span><span>{{ user.username || '-' }}</span></div>
        <div class="info-row"><span class="info-label">真实姓名</span><span>{{ user.realName || '-' }}</span></div>
        <div class="info-row"><span class="info-label">手机号</span><span>{{ userInfo.phone || '-' }}</span></div>
        <div class="info-row"><span class="info-label">用户编号</span><span>{{ user.userId || '-' }}</span></div>
        <div class="info-row"><span class="info-label">注册时间</span><span>{{ formatDateTime(userInfo.createTime) }}</span></div>
        <div class="info-row"><span class="info-label">账号状态</span><span class="tag tag-success">正常</span></div>

        <h3 class="mt-16">快捷入口</h3>
        <div class="quick-links">
          <router-link to="/orders" class="quick-link">我的订单</router-link>
          <router-link to="/after-sales" class="quick-link">我的售后</router-link>
          <router-link to="/category" class="quick-link">浏览商品</router-link>
          <router-link to="/home" class="quick-link">个性推荐</router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { getOrders, getAfterSales } from '../../api/user'
import { useUserStore, useAppStore } from '../../store'
import { formatDateTime } from '../../utils/format'

const user = useUserStore()
const app = useAppStore()
const userInfo = ref({})
const stat = ref({ orders: 0, afterSales: 0 })

const displayName = computed(() => user.realName || user.username || '用户')
const avatarText = computed(() => (displayName.value || '用').slice(0, 1))

async function loadStats() {
  try {
    const res = await getOrders({ pageNum: 1, pageSize: 1 })
    stat.value.orders = res.total || 0
  } catch (e) {}
  try {
    const res = await getAfterSales({ pageNum: 1, pageSize: 1 })
    stat.value.afterSales = res.total || 0
  } catch (e) {}
}

onMounted(() => {
  userInfo.value = user.userInfo || {}
  loadStats()
})
</script>

<style scoped>
.profile-layout {
  display: grid;
  grid-template-columns: 300px 1fr;
  gap: 20px;
}
.profile-card {
  text-align: center;
  align-self: start;
}
.profile-avatar {
  width: 80px;
  height: 80px;
  border-radius: 50%;
  background: var(--primary);
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 36px;
  font-weight: 700;
  margin: 10px auto 16px;
}
.profile-name {
  font-size: 20px;
  font-weight: 700;
}
.profile-role {
  color: var(--text-light);
  font-size: 13px;
  margin-top: 4px;
}
.profile-stats {
  display: flex;
  margin-top: 24px;
  border-top: 1px solid var(--border);
  padding-top: 20px;
}
.stat-item {
  flex: 1;
}
.stat-num {
  font-size: 22px;
  font-weight: 700;
  color: var(--primary);
}
.stat-label {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 4px;
}
.info-card h3 {
  font-size: 16px;
  margin-bottom: 16px;
}
.info-row {
  display: flex;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
  font-size: 14px;
}
.info-label {
  width: 100px;
  color: var(--text-light);
}
.quick-links {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.quick-link {
  padding: 10px 18px;
  background: var(--primary-light);
  color: var(--primary);
  border-radius: 8px;
  font-weight: 500;
}
.quick-link:hover {
  background: var(--primary);
  color: #fff;
}
@media (max-width: 768px) {
  .profile-layout {
    grid-template-columns: 1fr;
  }
}
</style>
