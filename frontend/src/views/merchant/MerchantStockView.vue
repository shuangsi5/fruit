<template>
  <div class="page-container">
    <h2 class="page-title">库存与预警</h2>

    <div class="tabs">
      <button class="tab" :class="{ active: tab === 'inventory' }" @click="tab = 'inventory'">库存列表</button>
      <button class="tab" :class="{ active: tab === 'warning' }" @click="tab = 'warning'">预警商品</button>
      <button class="tab" :class="{ active: tab === 'expiring' }" @click="tab = 'expiring'">临期商品</button>
    </div>

    <!-- 库存列表 -->
    <div class="card" v-if="tab === 'inventory'">
      <div class="filter-bar">
        <select v-model="invFilters.warningLevel" class="form-control" @change="loadInventory">
          <option value="">全部等级</option>
          <option value="0">正常</option>
          <option value="1">注意</option>
          <option value="2">警告</option>
          <option value="3">严重</option>
        </select>
        <input v-model="invFilters.keyword" class="form-control" placeholder="搜索商品名称" @keyup.enter="loadInventory" />
        <button class="btn btn-primary" @click="loadInventory">查询</button>
      </div>
      <div class="table-wrap" v-if="inventory.length">
        <table class="data-table">
          <thead>
            <tr>
              <th>商品名称</th>
              <th>当前库存</th>
              <th>锁定库存</th>
              <th>安全库存</th>
              <th>预警等级</th>
              <th>剩余天数</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="it in inventory" :key="it.productId">
              <td>{{ it.productName }}</td>
              <td :class="{ 'text-danger': it.stock < it.safetyStock }">{{ it.stock }}</td>
              <td>{{ it.lockedStock || 0 }}</td>
              <td>{{ it.safetyStock }}</td>
              <td><WarningTag :level="it.warningLevel" /></td>
              <td>{{ it.remainingDays !== undefined ? it.remainingDays + ' 天' : '-' }}</td>
              <td>
                <button class="btn btn-outline btn-sm" @click="openUpdate(it)">调整库存</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState v-else text="暂无库存数据" />
      <Pagination
        :pageNum="invPagination.pageNum" :pageSize="invPagination.pageSize"
        :total="invPagination.total" :pages="invPagination.pages"
        @change="(p) => { invPagination.pageNum = p; loadInventory() }"
      />
    </div>

    <!-- 预警商品 -->
    <div class="card" v-if="tab === 'warning'">
      <div class="filter-bar">
        <select v-model="warningFilters.level" class="form-control" @change="loadWarnings">
          <option value="1">注意及以上</option>
          <option value="2">警告及以上</option>
          <option value="3">严重</option>
        </select>
        <button class="btn btn-primary" @click="loadWarnings">查询</button>
      </div>
      <div class="table-wrap" v-if="warnings.length">
        <table class="data-table">
          <thead>
            <tr><th>商品名称</th><th>预警类型</th><th>预警等级</th><th>当前库存</th><th>说明</th><th>操作</th></tr>
          </thead>
          <tbody>
            <tr v-for="(w, idx) in warnings" :key="idx">
              <td>{{ w.productName }}</td>
              <td>{{ w.warningTypeText || w.warningType }}</td>
              <td><WarningTag :level="w.warningLevel" /></td>
              <td>{{ w.stock }}</td>
              <td>{{ w.message || w.remark || '-' }}</td>
              <td><button class="btn btn-outline btn-sm" @click="openUpdate({ productId: w.productId, productName: w.productName })">补货</button></td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState v-else text="暂无预警商品，库存状态良好" />
    </div>

    <!-- 临期商品 -->
    <div class="card" v-if="tab === 'expiring'">
      <div class="filter-bar">
        <select v-model="expiringDays" class="form-control" @change="loadExpiring">
          <option value="7">未来 7 天内到期</option>
          <option value="15">未来 15 天内到期</option>
          <option value="30">未来 30 天内到期</option>
        </select>
        <button class="btn btn-primary" @click="loadExpiring">查询</button>
      </div>
      <div class="table-wrap" v-if="expiring.length">
        <table class="data-table">
          <thead>
            <tr><th>商品名称</th><th>生产日期</th><th>保质期</th><th>到期日</th><th>剩余天数</th><th>库存</th><th>预警</th></tr>
          </thead>
          <tbody>
            <tr v-for="(e, idx) in expiring" :key="idx">
              <td>{{ e.productName }}</td>
              <td>{{ formatDate(e.productionDate) }}</td>
              <td>{{ e.shelfLifeDays }} 天</td>
              <td>{{ formatDate(e.expireDate) }}</td>
              <td class="text-danger">{{ e.remainingDays }} 天</td>
              <td>{{ e.stock }}</td>
              <td><WarningTag :level="e.warningLevel" /></td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState v-else text="暂无临期商品" />
    </div>

    <!-- 调整库存弹窗 -->
    <div class="dialog-mask" v-if="updateVisible" @click.self="updateVisible = false">
      <div class="small-dialog">
        <div class="dialog-title">调整库存 - {{ currentItem.productName }}</div>
        <div class="form-body">
          <div class="form-group">
            <label class="form-label">变动量 *（正数为入库，负数为出库）</label>
            <input v-model.number="updateForm.delta" type="number" class="form-control" placeholder="如：100 或 -20" />
          </div>
          <div class="form-group">
            <label class="form-label">变动类型 *</label>
            <select v-model.number="updateForm.changeType" class="form-control">
              <option value="1">采购入库</option>
              <option value="4">盘点调整</option>
              <option value="3">损耗出库</option>
            </select>
          </div>
          <div class="form-group">
            <label class="form-label">安全库存</label>
            <input v-model.number="updateForm.safetyStock" type="number" class="form-control" placeholder="留空则不修改" />
          </div>
          <div class="form-group">
            <label class="form-label">备注</label>
            <input v-model="updateForm.remark" class="form-control" placeholder="如：补货" />
          </div>
          <div class="form-error" v-if="updateError">{{ updateError }}</div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-plain" @click="updateVisible = false">取消</button>
          <button class="btn btn-primary" :disabled="updating" @click="submitUpdate">{{ updating ? '保存中...' : '确定' }}</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getInventory, updateInventory, getInventoryWarnings, getExpiringProducts } from '../../api/merchant'
