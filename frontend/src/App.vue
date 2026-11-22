<template>
  <div class="app-shell">
    <AppHeader />
    <div class="app-body">
      <AppSidebar v-if="showSidebar" />
      <main class="app-main" :class="{ 'with-sidebar': showSidebar }">
        <router-view />
      </main>
    </div>
  </div>
</template>

<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/AppHeader.vue'
import AppSidebar from './components/AppSidebar.vue'

const route = useRoute()

const showSidebar = computed(() => {
  const p = route.path
  return p.startsWith('/merchant/') || p.startsWith('/admin/')
})
</script>

<style scoped>
.app-shell {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}
.app-body {
  flex: 1;
  display: flex;
  align-items: stretch;
}
.app-main {
  flex: 1;
  min-width: 0;
}
.app-main.with-sidebar {
  background: var(--bg);
}
</style>
