<script setup>
import { ref, onMounted, watch } from 'vue'
import { usePostStore } from '../stores/post'
import PostCard from '../components/post/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'

const postStore = usePostStore()
const currentPage = ref(1)
const sortType = ref('CREATED_AT')

async function loadPosts() {
  await postStore.fetchPosts({
    postStatuses: 'PUBLISHED',
    page: currentPage.value - 1,
    size: 12,
    sort: sortType.value === 'VIEW_COUNT' ? 'viewCount,desc' : 'createdAt,desc',
  })
}

watch([currentPage, sortType], () => {
  loadPosts()
})

onMounted(loadPosts)
</script>

<template>
  <div>
    <div class="flex items-center justify-between mb-6">
      <h1 class="text-2xl font-bold">최신 글</h1>
      <select
        v-model="sortType"
        class="px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 cursor-pointer"
      >
        <option value="CREATED_AT">최신순</option>
        <option value="VIEW_COUNT">조회순</option>
      </select>
    </div>

    <div v-if="postStore.loading" class="text-center py-20 text-gray-400">
      불러오는 중...
    </div>
    <div v-else-if="postStore.posts.length === 0" class="text-center py-20 text-gray-400">
      게시글이 없습니다.
    </div>
    <div v-else class="grid grid-cols-1 md:grid-cols-2 lg:grid-cols-3 gap-6">
      <PostCard v-for="post in postStore.posts" :key="post.postId" :post="post" />
    </div>

    <Pagination
      v-model:currentPage="currentPage"
      :totalPages="postStore.totalPages"
    />
  </div>
</template>
