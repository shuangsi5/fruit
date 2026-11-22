<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title" style="margin: 0">我的售后</h2>
      <router-link to="/orders" class="btn btn-outline">返回订单</router-link>
    </div>

    <div class="card">
      <div class="tabs">
        <button
          v-for="t in tabs"
          :key="t.value"
          class="tab"
          :class="{ active: filters.status === t.value }"
          @click="changeTab(t.value)"
        >
          {{ t.label }}
        </button>
      </div>

      <div class="aftersale-list" v-if="list.length">
        <div v-for="a in list" :key="a.id" class="as-card">
          <div class="as-head">
            <div>
              <strong>售后单号：{{ a.afterSaleNo }}</strong>
              <span class="as-order">关联订单：{{ a.orderNo || a.orderId }}</span>
            </div>
            <span class="tag" :class="'tag-' + statusType(a.status)">{{ statusText(a.status) }}</span>
          </div>
          <div class="as-body">
            <div class="as-row"><span class="as-label">类型</span>{{ typeText(a.type) }}</div>
            <div class="as-row"><span class="as-label">原因</span>{{ a.reason }}</div>
            <div class="as-row"><span class="as-label">退款金额</span><span class="price">{{ formatMoney(a.amount) }}</span></div>
            <div class="as-row"><span class="as-label">申请时间</span>{{ formatDateTime(a.createTime) }}</div>
            <div class="as-row" v-if="a.handleRemark"><span class="as-label">处理说明</span>{{ a.handleRemark }}</div>
          </div>
        </div>
      </div>
      <EmptyState v-else text="暂无售后记录" />

      <Pagination
        :pageNum="pagination.pageNum"
        :pageSize="pagination.pageSize"
        :total="pagination.total"
        :pages="pagination.pages"
        @change="onPageChange"
      />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getAfterSales } from '../../api/user'
import { formatMoney, formatDateTime } from '../../utils/format'
import { AFTERSALE_STATUS, AFTERSALE_STATUS_TYPE, AFTERSALE_TYPE, textOf, typeOf } from '../../utils/enums'
import EmptyState from '../../components/EmptyState.vue'
import Pagination from '../../components/Pagination.vue'

const tabs = [
  { label: '全部', value: '' },
  { label: '待审核', value: 0 },
  { label: '审核通过', value: 1 },
  { label: '处理中', value: 2 },
  { label: '已完成', value: 3 },
  { label: '已驳回', value: 4 }
]
const filters = reactive({ status: '' })
const pagination = reactive({ pageNum: 1, pageSize: 8, total: 0, pages: 1 })
const list = ref([])

const statusText = (s) => textOf(AFTERSALE_STATUS, s)
const statusType = (s) => typeOf(AFTERSALE_STATUS_TYPE, s, 'info')
const typeText = (t) => textOf(AFTERSALE_TYPE, t, '-')

async function loadList() {
  const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
  if (filters.status !== '') params.status = filters.status
  try {
    const res = await getAfterSales(params)
    list.value = res.list || []
    pagination.total = res.total || 0
    pagination.pages = res.pages || 1
  } catch (e) {
    list.value = []
  }
}
function changeTab(v) {
  filters.status = v
  pagination.pageNum = 1
  loadList()
}
function onPageChange(p) {
  pagination.pageNum = p
  loadList()
}

onMounted(loadList)
</script>

<style scoped>
.tabs {
  display: flex;
  gap: 4px;
  border-bottom: 1px solid var(--border);
  margin-bottom: 16px;
  overflow-x: auto;
}
.tab {
  padding: 10px 16px;
  border: none;
  background: none;
  color: var(--text-secondary);
  font-weight: 500;
  white-space: nowrap;
  border-bottom: 2px solid transparent;
}
.tab.active {
  color: var(--primary);
  border-bottom-color: var(--primary);
}
.aftersale-list {
  display: flex;
  flex-direction: column;
  gap: 14px;
}
.as-card {
  border: 1px solid var(--border);
  border-radius: 10px;
  overflow: hidden;
}
.as-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f9fafb;
  gap: 10px;
  flex-wrap: wrap;
}
.as-order {
  margin-left: 16px;
  font-size: 12px;
  color: var(--text-light);
}
.as-body {
  padding: 14px 16px;
}
.as-row {
  padding: 5px 0;
  font-size: 14px;
}
.as-label {
  display: inline-block;
  width: 90px;
  color: var(--text-light);
}
</style>
