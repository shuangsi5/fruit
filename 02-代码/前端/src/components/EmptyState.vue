<template>
  <div class="empty-state">
    <div class="empty-icon">□</div>
    <div class="empty-text">{{ text }}</div>
    <slot name="action"></slot>
  </div>
</template>

<script setup>
defineProps({
  text: { type: String, default: '暂无数据' }
})
</script>

<style scoped>
.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: var(--text-light);
}
.empty-icon {
  font-size: 56px;
  line-height: 1;
  color: #d1d5db;
  margin-bottom: 16px;
}
.empty-text {
  font-size: 15px;
  margin-bottom: 16px;
}
</style>
