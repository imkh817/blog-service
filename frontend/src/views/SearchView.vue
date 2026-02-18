<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { postApi } from '../api/postApi'
import PostCard from '../components/post/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'

const route = useRoute()
const router = useRouter()

const posts = ref([])
const loading = ref(false)
const currentPage = ref(1)
const totalPages = ref(0)

const keyword = ref(route.query.keyword || '')
const tagFilter = ref(route.query.tag || '')
const sortType = ref('CREATED_AT')

async function search() {
  loading.value = true
  try {
    const params = {
      postStatuses: 'PUBLISHED',
      page: currentPage.value - 1,
      size: 10,
      sort: sortType.value === 'VIEW_COUNT' ? 'viewCount,desc' : 'createdAt,desc',
    }
    if (keyword.value.trim()) params.keyword = keyword.value.trim()
    if (tagFilter.value.trim()) params.tagNames = tagFilter.value.trim()

    const res = await postApi.search(params)
    posts.value = res.data.data || []
    const totalCount = parseInt(res.headers['x-total-count'] || '0')
    totalPages.value = Math.ceil(totalCount / 10)
  } finally {
    loading.value = false
  }
}

function handleSearch() {
  currentPage.value = 1
  router.replace({
    query: {
      ...(keyword.value ? { keyword: keyword.value } : {}),
      ...(tagFilter.value ? { tag: tagFilter.value } : {}),
    },
  })
  search()
}

function resetFilters() {
  keyword.value = ''
  tagFilter.value = ''
  sortType.value = 'CREATED_AT'
  handleSearch()
}

watch([currentPage, sortType], search)
watch(() => route.query.keyword, (val) => {
  if (val) {
    keyword.value = val
    search()
  }
})

onMounted(search)
</script>

<template>
  <div>
    <h1 class="text-2xl font-bold mb-6">검색</h1>

    <div class="bg-white rounded-xl border border-gray-200 p-6 mb-6">
      <div class="grid grid-cols-1 md:grid-cols-3 gap-4">
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">키워드</label>
          <input
            v-model="keyword"
            type="text"
            placeholder="제목 또는 내용 검색"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
            @keyup.enter="handleSearch"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">태그</label>
          <input
            v-model="tagFilter"
            type="text"
            placeholder="태그 이름"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-900"
            @keyup.enter="handleSearch"
          />
        </div>
        <div>
          <label class="block text-sm font-medium text-gray-700 mb-1">정렬</label>
          <select
            v-model="sortType"
            class="w-full px-3 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 cursor-pointer"
          >
            <option value="CREATED_AT">최신순</option>
            <option value="VIEW_COUNT">조회순</option>
          </select>
        </div>
      </div>
      <div class="flex justify-end gap-3 mt-4">
        <button
          @click="resetFilters"
          class="px-4 py-2 text-sm border border-gray-300 rounded-lg hover:bg-gray-50 cursor-pointer"
        >
          초기화
        </button>
        <button
          @click="handleSearch"
          class="px-4 py-2 bg-gray-900 text-white text-sm rounded-lg hover:bg-gray-700 cursor-pointer"
        >
          검색
        </button>
      </div>
    </div>

    <div v-if="loading" class="text-center py-20 text-gray-400">검색 중...</div>
    <div v-else-if="posts.length === 0" class="text-center py-20 text-gray-400">
      검색 결과가 없습니다.
    </div>
    <div v-else class="space-y-4">
      <PostCard v-for="post in posts" :key="post.postId" :post="post" />
    </div>

    <Pagination v-model:currentPage="currentPage" :totalPages="totalPages" />
  </div>
</template>
