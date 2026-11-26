<template>
  <span class="tag" :class="'tag-' + type">{{ text }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { WARNING_LEVEL, WARNING_LEVEL_TYPE, textOf, typeOf } from '../utils/enums'

const props = defineProps({
  level: { type: [String, Number], default: 0 }
})

const text = computed(() => textOf(WARNING_LEVEL, props.level))
const type = computed(() => typeOf(WARNING_LEVEL_TYPE, props.level, 'success'))
</script>
