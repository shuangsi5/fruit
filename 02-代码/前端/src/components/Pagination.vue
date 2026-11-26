<template>
  <div class="pagination" v-if="pages > 1">
    <button class="page-btn" :disabled="pageNum <= 1" @click="go(pageNum - 1)">上一页</button>
    <button
      v-for="p in visiblePages"
      :key="p"
      class="page-btn"
      :class="{ active: p === pageNum }"
      @click="go(p)"
    >
      {{ p }}
    </button>
    <button class="page-btn" :disabled="pageNum >= pages" @click="go(pageNum + 1)">下一页</button>
    <span class="page-total">共 {{ total }} 条</span>
  </div>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  pageNum: { type: Number, default: 1 },
  pageSize: { type: Number, default: 10 },
  total: { type: Number, default: 0 },
  pages: { type: Number, default: 1 }
})
const emit = defineEmits(['change'])

const visiblePages = computed(() => {
  const result = []
  const start = Math.max(1, props.pageNum - 2)
  const end = Math.min(props.pages, start + 4)
  for (let i = start; i <= end; i++) result.push(i)
  return result
})

function go(p) {
  if (p < 1 || p > props.pages || p === props.pageNum) return
  emit('change', p)
}
</script>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 6px;
  margin-top: 20px;
  flex-wrap: wrap;
}
.page-btn {
  min-width: 34px;
  height: 34px;
  padding: 0 10px;
  border: 1px solid var(--border);
  background: #fff;
  border-radius: 6px;
  color: var(--text-secondary);
  font-size: 13px;
}
.page-btn:hover:not(:disabled) {
  border-color: var(--primary);
  color: var(--primary);
}
.page-btn.active {
  background: var(--primary);
  border-color: var(--primary);
  color: #fff;
}
.page-btn:disabled {
  opacity: 0.45;
  cursor: not-allowed;
}
.page-total {
  margin-left: 10px;
  color: var(--text-light);
  font-size: 13px;
}
</style>
