<script setup>
import { ref, computed } from 'vue'
import { renderMarkdown } from '../../utils/markdown'

const props = defineProps({
  modelValue: { type: String, default: '' },
})

const emit = defineEmits(['update:modelValue'])
const textareaRef = ref(null)
const fileInputRef = ref(null)
const mobileView = ref('editor')
const imageMap = new Map() // blobUrl → File (저장 직전 base64로 변환)

const preview = computed(() => renderMarkdown(props.modelValue))

// ── 기본 입력 핸들러 ──────────────────────────────────
function handleInput(e) {
  emit('update:modelValue', e.target.value)
}

// ── 커서 위치에 텍스트 삽입 ───────────────────────────
function insert(text) {
  const el = textareaRef.value
  if (!el) return

  const start = el.selectionStart
  const end = el.selectionEnd
  const before = props.modelValue.substring(0, start)
  const after = props.modelValue.substring(end)

  emit('update:modelValue', before + text + after)

  const newPos = start + text.length
  setTimeout(() => {
    el.focus()
    el.setSelectionRange(newPos, newPos)
  }, 0)
}

// ── 선택 영역을 앞뒤 문자로 감싸기 (굵게, 기울임 등) ──
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
    setTimeout(() => {
      el.focus()
      el.setSelectionRange(start + before.length, end + before.length)
    }, 0)
  } else {
    emit('update:modelValue', prefix + before + after + suffix)
    setTimeout(() => {
      el.focus()
      el.setSelectionRange(start + before.length, start + before.length)
    }, 0)
  }
}

