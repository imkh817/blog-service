<script setup>
import { ref } from 'vue'

const props = defineProps({
  modelValue: { type: Array, default: () => [] },
  max: { type: Number, default: 10 },
})

const emit = defineEmits(['update:modelValue'])
const input = ref('')

function addTag() {
  const tag = input.value.trim()
  if (!tag) return
  if (props.modelValue.length >= props.max) return
  if (props.modelValue.includes(tag)) {
    input.value = ''
    return
  }
  emit('update:modelValue', [...props.modelValue, tag])
  input.value = ''
}

function removeTag(index) {
  const updated = [...props.modelValue]
  updated.splice(index, 1)
  emit('update:modelValue', updated)
}

function handleKeydown(e) {
  if (e.key === 'Enter') {
    e.preventDefault()
    addTag()
  }
  if (e.key === 'Backspace' && !input.value && props.modelValue.length > 0) {
    removeTag(props.modelValue.length - 1)
  }
}
</script>

<template>
  <div class="border border-gray-300 rounded-lg p-3 flex flex-wrap gap-2 focus-within:ring-2 focus-within:ring-gray-900 focus-within:border-transparent">
    <span
      v-for="(tag, index) in modelValue"
      :key="tag"
      class="inline-flex items-center gap-1 px-3 py-1 bg-gray-100 text-gray-700 text-sm rounded-full"
    >
      #{{ tag }}
      <button
        @click="removeTag(index)"
        class="text-gray-400 hover:text-gray-600 cursor-pointer"
      >
        &times;
      </button>
    </span>
    <input
      v-model="input"
      @keydown="handleKeydown"
      :placeholder="modelValue.length === 0 ? '태그를 입력하세요 (Enter로 추가)' : ''"
      :disabled="modelValue.length >= max"
      class="flex-1 min-w-[120px] text-sm focus:outline-none disabled:bg-transparent"
    />
    <span class="text-xs text-gray-400 self-center">{{ modelValue.length }}/{{ max }}</span>
  </div>
</template>
