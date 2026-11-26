<template>
  <div class="page-container">
    <h2 class="page-title">申请售后</h2>

    <div class="card form-card">
      <div class="form-group">
        <label class="form-label">订单编号</label>
        <input class="form-control" :value="order.orderNo || orderId" disabled />
      </div>

      <div class="form-group">
        <label class="form-label">售后类型 <span class="required">*</span></label>
        <div class="type-select">
          <button
            type="button"
            v-for="t in types"
            :key="t.value"
            class="type-option"
            :class="{ active: form.type === t.value }"
            @click="form.type = t.value"
          >
            {{ t.label }}
          </button>
        </div>
      </div>

      <div class="form-group">
        <label class="form-label">申请商品</label>
        <div class="product-select" v-if="order.items && order.items.length">
          <label v-for="(it, idx) in order.items" :key="idx" class="product-option">
            <input type="radio" v-model="form.productId" :value="it.productId" />
            <span>{{ it.productName }} x{{ it.quantity }}</span>
          </label>
        </div>
        <input v-else class="form-control" value="订单商品" disabled />
      </div>

      <div class="form-group">
        <label class="form-label">退款金额 <span class="required">*</span></label>
        <input v-model="form.amount" type="number" step="0.01" class="form-control" placeholder="请输入退款金额" />
      </div>

      <div class="form-group">
        <label class="form-label">申请原因 <span class="required">*</span></label>
        <textarea v-model="form.reason" class="form-control" placeholder="请描述售后原因，如：商品腐烂、与描述不符等"></textarea>
      </div>

      <div class="form-group">
        <label class="form-label">凭证图片（选填，输入图片URL，多个用逗号分隔）</label>
        <input v-model="imagesStr" class="form-control" placeholder="/img/proof1.jpg,/img/proof2.jpg" />
      </div>

      <div class="form-error" v-if="errorMsg">{{ errorMsg }}</div>

      <div class="form-actions">
        <button class="btn btn-primary btn-lg" :disabled="submitting" @click="submit">
          {{ submitting ? '提交中...' : '提交申请' }}
        </button>
        <router-link to="/after-sales" class="btn btn-plain btn-lg">返回</router-link>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getOrderDetail, applyAfterSale } from '../../api/user'
import { notifySuccess } from '../../utils/request'

const route = useRoute()
const router = useRouter()
const orderId = route.params.orderId
const order = ref({})

const types = [
  { value: 1, label: '仅退款' },
  { value: 2, label: '退货退款' }
]

const form = reactive({
  type: 1,
  productId: null,
  amount: '',
  reason: '',
  orderId: Number(orderId)
})
const imagesStr = ref('')
const errorMsg = ref('')
const submitting = ref(false)

async function loadOrder() {
  try {
    order.value = await getOrderDetail(orderId)
    form.amount = order.value.payAmount || ''
    if (order.value.items && order.value.items.length) {
      form.productId = order.value.items[0].productId
    }
  } catch (e) {}
}

async function submit() {
  errorMsg.value = ''
  if (!form.reason) {
    errorMsg.value = '请填写申请原因'
    return
  }
  if (!form.amount || Number(form.amount) <= 0) {
    errorMsg.value = '请填写正确的退款金额'
    return
  }
  submitting.value = true
  try {
    const payload = {
      orderId: Number(orderId),
      type: form.type,
      productId: form.productId,
      amount: Number(form.amount),
      reason: form.reason
    }
    if (imagesStr.value) {
      payload.images = imagesStr.value.split(',').map((s) => s.trim()).filter(Boolean)
    }
    await applyAfterSale(payload)
    notifySuccess('售后申请已提交，等待商家审核')
    router.push('/after-sales')
  } catch (e) {
    errorMsg.value = e.message || '提交失败'
  } finally {
    submitting.value = false
  }
}

onMounted(loadOrder)
</script>

<style scoped>
.form-card {
  max-width: 720px;
}
.required {
  color: var(--danger);
}
.type-select {
  display: flex;
  gap: 12px;
}
.type-option {
  padding: 10px 24px;
  border: 1px solid var(--border);
  background: #fff;
  border-radius: 8px;
  color: var(--text-secondary);
}
.type-option.active {
  border-color: var(--primary);
  background: var(--primary-light);
  color: var(--primary);
  font-weight: 600;
}
.product-select {
  display: flex;
  flex-direction: column;
  gap: 10px;
  padding: 12px;
  background: #f9fafb;
  border-radius: 8px;
}
.product-option {
  display: flex;
  align-items: center;
  gap: 10px;
  cursor: pointer;
}
.form-actions {
  display: flex;
  gap: 12px;
  margin-top: 20px;
}
</style>