import { formatDate } from '../../utils/format'
import { notifySuccess } from '../../utils/request'
import WarningTag from '../../components/WarningTag.vue'
import EmptyState from '../../components/EmptyState.vue'
import Pagination from '../../components/Pagination.vue'

const tab = ref('inventory')

const invFilters = reactive({ warningLevel: '', keyword: '' })
const invPagination = reactive({ pageNum: 1, pageSize: 10, total: 0, pages: 1 })
const inventory = ref([])

const warningFilters = reactive({ level: 1 })
const warnings = ref([])
const expiringDays = ref(7)
const expiring = ref([])

const updateVisible = ref(false)
const updating = ref(false)
const updateError = ref('')
const currentItem = ref({})
const updateForm = reactive({ delta: null, changeType: 1, safetyStock: null, remark: '' })

async function loadInventory() {
  const params = { pageNum: invPagination.pageNum, pageSize: invPagination.pageSize }
  if (invFilters.warningLevel !== '') params.warningLevel = Number(invFilters.warningLevel)
  if (invFilters.keyword) params.keyword = invFilters.keyword
  try {
    const res = await getInventory(params)
    inventory.value = res.list || []
    invPagination.total = res.total || 0
    invPagination.pages = res.pages || 1
  } catch (e) {
    inventory.value = []
  }
}
async function loadWarnings() {
  try {
    const res = await getInventoryWarnings({ level: Number(warningFilters.level), pageNum: 1, pageSize: 50 })
    warnings.value = res.list || []
  } catch (e) {
    warnings.value = []
  }
}
async function loadExpiring() {
  try {
    const res = await getExpiringProducts({ days: Number(expiringDays.value), pageNum: 1, pageSize: 50 })
    expiring.value = res.list || []
  } catch (e) {
    expiring.value = []
  }
}

function openUpdate(item) {
  currentItem.value = item
  updateForm.delta = null
  updateForm.changeType = 1
  updateForm.safetyStock = null
  updateForm.remark = ''
  updateError.value = ''
  updateVisible.value = true
}
async function submitUpdate() {
  if (!updateForm.delta || updateForm.delta === 0) {
    updateError.value = '请输入变动量（非0）'
    return
  }
  updating.value = true
  try {
    const payload = { delta: Number(updateForm.delta), changeType: Number(updateForm.changeType), remark: updateForm.remark }
    if (updateForm.safetyStock !== null && updateForm.safetyStock !== '') payload.safetyStock = Number(updateForm.safetyStock)
    const res = await updateInventory(currentItem.value.productId, payload)
    updateVisible.value = false
    notifySuccess('库存已更新，当前库存：' + res.afterStock)
    loadInventory()
    loadWarnings()
    loadExpiring()
  } catch (e) {
    updateError.value = e.message || '更新失败'
  } finally {
    updating.value = false
  }
}

onMounted(() => {
  loadInventory()
  loadWarnings()
  loadExpiring()
})
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 18px;
}
.tab {
  padding: 10px 20px;
  border: none;
  background: none;
  color: var(--text-secondary);
  font-weight: 500;
  border-bottom: 2px solid transparent;
}
.tab.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
}
.small-dialog {
  width: 100%;
  max-width: 460px;
  background: #fff;
  border-radius: 12px;
}
.dialog-title {
  padding: 20px 24px;
  font-size: 17px;
  font-weight: 700;
  border-bottom: 1px solid var(--border);
}
.form-body {
  padding: 20px 24px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border);
}
</style>
