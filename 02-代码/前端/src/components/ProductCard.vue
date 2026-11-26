<template>
  <router-link :to="'/product/' + product.id" class="product-card">
    <div class="card-img">
      <img :src="imageUrl" :alt="product.name" />
      <span v-if="isLowStock" class="img-tag warning">库存紧张</span>
      <span v-else-if="isExpiring" class="img-tag danger">临期</span>
    </div>
    <div class="card-info">
      <div class="card-name">{{ product.name }}</div>
      <div class="card-subtitle">{{ product.subtitle || product.origin || '新鲜水果' }}</div>
      <div class="card-price">
        <span class="price">{{ formatMoney(product.price) }}</span>
        <span v-if="product.originalPrice" class="price-original">{{ formatMoney(product.originalPrice) }}</span>
        <span class="unit">/{{ product.unit || '斤' }}</span>
      </div>
      <div class="card-meta">
        <span>已售 {{ product.salesCount || 0 }}</span>
        <span>{{ product.merchantName || '官方优选' }}</span>
      </div>
    </div>
  </router-link>
</template>

<script setup>
import { computed } from 'vue'
import { formatMoney } from '../utils/format'

const props = defineProps({
  product: { type: Object, required: true }
})

const imageUrl = computed(() => {
  const img = props.product.mainImage
  if (!img) return ''
  if (img.startsWith('http')) return img
  return img
})

const isLowStock = computed(() => props.product.stock !== undefined && props.product.stock > 0 && props.product.stock < 10)
const isExpiring = computed(() => props.product.remainingDays !== undefined && props.product.remainingDays >= 0 && props.product.remainingDays <= 3)
</script>

<style scoped>
.product-card {
  display: block;
  background: #fff;
  border-radius: var(--radius);
  overflow: hidden;
  box-shadow: var(--shadow);
  transition: transform 0.2s, box-shadow 0.2s;
}
.product-card:hover {
  transform: translateY(-4px);
  box-shadow: var(--shadow-lg);
}
.card-img {
  width: 100%;
  height: 180px;
  background: linear-gradient(135deg, #f0f7f1, #e3efe7);
  position: relative;
  overflow: hidden;
}
.card-img img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.img-tag {
  position: absolute;
  top: 8px;
  left: 8px;
  padding: 2px 8px;
  border-radius: 6px;
  font-size: 11px;
  color: #fff;
}
.img-tag.warning {
  background: var(--warning);
}
.img-tag.danger {
  background: var(--danger);
}
.card-info {
  padding: 14px;
}
.card-name {
  font-size: 15px;
  font-weight: 600;
  color: var(--text);
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-subtitle {
  font-size: 12px;
  color: var(--text-light);
  margin: 4px 0 8px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.card-price {
  display: flex;
  align-items: baseline;
  gap: 6px;
}
.card-price .price {
  font-size: 18px;
}
.unit {
  font-size: 12px;
  color: var(--text-light);
}
.card-meta {
  display: flex;
  justify-content: space-between;
  margin-top: 10px;
  font-size: 12px;
  color: var(--text-light);
}
</style>
