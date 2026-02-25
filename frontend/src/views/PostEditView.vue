<script setup>
import { ref, computed, watch, onMounted, onUnmounted } from 'vue'
import { useRoute, useRouter, onBeforeRouteLeave } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import PostEditor from '../components/post/PostEditor.vue'

const route  = useRoute()
const router = useRouter()
const auth   = useAuthStore()

// ── Form state ─────────────────────────────────────────────────────────────
const title    = ref('')
const content  = ref('')
const tags     = ref([])
const tagInput = ref('')

const visibility  = ref('PUBLIC')
const excerpt     = ref('')
const slug        = ref('')
const slugAutoGen = ref(false)  // false: don't auto-generate slug on edit
const scheduledAt = ref('')
const featuredImagePreview  = ref('')
const featuredImageInputRef = ref(null)

// ── Editor mode ────────────────────────────────────────────────────────────
const editorMode = ref('write')

const modes = [
  { key: 'write',   label: '쓰기' },
  { key: 'preview', label: '미리보기' },
  { key: 'split',   label: '분할' },
]

// ── Save state ─────────────────────────────────────────────────────────────
const saveStatus = ref('idle')
const lastSavedAt = ref(null)
const isDirty    = ref(false)
const loading    = ref(true)
const submitting = ref(false)
const error      = ref('')
const editorRef  = ref(null)
const titleRef   = ref(null)

// ── Exit dialog ────────────────────────────────────────────────────────────
const showExitDialog = ref(false)

const visibilityOptions = [
  { value: 'PUBLIC', label: '공개',  desc: '모든 사람이 볼 수 있습니다' },
  { value: 'HIDDEN', label: '비공개', desc: '나만 볼 수 있습니다' },
]

// ── Computed ───────────────────────────────────────────────────────────────
const saveStatusText = computed(() => {
  if (saveStatus.value !== 'saved' || !lastSavedAt.value) return null
  const t = lastSavedAt.value
  const pad = (n) => String(n).padStart(2, '0')
  return `${pad(t.getHours())}:${pad(t.getMinutes())}:${pad(t.getSeconds())}에 저장됨`
})

// ── Watchers ───────────────────────────────────────────────────────────────
watch(title,   () => { isDirty.value = true })
watch(content, () => { isDirty.value = true })
watch(tags,    () => { isDirty.value = true }, { deep: true })

// ── Load post ──────────────────────────────────────────────────────────────
async function fetchPost() {
  loading.value = true
  try {
    const res  = await postApi.getById(route.params.id)
    const post = res.data.data
    title.value   = post.title
    content.value = post.content
    tags.value    = post.tags || []
    // initialise dirty tracking only after load
    setTimeout(() => { isDirty.value = false }, 0)
  } finally {
    loading.value = false
  }
}

// ── Save (update) ──────────────────────────────────────────────────────────
async function saveChanges() {
  if (!title.value.trim()) { error.value = '제목을 입력하세요.'; return }
  saveStatus.value = 'saving'
  try {
    const resolvedContent = await editorRef.value?.resolveImages(content.value) ?? content.value
    await postApi.update(auth.user?.id, {
      postId: Number(route.params.id),
      title: title.value,
      content: resolvedContent,
      postStatus: 'DRAFT',
      tagNames: tags.value,
    })
    isDirty.value     = false
    lastSavedAt.value = new Date()
    saveStatus.value  = 'saved'
  } catch {
    saveStatus.value = 'idle'
  }
}

// ── Publish update ─────────────────────────────────────────────────────────
async function publish() {
  if (!title.value.trim())     { error.value = '제목을 입력하세요.';          return }
  if (!content.value.trim())   { error.value = '내용을 입력하세요.';          return }
  if (tags.value.length === 0) { error.value = '태그를 최소 1개 입력하세요.'; return }

  error.value   = ''
  submitting.value = true
  try {
    const resolvedContent = await editorRef.value?.resolveImages(content.value) ?? content.value
    const postStatus = visibility.value === 'HIDDEN' ? 'HIDDEN' : 'PUBLISHED'
    await postApi.update(auth.user?.id, {
      postId: Number(route.params.id),
      title: title.value,
      content: resolvedContent,
      postStatus,
      tagNames: tags.value,
    })
    isDirty.value = false
    router.push({ name: 'PostDetail', params: { id: route.params.id } })
  } catch (e) {
    error.value = e.response?.data?.message || '수정에 실패했습니다.'
  } finally {
    submitting.value = false
  }
}

// ── Tag management ─────────────────────────────────────────────────────────
function addTag() {
  const tag = tagInput.value.trim()
  if (!tag || tags.value.length >= 10 || tags.value.includes(tag)) {
    tagInput.value = ''
    return
  }
  tags.value = [...tags.value, tag]
  tagInput.value = ''
}
function removeTag(index) {
  tags.value = tags.value.filter((_, i) => i !== index)
}
function handleTagKeydown(e) {
  if (e.key === 'Enter' && !e.isComposing) { e.preventDefault(); addTag() }
  if (e.key === 'Backspace' && !tagInput.value && tags.value.length > 0) {
    removeTag(tags.value.length - 1)
  }
}

