<script setup>
import { computed, ref, watch, nextTick } from 'vue'
import { renderMarkdown } from '../../utils/markdown'
import api from '../../api/index'

const props = defineProps({
  content: { type: String, default: '' },
})

const containerRef = ref(null)
const html = computed(() => renderMarkdown(props.content))
const previewCache = new Map()

let debounceTimer = null
watch(html, () => {
  clearTimeout(debounceTimer)
  debounceTimer = setTimeout(async () => {
    await nextTick()
    enhanceBookmarkCards()
  }, 500)
}, { immediate: true })

async function enhanceBookmarkCards() {
  if (!containerRef.value) return
  const cards = containerRef.value.querySelectorAll('[data-bookmark-url]')

  for (const card of cards) {
    const url = card.dataset.bookmarkUrl
    if (!url) continue

    try {
      if (!previewCache.has(url)) {
        const { data } = await api.get(`/link-preview?url=${encodeURIComponent(url)}`)
        previewCache.set(url, data.data)
      }
      const preview = previewCache.get(url)

      if (preview.title) {
        const el = card.querySelector('.bm-title')
        if (el) el.textContent = preview.title
      }
      if (preview.description) {
        const el = card.querySelector('.bm-description')
        if (el) el.textContent = preview.description
      }
      if (preview.image) {
        const img = card.querySelector('.bm-image')
        const wrapper = card.querySelector('.bm-thumbnail-wrapper')
        if (img && wrapper) {
          img.src = preview.image
          img.style.display = 'block'
          wrapper.style.display = 'block'
        }
      }
      if (preview.favicon) {
        const img = card.querySelector('.bm-favicon')
        if (img) {
          img.src = preview.favicon
          img.style.display = 'inline-block'
        }
      }
    } catch {
      // OG 로딩 실패 시 기본 hostname 카드 그대로 표시
    }
  }
}
</script>

<template>
  <div ref="containerRef" class="prose prose-gray max-w-none" v-html="html" />
</template>
