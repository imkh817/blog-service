<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postApi } from '../api/postApi'
import BlogHeader from '../components/common/BlogHeader.vue'
import PostCard from '../components/post/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'

const route  = useRoute()
const router = useRouter()

const posts       = ref([])
const loading     = ref(false)
const currentPage = ref(1)
const totalPages  = ref(0)

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

function handleSearch(keyword) {
  router.push({ name: 'Home', query: { keyword } })
}

function handleTagSelect(tag) {
  router.push({ name: 'TagPosts', params: { tagName: tag } })
}
</script>

<template>
  <div>
    <BlogHeader @search="handleSearch" @tag-select="handleTagSelect" />

    <div class="tag-page">
      <h1 class="tag-title">
        <span class="tag-hash">#</span>{{ route.params.tagName }}
      </h1>

      <div v-if="loading" class="state-wrap">불러오는 중...</div>
      <div v-else-if="posts.length === 0" class="state-wrap">
        해당 태그의 게시글이 없습니다.
      </div>
      <div v-else class="posts-grid">
        <PostCard v-for="post in posts" :key="post.postId" :post="post" />
      </div>

      <Pagination v-model:currentPage="currentPage" :totalPages="totalPages" />
    </div>
  </div>
</template>

<style scoped>
.tag-page {
  max-width: 72rem;
  margin: 0 auto;
  padding: 2.5rem 2rem 4rem;
}

.tag-title {
  font-size: 1.6rem;
  font-weight: 800;
  color: #111827;
  letter-spacing: -0.02em;
  margin: 0 0 1.75rem;
}
.tag-hash {
  color: #9ca3af;
  margin-right: 1px;
}

.state-wrap {
  text-align: center;
  padding: 5rem 0;
  color: #9ca3af;
  font-size: 0.9rem;
}

.posts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem;
}

@media (max-width: 640px) {
  .posts-grid { grid-template-columns: 1fr; }
  .tag-page   { padding: 1.5rem 1rem 3rem; }
}
</style>