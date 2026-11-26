<template>
  <div class="not-found">
    <div class="code">404</div>
    <div class="desc">抱歉，您访问的页面不存在</div>
    <router-link to="/" class="btn btn-primary">返回首页</router-link>
  </div>
</template>

<style scoped>
.not-found {
  min-height: 60vh;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 16px;
}
.code {
  font-size: 96px;
  font-weight: 800;
  color: var(--primary);
}
.desc {
  font-size: 18px;
  color: var(--text-secondary);
}
</style>
