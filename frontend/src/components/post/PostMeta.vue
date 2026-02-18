<script setup>
defineProps({
  viewCount: { type: Number, default: 0 },
  createdAt: { type: String, default: '' },
  authorName: { type: String, default: '' },
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}
</script>

<template>
  <div class="flex items-center gap-3 text-sm text-gray-500">
    <span v-if="authorName">{{ authorName }}</span>
    <span v-if="createdAt">{{ formatDate(createdAt) }}</span>
    <span>조회 {{ viewCount }}</span>
  </div>
</template>
