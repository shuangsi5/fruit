<template>
  <div class="page-container">
    <div class="page-header">
      <h2 class="page-title" style="margin: 0">销售报表</h2>
      <div class="filter-bar" style="margin: 0">
        <input v-model="filters.startDate" type="date" class="form-control" />
        <span>至</span>
        <input v-model="filters.endDate" type="date" class="form-control" />
        <select v-model="filters.granularity" class="form-control" @change="loadReport">
          <option value="DAY">按日</option>
          <option value="WEEK">按周</option>
          <option value="MONTH">按月</option>
        </select>
        <button class="btn btn-primary" @click="loadReport">查询</button>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-label">销售总额</div>
        <div class="stat-value price">{{ formatMoney(report.summary?.totalSales || 0) }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">订单总数</div>
        <div class="stat-value">{{ report.summary?.totalOrders || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">销售件数</div>
        <div class="stat-value">{{ report.summary?.totalUnits || 0 }}</div>
      </div>
      <div class="stat-card">
        <div class="stat-label">客单价</div>
        <div class="stat-value price">{{ formatMoney(report.summary?.avgOrderAmount || 0) }}</div>
      </div>
    </div>

    <div class="card">
      <h3>销售趋势</h3>
      <div ref="trendChart" class="chart-lg"></div>
    </div>

    <div class="card">
      <h3>热销商品 TOP</h3>
      <div class="table-wrap" v-if="topProducts.length">
        <table class="data-table">
          <thead><tr><th>排名</th><th>商品名称</th><th>销量</th><th>销售额</th></tr></thead>
          <tbody>
            <tr v-for="(p, idx) in topProducts" :key="p.productId">
              <td>{{ idx + 1 }}</td>
              <td>{{ p.productName }}</td>
              <td>{{ p.units }}</td>
              <td class="price">{{ formatMoney(p.sales) }}</td>
            </tr>
          </tbody>
        </table>
      </div>
      <EmptyState v-else text="暂无销售数据" />
    </div>
  </div>
</template>

<script setup>
import { ref, reactive, computed, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getSalesReport } from '../../api/merchant'
import { formatMoney, today, daysAgo } from '../../utils/format'
import EmptyState from '../../components/EmptyState.vue'

const filters = reactive({ startDate: daysAgo(30), endDate: today(), granularity: 'DAY' })
const report = ref({ summary: {}, trend: [], topProducts: [] })
const trendChart = ref(null)
let chartInstance = null

const topProducts = computed(() => report.value.topProducts || [])

async function loadReport() {
  try {
    report.value = await getSalesReport({
      startDate: filters.startDate,
      endDate: filters.endDate,
      granularity: filters.granularity
    })
  } catch (e) {
    report.value = { summary: {}, trend: [], topProducts: [] }
  }
  await nextTick()
  renderChart()
}

function renderChart() {
  if (!trendChart.value) return
  if (chartInstance) chartInstance.dispose()
  chartInstance = echarts.init(trendChart.value)
  const trend = report.value.trend || []
  chartInstance.setOption({
    tooltip: { trigger: 'axis', axisPointer: { type: 'shadow' } },
    legend: { data: ['销售额', '订单数'] },
    grid: { left: 60, right: 40, top: 40, bottom: 40 },
    xAxis: { type: 'category', data: trend.map((t) => t.date) },
    yAxis: [
      { type: 'value', name: '销售额(元)' },
      { type: 'value', name: '订单数' }
    ],
    series: [
      {
        name: '销售额', type: 'bar',
        data: trend.map((t) => t.sales),
        itemStyle: { color: '#2e9e5b' }
      },
      {
        name: '订单数', type: 'line', yAxisIndex: 1, smooth: true,
        data: trend.map((t) => t.orders),
        lineStyle: { color: '#f5a623' },
        itemStyle: { color: '#f5a623' }
      }
    ]
  })
}

onMounted(loadReport)
</script>

<style scoped>
.filter-bar span {
  color: var(--text-light);
}
.stat-grid {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(200px, 1fr));
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
.card h3 {
  font-size: 16px;
  margin-bottom: 16px;
}
.chart-lg {
  height: 340px;
  width: 100%;
}
</style>
