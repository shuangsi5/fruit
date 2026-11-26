<template>
  <div class="page-container">
    <h2 class="page-title">我的订单</h2>

    <div class="card">
      <div class="tabs">
        <button
          v-for="t in tabs"
          :key="t.value"
          class="tab"
          :class="{ active: filters.status === t.value }"
          @click="changeTab(t.value)"
        >
          {{ t.label }}
        </button>
      </div>

      <div class="filter-bar">
        <input v-model="filters.keyword" class="form-control" placeholder="搜索商品名称" @keyup.enter="reload" />
        <button class="btn btn-primary" @click="reload">搜索</button>
      </div>

      <div class="order-list" v-if="orders.length">
        <div v-for="o in orders" :key="o.id" class="order-card">
          <div class="order-head">
            <div class="order-no">订单号：{{ o.orderNo }}</div>
            <OrderStatusTag :status="o.status" />
          </div>
          <div class="order-body">
            <div class="order-items">
              <div v-for="(it, idx) in o.itemPreview" :key="idx" class="oi">
                <div class="oi-img"><img :src="it.mainImage" :alt="it.productName" /></div>
                <div class="oi-name">{{ it.productName }}</div>
                <div class="oi-qty">x{{ it.quantity }}</div>
              </div>
            </div>
            <div class="order-amount">
              <div class="order-time">{{ formatDateTime(o.createTime) }}</div>
              <div>实付 <span class="price">{{ formatMoney(o.payAmount) }}</span></div>
            </div>
          </div>
          <div class="order-actions">
            <router-link :to="'/orders/' + o.id" class="btn btn-outline btn-sm">查看详情</router-link>
            <button v-if="o.status === 0" class="btn btn-primary btn-sm" @click="goPay(o)">去支付</button>
            <button v-if="o.status === 2" class="btn btn-primary btn-sm" @click="confirmReceipt(o)">确认收货</button>
            <router-link v-if="o.status === 3" :to="'/after-sale/apply/' + o.id" class="btn btn-outline btn-sm">申请售后</router-link>
          </div>
        </div>
      </div>
      <EmptyState v-else text="暂无相关订单" />

      <Pagination
        :pageNum="pagination.pageNum"
        :pageSize="pagination.pageSize"
        :total="pagination.total"
        :pages="pagination.pages"
        @change="onPageChange"
      />
    </div>

    <ConfirmDialog
      :visible="confirmVisible"
      title="确认收货"
      message="确认已收到商品？确认后订单将完成。"
      @confirm="doConfirm"
      @cancel="confirmVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getOrders, confirmOrder } from '../../api/user'
import { formatMoney, formatDateTime } from '../../utils/format'
import { notifySuccess } from '../../utils/request'
import ConfirmDialog from '../../components/ConfirmDialog.vue'
import OrderStatusTag from '../../components/OrderStatusTag.vue'
import EmptyState from '../../components/EmptyState.vue'
import Pagination from '../../components/Pagination.vue'

const router = useRouter()
const tabs = [
  { label: '全部', value: '' },
  { label: '待支付', value: 0 },
  { label: '待发货', value: 1 },
  { label: '运输中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已取消', value: 4 },
  { label: '售后中', value: 5 }
]
const filters = reactive({ status: '', keyword: '' })
const pagination = reactive({ pageNum: 1, pageSize: 8, total: 0, pages: 1 })
const orders = ref([])
const confirmVisible = ref(false)
const pendingOrder = ref(null)

async function loadOrders() {
  const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
  if (filters.status !== '') params.status = filters.status
  if (filters.keyword) params.keyword = filters.keyword
  try {
    const res = await getOrders(params)
    orders.value = res.list || []
    pagination.total = res.total || 0
    pagination.pages = res.pages || 1
  } catch (e) {
    orders.value = []
  }
}

function changeTab(v) {
  filters.status = v
  reload()
}
function reload() {
  pagination.pageNum = 1
  loadOrders()
}
function onPageChange(p) {
  pagination.pageNum = p
  loadOrders()
}
function goPay(o) {
  router.push('/orders/' + o.id)
}
function confirmReceipt(o) {
  pendingOrder.value = o
  confirmVisible.value = true
}
async function doConfirm() {
  try {
    await confirmOrder(pendingOrder.value.id)
    notifySuccess('确认收货成功')
    confirmVisible.value = false
    loadOrders()
  } catch (e) {}
}

onMounted(loadOrders)
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 16px;
  overflow-x: auto;
}
.tab {
  padding: 10px 16px;
  border: none;
  background: none;
  color: var(--text-secondary);
  font-weight: 500;
  white-space: nowrap;
  border-bottom: 2px solid transparent;
}
.tab.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
}
.order-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.order-card {
  border: 1px solid var(--border);
  border-radius: 10px;
  overflow: hidden;
}
.order-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f9fafb;
}
.order-no {
  font-size: 13px;
  color: var(--text-secondary);
}
.order-body {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 16px;
  gap: 16px;
  flex-wrap: wrap;
}
.order-items {
  display: flex;
  gap: 16px;
  flex: 1;
  flex-wrap: wrap;
}
.oi {
  display: flex;
  align-items: center;
  gap: 8px;
}
.oi-img {
  width: 56px;
  height: 56px;
  border-radius: 6px;
  background: #f0f7f1;
  overflow: hidden;
}
.oi-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.oi-qty {
  color: var(--text-light);
  font-size: 13px;
}
.order-amount {
  text-align: right;
}
.order-time {
  color: var(--text-light);
  font-size: 12px;
  margin-bottom: 6px;
}
.order-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid var(--border);
}
</style>
