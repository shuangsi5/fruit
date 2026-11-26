<template>
  <div class="page-container">
    <div class="page-header">
      <div>
        <h2 class="page-title" style="margin: 0">商家工作台</h2>
        <p class="welcome">欢迎回来，{{ user.username || '商家' }}。以下是您店铺的经营概况。</p>
      </div>
      <div class="header-actions">
        <router-link to="/merchant/products" class="btn btn-primary">管理商品</router-link>
        <router-link to="/merchant/orders" class="btn btn-outline">查看订单</router-link>
      </div>
    </div>

    <div class="stat-grid">
      <div class="stat-card">
        <div class="stat-icon green">币</div>
        <div class="stat-info">
          <div class="stat-label">销售总额</div>
          <div class="stat-value">{{ formatMoney(report.summary?.totalSales || 0) }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon blue">单</div>
        <div class="stat-info">
          <div class="stat-label">订单总数</div>
          <div class="stat-value">{{ report.summary?.totalOrders || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon orange">警</div>
        <div class="stat-info">
          <div class="stat-label">库存预警</div>
          <div class="stat-value">{{ warnings.pending || 0 }}</div>
        </div>
      </div>
      <div class="stat-card">
        <div class="stat-icon red">后</div>
        <div class="stat-info">
          <div class="stat-label">待处理售后</div>
          <div class="stat-value">{{ afterSalesPending }}</div>
        </div>
      </div>
    </div>

    <div class="dashboard-grid">
      <div class="card">
        <div class="card-head">
          <h3>近 7 日销售趋势</h3>
          <router-link to="/merchant/reports" class="more">详细报表 ></router-link>
        </div>
        <div ref="trendChart" class="chart"></div>
      </div>
      <div class="card">
        <div class="card-head">
          <h3>库存与临期预警</h3>
          <router-link to="/merchant/stock" class="more">前往处理 ></router-link>
        </div>
        <div class="warning-list" v-if="warningList.length">
          <div v-for="(w, idx) in warningList.slice(0, 6)" :key="idx" class="warning-row">
            <span class="w-name">{{ w.productName }}</span>
            <span class="w-type">{{ w.warningTypeText }}</span>
            <WarningTag :level="w.warningLevel" />
          </div>
        </div>
        <EmptyState v-else text="暂无预警，库存状态良好" />
      </div>
    </div>
  </div>
</template>

<script setup>
import { ref, onMounted, nextTick } from 'vue'
import * as echarts from 'echarts'
import { getSalesReport, getInventoryWarnings, getMerchantAfterSales } from '../../api/merchant'
import { useUserStore } from '../../store'
import { formatMoney, today, daysAgo } from '../../utils/format'
import WarningTag from '../../components/WarningTag.vue'
import EmptyState from '../../components/EmptyState.vue'

const user = useUserStore()
const trendChart = ref(null)
const report = ref({ summary: {} })
const warnings = ref({})
const warningList = ref([])
const afterSalesPending = ref(0)

async function loadData() {
  try {
    report.value = await getSalesReport({ startDate: daysAgo(7), endDate: today(), granularity: 'DAY' })
  } catch (e) {}
  try {
    const res = await getInventoryWarnings({ level: 1, pageNum: 1, pageSize: 10 })
    warningList.value = res.list || []
    warnings.value.pending = res.total || 0
  } catch (e) {}
  try {
    const res = await getMerchantAfterSales({ status: 0, pageNum: 1, pageSize: 1 })
    afterSalesPending.value = res.total || 0
  } catch (e) {}

  await nextTick()
  renderChart()
}

function renderChart() {
  if (!trendChart.value) return
  const chart = echarts.init(trendChart.value)
  const trend = report.value.trend || []
  chart.setOption({
    tooltip: { trigger: 'axis' },
    grid: { left: 50, right: 20, top: 30, bottom: 30 },
    xAxis: { type: 'category', data: trend.map((t) => t.date) },
    yAxis: { type: 'value' },
    series: [
      {
        name: '销售额',
        type: 'line',
        smooth: true,
        data: trend.map((t) => t.sales),
        areaStyle: { color: 'rgba(46,158,91,0.2)' },
        lineStyle: { color: '#2e9e5b' },
        itemStyle: { color: '#2e9e5b' }
      }
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
.stat-icon.green { background: #2e9e5b; }
.stat-icon.blue { background: #3b82f6; }
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
.warning-row {
  display: flex;
  align-items: center;
  gap: 12px;
  padding: 10px 0;
  border-bottom: 1px solid var(--border);
}
.w-name {
  flex: 1;
}
.w-type {
  color: var(--text-secondary);
  font-size: 13px;
}
@media (max-width: 900px) {
  .dashboard-grid {
    grid-template-columns: 1fr;
  }
}
</style>
