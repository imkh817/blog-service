<script setup>
import { ref } from 'vue'

defineProps({
  parentId: { type: Number, default: null },
  placeholder: { type: String, default: '댓글을 작성하세요...' },
})

const emit = defineEmits(['submit', 'cancel'])
const content = ref('')

function handleSubmit() {
  const trimmed = content.value.trim()
  if (!trimmed) return
  emit('submit', trimmed)
  content.value = ''
}
</script>

<template>
  <div class="space-y-2">
    <textarea
      v-model="content"
      :placeholder="placeholder"
      rows="3"
      class="w-full p-3 border border-gray-300 rounded-lg text-sm resize-none focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
    />
    <div class="flex justify-end gap-2">
      <button
        v-if="parentId"
        @click="emit('cancel')"
        class="px-4 py-2 text-sm text-gray-600 hover:text-gray-900 cursor-pointer"
      >
        취소
      </button>
      <button
        @click="handleSubmit"
        :disabled="!content.trim()"
        class="px-4 py-2 bg-gray-900 text-white text-sm rounded-lg hover:bg-gray-700 disabled:opacity-40 disabled:cursor-not-allowed cursor-pointer"
      >
        등록
      </button>
    </div>
  </div>
</template>