// ── Featured image ─────────────────────────────────────────────────────────
function handleFeaturedImage(e) {
  const file = e.target.files?.[0]
  if (!file) return
  featuredImagePreview.value = URL.createObjectURL(file)
}
function removeFeaturedImage() {
  featuredImagePreview.value = ''
  if (featuredImageInputRef.value) featuredImageInputRef.value.value = ''
}

// ── Title auto-resize ──────────────────────────────────────────────────────
function autoResizeTitle(e) {
  const el = e.target
  el.style.height = 'auto'
  el.style.height = el.scrollHeight + 'px'
}

// ── Unsaved-changes protection ─────────────────────────────────────────────
function handleBeforeUnload(e) {
  if (isDirty.value) { e.preventDefault(); e.returnValue = '' }
}
function tryExit() {
  if (isDirty.value) { showExitDialog.value = true } else { router.back() }
}
function confirmExit() {
  isDirty.value = false
  showExitDialog.value = false
  router.back()
}
onBeforeRouteLeave(() => {
  if (isDirty.value) { showExitDialog.value = true; return false }
})

// ── Autosave ───────────────────────────────────────────────────────────────
let autosaveTimer = null

// ── Lifecycle ──────────────────────────────────────────────────────────────
onMounted(() => {
  fetchPost()
  window.addEventListener('beforeunload', handleBeforeUnload)
  autosaveTimer = setInterval(() => {
    if (isDirty.value && title.value.trim()) saveChanges()
  }, 3000)
})
onUnmounted(() => {
  window.removeEventListener('beforeunload', handleBeforeUnload)
  clearInterval(autosaveTimer)
})
</script>

