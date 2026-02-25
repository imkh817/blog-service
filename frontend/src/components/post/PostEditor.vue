<script setup>
import { ref, computed } from 'vue'
import { renderMarkdown } from '../../utils/markdown'

const props = defineProps({
  modelValue: { type: String, default: '' },
  mode: { type: String, default: 'write' }, // 'write' | 'preview' | 'split'
})

const emit = defineEmits(['update:modelValue'])

const textareaRef = ref(null)
const fileInputRef = ref(null)
const imageMap = new Map()

const preview = computed(() => renderMarkdown(props.modelValue))

// ── Input handler ──────────────────────────────────────────────────────────
function handleInput(e) {
  emit('update:modelValue', e.target.value)
}

// ── Insert text at cursor ──────────────────────────────────────────────────
function insert(text) {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const before = props.modelValue.substring(0, start)
  const after = props.modelValue.substring(end)
  emit('update:modelValue', before + text + after)
  const newPos = start + text.length
  setTimeout(() => { el.focus(); el.setSelectionRange(newPos, newPos) }, 0)
}

// ── Wrap selection with before/after markers ───────────────────────────────
function wrap(before, after = before) {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = props.modelValue.substring(start, end)
  const prefix = props.modelValue.substring(0, start)
  const suffix = props.modelValue.substring(end)
  if (selected) {
    emit('update:modelValue', prefix + before + selected + after + suffix)
    setTimeout(() => { el.focus(); el.setSelectionRange(start + before.length, end + before.length) }, 0)
  } else {
    emit('update:modelValue', prefix + before + after + suffix)
    setTimeout(() => { el.focus(); el.setSelectionRange(start + before.length, start + before.length) }, 0)
  }
}

// ── Prefix the current line ────────────────────────────────────────────────
function prefixLine(prefix) {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const value = props.modelValue
  const lineStart = value.lastIndexOf('\n', start - 1) + 1
  const newValue = value.substring(0, lineStart) + prefix + value.substring(lineStart)
  emit('update:modelValue', newValue)
  setTimeout(() => { el.focus(); el.setSelectionRange(start + prefix.length, start + prefix.length) }, 0)
}

// ── Insert code block ──────────────────────────────────────────────────────
function insertCodeBlock() {
  const el = textareaRef.value
  if (!el) return
  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = props.modelValue.substring(start, end)
  const block = selected ? `\`\`\`\n${selected}\n\`\`\`` : `\`\`\`\n\n\`\`\``
  const prefix = props.modelValue.substring(0, start)
  const suffix = props.modelValue.substring(end)
  emit('update:modelValue', prefix + block + suffix)
  setTimeout(() => { el.focus(); el.setSelectionRange(start + 4, start + 4) }, 0)
}

// ── Image upload ───────────────────────────────────────────────────────────
function handleImageUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return
  const blobUrl = URL.createObjectURL(file)
  imageMap.set(blobUrl, file)
  const altText = file.name.replace(/\.[^/.]+$/, '')
  insert(`![${altText}](${blobUrl})`)
  e.target.value = ''
}

// ── Resolve blob URLs → base64 before saving ──────────────────────────────
async function resolveImages(content) {
  let resolved = content
  for (const [blobUrl, file] of imageMap.entries()) {
    if (!resolved.includes(blobUrl)) continue
    const base64 = await new Promise((res, rej) => {
      const reader = new FileReader()
      reader.onload = (ev) => res(ev.target.result)
      reader.onerror = rej
      reader.readAsDataURL(file)
    })
    resolved = resolved.replaceAll(blobUrl, base64)
  }
  return resolved
}

defineExpose({ resolveImages })

// ── Keyboard shortcuts ─────────────────────────────────────────────────────
function handleKeydown(e) {
  if (e.ctrlKey || e.metaKey) {
    if (e.key === 'b') { e.preventDefault(); wrap('**') }
    if (e.key === 'i') { e.preventDefault(); wrap('*') }
  }
}

// ── Grouped toolbar definition ─────────────────────────────────────────────
const toolbarGroups = [
  // Headings
  [
    { type: 'text', label: 'H1', title: '제목 1', action: () => prefixLine('# ') },
    { type: 'text', label: 'H2', title: '제목 2', action: () => prefixLine('## ') },
    { type: 'text', label: 'H3', title: '제목 3', action: () => prefixLine('### ') },
  ],
  // Formatting
  [
    { type: 'text', label: 'B', title: '굵게', shortcut: 'Ctrl+B', cls: 'font-bold', action: () => wrap('**') },
    { type: 'text', label: 'I', title: '기울임', shortcut: 'Ctrl+I', cls: 'italic', action: () => wrap('*') },
    { type: 'text', label: 'S', title: '취소선', cls: 'line-through', action: () => wrap('~~') },
  ],
  // Blocks
  [
    { type: 'icon', icon: 'ul',    title: '순서 없는 리스트', action: () => prefixLine('- ') },
    { type: 'icon', icon: 'ol',    title: '순서 있는 리스트', action: () => prefixLine('1. ') },
    { type: 'icon', icon: 'quote', title: '인용문',           action: () => prefixLine('> ') },
  ],
  // Code
  [
    { type: 'text', label: '`code`', title: '인라인 코드', cls: 'font-mono text-xs', action: () => wrap('`') },
    { type: 'icon', icon: 'codeblock', title: '코드 블록 (```)', action: insertCodeBlock },
  ],
  // Media
  [
    { type: 'icon', icon: 'image', title: '이미지 업로드', action: () => fileInputRef.value.click() },
  ],
]
</script>

