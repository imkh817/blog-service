<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: { type: Number, required: true },
  totalPages: { type: Number, required: true },
})

const emit = defineEmits(['update:currentPage'])

const pages = computed(() => {
  const total = props.totalPages
  const current = props.currentPage
  const range = []
  const start = Math.max(1, current - 2)
  const end = Math.min(total, current + 2)
  for (let i = start; i <= end; i++) {
    range.push(i)
  }
  return range
})

function goTo(page) {
  if (page >= 1 && page <= props.totalPages) {
    emit('update:currentPage', page)
  }
}
</script>

<template>
  <nav v-if="totalPages > 1" class="flex items-center justify-center gap-1 mt-8">
    <button
      @click="goTo(currentPage - 1)"
      :disabled="currentPage === 1"
      class="px-3 py-2 text-sm rounded-lg border border-gray-300 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
    >
      이전
    </button>
    <button
      v-for="page in pages"
      :key="page"
      @click="goTo(page)"
      :class="[
        'px-3 py-2 text-sm rounded-lg cursor-pointer',
        page === currentPage
          ? 'bg-gray-900 text-white'
          : 'border border-gray-300 hover:bg-gray-100'
      ]"
    >
      {{ page }}
    </button>
    <button
      @click="goTo(currentPage + 1)"
      :disabled="currentPage === totalPages"
      class="px-3 py-2 text-sm rounded-lg border border-gray-300 hover:bg-gray-100 disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
    >
      다음
    </button>
  </nav>
</template>
