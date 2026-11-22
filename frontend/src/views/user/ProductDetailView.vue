<template>
  <div class="page-container" v-if="product.id">
    <div class="breadcrumb">
      <router-link to="/home">首页</router-link> /
      <router-link to="/category">全部商品</router-link> /
      <span>{{ product.name }}</span>
    </div>

    <div class="card detail-card">
      <div class="detail-gallery">
        <img :src="mainImage" :alt="product.name" />
      </div>
      <div class="detail-info">
        <h1 class="detail-name">{{ product.name }}</h1>
        <p class="detail-subtitle">{{ product.subtitle || '新鲜优质水果，产地直发' }}</p>

        <div class="detail-pricing">
          <div class="price-line">
            <span class="price big">{{ formatMoney(product.price) }}</span>
            <span v-if="product.originalPrice" class="price-original">{{ formatMoney(product.originalPrice) }}</span>
          </div>
          <div class="price-meta">
            <span>单位：{{ product.unit || '斤' }}</span>
            <span>已售 {{ product.salesCount || 0 }}</span>
            <span>浏览 {{ product.viewCount || 0 }}</span>
          </div>
        </div>

        <div class="detail-attributes">
          <div class="attr-row"><span class="attr-label">产地</span><span>{{ product.origin || '-' }}</span></div>
          <div class="attr-row"><span class="attr-label">规格</span><span>{{ product.spec || '-' }}</span></div>
          <div class="attr-row"><span class="attr-label">生产日期</span><span>{{ formatDate(product.productionDate) }}</span></div>
          <div class="attr-row">
            <span class="attr-label">保质期</span>
            <span>{{ product.shelfLifeDays || 7 }} 天</span>
            <span v-if="remainingDays >= 0" class="tag" :class="remainingDays <= 3 ? 'tag-danger' : 'tag-info'">
              剩余 {{ remainingDays }} 天
            </span>
          </div>
          <div class="attr-row"><span class="attr-label">库存</span><span :class="{ 'text-danger': product.stock <= 0 }">{{ product.stock }} {{ product.unit || '斤' }}</span></div>
          <div class="attr-row"><span class="attr-label">商家</span><span>{{ product.merchantName || '官方优选' }}</span></div>
        </div>

        <div class="detail-desc">
          <h3>商品描述</h3>
          <p>{{ product.description || '新鲜采摘，品质保证，欢迎选购。' }}</p>
        </div>

        <div class="detail-actions">
          <div class="qty-select">
            <span>数量</span>
            <button class="qty-btn" :disabled="quantity <= 1" @click="quantity--">-</button>
            <span class="qty-num">{{ quantity }}</span>
            <button class="qty-btn" :disabled="quantity >= (product.stock || 0)" @click="quantity++">+</button>
          </div>
          <button class="btn btn-accent btn-lg" :disabled="product.stock <= 0" @click="addCart">
            {{ product.stock <= 0 ? '已售罄' : '加入购物车' }}
          </button>
          <button class="btn btn-primary btn-lg" :disabled="product.stock <= 0" @click="buyNow">立即购买</button>
        </div>
      </div>
    </div>

    <div class="section" v-if="related.length">
      <h2 class="section-title">相关推荐</h2>
      <div class="product-grid">
        <ProductCard v-for="p in related" :key="p.productId || p.id" :product="normalizeRelated(p)" />
      </div>
    </div>
  </div>

  <div class="page-container" v-else>
    <EmptyState text="商品不存在或已下架" />
  </div>
</template>

<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { getProductDetail, getRelatedProducts, addToCart } from '../../api/user'
import { useAppStore } from '../../store'
import { formatMoney, formatDate, today } from '../../utils/format'
import { notifySuccess } from '../../utils/request'
import ProductCard from '../../components/ProductCard.vue'
import EmptyState from '../../components/EmptyState.vue'

const route = useRoute()
const router = useRouter()
const app = useAppStore()
const product = ref({})
const related = ref([])
const quantity = ref(1)

const mainImage = computed(() => product.value.mainImage || '')
const remainingDays = computed(() => {
  if (!product.value.productionDate || !product.value.shelfLifeDays) return -1
  const start = new Date(product.value.productionDate)
  const end = new Date(start)
  end.setDate(end.getDate() + Number(product.value.shelfLifeDays))
  const diff = Math.ceil((end - new Date(today())) / 86400000)
  return diff
})

function normalizeRelated(p) {
  return {
    id: p.productId || p.id,
    name: p.name,
    mainImage: p.mainImage,
    price: p.price,
    salesCount: p.salesCount
  }
}

async function loadData() {
  const id = route.params.id
  try {
    product.value = await getProductDetail(id)
  } catch (e) {
    product.value = {}
  }
  try {
    const res = await getRelatedProducts(id, { limit: 6 })
    related.value = res.list || []
  } catch (e) {
    related.value = []
  }
}

async function addCart() {
  try {
    await addToCart({ productId: product.value.id, quantity: quantity.value })
    notifySuccess('已加入购物车')
    app.setCartCount(app.cartCount + quantity.value)
  } catch (e) {
    // 错误已在拦截器提示
  }
}

function buyNow() {
  addCart().then(() => router.push('/cart'))
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
.detail-card {
  display: grid;
  grid-template-columns: 420px 1fr;
  gap: 32px;
}
.detail-gallery {
  width: 100%;
  height: 420px;
  border-radius: var(--radius);
  background: linear-gradient(135deg, #f0f7f1, #e3efe7);
  overflow: hidden;
}
.detail-gallery img {
  width: 100%;
  height: 100%;
  object-fit: cover;
}
.detail-name {
  font-size: 26px;
  margin-bottom: 6px;
}
.detail-subtitle {
  color: var(--text-secondary);
  margin-bottom: 18px;
}
.detail-pricing {
  background: #f9fafb;
  border-radius: 10px;
  padding: 16px;
  margin-bottom: 18px;
}
.price.big {
  font-size: 30px;
}
.price-line {
  display: flex;
  align-items: baseline;
  gap: 12px;
}
.price-meta {
  display: flex;
  gap: 20px;
  margin-top: 8px;
  font-size: 13px;
  color: var(--text-secondary);
}
.detail-attributes {
  border-top: 1px solid var(--border);
  padding-top: 12px;
}
.attr-row {
  display: flex;
  align-items: center;
  gap: 16px;
  padding: 7px 0;
  font-size: 14px;
}
.attr-label {
  width: 70px;
  color: var(--text-light);
  flex-shrink: 0;
}
.detail-desc {
  margin-top: 16px;
}
.detail-desc h3 {
  font-size: 16px;
  margin-bottom: 8px;
}
.detail-desc p {
  color: var(--text-secondary);
  line-height: 1.8;
}
.detail-actions {
  display: flex;
  align-items: center;
  gap: 14px;
  margin-top: 24px;
  flex-wrap: wrap;
}
.qty-select {
  display: flex;
  align-items: center;
  gap: 10px;
}
.qty-btn {
  width: 32px;
  height: 32px;
  border: 1px solid var(--border);
  background: #fff;
  border-radius: 6px;
  font-size: 18px;
  line-height: 1;
}
.qty-btn:disabled {
  opacity: 0.4;
  cursor: not-allowed;
}
.qty-num {
  min-width: 28px;
  text-align: center;
}
.section {
  margin-top: 36px;
}
.section-title {
  font-size: 20px;
  margin-bottom: 18px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(200px, 1fr));
  gap: 18px;
}
@media (max-width: 900px) {
  .detail-card {
    grid-template-columns: 1fr;
  }
  .detail-gallery {
    height: 300px;
  }
}
</style>
