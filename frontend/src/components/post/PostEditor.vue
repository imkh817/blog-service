<script setup>
import { ref, computed } from 'vue'
import { renderMarkdown } from '../../utils/markdown'

const props = defineProps({
  modelValue: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue'])

const showPreview = ref(false)

const preview = computed(() => renderMarkdown(props.modelValue))

function handleInput(e) {
  emit('update:modelValue', e.target.value)
}
</script>

<template>
  <div class="border border-gray-300 rounded-lg overflow-hidden">
    <div class="flex border-b border-gray-200">
      <button
        @click="showPreview = false"
        :class="[
          'px-4 py-2 text-sm font-medium cursor-pointer',
          !showPreview ? 'bg-white text-gray-900 border-b-2 border-gray-900' : 'text-gray-500 hover:text-gray-700'
        ]"
      >
        작성
      </button>
      <button
        @click="showPreview = true"
        :class="[
          'px-4 py-2 text-sm font-medium cursor-pointer',
          showPreview ? 'bg-white text-gray-900 border-b-2 border-gray-900' : 'text-gray-500 hover:text-gray-700'
        ]"
      >
        미리보기
      </button>
    </div>
    <div v-if="!showPreview">
      <textarea
        :value="modelValue"
        @input="handleInput"
        placeholder="Markdown으로 작성하세요..."
        class="w-full min-h-[400px] p-4 text-sm font-mono resize-y focus:outline-none"
      />
    </div>
    <div v-else class="p-4 min-h-[400px] prose prose-gray max-w-none" v-html="preview" />
  </div>
</template>
