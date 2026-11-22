<template>
  <div class="cart-item">
    <label class="check-wrap">
      <input type="checkbox" :checked="item.selected === 1" @change="$emit('toggle', item.id, $event.target.checked)" />
    </label>
    <router-link :to="'/product/' + item.productId" class="item-img">
      <img :src="item.mainImage" :alt="item.productName" />
    </router-link>
    <div class="item-info">
      <router-link :to="'/product/' + item.productId" class="item-name">{{ item.productName }}</router-link>
      <div class="item-price">{{ formatMoney(item.price) }} / 份</div>
    </div>
    <div class="item-quantity">
      <button class="qty-btn" :disabled="item.quantity <= 1" @click="$emit('qty', item.id, item.quantity - 1)">-</button>
      <span class="qty-num">{{ item.quantity }}</span>
      <button class="qty-btn" :disabled="item.quantity >= item.stock" @click="$emit('qty', item.id, item.quantity + 1)">+</button>
    </div>
    <div class="item-subtotal price">{{ formatMoney(item.subtotal) }}</div>
    <button class="item-remove" @click="$emit('remove', item.id)">删除</button>
  </div>
</template>

<script setup>
import { formatMoney } from '../utils/format'
defineProps({
  item: { type: Object, required: true }
})
defineEmits(['toggle', 'qty', 'remove'])
</script>

<style scoped>
.cart-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 16px 0;
  border-bottom: 1px solid var(--border);
}
.check-wrap input {
  width: 18px;
  height: 18px;
  accent-color: var(--primary);
  cursor: pointer;
}
.item-img {
  width: 80px;
  height: 80px;
  border-radius: 8px;
  background: #f0f7f1;
  overflow: hidden;
  flex-shrink: 0;
}
.item-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.item-info {
  flex: 1;
  min-width: 0;
}
.item-name {
  font-weight: 600;
  color: var(--text);
  display: block;
  margin-bottom: 6px;
}
.item-name:hover {
  color: var(--primary);
}
.item-price {
  color: var(--text-secondary);
  font-size: 13px;
}
.item-quantity {
  display: flex;
  align-items: center;
  gap: 8px;
}
.qty-btn {
  width: 28px;
  height: 28px;
  border: 1px solid var(--border);
  background: #fff;
  border-radius: 6px;
  font-size: 16px;
  line-height: 1;
}
.qty-btn:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}
.qty-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.qty-num {
  min-width: 28px;
  text-align: center;
}
.item-subtotal {
  min-width: 80px;
  text-align: right;
  font-size: 16px;
}
.item-remove {
  border: none;
  background: none;
  color: var(--text-light);
  font-size: 13px;
  padding: 6px;
}
.item-remove:hover {
  color: var(--danger);
}
@media (max-width: 768px) {
  .cart-item {
    gap: 8px;
    flex-wrap: wrap;
  }
  .item-info {
    width: calc(100% - 120px);
  }
}
</style>
