<template>
  <div class="page-container">
    <h2 class="page-title">订单管理</h2>

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
        <button class="btn btn-primary" @click="reload">查询</button>
      </div>

      <div class="order-list" v-if="orders.length">
        <div v-for="o in orders" :key="o.id" class="order-card">
          <div class="order-head">
            <div>
              <strong>订单号：{{ o.orderNo }}</strong>
              <span class="receiver">收货：{{ o.receiver || o.userName || '-' }} {{ o.phone || '' }}</span>
            </div>
            <OrderStatusTag :status="o.status" />
          </div>
          <div class="order-body">
            <div class="order-items">
              <div v-for="(it, idx) in o.itemPreview || o.items || []" :key="idx" class="oi">
                <div class="oi-img"><img :src="it.mainImage" :alt="it.productName" /></div>
                <span>{{ it.productName }}</span>
                <span class="text-muted">x{{ it.quantity }}</span>
              </div>
            </div>
            <div class="order-amount">
              <div class="order-time">{{ formatDateTime(o.createTime) }}</div>
              <div>金额 <span class="price">{{ formatMoney(o.payAmount) }}</span></div>
            </div>
          </div>
          <div class="order-actions">
            <button v-if="o.status === 1" class="btn btn-primary btn-sm" @click="openShip(o)">发货</button>
            <router-link :to="'/orders/' + o.id" class="btn btn-outline btn-sm">查看详情</router-link>
          </div>
        </div>
      </div>
      <EmptyState v-else text="暂无订单" />
      <Pagination
        :pageNum="pagination.pageNum" :pageSize="pagination.pageSize"
        :total="pagination.total" :pages="pagination.pages"
        @change="onPageChange"
      />
    </div>

    <!-- 发货弹窗 -->
    <div class="dialog-mask" v-if="shipVisible" @click.self="shipVisible = false">
      <div class="small-dialog">
        <div class="dialog-title">订单发货</div>
        <div class="form-body">
          <div class="form-group">
            <label class="form-label">订单号</label>
            <input class="form-control" :value="currentOrder.orderNo" disabled />
          </div>
          <div class="form-group">
            <label class="form-label">物流公司 *</label>
            <select v-model="shipForm.logisticsCompany" class="form-control">
              <option value="顺丰速运">顺丰速运</option>
              <option value="中通快递">中通快递</option>
              <option value="圆通速递">圆通速递</option>
              <option value="韵达快递">韵达快递</option>
              <option value="京东物流">京东物流</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">运单号 *</label>
            <input v-model="shipForm.trackingNo" class="form-control" placeholder="请输入运单号" />
          </div>
          <div class="form-error" v-if="shipError">{{ shipError }}</div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-plain" @click="shipVisible = false">取消</button>
          <button class="btn btn-primary" :disabled="shipping" @click="submitShip">{{ shipping ? '发货中...' : '确认发货' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMerchantOrders, shipOrder } from '../../api/merchant'
import { formatMoney, formatDateTime } from '../../utils/format'
import { notifySuccess } from '../../utils/request'
import OrderStatusTag from '../../components/OrderStatusTag.vue'
import EmptyState from '../../components/EmptyState.vue'
import Pagination from '../../components/Pagination.vue'

const tabs = [
  { label: '全部', value: '' },
  { label: '待发货', value: 1 },
  { label: '运输中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '售后中', value: 5 }
]
const filters = reactive({ status: '', keyword: '' })
const pagination = reactive({ pageNum: 1, pageSize: 8, total: 0, pages: 1 })
const orders = ref([])

const shipVisible = ref(false)
const shipping = ref(false)
const shipError = ref('')
const currentOrder = ref({})
const shipForm = reactive({ logisticsCompany: '顺丰速运', trackingNo: '' })

async function loadOrders() {
  const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
  if (filters.status !== '') params.status = filters.status
  if (filters.keyword) params.keyword = filters.keyword
  try {
    const res = await getMerchantOrders(params)
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

function openShip(o) {
  currentOrder.value = o
  shipForm.logisticsCompany = '顺丰速运'
  shipForm.trackingNo = ''
  shipError.value = ''
  shipVisible.value = true
}
async function submitShip() {
  if (!shipForm.trackingNo) {
    shipError.value = '请输入运单号'
    return
  }
  shipping.value = true
  try {
    await shipOrder(currentOrder.value.id, {
      carrier: shipForm.logisticsCompany,
      logisticsNo: shipForm.trackingNo
    })
    notifySuccess('发货成功')
    shipVisible.value = false
    loadOrders()
  } catch (e) {
    shipError.value = e.message || '发货失败'
  } finally {
    shipping.value = false
  }
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
  gap: 12px;
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
  gap: 10px;
  flex-wrap: wrap;
}
.receiver {
  margin-left: 16px;
  font-size: 12px;
  color: var(--text-light);
}
.order-body {
  display: flex;
  justify-content: space-between;
  padding: 14px 16px;
  gap: 16px;
  flex-wrap: wrap;
}
.order-items {
  display: flex;
  gap: 14px;
  flex: 1;
  flex-wrap: wrap;
}
.oi {
  display: flex;
  align-items: center;
  gap: 8px;
}
.oi-img {
  width: 48px;
  height: 48px;
  border-radius: 6px;
  background: #f0f7f1;
  overflow: hidden;
}
.oi-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
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
.small-dialog {
  width: 100%;
  max-width: 440px;
  background: #fff;
  border-radius: 12px;
}
.dialog-title {
  padding: 20px 24px;
  font-size: 17px;
  font-weight: 700;
  border-bottom: 1px solid var(--border);
}
.form-body {
  padding: 20px 24px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border);
}
</style>
