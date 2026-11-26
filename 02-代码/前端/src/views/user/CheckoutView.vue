<template>
  <div class="page-container">
    <h2 class="page-title">确认下单</h2>

    <div class="card">
      <h3 class="block-title">收货地址</h3>
      <div class="address-list" v-if="addresses.length">
        <div
          v-for="a in addresses"
          :key="a.id"
          class="address-item"
          :class="{ active: selectedAddressId === a.id }"
          @click="selectedAddressId = a.id"
        >
          <div class="address-radio"></div>
          <div class="address-main">
            <div class="address-top">
              <strong>{{ a.receiver }}</strong>
              <span>{{ a.phone }}</span>
              <span v-if="a.isDefault === 1" class="tag tag-success">默认</span>
            </div>
            <div class="address-detail">{{ a.province }} {{ a.city }} {{ a.district }} {{ a.detail }}</div>
          </div>
        </div>
      </div>
      <EmptyState v-else text="暂无收货地址，请先新增" />

      <div class="add-address" v-if="showAddForm">
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">收货人</label>
            <input v-model="newAddress.receiver" class="form-control" placeholder="收货人姓名" />
          </div>
          <div class="form-group">
            <label class="form-label">手机号</label>
            <input v-model="newAddress.phone" class="form-control" placeholder="联系电话" />
          </div>
        </div>
        <div class="form-row">
          <div class="form-group">
            <label class="form-label">省</label>
            <input v-model="newAddress.province" class="form-control" placeholder="省份" />
          </div>
          <div class="form-group">
            <label class="form-label">市</label>
            <input v-model="newAddress.city" class="form-control" placeholder="城市" />
          </div>
        </div>
        <div class="form-group">
          <label class="form-label">详细地址</label>
          <input v-model="newAddress.detail" class="form-control" placeholder="区/街道/门牌号" />
        </div>
        <label class="check-default">
          <input type="checkbox" v-model="newAddress.isDefault" /> 设为默认地址
        </label>
        <button class="btn btn-primary" @click="saveAddress">保存地址</button>
      </div>
      <button v-else class="btn btn-outline mt-16" @click="showAddForm = true">+ 新增收货地址</button>
    </div>

    <div class="card">
      <h3 class="block-title">商品清单</h3>
      <div class="checkout-item" v-for="item in checkedItems" :key="item.id">
        <div class="ci-img"><img :src="item.mainImage" :alt="item.productName" /></div>
        <div class="ci-name">{{ item.productName }}</div>
        <div class="ci-price">{{ formatMoney(item.price) }}</div>
        <div class="ci-qty">x {{ item.quantity }}</div>
        <div class="ci-subtotal price">{{ formatMoney(item.subtotal) }}</div>
      </div>
    </div>

    <div class="card">
      <div class="form-group">
        <label class="form-label">订单备注（选填）</label>
        <textarea v-model="remark" class="form-control" placeholder="如：尽快发货、周末送达等"></textarea>
      </div>
      <div class="form-group">
        <label class="form-label">支付方式</label>
        <div class="pay-methods">
          <label><input type="radio" v-model="payMethod" :value="1" /> 模拟微信支付</label>
          <label><input type="radio" v-model="payMethod" :value="2" /> 模拟支付宝</label>
        </div>
      </div>

      <div class="checkout-summary">
        <div>共 {{ checkedItems.length }} 种商品，{{ totalQuantity }} 件</div>
        <div>应付金额：<span class="price big">{{ formatMoney(totalAmount) }}</span></div>
      </div>
      <div class="submit-row">
        <button class="btn btn-accent btn-lg" :disabled="!canSubmit || submitting" @click="submitOrder">
          {{ submitting ? '提交中...' : '提交订单' }}
        </button>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, getAddresses, addAddress, createOrder } from '../../api/user'
import { useAppStore } from '../../store'
import { formatMoney } from '../../utils/format'
import { notifySuccess, notifyError } from '../../utils/request'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const app = useAppStore()
const addresses = ref([])
const selectedAddressId = ref(null)
const cartItems = ref([])
const remark = ref('')
const payMethod = ref(1)
const submitting = ref(false)
const showAddForm = ref(false)
const newAddress = ref({ receiver: '', phone: '', province: '', city: '', district: '', detail: '', isDefault: false })

