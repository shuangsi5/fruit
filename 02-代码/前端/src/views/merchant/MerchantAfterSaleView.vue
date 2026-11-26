<template>
  <div class="page-container">
    <h2 class="page-title">售后处理</h2>

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

      <div class="table-wrap" v-if="list.length">
        <table class="data-table">
          <thead>
            <tr>
              <th>售后单号</th>
              <th>类型</th>
              <th>原因</th>
              <th>金额</th>
              <th>状态</th>
              <th>申请时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="a in list" :key="a.id">
              <td>{{ a.afterSaleNo }}</td>
              <td>{{ typeText(a.type) }}</td>
              <td class="reason-cell">{{ a.reason }}</td>
              <td class="price">{{ formatMoney(a.amount) }}</td>
              <td><span class="tag" :class="'tag-' + statusType(a.status)">{{ statusText(a.status) }}</span></td>
              <td>{{ formatDateTime(a.createTime) }}</td>
              <td>
                <button v-if="a.status === 0" class="btn btn-primary btn-sm" @click="openHandle(a)">审核</button>
                <span v-else class="text-muted">-</span>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState v-else text="暂无售后申请" />
      <Pagination
        :pageNum="pagination.pageNum" :pageSize="pagination.pageSize"
        :total="pagination.total" :pages="pagination.pages"
        @change="onPageChange"
      />
    </div>

    <!-- 审核弹窗 -->
    <div class="dialog-mask" v-if="handleVisible" @click.self="handleVisible = false">
      <div class="small-dialog">
        <div class="dialog-title">审核售后申请</div>
        <div class="form-body">
          <div class="info-block">
            <div><span>售后单号</span>{{ current.afterSaleNo }}</div>
            <div><span>类型</span>{{ typeText(current.type) }}</div>
            <div><span>金额</span><span class="price">{{ formatMoney(current.amount) }}</span></div>
            <div><span>原因</span>{{ current.reason }}</div>
          </div>
          <div class="form-group">
            <label class="form-label">处理说明</label>
            <textarea v-model="handleForm.remark" class="form-control" placeholder="填写审核意见"></textarea>
          </div>
          <div class="form-error" v-if="handleError">{{ handleError }}</div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-danger" :disabled="handling" @click="submitHandle('REJECT')">驳回</button>
          <button class="btn btn-primary" :disabled="handling" @click="submitHandle('APPROVE')">审核通过</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMerchantAfterSales, handleAfterSale } from '../../api/merchant'
import { formatMoney, formatDateTime } from '../../utils/format'
import { AFTERSALE_STATUS, AFTERSALE_STATUS_TYPE, AFTERSALE_TYPE, textOf, typeOf } from '../../utils/enums'
import { notifySuccess } from '../../utils/request'
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
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0, pages: 1 })
const list = ref([])

const handleVisible = ref(false)
const handling = ref(false)
const handleError = ref('')
const current = ref({})
const handleForm = reactive({ remark: '' })

const statusText = (s) => textOf(AFTERSALE_STATUS, s)
const statusType = (s) => typeOf(AFTERSALE_STATUS_TYPE, s, 'info')
const typeText = (t) => textOf(AFTERSALE_TYPE, t, '-')

async function loadList() {
  const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
  if (filters.status !== '') params.status = filters.status
  try {
    const res = await getMerchantAfterSales(params)
    list.value = res.list || []
    pagination.total = res.total || 0
    pagination.pages = res.pages || 1
  } catch (e) {
    list.value = []
  }
}
function changeTab(v) {
  filters.status = v
  reload()
}
function reload() {
  pagination.pageNum = 1
  loadList()
}
function onPageChange(p) {
  pagination.pageNum = p
  loadList()
}

function openHandle(a) {
  current.value = a
  handleForm.remark = ''
  handleError.value = ''
  handleVisible.value = true
}
async function submitHandle(action) {
  handling.value = true
  try {
    await handleAfterSale(current.value.id, { action, handleRemark: handleForm.remark })
    notifySuccess(action === 'APPROVE' ? '已审核通过' : '已驳回申请')
    handleVisible.value = false
    loadList()
  } catch (e) {
    handleError.value = e.message || '操作失败'
  } finally {
    handling.value = false
  }
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
.reason-cell {
  max-width: 200px;
  overflow: hidden;
  text-overflow: ellipsis;
}
.small-dialog {
  width: 100%;
  max-width: 480px;
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
.info-block {
  background: #f9fafb;
  border-radius: 8px;
  padding: 12px 16px;
  margin-bottom: 16px;
}
.info-block > div {
  display: flex;
  gap: 12px;
  padding: 4px 0;
}
.info-block span:first-child {
  color: var(--text-light);
  min-width: 70px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border);
}
</style>