// ── 현재 줄 앞에 접두어 삽입 (제목, 리스트, 인용 등) ──
function prefixLine(prefix) {
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

// ── 코드 블록 삽입 ────────────────────────────────────
function insertCodeBlock() {
  const el = textareaRef.value
  if (!el) return

  const start = el.selectionStart
  const end = el.selectionEnd
  const selected = props.modelValue.substring(start, end)

  const block = selected
    ? `\`\`\`\n${selected}\n\`\`\``
    : `\`\`\`\n\n\`\`\``

  const prefix = props.modelValue.substring(0, start)
  const suffix = props.modelValue.substring(end)

  emit('update:modelValue', prefix + block + suffix)

  setTimeout(() => {
    el.focus()
    // 선택 텍스트가 없으면 ``` 와 ``` 사이에 커서 위치
    const cursorPos = start + 4
    el.setSelectionRange(cursorPos, cursorPos)
  }, 0)
}

// ── 이미지 업로드 (blob URL → 미리보기, 저장 시 base64로 변환) ─────────────
function handleImageUpload(e) {
  const file = e.target.files?.[0]
  if (!file) return

  const blobUrl = URL.createObjectURL(file)
  imageMap.set(blobUrl, file)

  const altText = file.name.replace(/\.[^/.]+$/, '')
  insert(`![${altText}](${blobUrl})`)

  e.target.value = ''
}

// ── 저장 직전 blob URL → base64 변환 ─────────────────────
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

// ── 툴바 정의 ─────────────────────────────────────────
const toolbarItems = [
  // 제목
  { type: 'button', label: 'H1', title: '제목 1', action: () => prefixLine('# ') },
  { type: 'button', label: 'H2', title: '제목 2', action: () => prefixLine('## ') },
  { type: 'button', label: 'H3', title: '제목 3', action: () => prefixLine('### ') },
  { type: 'divider' },
  // 텍스트 서식
  { type: 'button', label: 'B', title: '굵게', bold: true, action: () => wrap('**') },
  { type: 'button', label: 'I', title: '기울임', italic: true, action: () => wrap('*') },
  { type: 'button', label: 'S', title: '취소선', strike: true, action: () => wrap('~~') },
  { type: 'divider' },
  // 리스트 / 인용
  { type: 'icon', title: '순서 없는 리스트', icon: 'ul', action: () => prefixLine('- ') },
  { type: 'icon', title: '순서 있는 리스트', icon: 'ol', action: () => prefixLine('1. ') },
  { type: 'icon', title: '인용문', icon: 'quote', action: () => prefixLine('> ') },
  { type: 'divider' },
  // 코드
  { type: 'button', label: '`코드`', title: '인라인 코드', mono: true, action: () => wrap('`') },
  { type: 'icon', title: '코드 블록', icon: 'codeblock', action: insertCodeBlock },
  { type: 'divider' },
  // 이미지
  { type: 'icon', title: '이미지 업로드', icon: 'image', action: () => fileInputRef.value.click() },
]
</script>

<template>
  <div>

    <!-- 모바일 탭 -->
    <div class="flex md:hidden border-b border-gray-200 mb-3">
      <button
        @click="mobileView = 'editor'"
        :class="[
          'px-4 py-2 text-sm font-medium border-b-2 -mb-px transition',
          mobileView === 'editor'
            ? 'border-blue-500 text-blue-600'
            : 'border-transparent text-gray-400 hover:text-gray-600',
        ]"
      >편집</button>
      <button
        @click="mobileView = 'preview'"
        :class="[
          'px-4 py-2 text-sm font-medium border-b-2 -mb-px transition',
          mobileView === 'preview'
            ? 'border-blue-500 text-blue-600'
            : 'border-transparent text-gray-400 hover:text-gray-600',
        ]"
      >미리보기</button>
    </div>

    <!-- split layout -->
    <div class="flex gap-6">

      <!-- ── 에디터 영역 ── -->
      <div
        :class="[
          'flex-1 min-w-0 flex flex-col',
          mobileView === 'preview' ? 'hidden md:flex' : 'flex',
        ]"
      >
        <!-- 툴바 -->
        <div class="bg-white rounded-xl px-3 py-2 flex items-center gap-0.5 mb-2 flex-wrap sticky top-16 z-10 border border-gray-100">
          <template v-for="(item, i) in toolbarItems" :key="i">

            <!-- 구분선 -->
            <span v-if="item.type === 'divider'" class="w-px h-4 bg-gray-200 mx-1" />

            <!-- 텍스트 버튼 (H1, H2, B, I 등) -->
            <button
              v-else-if="item.type === 'button'"
              @click="item.action"
              :title="item.title"
              :class="[
                'px-2 py-1 rounded text-gray-600 hover:bg-gray-100 transition cursor-pointer text-sm leading-none',
                item.bold   ? 'font-bold'       : '',
                item.italic ? 'italic'          : '',
                item.strike ? 'line-through'    : '',
                item.mono   ? 'font-mono text-xs' : '',
              ]"
            >{{ item.label }}</button>

            <!-- 아이콘 버튼 -->
            <button
              v-else-if="item.type === 'icon'"
              @click="item.action"
              :title="item.title"
              class="p-1.5 rounded text-gray-600 hover:bg-gray-100 transition cursor-pointer flex items-center justify-center"
            >
              <!-- 순서 없는 리스트 -->
              <svg v-if="item.icon === 'ul'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <line x1="9" y1="6"  x2="20" y2="6"  stroke-width="2" stroke-linecap="round"/>
                <line x1="9" y1="12" x2="20" y2="12" stroke-width="2" stroke-linecap="round"/>
                <line x1="9" y1="18" x2="20" y2="18" stroke-width="2" stroke-linecap="round"/>
                <circle cx="4" cy="6"  r="1" fill="currentColor"/>
                <circle cx="4" cy="12" r="1" fill="currentColor"/>
                <circle cx="4" cy="18" r="1" fill="currentColor"/>
              </svg>

              <!-- 순서 있는 리스트 -->
              <svg v-else-if="item.icon === 'ol'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <line x1="10" y1="6"  x2="21" y2="6"  stroke-width="2" stroke-linecap="round"/>
                <line x1="10" y1="12" x2="21" y2="12" stroke-width="2" stroke-linecap="round"/>
                <line x1="10" y1="18" x2="21" y2="18" stroke-width="2" stroke-linecap="round"/>
                <text x="2" y="8"  font-size="6" fill="currentColor" stroke="none">1</text>
                <text x="2" y="14" font-size="6" fill="currentColor" stroke="none">2</text>
                <text x="2" y="20" font-size="6" fill="currentColor" stroke="none">3</text>
              </svg>

              <!-- 인용문 -->
              <svg v-else-if="item.icon === 'quote'" class="w-4 h-4" fill="currentColor" viewBox="0 0 24 24">
                <path d="M3 21c3 0 7-1 7-8V5c0-1.25-.756-2.017-2-2H4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2 1 0 1 0 1 1v1c0 1-1 2-2 2s-1 .008-1 1.031V20c0 1 0 1 1 1z"/>
                <path d="M15 21c3 0 7-1 7-8V5c0-1.25-.757-2.017-2-2h-4c-1.25 0-2 .75-2 1.972V11c0 1.25.75 2 2 2h.75c0 2.25.25 4-2.75 4v3c0 1 0 1 1 1z"/>
              </svg>

              <!-- 코드 블록 -->
              <svg v-else-if="item.icon === 'codeblock'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M10 20l4-16m4 4l4 4-4 4M6 16l-4-4 4-4"/>
              </svg>

              <!-- 이미지 -->
              <svg v-else-if="item.icon === 'image'" class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                <rect x="3" y="3" width="18" height="18" rx="2" ry="2" stroke-width="2"/>
                <circle cx="8.5" cy="8.5" r="1.5" stroke-width="2"/>
                <polyline points="21 15 16 10 5 21" stroke-width="2"/>
              </svg>
            </button>
          </template>
        </div>

        <!-- 숨겨진 파일 input -->
        <input
          ref="fileInputRef"
          type="file"
          accept="image/*"
          class="hidden"
          @change="handleImageUpload"
        />

        <!-- 텍스트에리어 -->
        <textarea
          ref="textareaRef"
          :value="modelValue"
          @input="handleInput"
          placeholder="당신의 이야기를 마크다운으로 작성해보세요...

# 제목
## 소제목

**굵게**, *기울임*, ~~취소선~~

- 리스트 항목
1. 순서 있는 리스트

> 인용문

\`인라인 코드\`

\`\`\`javascript
// 코드 블록
const hello = 'world'
\`\`\`"
          class="w-full min-h-[600px] p-3 text-sm bg-transparent text-gray-800 placeholder-gray-300 resize-none focus:outline-none font-mono leading-relaxed"
        />
      </div>

      <!-- 구분선 (데스크탑) -->
      <div class="hidden md:block w-px bg-gray-200 shrink-0" />

      <!-- ── 미리보기 영역 ── -->
      <div
        :class="[
          'flex-1 min-w-0',
          mobileView === 'editor' ? 'hidden md:block' : 'block',
        ]"
      >
        <p class="text-xs font-medium text-gray-400 mb-3 hidden md:block">미리보기</p>

        <div
          v-if="modelValue"
          class="prose max-w-none"
          v-html="preview"
        />
        <p v-else class="text-sm text-gray-300 pt-1">
          왼쪽에 내용을 입력하면 여기서 미리볼 수 있습니다.
        </p>
      </div>

    </div>
  </div>
</template>
