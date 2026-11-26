<template>
  <aside class="sidebar">
    <div class="side-title">{{ sideTitle }}</div>
    <nav class="side-nav">
      <template v-if="user.role === 'MERCHANT'">
        <router-link to="/merchant/dashboard" class="side-item">工作台</router-link>
        <router-link to="/merchant/products" class="side-item">商品管理</router-link>
        <router-link to="/merchant/stock" class="side-item">库存与预警</router-link>
        <router-link to="/merchant/orders" class="side-item">订单管理</router-link>
        <router-link to="/merchant/after-sales" class="side-item">售后处理</router-link>
        <router-link to="/merchant/reports" class="side-item">销售报表</router-link>
      </template>
      <template v-else-if="user.role === 'ADMIN'">
        <router-link to="/admin/dashboard" class="side-item">工作台</router-link>
        <router-link to="/admin/merchants" class="side-item">商家审核</router-link>
        <router-link to="/admin/users" class="side-item">用户管理</router-link>
        <router-link to="/admin/complaints" class="side-item">投诉处理</router-link>
        <router-link to="/admin/overview" class="side-item">数据总览</router-link>
      </template>
    </nav>
  </aside>
</template>

<script setup>
import { computed } from 'vue'
import { useUserStore } from '../store'

const user = useUserStore()
const sideTitle = computed(() => (user.role === 'ADMIN' ? '平台管理' : '商家中心'))
</script>

<style scoped>
.sidebar {
  width: 200px;
  flex-shrink: 0;
  background: #fff;
  border-right: 1px solid var(--border);
  min-height: calc(100vh - 64px);
}
.side-title {
  padding: 20px 20px 12px;
  font-size: 13px;
  font-weight: 600;
  color: var(--text-light);
  letter-spacing: 1px;
}
.side-nav {
  display: flex;
  flex-direction: column;
  padding: 0 10px;
}
.side-item {
  padding: 11px 14px;
  border-radius: 8px;
  color: var(--text-secondary);
  font-weight: 500;
  margin-bottom: 3px;
  transition: all 0.2s;
}
.side-item:hover {
  background: var(--primary-light);
  color: var(--primary);
}
.side-item.router-link-active {
  background: var(--primary);
  color: #fff;
}
@media (max-width: 768px) {
  .sidebar {
    width: 100%;
    min-height: auto;
    border-right: none;
    border-bottom: 1px solid var(--border);
  }
  .app-body:has(.sidebar) {
    flex-direction: column;
  }
  .side-nav {
    flex-direction: row;
    overflow-x: auto;
  }
  .side-item {
    white-space: nowrap;
  }
  .side-title {
    display: none;
  }
}
</style>
