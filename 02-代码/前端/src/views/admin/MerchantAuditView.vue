<template>
  <div class="page-container">
    <h2 class="page-title">商家审核</h2>

    <div class="card">
      <div class="filter-bar">
        <select v-model="filters.auditStatus" class="form-control" @change="reload">
          <option value="">全部状态</option>
          <option value="0">待审核</option>
          <option value="1">审核通过</option>
          <option value="2">审核驳回</option>
        </select>
        <input v-model="filters.keyword" class="form-control" placeholder="搜索商家名称" @keyup.enter="reload" />
        <button class="btn btn-primary" @click="reload">查询</button>
      </div>

      <div class="table-wrap" v-if="list.length">
        <table class="data-table">
          <thead>
            <tr>
              <th>商家名称</th>
              <th>登录账号</th>
              <th>联系人</th>
              <th>联系电话</th>
              <th>营业执照号</th>
              <th>审核状态</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="m in list" :key="m.id">
              <td>{{ m.merchantName }}</td>
              <td>{{ m.userName || m.username }}</td>
              <td>{{ m.contactPerson || '-' }}</td>
              <td>{{ m.contactPhone || '-' }}</td>
              <td>{{ m.licenseNo || '-' }}</td>
              <td><span class="tag" :class="'tag-' + auditType(m.auditStatus)">{{ auditText(m.auditStatus) }}</span></td>
              <td>
                <button v-if="m.auditStatus === 0" class="btn btn-primary btn-sm" @click="openAudit(m)">审核</button>
                <button v-else class="btn btn-plain btn-sm" @click="viewDetail(m)">详情</button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState v-else text="暂无商家数据" />
      <Pagination
        :pageNum="pagination.pageNum" :pageSize="pagination.pageSize"
        :total="pagination.total" :pages="pagination.pages"
        @change="onPageChange"
      />
    </div>

    <!-- 审核弹窗 -->
    <div class="dialog-mask" v-if="auditVisible" @click.self="auditVisible = false">
      <div class="small-dialog">
        <div class="dialog-title">审核商家资质</div>
        <div class="form-body">
          <div class="info-block">
            <div><span>商家名称</span>{{ current.merchantName }}</div>
            <div><span>联系人</span>{{ current.contactPerson || '-' }}</div>
            <div><span>联系电话</span>{{ current.contactPhone || '-' }}</div>
            <div><span>营业执照号</span>{{ current.licenseNo || '-' }}</div>
            <div v-if="current.licenseImage"><span>执照图片</span><img :src="current.licenseImage" class="license-img" /></div>
          </div>
          <div class="form-group">
            <label class="form-label">审核意见</label>
            <textarea v-model="auditForm.remark" class="form-control" placeholder="填写审核意见"></textarea>
          </div>
          <div class="form-error" v-if="auditError">{{ auditError }}</div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-danger" :disabled="auditing" @click="submitAudit('REJECT')">驳回</button>
          <button class="btn btn-primary" :disabled="auditing" @click="submitAudit('APPROVE')">通过</button>
        </div>
      </div>
    </div>

    <!-- 详情弹窗 -->
    <div class="dialog-mask" v-if="detailVisible" @click.self="detailVisible = false">
      <div class="small-dialog">
        <div class="dialog-title">商家详情</div>
        <div class="form-body">
          <div class="info-block">
            <div><span>商家名称</span>{{ current.merchantName }}</div>
            <div><span>登录账号</span>{{ current.userName || current.username }}</div>
            <div><span>联系人</span>{{ current.contactPerson || '-' }}</div>
            <div><span>联系电话</span>{{ current.contactPhone || '-' }}</div>
            <div><span>营业执照号</span>{{ current.licenseNo || '-' }}</div>
            <div><span>审核状态</span><span class="tag" :class="'tag-' + auditType(current.auditStatus)">{{ auditText(current.auditStatus) }}</span></div>
            <div><span>营业状态</span>{{ current.status === 1 ? '营业中' : '已停业' }}</div>
            <div><span>审核意见</span>{{ current.auditRemark || '-' }}</div>
          </div>
        </div>
        <div class="dialog-footer">
          <button class="btn btn-plain" @click="detailVisible = false">关闭</button>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getMerchants, auditMerchant } from '../../api/admin'
import { MERCHANT_AUDIT_STATUS, MERCHANT_AUDIT_TYPE, textOf, typeOf } from '../../utils/enums'
import { notifySuccess } from '../../utils/request'
import EmptyState from '../../components/EmptyState.vue'
import Pagination from '../../components/Pagination.vue'

const filters = reactive({ auditStatus: '', keyword: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0, pages: 1 })
const list = ref([])

const auditVisible = ref(false)
const detailVisible = ref(false)
const auditing = ref(false)
const auditError = ref('')
const current = ref({})
const auditForm = reactive({ remark: '' })

const auditText = (s) => textOf(MERCHANT_AUDIT_STATUS, s)
const auditType = (s) => typeOf(MERCHANT_AUDIT_TYPE, s, 'info')

async function loadList() {
  const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
  if (filters.auditStatus !== '') params.auditStatus = Number(filters.auditStatus)
  if (filters.keyword) params.keyword = filters.keyword
  try {
    const res = await getMerchants(params)
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
function openAudit(m) {
  current.value = m
  auditForm.remark = ''
  auditError.value = ''
  auditVisible.value = true
}
function viewDetail(m) {
  current.value = m
  detailVisible.value = true
}
async function submitAudit(action) {
  auditing.value = true
  try {
    await auditMerchant(current.value.id, { action, remark: auditForm.remark })
    notifySuccess(action === 'APPROVE' ? '已审核通过' : '已驳回申请')
    auditVisible.value = false
    loadList()
  } catch (e) {
    auditError.value = e.message || '操作失败'
  } finally {
    auditing.value = false
  }
}

onMounted(loadList)
</script>

<style scoped>
.table-wrap table img {
  max-width: 60px;
}
.small-dialog {
  width: 100%;
  max-width: 500px;
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
  padding: 14px 16px;
  margin-bottom: 16px;
}
.info-block > div {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 5px 0;
}
.info-block span:first-child {
  color: var(--text-light);
  min-width: 90px;
  flex-shrink: 0;
}
.license-img {
  max-width: 120px;
  max-height: 80px;
  object-fit: contain;
  border: 1px solid var(--border);
  border-radius: 4px;
}
.dialog-footer {
  display: flex;
  justify-content: flex-end;
  gap: 10px;
  padding: 16px 24px;
  border-top: 1px solid var(--border);
}
</style>
