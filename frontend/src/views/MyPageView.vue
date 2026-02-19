<script setup>
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import PostCard from '../components/post/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'

const auth = useAuthStore()

const activeTab = ref('PUBLISHED')
const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(0)

const tabs = [
  { value: 'PUBLISHED', label: '발행됨' },
  { value: 'DRAFT', label: '임시저장' },
  { value: 'HIDDEN', label: '숨김' },
]

async function fetchPosts() {
  loading.value = true
  try {
    const res = await postApi.search({
      postStatuses: activeTab.value,
      page: currentPage.value - 1,
      size: 10,
      sort: 'createdAt,desc',
    })
    posts.value = res.data.data || []
    const totalCount = parseInt(res.headers['x-total-count'] || '0')
    totalPages.value = Math.ceil(totalCount / 10)
  } finally {
    loading.value = false
  }
}

function switchTab(tab) {
  activeTab.value = tab
  currentPage.value = 1
}

watch([activeTab, currentPage], fetchPosts)
onMounted(fetchPosts)
</script>

<template>
  <div>
    <!-- 프로필 헤더 -->
    <div class="mb-8 flex items-center gap-4">
      <div class="w-16 h-16 bg-gray-200 rounded-full flex items-center justify-center text-2xl font-bold text-gray-600 shrink-0">
        {{ auth.user?.nickname?.charAt(0)?.toUpperCase() || 'U' }}
      </div>
      <div>
        <h1 class="text-2xl font-bold text-gray-900">{{ auth.user?.nickname || '사용자' }}</h1>
        <p class="text-gray-500 text-sm">{{ auth.user?.email }}</p>
      </div>
    </div>

    <!-- 탭 -->
    <div class="flex gap-1 border-b border-gray-200 mb-6">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        @click="switchTab(tab.value)"
        :class="[
          'px-4 py-2.5 text-sm font-medium transition cursor-pointer',
          activeTab === tab.value
            ? 'border-b-2 border-gray-900 text-gray-900'
            : 'text-gray-500 hover:text-gray-700'
        ]"
      >
        {{ tab.label }}
      </button>
    </div>

    <div v-if="loading" class="text-center py-20 text-gray-400">불러오는 중...</div>
    <div v-else-if="posts.length === 0" class="text-center py-20 text-gray-400">
      게시글이 없습니다.
    </div>
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <PostCard v-for="post in posts" :key="post.postId" :post="post" />
    </div>

    <Pagination v-model:currentPage="currentPage" :totalPages="totalPages" />
  </div>
</template>