const checkedItems = computed(() => cartItems.value.filter((i) => i.selected === 1))
const totalQuantity = computed(() => checkedItems.value.reduce((s, i) => s + i.quantity, 0))
const totalAmount = computed(() => checkedItems.value.reduce((s, i) => s + Number(i.subtotal || 0), 0))
const canSubmit = computed(() => selectedAddressId.value !== null && checkedItems.value.length > 0)

async function loadData() {
  try {
    addresses.value = await getAddresses()
    const def = addresses.value.find((a) => a.isDefault === 1)
    selectedAddressId.value = def ? def.id : addresses.value[0]?.id || null
  } catch (e) {
    addresses.value = []
  }
  try {
    const cart = await getCart()
    cartItems.value = cart.items || []
  } catch (e) {
    cartItems.value = []
  }
}

async function saveAddress() {
  if (!newAddress.value.receiver || !newAddress.value.phone || !newAddress.value.detail) {
    notifyError('请填写收货人、手机号和详细地址')
    return
  }
  try {
    await addAddress({
      receiver: newAddress.value.receiver,
      phone: newAddress.value.phone,
      province: newAddress.value.province,
      city: newAddress.value.city,
      district: newAddress.value.district,
      detail: newAddress.value.detail,
      isDefault: newAddress.value.isDefault ? 1 : 0
    })
    notifySuccess('地址已保存')
    showAddForm.value = false
    await loadData()
  } catch (e) {}
}

async function submitOrder() {
  if (!canSubmit.value) {
    notifyError('请选择收货地址并勾选商品')
    return
  }
  submitting.value = true
  try {
    const data = await createOrder({
      addressId: selectedAddressId.value,
      payMethod: payMethod.value,
      remark: remark.value,
      items: checkedItems.value.map((i) => ({ productId: i.productId, quantity: i.quantity }))
    })
    notifySuccess('订单创建成功，请完成支付')
    app.setCartCount(0)
    router.push('/orders/' + data.orderId)
  } catch (e) {
    // 错误已提示
  } finally {
    submitting.value = false
  }
}

onMounted(loadData)
</script>

<style scoped>
.block-title {
  font-size: 16px;
  margin-bottom: 16px;
  padding-bottom: 12px;
  border-bottom: 1px solid var(--border);
}
.address-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(280px, 1fr));
  gap: 12px;
}
.address-item {
  display: flex;
  gap: 12px;
  padding: 14px;
  border: 2px solid var(--border);
  border-radius: 10px;
  cursor: pointer;
  transition: border-color 0.2s;
}
.address-item.active {
  border-color: var(--primary);
  background: var(--primary-light);
}
.address-radio {
  width: 16px;
  height: 16px;
  border: 2px solid var(--border);
  border-radius: 50%;
  flex-shrink: 0;
  margin-top: 3px;
}
.address-item.active .address-radio {
  border-color: var(--primary);
  background: var(--primary);
  box-shadow: inset 0 0 0 3px #fff;
}
.address-top {
  display: flex;
  align-items: center;
  gap: 10px;
  margin-bottom: 6px;
}
.address-detail {
  color: var(--text-secondary);
  font-size: 13px;
}
.add-address {
  margin-top: 16px;
  padding: 16px;
  background: #f9fafb;
  border-radius: 10px;
}
.check-default {
  display: flex;
  align-items: center;
  gap: 8px;
  margin-bottom: 16px;
  font-size: 13px;
}
.checkout-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 1px solid var(--border);
}
.ci-img {
  width: 60px;
  height: 60px;
  border-radius: 6px;
  background: #f0f7f1;
  overflow: hidden;
}
.ci-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.ci-name {
  flex: 1;
}
.ci-qty {
  color: var(--text-secondary);
}
.ci-subtotal {
  min-width: 80px;
  text-align: right;
}
.pay-methods {
  display: flex;
  gap: 24px;
}
.pay-methods label {
  cursor: pointer;
}
.checkout-summary {
  display: flex;
  justify-content: flex-end;
  align-items: center;
  gap: 30px;
  padding: 16px 0;
  border-top: 1px solid var(--border);
  font-size: 14px;
}
.price.big {
  font-size: 24px;
}
.submit-row {
  display: flex;
  justify-content: flex-end;
}
</style>
