<script setup>
import { ref, computed } from 'vue'
import { renderMarkdown } from '../../utils/markdown'

const props = defineProps({
  modelValue: { type: String, default: '' },
  showPreview: { type: Boolean, default: false },
})

const emit = defineEmits(['update:modelValue'])
const textareaRef = ref(null)
const preview = computed(() => renderMarkdown(props.modelValue))

function handleInput(e) {
  emit('update:modelValue', e.target.value)
}

function wrapText(before, after = before) {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = props.modelValue.substring(start, end)

  let newValue, cursorStart, cursorEnd
  if (selected) {
    newValue = props.modelValue.substring(0, start) + before + selected + after + props.modelValue.substring(end)
    cursorStart = start + before.length
    cursorEnd = end + before.length
  } else {
    newValue = props.modelValue.substring(0, start) + before + after + props.modelValue.substring(end)
    cursorStart = start + before.length
    cursorEnd = cursorStart
  }

  emit('update:modelValue', newValue)
  setTimeout(() => {
    el.focus()
    el.setSelectionRange(cursorStart, cursorEnd)
  }, 0)
}

function insertLinePrefix(prefix) {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const value = props.modelValue
  const lineStart = value.lastIndexOf('\n', start - 1) + 1
  const newValue = value.substring(0, lineStart) + prefix + value.substring(lineStart)
  emit('update:modelValue', newValue)
  setTimeout(() => {
    el.focus()
    el.setSelectionRange(start + prefix.length, start + prefix.length)
  }, 0)
}

const toolbarGroups = [
  [
    { label: 'B', title: '굵게', style: 'font-bold', action: () => wrapText('**') },
    { label: '/', title: '기울임', style: 'italic', action: () => wrapText('*') },
    { label: 'S', title: '취소선', style: 'line-through', action: () => wrapText('~~') },
  ],
  [
    { label: 'H1', title: '제목 1', style: '', action: () => insertLinePrefix('# ') },
    { label: 'H2', title: '제목 2', style: '', action: () => insertLinePrefix('## ') },
  ],
]
</script>

<template>
  <div>
    <div v-if="!showPreview">
      <!-- Formatting toolbar -->
      <div class="bg-white rounded-xl px-3 py-2 flex items-center gap-0.5 mb-2">
        <template v-for="(group, gi) in toolbarGroups" :key="gi">
          <span v-if="gi > 0" class="w-px h-4 bg-gray-200 mx-2" />
          <button
            v-for="item in group"
            :key="item.label"
            @click="item.action"
            :title="item.title"
            :class="[
              'px-2.5 py-1 text-sm rounded hover:bg-gray-100 transition cursor-pointer text-gray-600',
              item.style,
            ]"
          >
            {{ item.label }}
          </button>
        </template>
        <span class="w-px h-4 bg-gray-200 mx-2" />
        <!-- Image icon -->
        <button
          title="이미지"
          class="px-2 py-1 rounded hover:bg-gray-100 transition cursor-pointer text-gray-600"
          @click="wrapText('![이미지 설명](', ')')"
        >
          <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <rect x="3" y="3" width="18" height="18" rx="2" ry="2" stroke-width="2"/>
            <circle cx="8.5" cy="8.5" r="1.5" stroke-width="2"/>
            <polyline points="21 15 16 10 5 21" stroke-width="2"/>
          </svg>
        </button>
      </div>

      <!-- Textarea -->
      <textarea
        ref="textareaRef"
        :value="modelValue"
        @input="handleInput"
        placeholder="당신의 이야기를 적어보세요..."
        class="w-full min-h-[500px] p-2 text-base bg-transparent text-gray-800 placeholder-gray-300 resize-none focus:outline-none"
      />
    </div>
    <div v-else class="p-4 min-h-[500px] prose prose-gray max-w-none" v-html="preview" />
  </div>
</template>
