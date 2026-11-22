<template>
  <span class="tag" :class="'tag-' + type">{{ text }}</span>
</template>

<script setup>
import { computed } from 'vue'

const props = defineProps({
  text: { type: [String, Number], default: '-' },
  type: { type: String, default: 'info' }
})

const typeClass = computed(() => 'tag-' + props.type)
</script>
