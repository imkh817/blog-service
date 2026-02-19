<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'
import { postApi } from '../api/postApi'
import PostCard from '../components/post/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'

const route = useRoute()
const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(0)

async function fetchPosts() {
  loading.value = true
  try {
    const res = await postApi.search({
      tagNames: route.params.tagName,
      postStatuses: 'PUBLISHED',
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

watch(currentPage, fetchPosts)
watch(() => route.params.tagName, () => {
  currentPage.value = 1
  fetchPosts()
})

onMounted(fetchPosts)
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold text-gray-900 mb-6">
      <span class="text-gray-500">#</span>{{ route.params.tagName }}
    </h1>

    <div v-if="loading" class="text-center py-20 text-gray-400">불러오는 중...</div>
    <div v-else-if="posts.length === 0" class="text-center py-20 text-gray-400">
      해당 태그의 게시글이 없습니다.
    </div>
    <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-4">
      <PostCard v-for="post in posts" :key="post.postId" :post="post" />
    </div>

    <Pagination v-model:currentPage="currentPage" :totalPages="totalPages" />
  </div>
</template>
