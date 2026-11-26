<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title" style="margin: 0">平台工作台</h2>
        <p class="welcome">欢迎回来，{{ user.username || '管理员' }}。以下是平台运营概况。</p>
      </div>
      <div class="header-actions">
        <router-link to="/admin/merchants" class="btn btn-primary">审核商家</router-link>
        <router-link to="/admin/overview" class="btn btn-outline">数据总览</router-link>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon blue">户</div>
        <div class="stat-info">
          <div class="stat-label">平台用户数</div>
          <div class="stat-value">{{ overview.users?.total || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon green">商</div>
        <div class="stat-info">
          <div class="stat-label">入驻商家</div>
          <div class="stat-value">{{ overview.merchants?.total || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">审</div>
        <div class="stat-info">
          <div class="stat-label">待审商家</div>
          <div class="stat-value">{{ overview.merchants?.pendingAudit || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon red">诉</div>
        <div class="stat-info">
          <div class="stat-label">待处理投诉</div>
          <div class="stat-value">{{ overview.complaints?.pending || 0 }}</div>
        </div>
      </div>
    </div>

    <div class="dashboard-grid">
      <div class="card">
        <div class="card-head">
          <h3>平台销售趋势</h3>
          <router-link to="/admin/overview" class="more">详细总览 ></router-link>
        </div>
        <div ref="trendChart" class="chart"></div>
      </div>
      <div class="card">
        <div class="card-head"><h3>快捷处理</h3></div>
        <div class="quick-list">
          <router-link to="/admin/merchants" class="quick-item">
            <span class="q-icon">审</span>
            <div><div class="q-title">商家资质审核</div><div class="q-desc">{{ overview.merchants?.pendingAudit || 0 }} 家待审核</div></div>
          </router-link>
          <router-link to="/admin/complaints" class="quick-item">
            <span class="q-icon red">诉</span>
            <div><div class="q-title">投诉处理</div><div class="q-desc">{{ overview.complaints?.pending || 0 }} 条待处理</div></div>
          </router-link>
          <router-link to="/admin/users" class="quick-item">
            <span class="q-icon blue">户</span>
            <div><div class="q-title">用户账号管理</div><div class="q-desc">查看与管理系统用户</div></div>
          </router-link>
        </div>
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverview } from '../../api/admin'
import { useUserStore } from '../../store'
import { formatMoney } from '../../utils/format'

const user = useUserStore()
const trendChart = ref(null)
const overview = ref({ users: {}, merchants: {}, orders: {}, afterSales: {}, complaints: {}, trend: [] })

async function loadData() {
  try {
    overview.value = await getOverview({ days: 30 })
  } catch (e) {}
  await nextTick()
  renderChart()
}
function renderChart() {
  if (!trendChart.value) return
  const chart = echarts.init(trendChart.value)
  const trend = overview.value.trend || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 60, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: trend.map((t) => t.date) },
    yAxis: [
      { type: 'value', name: '订单' },
      { type: 'value', name: '销售额' }
    ],
    series: [
      { name: '订单数', type: 'bar', data: trend.map((t) => t.orders), itemStyle: { color: '#3b82f6' } },
      { name: '销售额', type: 'line', yAxisIndex: 1, smooth: true, data: trend.map((t) => t.sales), lineStyle: { color: '#f5a623' }, itemStyle: { color: '#f5a623' } }
    ]
  })
}

onMounted(loadData)
</script>

<style scoped>
.welcome {
  color: var(--text-secondary);
  font-size: 13px;
  margin-top: 4px;
}
.header-actions {
  display: flex;
  gap: 10px;
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(220px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 22px;
  display: flex;
  align-items: center;
  gap: 16px;
}
.stat-icon {
  width: 52px;
  height: 52px;
  border-radius: 12px;
  color: #fff;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 20px;
  font-weight: 700;
}
.stat-icon.blue { background: #3b82f6; }
.stat-icon.green { background: #2e9e5b; }
.stat-icon.orange { background: #f5a623; }
.stat-icon.red { background: #e0483a; }
.stat-label {
  color: var(--text-secondary);
  font-size: 13px;
}
.stat-value {
  font-size: 24px;
  font-weight: 700;
  margin-top: 4px;
}
.dashboard-grid {
  display: grid;
  grid-template-columns: 1.4fr 1fr;
  gap: 20px;
}
.card-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 16px;
}
.card-head h3 {
  font-size: 16px;
}
.more {
  color: var(--primary);
  font-size: 13px;
}
.chart {
  height: 300px;
  width: 100%;
}
.quick-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}
.quick-item {
  display: flex;
  align-items: center;
  gap: 14px;
  padding: 14px;
  border: 1px solid var(--border);
  border-radius: 10px;
}
.quick-item:hover {
  border-color: var(--primary);
}
.q-icon {
  width: 42px;
  height: 42px;
  border-radius: 10px;
  background: var(--primary-light);
  color: var(--primary);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 18px;
  font-weight: 700;
}
.q-icon.red { background: #fdecea; color: var(--danger); }
.q-icon.blue { background: #e8f0fe; color: #3b82f6; }
.q-title {
  font-weight: 600;
}
.q-desc {
  color: var(--text-light);
  font-size: 12px;
  margin-top: 2px;
}
@media (max-width: 900px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
