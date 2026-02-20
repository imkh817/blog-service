<script setup>
import { computed } from 'vue'

const props = defineProps({
  currentPage: { type: Number, required: true },
  totalPages:  { type: Number, required: true },
})
const emit = defineEmits(['update:currentPage'])

const pages = computed(() => {
  const total = props.totalPages
  const current = props.currentPage
  const range = []
  const start = Math.max(1, current - 2)
  const end   = Math.min(total, current + 2)
  for (let i = start; i <= end; i++) range.push(i)
  return range
})

function goTo(page) {
  if (page >= 1 && page <= props.totalPages) {
    emit('update:currentPage', page)
  }
}
</script>

<template>
  <nav v-if="totalPages > 1" class="pagination">
    <button
      @click="goTo(currentPage - 1)"
      :disabled="currentPage === 1"
      class="page-btn nav-btn"
    >
      <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M12.707 5.293a1 1 0 010 1.414L9.414 10l3.293 3.293a1 1 0 01-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>
    </button>

    <button
      v-for="page in pages"
      :key="page"
      @click="goTo(page)"
      class="page-btn"
      :class="{ active: page === currentPage }"
    >{{ page }}</button>

    <button
      @click="goTo(currentPage + 1)"
      :disabled="currentPage === totalPages"
      class="page-btn nav-btn"
    >
      <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/></svg>
    </button>
  </nav>
</template>

<style scoped>
.pagination {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.35rem;
  margin-top: 2.5rem;
}

.page-btn {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 10px;
  border: 1.5px solid #E8EDF8;
  background: white;
  color: #64748B;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: 'Noto Sans KR', sans-serif;
}
.page-btn svg { width: 14px; height: 14px; }
.page-btn:hover:not(:disabled):not(.active) {
  border-color: #C7D2FE;
  color: #4F46E5;
  background: #EEF2FF;
}
.page-btn:disabled {
  opacity: 0.35;
  cursor: not-allowed;
}
.page-btn.active {
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  border-color: transparent;
  color: white;
  box-shadow: 0 2px 10px rgba(71,118,230,0.28);
}
.nav-btn { color: #94A3B8; }
</style>
