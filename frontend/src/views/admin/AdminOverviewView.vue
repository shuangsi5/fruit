<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title" style="margin: 0">数据总览</h2>
      <div class="filter-bar" style="margin: 0">
        <select v-model="days" class="form-control" @change="loadData">
          <option :value="7">近 7 天</option>
          <option :value="30">近 30 天</option>
          <option :value="90">近 90 天</option>
        </select>
        <button class="btn btn-primary" @click="loadData">刷新</button>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-label">平台用户总数</div>
        <div class="stat-value">{{ data.users?.total || 0 }}</div>
        <div class="stat-sub">新增 {{ data.users?.newUsers || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">入驻商家</div>
        <div class="stat-value">{{ data.merchants?.total || 0 }}</div>
        <div class="stat-sub">营业中 {{ data.merchants?.active || 0 }} / 待审 {{ data.merchants?.pendingAudit || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">累计订单</div>
        <div class="stat-value">{{ data.orders?.total || 0 }}</div>
        <div class="stat-sub">今日 {{ data.orders?.today || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">累计销售额</div>
        <div class="stat-value price">{{ formatMoney(data.orders?.totalSales || 0) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">售后单</div>
        <div class="stat-value">{{ data.afterSales?.total || 0 }}</div>
        <div class="stat-sub">待处理 {{ data.afterSales?.pending || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">投诉</div>
        <div class="stat-value">{{ data.complaints?.total || 0 }}</div>
        <div class="stat-sub">待处理 {{ data.complaints?.pending || 0 }}</div>
      </div>
    </div>

    <div class="card">
      <h3>订单与销售额趋势</h3>
      <div ref="trendChart" class="chart-lg"></div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getOverview } from '../../api/admin'
import { formatMoney } from '../../utils/format'

const days = ref(30)
const trendChart = ref(null)
const data = ref({ users: {}, merchants: {}, orders: {}, afterSales: {}, complaints: {}, trend: [] })
let chartInstance = null

async function loadData() {
  try {
    data.value = await getOverview({ days: days.value })
  } catch (e) {}
  await nextTick()
  renderChart()
}
function renderChart() {
  if (!trendChart.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(trendChart.value)
  const trend = data.value.trend || []
  chartInstance.setOption({
    tooltip: { trigger: 'axis' },
    legend: { data: ['订单数', '销售额'] },
    grid: { left: 60, right: 60, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: trend.map((t) => t.date) },
    yAxis: [
      { type: 'value', name: '订单数' },
      { type: 'value', name: '销售额(元)' }
    ],
    series: [
      {
        name: '订单数', type: 'bar', data: trend.map((t) => t.orders),
        itemStyle: { color: '#2e9e5b' }
      },
      {
        name: '销售额', type: 'line', yAxisIndex: 1, smooth: true,
        data: trend.map((t) => t.sales),
        lineStyle: { color: '#f5a623', width: 3 },
        itemStyle: { color: '#f5a623' }
      }
    ]
  })
}

onMounted(loadData)
</script>

<style scoped>
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 16px;
  margin-bottom: 20px;
}
.stat-card {
  background: #fff;
  border-radius: var(--radius);
  box-shadow: var(--shadow);
  padding: 20px;
}
.stat-label {
  color: var(--text-secondary);
  font-size: 13px;
}
.stat-value {
  font-size: 26px;
  font-weight: 700;
  margin-top: 8px;
}
.stat-sub {
  color: var(--text-light);
  font-size: 12px;
  margin-top: 6px;
}
.card h3 {
  font-size: 16px;
  margin-bottom: 16px;
}
.chart-lg {
  height: 360px;
  width: 100%;
}
</style>
