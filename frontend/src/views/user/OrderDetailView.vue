<template>
  <div class="page-container">
    <div class="breadcrumb">
      <router-link to="/orders">我的订单</router-link> / 订单详情
    </div>

    <div class="card" v-if="order.id">
      <div class="order-head-card">
        <div>
          <div class="order-no">订单号：{{ order.orderNo }}</div>
          <div class="order-time">下单时间：{{ formatDateTime(order.createTime) }}</div>
        </div>
        <OrderStatusTag :status="order.status" />
      </div>

      <!-- 状态时间轴 -->
      <div class="timeline-section">
        <h3>订单进度</h3>
        <div class="timeline">
          <div
            v-for="(step, idx) in timeline"
            :key="idx"
            class="timeline-step"
            :class="{ done: idx <= currentStep, current: idx === currentStep }"
          >
            <div class="step-dot">{{ idx <= currentStep ? '✓' : idx + 1 }}</div>
            <div class="step-label">{{ step.label }}</div>
            <div class="step-desc" v-if="step.desc">{{ step.desc }}</div>
          </div>
        </div>
      </div>

      <!-- 待支付倒计时 / 支付操作 -->
      <div class="pay-banner" v-if="order.status === 0">
        <div>
          订单待支付，支付金额 <span class="price big">{{ formatMoney(order.payAmount) }}</span>
          <div class="expire-hint" v-if="order.expireTime">请在 {{ formatDateTime(order.expireTime) }} 前完成支付</div>
        </div>
        <div class="pay-actions">
          <button class="btn btn-primary" @click="doPay(1)">模拟微信支付</button>
          <button class="btn btn-outline" @click="doPay(2)">模拟支付宝</button>
          <button class="btn btn-plain" @click="doCancel">取消订单</button>
        </div>
      </div>

      <div class="logistics-section" v-if="logistics.length">
        <h3>物流信息</h3>
        <div v-if="logisticsNo" class="logistics-no">运单号：{{ logisticsNo }}</div>
        <div class="logistics-timeline">
          <div v-for="(l, idx) in logistics" :key="idx" class="log-item">
            <div class="log-dot"></div>
            <div class="log-content">
              <div class="log-text">{{ l.desc || l.text || l.content || l.description || l.statusText }}</div>
              <div class="log-time">{{ formatDateTime(l.time || l.createTime || l.logTime) }}</div>
            </div>
          </div>
        </div>
      </div>
    </div>

    <div class="card" v-if="order.id">
      <h3>商品明细</h3>
      <div class="detail-item" v-for="(it, idx) in order.items" :key="idx">
        <div class="di-img"><img :src="it.mainImage" :alt="it.productName" /></div>
        <div class="di-name">{{ it.productName }}</div>
        <div class="di-price">{{ formatMoney(it.price) }}</div>
        <div class="di-qty">x {{ it.quantity }}</div>
        <div class="di-subtotal price">{{ formatMoney(it.subtotal) }}</div>
      </div>
      <div class="order-total">
        实付金额：<span class="price big">{{ formatMoney(order.payAmount) }}</span>
      </div>
    </div>

    <div class="card" v-if="order.id && (order.address || order.receiver)">
      <h3>收货信息</h3>
      <div class="receiver-info">
        <span>{{ order.receiver || (order.address && order.address.receiver) }}</span>
        <span>{{ order.phone || (order.address && order.address.phone) }}</span>
      </div>
      <div class="receiver-address">
        {{ order.addressDetail || (order.address && (order.address.province + ' ' + order.address.city + ' ' + order.address.district + ' ' + order.address.detail)) }}
      </div>
    </div>

    <div class="action-bar" v-if="order.id">
      <button v-if="order.status === 2" class="btn btn-primary" @click="doConfirm">确认收货</button>
      <router-link v-if="order.status === 3" :to="'/after-sale/apply/' + order.id" class="btn btn-outline">申请售后</router-link>
      <router-link to="/orders" class="btn btn-plain">返回订单列表</router-link>
    </div>

    <EmptyState v-if="!order.id" text="订单不存在" />

    <ConfirmDialog
      :visible="dialogVisible"
      :title="dialogTitle"
      :message="dialogMessage"
      :danger="dialogDanger"
      @confirm="onDialogConfirm"
      @cancel="dialogVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, payOrder, cancelOrder, confirmOrder, getLogistics } from '../../api/user'
import { formatMoney, formatDateTime } from '../../utils/format'
import { notifySuccess } from '../../utils/request'
import OrderStatusTag from '../../components/OrderStatusTag.vue'
import EmptyState from '../../components/EmptyState.vue'
import ConfirmDialog from '../../components/ConfirmDialog.vue'

const route = useRoute()
const router = useRouter()
const order = ref({})
const logistics = ref([])
const logisticsNo = ref('')

const dialogVisible = ref(false)
const dialogTitle = ref('')
const dialogMessage = ref('')
const dialogDanger = ref(false)
let dialogAction = null

const steps = [
  { label: '提交订单', desc: '' },
  { label: '付款', desc: '' },
  { label: '商家发货', desc: '' },
  { label: '收货', desc: '' },
  { label: '完成', desc: '' }
]

