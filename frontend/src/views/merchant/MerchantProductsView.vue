<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title" style="margin: 0">商品管理</h2>
      <button class="btn btn-primary" @click="openCreate">+ 上架新商品</button>
    </div>

    <div class="card">
      <div class="filter-bar">
        <select v-model="filters.status" class="form-control" @change="reload">
          <option value="">全部状态</option>
          <option value="1">上架中</option>
          <option value="0">已下架</option>
        </select>
        <input v-model="filters.keyword" class="form-control" placeholder="搜索商品名称" @keyup.enter="reload" />
        <button class="btn btn-primary" @click="reload">查询</button>
      </div>

      <div class="table-wrap" v-if="products.length">
        <table class="data-table">
          <thead>
            <tr>
              <th>商品信息</th>
              <th>价格</th>
              <th>库存</th>
              <th>状态</th>
              <th>销量</th>
              <th>创建时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="p in products" :key="p.id">
              <td>
                <div class="product-cell">
                  <img :src="p.mainImage" :alt="p.name" />
                  <span>{{ p.name }}</span>
                </div>
              </td>
              <td class="price">{{ formatMoney(p.price) }}</td>
              <td>{{ p.stock }}</td>
              <td>
                <span class="tag" :class="p.status === 1 ? 'tag-success' : 'tag-info'">
                  {{ p.status === 1 ? '上架中' : '已下架' }}
                </span>
              </td>
              <td>{{ p.salesCount || 0 }}</td>
              <td>{{ formatDateTime(p.createTime) }}</td>
              <td>
                <div class="cell-actions">
                  <button class="btn btn-plain btn-sm" @click="openEdit(p)">编辑</button>
                  <button v-if="p.status === 1" class="btn btn-outline btn-sm" @click="offShelf(p)">下架</button>
                  <button v-else class="btn btn-primary btn-sm" @click="reShelf(p)">重新上架</button>
                </div>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState v-else text="暂无商品" />
      <Pagination
        :pageNum="pagination.pageNum"
        :pageSize="pagination.pageSize"
        :total="pagination.total"
        :pages="pagination.pages"
        @change="onPageChange"
      />
    </div>

    <!-- 商品表单弹窗 -->
    <div class="dialog-mask" v-if="formVisible" @click.self="formVisible = false">
      <div class="form-dialog">
        <div class="dialog-title">{{ editingId ? '编辑商品' : '上架新商品' }}</div>
        <div class="form-body">
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">商品名称 *</label>
              <input v-model="form.name" class="form-control" placeholder="如：赣南脐橙" />
            </div>
            <div class="form-group">
              <label class="form-label">分类 ID *</label>
              <input v-model.number="form.categoryId" type="number" class="form-control" placeholder="分类ID" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">售价 *</label>
              <input v-model.number="form.price" type="number" step="0.01" class="form-control" />
            </div>
            <div class="form-group">
              <label class="form-label">原价</label>
              <input v-model.number="form.originalPrice" type="number" step="0.01" class="form-control" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">单位</label>
              <input v-model="form.unit" class="form-control" placeholder="斤" />
            </div>
            <div class="form-group">
              <label class="form-label">规格</label>
              <input v-model="form.spec" class="form-control" placeholder="5斤装" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">产地</label>
            <input v-model="form.origin" class="form-control" placeholder="江西赣州" />
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">保质期(天) *</label>
              <input v-model.number="form.shelfLifeDays" type="number" class="form-control" />
            </div>
            <div class="form-group">
              <label class="form-label">生产日期 *</label>
              <input v-model="form.productionDate" type="date" class="form-control" />
            </div>
          </div>
          <div class="form-row">
            <div class="form-group">
              <label class="form-label">库存 *</label>
              <input v-model.number="form.stock" type="number" class="form-control" />
            </div>
            <div class="form-group">
              <label class="form-label">安全库存</label>
              <input v-model.number="form.safetyStock" type="number" class="form-control" placeholder="10" />
            </div>
          </div>
          <div class="form-group">
            <label class="form-label">主图 URL *</label>
            <input v-model="form.imageUrl" class="form-control" placeholder="/img/orange.jpg" />
          </div>
          <div class="form-group">
            <label class="form-label">商品描述</label>
            <textarea v-model="form.description" class="form-control" placeholder="产地直发，新鲜采摘"></textarea>
          </div>
          <div class="form-error" v-if="formError">{{ formError }}</div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-plain" @click="formVisible = false">取消</button>
          <button class="btn btn-primary" :disabled="saving" @click="saveProduct">{{ saving ? '保存中...' : '保存' }}</button>
        </div>
      </div>
    </div>

    <ConfirmDialog
      :visible="confirmVisible"
      title="下架商品"
      message="确定下架该商品吗？下架后用户将无法购买。"
      danger
      @confirm="doOffShelf"
      @cancel="confirmVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMerchantProducts, createProduct, updateProduct, offShelfProduct } from '../../api/merchant'
