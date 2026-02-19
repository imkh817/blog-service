<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usePostStore } from '../stores/post'
import { postApi } from '../api/postApi'
import PostCard from '../components/post/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'

const postStore = usePostStore()
const currentPage = ref(1)
const selectedTag = ref('')
const trendingPosts = ref([])

const popularTags = computed(() => {
  const tagCount = {}
  for (const post of postStore.posts) {
    for (const tag of (post.tags || [])) {
      tagCount[tag] = (tagCount[tag] || 0) + 1
    }
  }
  return Object.entries(tagCount)
    .sort((a, b) => b[1] - a[1])
    .slice(0, 8)
    .map(([name]) => name)
})

async function loadPosts() {
  const params = {
    postStatuses: 'PUBLISHED',
    page: currentPage.value - 1,
    size: 12,
    sort: 'createdAt,desc',
  }
  if (selectedTag.value) {
    params.tagNames = selectedTag.value
  }
  await postStore.fetchPosts(params)
}

async function loadTrending() {
  try {
    const res = await postApi.search({
      postStatuses: 'PUBLISHED',
      page: 0,
      size: 4,
      sort: 'viewCount,desc',
    })
    trendingPosts.value = res.data.data || []
  } catch {
    trendingPosts.value = []
  }
}

function selectTag(tag) {
  selectedTag.value = tag
  currentPage.value = 1
}

watch([currentPage, selectedTag], loadPosts)
onMounted(() => {
  loadPosts()
  loadTrending()
})
</script>

<template>
  <div>
    <!-- 카테고리 탭 -->
    <div class="flex items-center gap-1 flex-wrap mb-8 pt-1">
      <button
        @click="selectTag('')"
        :class="[
          'px-4 py-1.5 text-sm font-medium rounded-full transition cursor-pointer',
          selectedTag === ''
            ? 'bg-gray-900 text-white'
            : 'text-gray-600 hover:text-gray-900 hover:bg-gray-200'
        ]"
      >
        전체
      </button>
      <button
        v-for="tag in popularTags"
        :key="tag"
        @click="selectTag(tag)"
        :class="[
          'px-4 py-1.5 text-sm font-medium rounded-full transition cursor-pointer',
          selectedTag === tag
            ? 'bg-gray-900 text-white'
            : 'text-gray-600 hover:text-gray-900 hover:bg-gray-200'
        ]"
      >
        {{ tag }}
      </button>
    </div>

    <div class="grid grid-cols-1 lg:grid-cols-[1fr_280px] gap-8">
      <!-- 메인 콘텐츠 -->
      <div>
        <div v-if="postStore.loading" class="text-center py-20 text-gray-400">
          불러오는 중...
        </div>
        <div v-else-if="postStore.posts.length === 0" class="text-center py-20 text-gray-400">
          게시글이 없습니다.
        </div>
        <div v-else class="grid grid-cols-1 sm:grid-cols-2 gap-4">
          <PostCard v-for="post in postStore.posts" :key="post.postId" :post="post" />
        </div>

        <Pagination
          v-model:currentPage="currentPage"
          :totalPages="postStore.totalPages"
        />
      </div>

      <!-- 트렌딩 사이드바 -->
      <aside class="hidden lg:block">
        <div class="bg-white rounded-xl p-5 sticky top-24">
          <div class="flex items-center gap-2 mb-4">
            <svg class="w-4 h-4 text-teal-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
              <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                d="M13 7h8m0 0v8m0-8l-8 8-4-4-6 6" />
            </svg>
            <h2 class="text-sm font-bold text-gray-900">트렌딩 포스트</h2>
          </div>
          <div v-if="trendingPosts.length === 0" class="text-sm text-gray-400">
            게시글이 없습니다.
          </div>
          <ol v-else class="space-y-4">
            <li
              v-for="(post, index) in trendingPosts"
              :key="post.postId"
              class="flex items-start gap-3"
            >
              <span class="text-lg font-bold text-blue-400 w-5 shrink-0 leading-tight">
                {{ index + 1 }}
              </span>
              <div class="min-w-0">
                <router-link
                  :to="{ name: 'PostDetail', params: { id: post.postId } }"
                  class="text-sm font-medium text-gray-900 hover:text-gray-600 transition no-underline line-clamp-2 block"
                >
                  {{ post.title }}
                </router-link>
                <p class="text-xs text-gray-500 mt-0.5 flex items-center gap-1">
                  {{ post.authorNickname }}
                  <span>·</span>
                  <svg class="w-3 h-3" fill="none" stroke="currentColor" viewBox="0 0 24 24">
                    <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
                      d="M4.318 6.318a4.5 4.5 0 000 6.364L12 20.364l7.682-7.682a4.5 4.5 0 00-6.364-6.364L12 7.636l-1.318-1.318a4.5 4.5 0 00-6.364 0z" />
                  </svg>
                  {{ post.viewCount }}
                </p>
              </div>
            </li>
          </ol>
        </div>
      </aside>
    </div>
  </div>
</template>
