<template>
  <div class="page-container">
    <h2 class="page-title">全部商品</h2>

    <div class="card filter-card">
      <div class="filter-bar">
        <select v-model="filters.categoryId" class="form-control" @change="reload">
          <option value="">全部分类</option>
          <option v-for="c in categories" :key="c.id" :value="c.id">{{ c.name }}</option>
        </select>
        <input v-model="filters.keyword" class="form-control" placeholder="搜索商品名称" @keyup.enter="reload" />
        <select v-model="filters.minPrice" class="form-control" @change="reload">
          <option value="">最低价</option>
          <option value="0">0 元</option>
          <option value="20">20 元</option>
          <option value="50">50 元</option>
          <option value="100">100 元</option>
        </select>
        <select v-model="filters.maxPrice" class="form-control" @change="reload">
          <option value="">最高价</option>
          <option value="30">30 元</option>
          <option value="60">60 元</option>
          <option value="100">100 元</option>
          <option value="500">500 元</option>
        </select>
        <select v-model="filters.sortBy" class="form-control" @change="reload">
          <option value="newest">最新上架</option>
          <option value="sales">销量优先</option>
          <option value="priceAsc">价格从低到高</option>
          <option value="priceDesc">价格从高到低</option>
        </select>
        <button class="btn btn-primary" @click="reload">筛选</button>
        <button class="btn btn-plain" @click="clearFilters">清空筛选</button>
      </div>
    </div>

    <div class="result-info">共找到 {{ pagination.total }} 件商品</div>

    <div class="product-grid" v-if="products.length">
      <ProductCard v-for="p in products" :key="p.id" :product="p" />
    </div>
    <EmptyState v-else text="暂无商品，去看看其他分类吧">
      <template #action>
        <button class="btn btn-outline" @click="clearFilters">清空筛选</button>
      </template>
    </EmptyState>

    <Pagination
      :pageNum="pagination.pageNum"
      :pageSize="pagination.pageSize"
      :total="pagination.total"
      :pages="pagination.pages"
      @change="onPageChange"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { useRoute } from 'vue-router'
import { getCategories, getProducts } from '../../api/user'
import ProductCard from '../../components/ProductCard.vue'
import EmptyState from '../../components/EmptyState.vue'
import Pagination from '../../components/Pagination.vue'

const route = useRoute()
const categories = ref([])
const products = ref([])
const filters = reactive({
  categoryId: '',
  keyword: '',
  minPrice: '',
  maxPrice: '',
  sortBy: 'newest'
})
const pagination = reactive({ pageNum: 1, pageSize: 12, total: 0, pages: 1 })

async function loadCategories() {
  try {
    const cats = await getCategories({ parentId: 0 })
    categories.value = Array.isArray(cats) ? cats : cats.list || []
  } catch (e) {
    categories.value = []
  }
}

async function loadProducts() {
  const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize, sortBy: filters.sortBy }
  if (filters.categoryId) params.categoryId = filters.categoryId
  if (filters.keyword) params.keyword = filters.keyword
  if (filters.minPrice) params.minPrice = filters.minPrice
  if (filters.maxPrice) params.maxPrice = filters.maxPrice
  try {
    const res = await getProducts(params)
    products.value = res.list || []
    pagination.total = res.total || 0
    pagination.pages = res.pages || 1
  } catch (e) {
    products.value = []
  }
}

function reload() {
  pagination.pageNum = 1
  loadProducts()
}
function onPageChange(p) {
  pagination.pageNum = p
  loadProducts()
}
function clearFilters() {
  filters.categoryId = ''
  filters.keyword = ''
  filters.minPrice = ''
  filters.maxPrice = ''
  filters.sortBy = 'newest'
  reload()
}

onMounted(() => {
  if (route.query.categoryId) filters.categoryId = route.query.categoryId
  if (route.query.id) filters.categoryId = route.query.id
  if (route.query.keyword) filters.keyword = route.query.keyword
  loadCategories()
  loadProducts()
})
</script>

<style scoped>
.filter-card {
  padding: 16px 18px;
}
.filter-bar .form-control {
  min-width: 130px;
}
.result-info {
  margin: 16px 0;
  color: var(--text-secondary);
  font-size: 13px;
}
.product-grid {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(220px, 1fr));
  gap: 18px;
}
</style>