import { formatMoney, formatDateTime, today } from '../../utils/format'
import { notifySuccess, notifyError } from '../../utils/request'
import EmptyState from '../../components/EmptyState.vue'
import Pagination from '../../components/Pagination.vue'
import ConfirmDialog from '../../components/ConfirmDialog.vue'

const filters = reactive({ status: '', keyword: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0, pages: 1 })
const products = ref([])

const formVisible = ref(false)
const editingId = ref(null)
const saving = ref(false)
const formError = ref('')
const emptyForm = () => ({
  name: '', categoryId: 11, price: null, originalPrice: null, unit: '斤', spec: '',
  origin: '', shelfLifeDays: 7, productionDate: today(), stock: 0, safetyStock: 10,
  imageUrl: '', description: ''
})
const form = reactive(emptyForm())

const confirmVisible = ref(false)
let pendingOffId = null

async function loadProducts() {
  const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
  if (filters.status !== '') params.status = Number(filters.status)
  if (filters.keyword) params.keyword = filters.keyword
  try {
    const res = await getMerchantProducts(params)
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

function openCreate() {
  editingId.value = null
  Object.assign(form, emptyForm())
  formError.value = ''
  formVisible.value = true
}
function openEdit(p) {
  editingId.value = p.id
  Object.assign(form, {
    name: p.name, categoryId: p.categoryId, price: p.price, originalPrice: p.originalPrice,
    unit: p.unit || '斤', spec: p.spec || '', origin: p.origin || '',
    shelfLifeDays: p.shelfLifeDays || 7, productionDate: p.productionDate || today(),
    stock: p.stock, safetyStock: p.safetyStock || 10, imageUrl: p.mainImage || '',
    description: p.description || ''
  })
  formError.value = ''
  formVisible.value = true
}

async function saveProduct() {
  formError.value = ''
  if (!form.name || !form.price || !form.shelfLifeDays || !form.imageUrl) {
    formError.value = '请填写商品名称、售价、保质期和主图URL'
    return
  }
  saving.value = true
  try {
    const payload = {
      name: form.name, categoryId: Number(form.categoryId), price: Number(form.price),
      originalPrice: form.originalPrice ? Number(form.originalPrice) : undefined,
      unit: form.unit, spec: form.spec, origin: form.origin,
      shelfLifeDays: Number(form.shelfLifeDays), productionDate: form.productionDate,
      stock: Number(form.stock), safetyStock: Number(form.safetyStock),
      images: [form.imageUrl], description: form.description
    }
    if (editingId.value) {
      await updateProduct(editingId.value, payload)
      notifySuccess('商品已更新')
    } else {
      await createProduct(payload)
      notifySuccess('商品上架成功')
    }
    formVisible.value = false
    loadProducts()
  } catch (e) {
    formError.value = e.message || '保存失败'
  } finally {
    saving.value = false
  }
}

function offShelf(p) {
  pendingOffId = p.id
  confirmVisible.value = true
}
async function doOffShelf() {
  confirmVisible.value = false
  if (!pendingOffId) return
  try {
    await offShelfProduct(pendingOffId)
    notifySuccess('商品已下架')
    loadProducts()
  } catch (e) {}
  pendingOffId = null
}
async function reShelf(p) {
  try {
    await updateProduct(p.id, { status: 1 })
    notifySuccess('商品已重新上架')
    loadProducts()
  } catch (e) {
    notifyError('操作失败')
  }
}

onMounted(loadProducts)
</script>

<style scoped>
.product-cell {
  display: flex;
  align-items: center;
  gap: 10px;
}
.product-cell img {
  width: 44px;
  height: 44px;
  border-radius: 6px;
  background: #f0f7f1;
  object-fit: cover;
}
.form-dialog {
  width: 100%;
  max-width: 640px;
  max-height: 90vh;
  background: #fff;
  border-radius: 12px;
  display: flex;
  flex-direction: column;
}
.dialog-title {
  padding: 20px 24px;
  font-size: 17px;
  font-weight: 700;
  border-bottom: 1px solid var(--border);
}
.form-body {
  padding: 20px 24px;
  overflow-y: auto;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border);
}
</style>