<template>
  <div>
    <!-- ── Toolbar ──────────────────────────────────────────────────────── -->
    <div class="bg-white border border-gray-200 rounded-lg px-2 py-1.5 flex items-center gap-0.5 mb-4 flex-wrap sticky top-0 z-10">
      <template v-for="(group, gi) in toolbarGroups" :key="gi">
        <!-- Group separator -->
        <span v-if="gi > 0" class="w-px h-4 bg-gray-200 mx-1.5 shrink-0" />

        <!-- Items -->
        <div v-for="(item, ii) in group" :key="ii" class="relative group/tip">
          <!-- Text button (H1, B, I, etc.) -->
          <button
            v-if="item.type === 'text'"
            @click="item.action"
            :class="[
              'px-2 py-1 rounded text-gray-600 hover:bg-gray-100 active:bg-gray-200 transition-colors cursor-pointer text-sm leading-none select-none',
              item.cls ?? '',
            ]"
          >{{ item.label }}</button>

          <!-- Icon button -->
          <button
            v-else
            @click="item.action"
            class="p-1.5 rounded text-gray-500 hover:bg-gray-100 active:bg-gray-200 transition-colors cursor-pointer flex items-center justify-center select-none"
          >
            <svg v-if="item.icon === 'ul'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <line x1="9" y1="6" x2="20" y2="6" stroke-width="2" stroke-linecap="round"/>
              <line x1="9" y1="12" x2="20" y2="12" stroke-width="2" stroke-linecap="round"/>
              <line x1="9" y1="18" x2="20" y2="18" stroke-width="2" stroke-linecap="round"/>
              <circle cx="4" cy="6" r="1.5" fill="currentColor"/>
              <circle cx="4" cy="12" r="1.5" fill="currentColor"/>
              <circle cx="4" cy="18" r="1.5" fill="currentColor"/>
            </svg>
            <svg v-else-if="item.icon === 'ol'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <line x1="10" y1="6" x2="21" y2="6" stroke-width="2" stroke-linecap="round"/>
              <line x1="10" y1="12" x2="21" y2="12" stroke-width="2" stroke-linecap="round"/>
              <line x1="10" y1="18" x2="21" y2="18" stroke-width="2" stroke-linecap="round"/>
              <text x="2" y="8" font-size="6" fill="currentColor" stroke="none">1</text>
              <text x="2" y="14" font-size="6" fill="currentColor" stroke="none">2</text>
              <text x="2" y="20" font-size="6" fill="currentColor" stroke="none">3</text>
            </svg>
            <svg v-else-if="item.icon === 'quote'" class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
              <path d="M3 21c3 0 7-1 7-8V5c0-1.25-.756-2.017-2-2H4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2 1 0 1 0 1 1v1c0 1-1 2-2 2s-1 .008-1 1.031V20c0 1 0 1 1 1z"/>
              <path d="M15 21c3 0 7-1 7-8V5c0-1.25-.757-2.017-2-2h-4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2h.75c0 2.25.25 4-2.75 4v3c0 1 0 1 1 1z"/>
            </svg>
            <svg v-else-if="item.icon === 'codeblock'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"/>
            </svg>
            <svg v-else-if="item.icon === 'image'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <rect x="3" y="3" width="18" height="18" rx="2" ry="2" stroke-width="2"/>
              <circle cx="8.5" cy="8.5" r="1.5" stroke-width="2"/>
              <polyline points="21 15 16 10 5 21" stroke-width="2"/>
            </svg>
          </button>

          <!-- Tooltip -->
          <div class="absolute left-1/2 -translate-x-1/2 top-full mt-2 z-30 pointer-events-none opacity-0 group-hover/tip:opacity-100 transition-opacity delay-300 duration-150 whitespace-nowrap">
            <div class="bg-gray-800 text-white text-[11px] rounded-md px-2 py-1.5 flex flex-col items-center gap-0.5 shadow-lg">
              <span>{{ item.title }}</span>
              <span v-if="item.shortcut" class="text-gray-400 text-[10px]">{{ item.shortcut }}</span>
            </div>
          </div>
        </div>
      </template>
    </div>

    <!-- Hidden file input -->
    <input ref="fileInputRef" type="file" accept="image/*" class="hidden" @change="handleImageUpload" />

    <!-- ── Editor / Preview area ──────────────────────────────────────── -->
    <div class="flex gap-6">
      <!-- Editor column -->
      <div v-show="mode !== 'preview'" :class="mode === 'split' ? 'w-1/2' : 'w-full'">
        <textarea
          ref="textareaRef"
          :value="modelValue"
          @input="handleInput"
          @keydown="handleKeydown"
          placeholder="당신의 이야기를 마크다운으로 작성하세요..."
          class="w-full min-h-[560px] text-[15px] text-gray-800 bg-transparent placeholder-gray-300 resize-none focus:outline-none font-mono leading-7"
        />
      </div>

      <!-- Split divider -->
      <div v-if="mode === 'split'" class="w-px bg-gray-200 shrink-0 self-stretch" />

      <!-- Preview column -->
      <div v-show="mode !== 'write'" :class="mode === 'split' ? 'w-1/2' : 'w-full'">
        <div v-if="modelValue" class="prose max-w-none" v-html="preview" />
        <p v-else class="text-sm text-gray-300 pt-1">내용을 입력하면 여기서 미리볼 수 있습니다.</p>
      </div>
    </div>
  </div>
</template>