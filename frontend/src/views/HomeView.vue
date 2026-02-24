<script setup>
import { ref, computed, onMounted, watch } from 'vue'
import { usePostStore } from '../stores/post'
import BlogHeader from '../components/common/BlogHeader.vue'
import PostCard from '../components/post/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'

const postStore = usePostStore()

const currentPage   = ref(1)
const selectedTag   = ref('')
const searchKeyword = ref('')
const showAllTags   = ref(false)
const sortType      = ref('createdAt,desc')

const SORT_OPTIONS = [
  { label: 'Latest',      value: 'createdAt,desc' },
  { label: 'Most Viewed', value: 'viewCount,desc'  },
  { label: 'Most Liked',  value: 'likeCount,desc'  },
]

const allTags = computed(() => {
  const tagCount = {}
  for (const post of postStore.posts) {
    for (const tag of (post.tags || [])) {
      tagCount[tag] = (tagCount[tag] || 0) + 1
    }
  }
  return Object.entries(tagCount)
    .sort((a, b) => b[1] - a[1])
    .map(([name]) => name)
})

const visibleTags = computed(() =>
  showAllTags.value ? allTags.value : allTags.value.slice(0, 16)
)

async function loadPosts() {
  const params = {
    postStatuses: 'PUBLISHED',
    page: currentPage.value - 1,
    size: 9,
    sort: sortType.value,
  }
  if (selectedTag.value)   params.tagNames = selectedTag.value
  if (searchKeyword.value) params.keyword  = searchKeyword.value
  await postStore.fetchPosts(params)
}

function selectTag(tag) {
  selectedTag.value   = selectedTag.value === tag ? '' : tag
  searchKeyword.value = ''
  currentPage.value   = 1
}

function handleSearch(keyword) {
  searchKeyword.value = keyword
  selectedTag.value   = ''
  currentPage.value   = 1
}

function setSort(value) {
  sortType.value = value
  currentPage.value = 1
}

watch([currentPage, selectedTag, searchKeyword, sortType], loadPosts)
onMounted(loadPosts)
</script>

<template>
  <div class="home-page">

    <!-- ── 공통 헤더 ── -->
    <BlogHeader
      :tags="allTags"
      @search="handleSearch"
      @tag-select="selectTag"
    />

    <!-- ── 히어로 ── -->
    <section class="hero">
      <h1 class="hero-title">{{ searchKeyword || selectedTag || 'blog' }}</h1>
      <p v-if="searchKeyword" class="hero-sub">"{{ searchKeyword }}" 검색 결과</p>
      <p v-else-if="selectedTag" class="hero-sub">{{ selectedTag }} 관련 포스트</p>
      <p v-else class="hero-sub">개발자들의 기술 경험과 인사이트를 공유합니다</p>
    </section>

    <!-- ── 메인 콘텐츠 ── -->
    <main class="home-main">
      <div class="content-inner">

        <!-- Tags 섹션 -->
        <section class="tags-section">
          <div class="tags-header">
            <h2 class="section-title">Tags</h2>
            <button
              v-if="allTags.length > 16"
              class="more-btn"
              @click="showAllTags = !showAllTags"
            >{{ showAllTags ? 'Less ←' : 'More →' }}</button>
          </div>
          <div class="tags-wrap">
            <button
              class="tag-pill"
              :class="{ active: selectedTag === '' }"
              @click="selectTag('')"
            >전체</button>
            <button
              v-for="tag in visibleTags"
              :key="tag"
              class="tag-pill"
              :class="{ active: selectedTag === tag }"
              @click="selectTag(tag)"
            >{{ tag }}</button>
          </div>
        </section>

        <!-- Posts 섹션 -->
        <section class="posts-section">
          <div class="posts-header">
            <h2 class="section-title">Posts</h2>
            <div class="sort-tabs">
              <button
                v-for="opt in SORT_OPTIONS"
                :key="opt.value"
                class="sort-tab"
                :class="{ active: sortType === opt.value }"
                @click="setSort(opt.value)"
              >{{ opt.label }}</button>
            </div>
          </div>

          <div v-if="postStore.loading" class="state-box">
            <div class="loading-dots"><span></span><span></span><span></span></div>
            <p>불러오는 중...</p>
          </div>

          <div v-else-if="postStore.posts.length === 0" class="state-box">
            <p class="state-title">게시글이 없습니다.</p>
            <p class="state-sub">첫 번째 글의 주인공이 되어보세요.</p>
          </div>

          <div v-else class="post-grid">
            <PostCard
              v-for="post in postStore.posts"
              :key="post.postId"
              :post="post"
            />
          </div>

          <Pagination v-model:currentPage="currentPage" :totalPages="postStore.totalPages" />
        </section>

      </div>
    </main>

  </div>
