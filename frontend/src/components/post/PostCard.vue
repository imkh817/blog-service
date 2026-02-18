<script setup>
import TagBadge from '../common/TagBadge.vue'
import PostStatusBadge from './PostStatusBadge.vue'

defineProps({
  post: { type: Object, required: true },
})

function truncate(text, length = 150) {
  if (!text) return ''
  return text.length > length ? text.substring(0, length) + '...' : text
}
</script>

<template>
  <router-link
    :to="{ name: 'PostDetail', params: { id: post.postId } }"
    class="block bg-white rounded-xl border border-gray-200 p-6 hover:shadow-md transition no-underline"
  >
    <div class="flex items-center gap-2 mb-3">
      <PostStatusBadge v-if="post.postStatus !== 'PUBLISHED'" :status="post.postStatus" />
      <span class="text-xs text-gray-400">조회 {{ post.viewCount }}</span>
    </div>
    <h2 class="text-lg font-semibold text-gray-900 mb-2 line-clamp-2">
      {{ post.title }}
    </h2>
    <p class="text-sm text-gray-600 mb-4 line-clamp-3">
      {{ truncate(post.content) }}
    </p>
    <div class="flex flex-wrap gap-2">
      <TagBadge v-for="tag in post.tags" :key="tag" :name="tag" :clickable="false" />
    </div>
  </router-link>
</template>