const currentStep = computed(() => {
  const s = order.value.status
  if (s === 0) return 0
  if (s === 1) return 1
  if (s === 2) return 2
  if (s === 3) return 3
  return 0
})

const timeline = computed(() => {
  if (order.value.status === 4) return [{ label: '订单已取消', desc: '订单已关闭' }]
  return steps
})

async function loadData() {
  const id = route.params.id
  try {
    order.value = await getOrderDetail(id)
  } catch (e) {
    order.value = {}
  }
  if (order.value.status >= 2) {
    try {
      const log = await getLogistics(id)
      if (Array.isArray(log)) {
        logistics.value = log
      } else {
        logistics.value = log.traces || log.nodes || log.list || []
        logisticsNo.value = log.logisticsNo || log.trackingNo || ''
      }
    } catch (e) {
      logistics.value = []
    }
  }
}

async function doPay(method) {
  try {
    await payOrder(order.value.id, { payMethod: method })
    notifySuccess('支付成功')
    loadData()
  } catch (e) {}
}

function doCancel() {
  dialogTitle.value = '取消订单'
  dialogMessage.value = '确定要取消该订单吗？取消后预占库存将释放。'
  dialogDanger.value = true
  dialogAction = async () => {
    await cancelOrder(order.value.id)
    notifySuccess('订单已取消')
    loadData()
  }
  dialogVisible.value = true
}

function doConfirm() {
  dialogTitle.value = '确认收货'
  dialogMessage.value = '确认已收到商品？确认后订单将完成。'
  dialogDanger.value = false
  dialogAction = async () => {
    await confirmOrder(order.value.id)
    notifySuccess('确认收货成功')
    loadData()
  }
  dialogVisible.value = true
}

async function onDialogConfirm() {
  dialogVisible.value = false
  if (dialogAction) {
    try {
      await dialogAction()
    } catch (e) {}
    dialogAction = null
  }
}

onMounted(loadData)
</script>

<style scoped>
.breadcrumb {
  font-size: 13px;
  color: var(--text-light);
  margin-bottom: 16px;
}
.breadcrumb a:hover {
  color: var(--primary);
}
.order-head-card {
  display: flex;
  align-items: flex-start;
  justify-content: space-between;
  padding-bottom: 18px;
  border-bottom: 1px solid var(--border);
}
.order-no {
  font-size: 15px;
  font-weight: 600;
}
.order-time {
  color: var(--text-light);
  font-size: 12px;
  margin-top: 6px;
}
.card h3 {
  font-size: 16px;
  margin-bottom: 16px;
}
.timeline-section {
  padding: 22px 0;
}
.timeline {
  display: flex;
  justify-content: space-between;
  position: relative;
  padding-top: 10px;
}
.timeline::before {
  content: '';
  position: absolute;
  top: 25px;
  left: 5%;
  right: 5%;
  height: 2px;
  background: var(--border);
}
.timeline-step {
  position: relative;
  z-index: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  flex: 1;
  text-align: center;
}
.step-dot {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 13px;
  font-weight: 600;
}
.timeline-step.done .step-dot {
  background: var(--primary);
}
.step-label {
  margin-top: 10px;
  font-size: 13px;
  color: var(--text-secondary);
}
.timeline-step.current .step-label {
  color: var(--primary);
  font-weight: 600;
}
.step-desc {
  font-size: 11px;
  color: var(--text-light);
}
.pay-banner {
  display: flex;
  align-items: center;
  justify-content: space-between;
  background: #fff8ec;
  border: 1px solid #f5d9a8;
  border-radius: 10px;
  padding: 18px;
  gap: 16px;
  flex-wrap: wrap;
}
.price.big {
  font-size: 24px;
}
.expire-hint {
  font-size: 12px;
  color: var(--warning);
  margin-top: 6px;
}
.pay-actions {
  display: flex;
  gap: 10px;
  flex-wrap: wrap;
}
.logistics-section {
  padding-top: 20px;
}
.logistics-no {
  font-size: 13px;
  color: var(--text-secondary);
  margin-bottom: 14px;
}
.logistics-timeline {
  border-left: 2px solid var(--border);
  margin-left: 6px;
  padding-left: 20px;
}
.log-item {
  position: relative;
  padding-bottom: 18px;
}
.log-dot {
  position: absolute;
  left: -27px;
  top: 4px;
  width: 10px;
  height: 10px;
  border-radius: 50%;
  background: var(--primary);
}
.log-text {
  font-size: 14px;
}
.log-time {
  font-size: 12px;
  color: var(--text-light);
  margin-top: 4px;
}
.detail-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}
.di-img {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  background: #f0f7f1;
  overflow: hidden;
}
.di-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.di-name {
  flex: 1;
}
.di-qty {
  color: var(--text-secondary);
}
.di-subtotal {
  min-width: 80px;
  text-align: right;
}
.order-total {
  text-align: right;
  padding-top: 16px;
}
.receiver-info {
  display: flex;
  gap: 16px;
  margin-bottom: 6px;
}
.receiver-address {
  color: var(--text-secondary);
}
.action-bar {
  display: flex;
  gap: 12px;
  justify-content: flex-end;
  margin-top: 20px;
}
</style>