</template>

<style scoped>
.home-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  background: #fff;
}

/* ── 히어로 ── */
.hero {
  background: #fff;
  padding: 3.5rem 0 3rem;
  text-align: center;
  border-bottom: 1px solid #f0f0f0;
}
.hero-title {
  font-size: 2.75rem;
  font-weight: 800;
  color: #111827;
  letter-spacing: -0.03em;
  margin: 0 0 0.75rem;
  line-height: 1.2;
}
.hero-sub {
  font-size: 0.9375rem;
  color: #9ca3af;
  margin: 0;
}

/* ── 메인 ── */
.home-main {
  flex: 1;
  background: #f5f5f5;
}

.content-inner {
  max-width: 72rem;
  margin: 0 auto;
  padding: 2.5rem 2rem 4rem;
}

/* ── Tags 섹션 ── */
.tags-section {
  padding-bottom: 2rem;
  border-bottom: 1px solid #e5e7eb;
  margin-bottom: 2.5rem;
}
.tags-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 1.25rem;
}
.section-title {
  font-size: 1.25rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
  letter-spacing: -0.02em;
}
.more-btn {
  display: inline-flex;
  align-items: center;
  padding: 0.2rem 0.75rem;
  border: 1px solid #d1d5db;
  border-radius: 999px;
  font-size: 0.78rem;
  color: #6b7280;
  background: none;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
}
.more-btn:hover { border-color: #4776E6; color: #4776E6; }

.tags-wrap {
  display: flex;
  flex-wrap: wrap;
  gap: 0.5rem;
}
.tag-pill {
  display: inline-flex;
  align-items: center;
  padding: 0.3rem 0.9rem;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  font-size: 0.8125rem;
  color: #6b7280;
  background: #fff;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
  white-space: nowrap;
}
.tag-pill:hover { border-color: #4776E6; color: #4776E6; background: rgba(71,118,230,0.04); }
.tag-pill.active { border-color: #4776E6; color: #4776E6; background: rgba(71,118,230,0.08); font-weight: 500; }

/* ── Posts 섹션 ── */
.posts-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.5rem;
}

.sort-tabs {
  display: flex;
  align-items: center;
  gap: 0.25rem;
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  padding: 0.2rem;
}

.sort-tab {
  padding: 0.25rem 0.85rem;
  border-radius: 999px;
  font-size: 0.78rem;
  font-weight: 500;
  color: #6b7280;
  background: none;
  border: none;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
  white-space: nowrap;
}
.sort-tab:hover { color: #374151; }
.sort-tab.active {
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: #fff;
  font-weight: 600;
}

.post-grid {
  display: grid;
  grid-template-columns: repeat(3, 1fr);
  gap: 1.5rem;
  margin-top: 1.5rem;
}

/* ── 상태 ── */
.state-box { padding: 5rem 0; text-align: center; }
.state-title { font-size: 1rem; font-weight: 600; color: #374151; margin: 0 0 0.4rem; }
.state-sub { font-size: 0.875rem; color: #9ca3af; margin: 0; }

.loading-dots { display: flex; gap: 5px; justify-content: center; margin-bottom: 1rem; }
.loading-dots span {
  width: 6px; height: 6px;
  background: #4776E6;
  border-radius: 50%;
  animation: db 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.15s; }
.loading-dots span:nth-child(3) { animation-delay: 0.3s; }
@keyframes db {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.3; }
  40% { transform: scale(1); opacity: 1; }
}

@media (max-width: 1024px) { .post-grid { grid-template-columns: repeat(2, 1fr); } }
@media (max-width: 600px) { .post-grid { grid-template-columns: 1fr; } .hero-title { font-size: 2rem; } }
</style>
