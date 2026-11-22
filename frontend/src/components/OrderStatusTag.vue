<template>
  <span class="tag" :class="'tag-' + type">{{ text }}</span>
</template>

<script setup>
import { computed } from 'vue'
import { ORDER_STATUS, ORDER_STATUS_TYPE, textOf, typeOf } from '../utils/enums'

const props = defineProps({
  status: { type: [String, Number], default: null }
})

const text = computed(() => textOf(ORDER_STATUS, props.status))
const type = computed(() => typeOf(ORDER_STATUS_TYPE, props.status, 'info'))
</script>
