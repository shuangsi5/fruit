<template>
  <div class="page-container">
    <!-- 搜索横幅 -->
    <section class="hero">
      <div class="hero-text">
        <h1>新鲜水果，产地直达</h1>
        <p>应季好果，智能推荐，守护您的每一口新鲜</p>
      </div>
      <div class="hero-search">
        <input v-model="keyword" class="search-input" placeholder="搜索想买的水果，如：赣南脐橙" @keyup.enter="goSearch" />
        <button class="btn btn-accent" @click="goSearch">搜索</button>
      </div>
    </section>

    <!-- 分类导航 -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">商品分类</h2>
        <router-link to="/category" class="section-more">查看全部 ></router-link>
      </div>
      <div class="category-grid" v-if="categories.length">
        <router-link
          v-for="c in categories"
          :key="c.id"
          :to="'/category?id=' + c.id"
          class="category-item"
        >
          <span class="category-icon">{{ (c.name || '果').slice(0, 1) }}</span>
          <span>{{ c.name }}</span>
        </router-link>
      </div>
      <EmptyState v-else text="暂无分类数据" />
    </section>

    <!-- 个性化推荐 -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">为您推荐</h2>
        <span class="recommend-hint">基于您的偏好智能推荐</span>
      </div>
      <div class="product-grid" v-if="recommendList.length">
        <ProductCard v-for="p in recommendList" :key="p.productId || p.id" :product="normalizeProduct(p)" />
      </div>
      <EmptyState v-else text="暂无推荐商品" />
    </section>

    <!-- 热销榜单 -->
    <section class="section">
      <div class="section-head">
        <h2 class="section-title">热销水果</h2>
        <router-link to="/category" class="section-more">更多好果 ></router-link>
      </div>
      <div class="product-grid" v-if="hotProducts.length">
        <ProductCard v-for="p in hotProducts" :key="p.id" :product="p" />
      </div>
      <EmptyState v-else text="暂无商品，去看看其他分类吧" />
    </section>
  </div>
</template>

<script setup>
import { ref, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import { getCategories, getProducts, getRecommendations } from '../../api/user'
import ProductCard from '../../components/ProductCard.vue'
import EmptyState from '../../components/EmptyState.vue'

const router = useRouter()
const keyword = ref('')
const categories = ref([])
const recommendList = ref([])
const hotProducts = ref([])

function normalizeProduct(p) {
  return {
    id: p.productId || p.id,
    name: p.name,
    subtitle: p.subtitle,
    mainImage: p.mainImage,
    price: p.price,
    originalPrice: p.originalPrice,
    unit: p.unit,
    origin: p.origin,
    salesCount: p.salesCount,
    merchantName: p.merchantName,
    stock: p.stock,
    reason: p.reason
  }
}

function goSearch() {
  router.push({ path: '/category', query: { keyword: keyword.value } })
}

async function loadData() {
  try {
    const cats = await getCategories({ parentId: 0 })
    categories.value = Array.isArray(cats) ? cats : cats.list || []
  } catch (e) {
    categories.value = []
  }
  try {
    const res = await getProducts({ sortBy: 'sales', pageNum: 1, pageSize: 8 })
    hotProducts.value = res.list || []
  } catch (e) {
    hotProducts.value = []
  }
  try {
    const rec = await getRecommendations({ scene: 'HOME', limit: 10 })
    recommendList.value = rec.list || []
  } catch (e) {
    recommendList.value = []
  }
}

onMounted(loadData)
</script>

<style scoped>
.hero {
  background: linear-gradient(135deg, #2e9e5b, #46b56f);
  border-radius: 16px;
  padding: 40px 48px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: space-between;
  flex-wrap: wrap;
  gap: 24px;
}
.hero-text h1 {
  font-size: 30px;
  margin-bottom: 8px;
}
.hero-text p {
  font-size: 15px;
  opacity: 0.9;
}
.hero-search {
  display: flex;
  gap: 8px;
  width: 400px;
  max-width: 100%;
}
.search-input {
  flex: 1;
  padding: 12px 16px;
  border: none;
  border-radius: 8px;
  outline: none;
  font-size: 14px;
}
.section {
  margin-top: 36px;
}
.section-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 18px;
}
.section-title {
  font-size: 20px;
  font-weight: 700;
}
.section-more {
  color: var(--primary);
  font-size: 13px;
}
.recommend-hint {
  color: var(--text-light);
  font-size: 12px;
}
.category-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(120px, 1fr));
  gap: 14px;
}
.category-item {
  background: #fff;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 20px 10px;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 10px;
  transition: transform 0.2s;
}
.category-item:hover {
  transform: translateY(-3px);
  color: var(--primary);
}
.category-icon {
  width: 48px;
  height: 48px;
  border-radius: 50%;
  background: var(--primary-light);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
}
@media (max-width: 768px) {
  .hero {
    padding: 28px 20px;
  }
  .hero-text h1 {
    font-size: 24px;
  }
}
</style>
