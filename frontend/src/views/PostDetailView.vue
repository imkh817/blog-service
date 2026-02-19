<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import PostContent from '../components/post/PostContent.vue'
import PostStatusBadge from '../components/post/PostStatusBadge.vue'
import CommentList from '../components/comment/CommentList.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const post = ref(null)
const loading = ref(true)

const isAuthor = () => auth.user?.id === post.value?.authorId

const readTime = computed(() => {
  if (!post.value?.content) return 1
  const words = post.value.content.split(/\s+/).length
  return Math.max(1, Math.round(words / 200))
})

const formattedDate = computed(() => {
  if (!post.value?.createdAt) return ''
  const d = new Date(post.value.createdAt)
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일`
})

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

    <!-- Back button -->
    <button
      @click="router.back()"
      class="flex items-center gap-1.5 text-gray-400 hover:text-gray-700 text-sm mb-8 cursor-pointer transition"
    >
      <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
        <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
      </svg>
      돌아가기
    </button>

    <!-- Title & Meta -->
    <div class="mb-6">
      <div v-if="post.postStatus !== 'PUBLISHED'" class="mb-3">
        <PostStatusBadge :status="post.postStatus" />
      </div>

      <h1 class="text-4xl font-bold text-gray-900 leading-tight mb-5">{{ post.title }}</h1>

      <!-- Author row -->
      <div class="flex items-center gap-3 mb-5">
        <div class="w-9 h-9 bg-gray-300 rounded-full flex items-center justify-center text-sm font-bold text-gray-600 shrink-0">
          {{ post.authorNickname?.charAt(0)?.toUpperCase() || 'U' }}
        </div>
        <div>
          <p class="text-sm font-medium text-gray-900">{{ post.authorNickname || '익명' }}</p>
          <p class="text-xs text-gray-400">{{ formattedDate }} · {{ readTime }}분 읽기</p>
        </div>
      </div>

      <!-- Tags - outline pill style -->
      <div v-if="post.tags?.length" class="flex flex-wrap gap-2 mb-5">
        <router-link
          v-for="tag in post.tags"
          :key="tag"
          :to="{ name: 'TagPosts', params: { tagName: tag } }"
          class="px-3 py-1 border border-gray-300 text-gray-500 text-xs rounded-full hover:border-gray-500 hover:text-gray-700 transition no-underline"
        >
          {{ tag }}
        </router-link>
      </div>

      <hr class="border-gray-200" />
    </div>

    <!-- Content -->
    <div class="mb-10">
      <PostContent :content="post.content" />
    </div>

    <!-- Like / Share -->
    <div class="flex items-center gap-3 py-6 border-t border-b border-gray-200 mb-8">
      <button class="flex items-center gap-2 px-5 py-2 border border-gray-300 rounded-full text-sm text-gray-600 hover:border-gray-400 hover:text-gray-800 transition cursor-pointer">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z"/>
        </svg>
        좋아요 {{ post.viewCount || 0 }}
      </button>
      <button class="flex items-center gap-2 px-5 py-2 border border-gray-300 rounded-full text-sm text-gray-600 hover:border-gray-400 hover:text-gray-800 transition cursor-pointer">
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M8.684 13.342C8.886 12.938 9 12.482 9 12c0-.482-.114-.938-.316-1.342m0 2.684a3 3 0 110-2.684m0 2.684l6.632 3.316m-6.632-6l6.632-3.316m0 0a3 3 0 105.367-2.684 3 3 0 00-5.367 2.684zm0 9.316a3 3 0 105.368 2.684 3 3 0 00-5.368-2.684z"/>
        </svg>
        공유
      </button>
    </div>

    <!-- Author card -->
    <div class="bg-white rounded-xl p-6 mb-8 flex items-start gap-4">
      <div class="w-14 h-14 bg-gray-200 rounded-full flex items-center justify-center text-xl font-bold text-gray-600 shrink-0">
        {{ post.authorNickname?.charAt(0)?.toUpperCase() || 'U' }}
      </div>
      <div>
        <p class="font-semibold text-gray-900 text-base mb-0.5">{{ post.authorNickname || '익명' }}</p>
        <p class="text-sm text-gray-400 mb-2">@user{{ post.authorId }}</p>
        <p class="text-sm text-gray-500">글 쓰는 것을 좋아합니다.</p>
      </div>
    </div>

    <!-- Author actions -->
    <div v-if="isAuthor()" class="flex gap-3 mb-8">
      <router-link
        :to="{ name: 'PostEdit', params: { id: post.postId } }"
        class="px-4 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-100 transition no-underline"
      >
        수정
      </router-link>
      <button
        v-if="post.postStatus === 'HIDDEN' || post.postStatus === 'DRAFT'"
        @click="handlePublish"
        class="px-4 py-2 bg-green-600 text-white text-sm rounded-lg hover:bg-green-500 transition cursor-pointer"
      >
        발행
      </button>
      <button
        v-if="post.postStatus === 'PUBLISHED'"
        @click="handleHide"
        class="px-4 py-2 border border-gray-300 text-gray-700 text-sm rounded-lg hover:bg-gray-100 transition cursor-pointer"
      >
        숨기기
      </button>
      <button
        @click="handleDelete"
        class="px-4 py-2 bg-red-500 text-white text-sm rounded-lg hover:bg-red-400 transition cursor-pointer"
      >
        삭제
      </button>
    </div>

    <CommentList :post-id="post.postId" />
  </article>
</template>
