<script setup>
import { computed } from 'vue'

const props = defineProps({
  status: { type: String, required: true },
})

const label = computed(() => {
  const map = {
    DRAFT: '임시저장',
    PUBLISHED: '발행됨',
    HIDDEN: '숨김',
    DELETED: '삭제됨',
  }
  return map[props.status] || props.status
})

const colorClass = computed(() => {
  const map = {
    DRAFT: 'bg-yellow-100 text-yellow-800',
    PUBLISHED: 'bg-green-100 text-green-800',
    HIDDEN: 'bg-gray-100 text-gray-800',
    DELETED: 'bg-red-100 text-red-800',
  }
  return map[props.status] || 'bg-gray-100 text-gray-800'
})
</script>

<template>
  <span :class="['inline-block px-2 py-0.5 text-xs font-medium rounded-full', colorClass]">
    {{ label }}
  </span>
</template>
