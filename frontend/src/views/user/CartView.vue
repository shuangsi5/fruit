<template>
  <div class="page-container">
    <h2 class="page-title">我的购物车 <span class="count-hint">({{ cart.totalQuantity || 0 }} 件商品)</span></h2>

    <div class="card" v-if="cart.items && cart.items.length">
      <div class="cart-header">
        <label class="check-all">
          <input type="checkbox" :checked="allSelected" @change="toggleAll($event.target.checked)" />
          全选
        </label>
        <span class="unit-price">单价</span>
        <span class="quantity-col">数量</span>
        <span class="subtotal-col">小计</span>
        <span class="action-col">操作</span>
      </div>
      <CartItem
        v-for="item in cart.items"
        :key="item.id"
        :item="item"
        @toggle="onToggle"
        @qty="onQty"
        @remove="onRemove"
      />

      <div class="cart-footer">
        <div class="selected-info">已选 {{ selectedCount }} 件商品</div>
        <div class="total-info">
          合计：<span class="price big">{{ formatMoney(cart.selectedSubtotal || 0) }}</span>
        </div>
        <button class="btn btn-accent btn-lg" :disabled="selectedCount === 0" @click="checkout">去结算</button>
      </div>
    </div>

    <EmptyState v-else text="购物车还是空的，去挑选新鲜水果吧">
      <template #action>
        <router-link to="/home" class="btn btn-primary">去逛逛</router-link>
      </template>
    </EmptyState>
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCart, updateCartItem, deleteCartItem } from '../../api/user'
import { useAppStore } from '../../store'
import { formatMoney } from '../../utils/format'
import { notifySuccess, notifyError } from '../../utils/request'
import CartItem from '../../components/CartItem.vue'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const app = useAppStore()
const cart = ref({ items: [], totalQuantity: 0, selectedSubtotal: 0 })

const allSelected = computed(() => cart.value.items.length > 0 && cart.value.items.every((i) => i.selected === 1))
const selectedCount = computed(() => cart.value.items.filter((i) => i.selected === 1).length)

async function loadCart() {
  try {
    cart.value = await getCart()
    app.setCartCount(cart.value.totalQuantity || 0)
  } catch (e) {
    cart.value = { items: [], totalQuantity: 0, selectedSubtotal: 0 }
  }
}

async function onToggle(id, checked) {
  try {
    await updateCartItem(id, { quantity: findItem(id).quantity, selected: checked ? 1 : 0 })
    await loadCart()
  } catch (e) {}
}

async function onQty(id, qty) {
  const item = findItem(id)
  if (qty > item.stock) {
    notifyError('库存不足，最多可购买 ' + item.stock + ' 件')
    return
  }
  try {
    await updateCartItem(id, { quantity: qty })
    await loadCart()
  } catch (e) {}
}

async function onRemove(id) {
  try {
    await deleteCartItem(id)
    notifySuccess('已删除')
    await loadCart()
  } catch (e) {}
}

async function toggleAll(checked) {
  try {
    for (const item of cart.value.items) {
      if ((item.selected === 1) !== checked) {
        await updateCartItem(item.id, { quantity: item.quantity, selected: checked ? 1 : 0 })
      }
    }
    await loadCart()
  } catch (e) {}
}

function findItem(id) {
  return cart.value.items.find((i) => i.id === id)
}

function checkout() {
  router.push('/checkout')
}

onMounted(loadCart)
</script>

<style scoped>
.count-hint {
  font-size: 15px;
  color: var(--text-light);
  font-weight: 400;
}
.cart-header {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 12px 0;
  border-bottom: 2px solid var(--border);
  font-size: 13px;
  color: var(--text-secondary);
}
.check-all {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 100px;
}
.check-all input {
  width: 18px;
  height: 18px;
  accent-color: var(--primary);
}
.unit-price {
  margin-left: 130px;
}
.quantity-col {
  margin-left: 80px;
}
.subtotal-col {
  margin-left: 60px;
}
.action-col {
  margin-left: 40px;
}
.cart-footer {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 24px;
  padding-top: 20px;
  flex-wrap: wrap;
}
.selected-info {
  color: var(--text-secondary);
  font-size: 13px;
  margin-right: auto;
}
.total-info {
  font-size: 15px;
}
.price.big {
  font-size: 24px;
}
@media (max-width: 768px) {
  .cart-header {
    display: none;
  }
}
</style>
