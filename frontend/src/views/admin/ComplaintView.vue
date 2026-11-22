<template>
  <div class="page-container">
    <h2 class="page-title">投诉处理</h2>

    <div class="card">
      <div class="filter-bar">
        <select v-model="filters.status" class="form-control" @change="reload">
          <option value="">全部状态</option>
          <option value="0">待处理</option>
          <option value="1">处理中</option>
          <option value="2">已处理</option>
          <option value="3">已关闭</option>
        </select>
        <select v-model="filters.type" class="form-control" @change="reload">
          <option value="">全部类型</option>
          <option value="1">商品质量</option>
          <option value="2">服务态度</option>
          <option value="3">物流问题</option>
          <option value="4">其他</option>
        </select>
        <button class="btn btn-primary" @click="reload">查询</button>
      </div>

      <div class="complaint-list" v-if="list.length">
        <div v-for="c in list" :key="c.id" class="complaint-card">
          <div class="c-head">
            <div>
              <strong>投诉单号：{{ c.complaintNo }}</strong>
              <span class="c-type">{{ typeText(c.type) }}</span>
            </div>
            <span class="tag" :class="'tag-' + statusType(c.status)">{{ statusText(c.status) }}</span>
          </div>
          <div class="c-body">
            <div class="c-meta">
              <span>投诉人：{{ c.userName || '-' }}</span>
              <span>被诉商家：{{ c.merchantName || '-' }}</span>
              <span>{{ formatDateTime(c.createTime) }}</span>
            </div>
            <div class="c-content">{{ c.content }}</div>
            <div class="c-remark" v-if="c.handleRemark">处理说明：{{ c.handleRemark }}</div>
          </div>
          <div class="c-actions" v-if="c.status !== 2 && c.status !== 3">
            <button class="btn btn-outline btn-sm" @click="openHandle(c, 'PROCESS')">标记处理中</button>
            <button class="btn btn-primary btn-sm" @click="openHandle(c, 'CLOSE')">关闭投诉</button>
          </div>
        </div>
      </div>
      <EmptyState v-else text="暂无投诉记录" />
      <Pagination
        :pageNum="pagination.pageNum" :pageSize="pagination.pageSize"
        :total="pagination.total" :pages="pagination.pages"
        @change="onPageChange"
      />
    </div>

    <div class="dialog-mask" v-if="handleVisible" @click.self="handleVisible = false">
      <div class="small-dialog">
        <div class="dialog-title">{{ handleAction === 'CLOSE' ? '关闭投诉' : '处理投诉' }}</div>
        <div class="form-body">
          <div class="form-group">
            <label class="form-label">处理说明 *</label>
            <textarea v-model="handleForm.remark" class="form-control" placeholder="填写处理说明"></textarea>
          </div>
          <div class="form-error" v-if="handleError">{{ handleError }}</div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-plain" @click="handleVisible = false">取消</button>
          <button class="btn btn-primary" :disabled="handling" @click="submitHandle">确定</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getComplaints, handleComplaint } from '../../api/admin'
import { COMPLAINT_TYPE, COMPLAINT_STATUS, COMPLAINT_STATUS_TYPE, textOf, typeOf } from '../../utils/enums'
import { formatDateTime } from '../../utils/format'
import { notifySuccess } from '../../utils/request'
import EmptyState from '../../components/EmptyState.vue'
import Pagination from '../../components/Pagination.vue'

const filters = reactive({ status: '', type: '' })
const pagination = reactive({ pageNum: 1, pageSize: 8, total: 0, pages: 1 })
const list = ref([])

const handleVisible = ref(false)
const handling = ref(false)
const handleError = ref('')
const handleAction = ref('PROCESS')
const current = ref({})
const handleForm = reactive({ remark: '' })

const typeText = (t) => textOf(COMPLAINT_TYPE, t, '-')
const statusText = (s) => textOf(COMPLAINT_STATUS, s)
const statusType = (s) => typeOf(COMPLAINT_STATUS_TYPE, s, 'info')

async function loadList() {
  const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
  if (filters.status !== '') params.status = Number(filters.status)
  if (filters.type !== '') params.type = Number(filters.type)
  try {
    const res = await getComplaints(params)
    list.value = res.list || []
    pagination.total = res.total || 0
    pagination.pages = res.pages || 1
  } catch (e) {
    list.value = []
  }
}
function reload() {
  pagination.pageNum = 1
  loadList()
}
function onPageChange(p) {
  pagination.pageNum = p
  loadList()
}
function openHandle(c, action) {
  current.value = c
  handleAction.value = action
  handleForm.remark = ''
  handleError.value = ''
  handleVisible.value = true
}
async function submitHandle() {
  if (!handleForm.remark) {
    handleError.value = '请填写处理说明'
    return
  }
  handling.value = true
  try {
    await handleComplaint(current.value.id, { action: handleAction.value, handleRemark: handleForm.remark })
    notifySuccess(handleAction.value === 'CLOSE' ? '投诉已关闭' : '已标记为处理中')
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
.complaint-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.complaint-card {
  border: 1px solid var(--border);
  border-radius: 10px;
  overflow: hidden;
}
.c-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  background: #f9fafb;
  gap: 10px;
  flex-wrap: wrap;
}
.c-type {
  margin-left: 16px;
  font-size: 12px;
  color: var(--text-light);
}
.c-body {
  padding: 14px 16px;
}
.c-meta {
  display: flex;
  gap: 20px;
  font-size: 12px;
  color: var(--text-light);
  margin-bottom: 10px;
  flex-wrap: wrap;
}
.c-content {
  font-size: 14px;
  line-height: 1.7;
}
.c-remark {
  margin-top: 8px;
  font-size: 13px;
  color: var(--primary);
}
.c-actions {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 12px 16px;
  border-top: 1px solid var(--border);
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
