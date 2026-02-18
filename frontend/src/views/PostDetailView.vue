<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import PostContent from '../components/post/PostContent.vue'
import PostMeta from '../components/post/PostMeta.vue'
import PostStatusBadge from '../components/post/PostStatusBadge.vue'
import TagBadge from '../components/common/TagBadge.vue'
import CommentList from '../components/comment/CommentList.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const post = ref(null)
const loading = ref(true)

const isAuthor = () => auth.user?.id === post.value?.authorId

async function fetchPost() {
  loading.value = true
  try {
    const res = await postApi.getById(route.params.id)
    post.value = res.data.data
  } finally {
    loading.value = false
  }
}

async function handlePublish() {
  await postApi.publish(post.value.postId)
  await fetchPost()
}

async function handleHide() {
  await postApi.hide(post.value.postId)
  await fetchPost()
}

async function handleDelete() {
  if (!confirm('정말 삭제하시겠습니까?')) return
  await postApi.delete(post.value.postId)
  router.push({ name: 'Home' })
}

onMounted(fetchPost)
</script>

<template>
  <div v-if="loading" class="text-center py-20 text-gray-400">불러오는 중...</div>
  <article v-else-if="post" class="max-w-3xl mx-auto">
    <div class="mb-6">
      <div class="flex items-center gap-2 mb-3">
        <PostStatusBadge :status="post.postStatus" />
      </div>
      <h1 class="text-3xl font-bold mb-4">{{ post.title }}</h1>
      <PostMeta :view-count="post.viewCount" :created-at="post.createdAt" />
    </div>

    <div class="flex flex-wrap gap-2 mb-8">
      <TagBadge v-for="tag in post.tags" :key="tag" :name="tag" />
    </div>

    <PostContent :content="post.content" class="mb-8" />

    <div v-if="isAuthor()" class="flex gap-3 border-t border-gray-200 pt-6 mb-8">
      <router-link
        :to="{ name: 'PostEdit', params: { id: post.postId } }"
        class="px-4 py-2 border border-gray-300 text-sm rounded-lg hover:bg-gray-50 no-underline text-gray-700"
      >
        수정
      </router-link>
      <button
        v-if="post.postStatus === 'HIDDEN' || post.postStatus === 'DRAFT'"
        @click="handlePublish"
        class="px-4 py-2 bg-green-600 text-white text-sm rounded-lg hover:bg-green-700 cursor-pointer"
      >
        발행
      </button>
      <button
        v-if="post.postStatus === 'PUBLISHED'"
        @click="handleHide"
        class="px-4 py-2 border border-gray-300 text-sm rounded-lg hover:bg-gray-50 cursor-pointer"
      >
        숨기기
      </button>
      <button
        @click="handleDelete"
        class="px-4 py-2 bg-red-600 text-white text-sm rounded-lg hover:bg-red-700 cursor-pointer"
      >
        삭제
      </button>
    </div>

    <CommentList :post-id="post.postId" />
  </article>
</template>