<template>
  <div class="h-screen flex flex-col bg-gray-50 overflow-hidden">

    <!-- ══════════════════════ STICKY HEADER ══════════════════════════════ -->
    <header class="h-14 shrink-0 bg-white border-b border-gray-200 flex items-center px-5 gap-4 z-20">

      <!-- Left: Exit -->
      <button
        @click="tryExit"
        class="flex items-center gap-1.5 text-sm text-gray-500 hover:text-gray-900 transition-colors cursor-pointer shrink-0"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        나가기
      </button>

      <!-- Center: Save status -->
      <div class="flex-1 flex items-center justify-center gap-2.5 min-w-0">
        <div v-if="saveStatus === 'saving'" class="flex items-center gap-1.5 text-xs text-gray-400">
          <svg class="w-3.5 h-3.5 animate-spin" viewBox="0 0 24 24" fill="none">
            <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
            <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
          </svg>
          저장 중...
        </div>
        <div v-else-if="saveStatusText" class="flex items-center gap-1.5 text-xs text-gray-400">
          <svg class="w-3.5 h-3.5 shrink-0" style="color: #10b981" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2.5" d="M5 13l4 4L19 7"/>
          </svg>
          {{ saveStatusText }}
        </div>
        <span class="px-2 py-0.5 text-[10px] font-bold tracking-widest uppercase bg-sky-100 text-sky-600 rounded">
          편집
        </span>
      </div>

      <!-- Right: Mode toggle + actions -->
      <div class="flex items-center gap-2 shrink-0">
        <div class="flex items-center bg-gray-100 rounded-lg p-0.5">
          <button
            v-for="m in modes"
            :key="m.key"
            @click="editorMode = m.key"
            :class="[
              'px-3 py-1 text-xs font-medium rounded-md transition-all cursor-pointer',
              editorMode === m.key
                ? 'bg-white text-gray-800 shadow-sm'
                : 'text-gray-500 hover:text-gray-700',
            ]"
          >{{ m.label }}</button>
        </div>

        <div class="w-px h-5 bg-gray-200" />

        <button
          @click="saveChanges"
          :disabled="submitting || saveStatus === 'saving'"
          class="px-3.5 py-1.5 text-xs font-medium text-gray-600 border border-gray-300 rounded-lg hover:bg-gray-50 disabled:opacity-40 cursor-pointer transition-colors"
        >임시저장</button>

        <button
          @click="publish"
          :disabled="submitting"
          class="px-4 py-1.5 text-xs font-semibold text-white bg-blue-500 rounded-lg hover:bg-blue-600 active:bg-blue-700 disabled:opacity-50 cursor-pointer transition-colors"
        >수정 완료</button>
      </div>
    </header>

    <!-- ══════════════════════ BODY ════════════════════════════════════════ -->

    <!-- Loading state -->
    <div v-if="loading" class="flex-1 flex items-center justify-center">
      <div class="flex items-center gap-2 text-sm text-gray-400">
        <svg class="w-4 h-4 animate-spin" viewBox="0 0 24 24" fill="none">
          <circle class="opacity-25" cx="12" cy="12" r="10" stroke="currentColor" stroke-width="4"/>
          <path class="opacity-75" fill="currentColor" d="M4 12a8 8 0 018-8V0C5.373 0 0 5.373 0 12h4z"/>
        </svg>
        불러오는 중...
      </div>
    </div>

    <div v-else class="flex flex-1 min-h-0">

      <!-- Main editor area -->
      <main class="flex-1 min-w-0 overflow-y-auto">
        <div class="max-w-3xl mx-auto px-8 pt-8 pb-16">

          <div
            v-if="error"
            class="mb-5 px-4 py-3 bg-red-50 border border-red-200 text-red-600 text-sm rounded-lg flex items-start gap-2"
          >
            <svg class="w-4 h-4 mt-0.5 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01M10.29 3.86L1.82 18a2 2 0 001.71 3h16.94a2 2 0 001.71-3L13.71 3.86a2 2 0 00-3.42 0z"/>
            </svg>
            {{ error }}
          </div>

          <!-- Title -->
          <textarea
            ref="titleRef"
            v-model="title"
            placeholder="제목을 입력하세요"
            maxlength="100"
            rows="1"
            class="w-full text-[2.25rem] font-bold text-gray-900 bg-transparent placeholder-gray-300 border-none outline-none resize-none leading-snug overflow-hidden mb-5"
            @input="autoResizeTitle"
          />

          <div class="border-t border-gray-200 mb-5" />

          <PostEditor ref="editorRef" v-model="content" :mode="editorMode" />
        </div>
      </main>

      <!-- ═══════════════════ RIGHT SETTINGS PANEL ══════════════════════ -->
      <aside class="w-[272px] shrink-0 border-l border-gray-200 bg-white overflow-y-auto flex flex-col">

        <!-- Tags -->
        <section class="px-5 py-4 border-b border-gray-100">
          <h3 class="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-3">태그</h3>
          <div v-if="tags.length" class="flex flex-wrap gap-1.5 mb-2.5">
            <span
              v-for="(tag, i) in tags"
              :key="tag"
              class="inline-flex items-center gap-1 px-2.5 py-0.5 bg-blue-50 text-blue-700 text-xs font-medium rounded-full border border-blue-100"
            >
              {{ tag }}
              <button @click="removeTag(i)" class="text-blue-400 hover:text-blue-600 cursor-pointer leading-none ml-0.5 text-sm">&times;</button>
            </span>
          </div>
          <div class="flex items-center gap-2 bg-gray-50 border border-gray-200 rounded-lg px-3 py-1.5 focus-within:ring-1 focus-within:ring-blue-300 focus-within:border-blue-300 transition-all">
            <svg class="w-3.5 h-3.5 text-gray-400 shrink-0" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M7 7h.01M7 3h5c.512 0 1.024.195 1.414.586l7 7a2 2 0 010 2.828l-7 7a2 2 0 01-2.828 0l-7-7A1.994 1.994 0 013 12V7a4 4 0 014-4z"/>
            </svg>
            <input
              v-model="tagInput"
              @keydown="handleTagKeydown"
              :disabled="tags.length >= 10"
              placeholder="태그 입력 후 Enter"
              class="flex-1 text-xs text-gray-700 placeholder-gray-300 bg-transparent outline-none disabled:opacity-40"
            />
          </div>
          <p class="mt-1.5 text-[11px] text-gray-400">{{ tags.length }}/10</p>
        </section>

        <!-- Visibility -->
        <section class="px-5 py-4 border-b border-gray-100">
          <h3 class="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-3">공개 설정</h3>
          <div class="flex flex-col gap-1.5">
            <label
              v-for="opt in visibilityOptions"
              :key="opt.value"
              :class="[
                'flex items-start gap-2.5 px-3 py-2.5 rounded-lg border cursor-pointer transition-colors',
                visibility === opt.value ? 'bg-blue-50 border-blue-200' : 'border-gray-200 hover:bg-gray-50',
              ]"
            >
              <input type="radio" :value="opt.value" v-model="visibility" class="mt-0.5 accent-blue-500 cursor-pointer" />
              <div>
                <p class="text-xs font-medium text-gray-700 leading-tight">{{ opt.label }}</p>
                <p class="text-[11px] text-gray-400 mt-0.5 leading-tight">{{ opt.desc }}</p>
              </div>
            </label>
          </div>
        </section>

        <!-- Featured Image -->
        <section class="px-5 py-4 border-b border-gray-100">
          <h3 class="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-3">대표 이미지</h3>
          <div v-if="featuredImagePreview" class="relative rounded-lg overflow-hidden border border-gray-200">
            <img :src="featuredImagePreview" alt="" class="w-full h-32 object-cover" />
            <button
              @click="removeFeaturedImage"
              class="absolute top-1.5 right-1.5 w-6 h-6 bg-black/60 hover:bg-black/80 text-white rounded-full flex items-center justify-center text-base leading-none cursor-pointer transition-colors"
            >&times;</button>
          </div>
          <label
            v-else
            class="flex flex-col items-center justify-center gap-2 h-28 border-2 border-dashed border-gray-200 rounded-lg bg-gray-50 hover:bg-gray-100 cursor-pointer transition-colors group"
          >
            <svg class="w-7 h-7 text-gray-300 group-hover:text-gray-400 transition-colors" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="1.5" d="M4 16l4.586-4.586a2 2 0 012.828 0L16 16m-2-2l1.586-1.586a2 2 0 012.828 0L20 14m-6-6h.01M6 20h12a2 2 0 002-2V6a2 2 0 00-2-2H6a2 2 0 00-2 2v12a2 2 0 002 2z"/>
            </svg>
            <span class="text-xs text-gray-400">클릭하여 업로드</span>
            <input ref="featuredImageInputRef" type="file" accept="image/*" class="hidden" @change="handleFeaturedImage" />
          </label>
        </section>

        <!-- Excerpt -->
        <section class="px-5 py-4 border-b border-gray-100">
          <h3 class="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-3">요약</h3>
          <textarea
            v-model="excerpt"
            placeholder="포스트에 대한 짧은 설명 (선택)"
            maxlength="300"
            rows="3"
            class="w-full text-xs text-gray-700 placeholder-gray-300 bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 resize-none focus:outline-none focus:ring-1 focus:ring-blue-300 focus:border-blue-300 transition-all leading-relaxed"
          />
          <p class="mt-1 text-[11px] text-gray-400 text-right">{{ excerpt.length }}/300</p>
        </section>

        <!-- Slug -->
        <section class="px-5 py-4 border-b border-gray-100">
          <h3 class="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-3">URL 슬러그</h3>
          <div class="flex items-center gap-1 bg-gray-50 border border-gray-200 rounded-lg px-3 py-1.5 focus-within:ring-1 focus-within:ring-blue-300 focus-within:border-blue-300 transition-all">
            <span class="text-[11px] text-gray-400 shrink-0 font-mono">/@/</span>
            <input
              v-model="slug"
              @input="slugAutoGen = false"
              placeholder="url-slug"
              class="flex-1 text-xs font-mono text-gray-700 placeholder-gray-300 bg-transparent outline-none min-w-0"
            />
          </div>
        </section>

        <!-- Scheduled publishing -->
        <section class="px-5 py-4">
          <h3 class="text-[11px] font-semibold text-gray-400 uppercase tracking-widest mb-3">예약 발행</h3>
          <input
            v-model="scheduledAt"
            type="datetime-local"
            class="w-full text-xs text-gray-700 bg-gray-50 border border-gray-200 rounded-lg px-3 py-2 focus:outline-none focus:ring-1 focus:ring-blue-300 focus:border-blue-300 transition-all"
          />
          <p class="mt-1.5 text-[11px] text-gray-400">비워두면 즉시 발행됩니다.</p>
        </section>
      </aside>
    </div>

    <!-- ══════════════════════ EXIT CONFIRMATION DIALOG ═══════════════════ -->
    <Teleport to="body">
      <div
        v-if="showExitDialog"
        class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
        style="backdrop-filter: blur(2px)"
        @click.self="showExitDialog = false"
      >
        <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm mx-4 p-6">
          <div class="w-10 h-10 bg-amber-100 rounded-xl flex items-center justify-center mb-4">
            <svg class="w-5 h-5 text-amber-600" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M12 9v2m0 4h.01m-6.938 4h13.856c1.54 0 2.502-1.667 1.732-3L13.732 4c-.77-1.333-2.694-1.333-3.464 0L3.34 16c-.77 1.333.192 3 1.732 3z"/>
            </svg>
          </div>
          <h2 class="text-sm font-semibold text-gray-900 mb-1">저장하지 않고 나가시겠어요?</h2>
          <p class="text-xs text-gray-500 leading-relaxed">저장되지 않은 변경 사항이 사라집니다.</p>
          <div class="flex gap-2 mt-5">
            <button
              @click="showExitDialog = false"
              class="flex-1 px-4 py-2 text-xs font-medium border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
            >계속 편집</button>
            <button
              @click="confirmExit"
              class="flex-1 px-4 py-2 text-xs font-semibold bg-red-500 text-white rounded-lg hover:bg-red-600 cursor-pointer transition-colors"
            >나가기</button>
          </div>
        </div>
      </div>
    </Teleport>

  </div>
</template>