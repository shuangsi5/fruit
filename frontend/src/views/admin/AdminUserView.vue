<template>
  <div class="page-container">
    <h2 class="page-title">用户管理</h2>

    <div class="card">
      <div class="filter-bar">
        <select v-model="filters.role" class="form-control" @change="reload">
          <option value="">全部角色</option>
          <option value="USER">普通用户</option>
          <option value="MERCHANT">商家</option>
          <option value="ADMIN">管理员</option>
        </select>
        <select v-model="filters.status" class="form-control" @change="reload">
          <option value="">全部状态</option>
          <option value="1">启用</option>
          <option value="0">禁用</option>
        </select>
        <input v-model="filters.keyword" class="form-control" placeholder="用户名/手机号" @keyup.enter="reload" />
        <button class="btn btn-primary" @click="reload">查询</button>
      </div>

      <div class="table-wrap" v-if="list.length">
        <table class="data-table">
          <thead>
            <tr>
              <th>ID</th>
              <th>用户名</th>
              <th>真实姓名</th>
              <th>手机号</th>
              <th>角色</th>
              <th>状态</th>
              <th>注册时间</th>
              <th>操作</th>
            </tr>
          </thead>
          <tbody>
            <tr v-for="u in list" :key="u.id">
              <td>{{ u.id }}</td>
              <td>{{ u.username }}</td>
              <td>{{ u.realName || '-' }}</td>
              <td>{{ u.phone || '-' }}</td>
              <td><span class="tag" :class="roleType(u.role)">{{ roleText(u.role) }}</span></td>
              <td>
                <span class="tag" :class="u.status === 1 ? 'tag-success' : 'tag-danger'">
                  {{ u.status === 1 ? '启用' : '禁用' }}
                </span>
              </td>
              <td>{{ formatDateTime(u.createTime) }}</td>
              <td>
                <button
                  class="btn btn-sm"
                  :class="u.status === 1 ? 'btn-danger' : 'btn-primary'"
                  @click="toggleStatus(u)"
                >
                  {{ u.status === 1 ? '禁用' : '启用' }}
                </button>
              </td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState v-else text="暂无用户数据" />
      <Pagination
        :pageNum="pagination.pageNum" :pageSize="pagination.pageSize"
        :total="pagination.total" :pages="pagination.pages"
        @change="onPageChange"
      />
    </div>

    <ConfirmDialog
      :visible="confirmVisible"
      :title="confirmTitle"
      :message="confirmMessage"
      :danger="pendingStatus === 0"
      @confirm="doToggle"
      @cancel="confirmVisible = false"
    />
  </div>
</template>

<script setup>
import { ref, reactive, onMounted } from 'vue'
import { getUsers, updateUserStatus } from '../../api/admin'
import { ROLE, ROLE_TEXT } from '../../utils/enums'
import { formatDateTime } from '../../utils/format'
import { notifySuccess } from '../../utils/request'
import EmptyState from '../../components/EmptyState.vue'
import Pagination from '../../components/Pagination.vue'
import ConfirmDialog from '../../components/ConfirmDialog.vue'

const filters = reactive({ role: '', status: '', keyword: '' })
const pagination = reactive({ pageNum: 1, pageSize: 10, total: 0, pages: 1 })
const list = ref([])

const confirmVisible = ref(false)
const confirmTitle = ref('')
const confirmMessage = ref('')
const pendingUser = ref(null)
const pendingStatus = ref(null)

const roleText = (r) => ROLE_TEXT[r] || r || '-'
const roleType = (r) => {
  if (r === ROLE.ADMIN) return 'tag-danger'
  if (r === ROLE.MERCHANT) return 'tag-warning'
  return 'tag-primary'
}

async function loadList() {
  const params = { pageNum: pagination.pageNum, pageSize: pagination.pageSize }
  if (filters.role) params.role = filters.role
  if (filters.status !== '') params.status = Number(filters.status)
  if (filters.keyword) params.keyword = filters.keyword
  try {
    const res = await getUsers(params)
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
function toggleStatus(u) {
  pendingUser.value = u
  pendingStatus.value = u.status === 1 ? 0 : 1
  confirmTitle.value = pendingStatus.value === 0 ? '禁用账号' : '启用账号'
  confirmMessage.value = pendingStatus.value === 0
    ? '确定禁用该用户吗？禁用后该用户将无法登录。'
    : '确定启用该用户吗？'
  confirmVisible.value = true
}
async function doToggle() {
  confirmVisible.value = false
  if (!pendingUser.value) return
  try {
    await updateUserStatus(pendingUser.value.id, { status: pendingStatus.value })
    notifySuccess(pendingStatus.value === 0 ? '账号已禁用' : '账号已启用')
    loadList()
  } catch (e) {}
  pendingUser.value = null
}

onMounted(loadList)
</script>

<style scoped>
</style>
