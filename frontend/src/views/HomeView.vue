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
  if (selectedTag.value) params.tagNames = selectedTag.value
  await postStore.fetchPosts(params)
}

async function loadTrending() {
  try {
    const res = await postApi.search({
      postStatuses: 'PUBLISHED',
      page: 0, size: 4,
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
onMounted(() => { loadPosts(); loadTrending() })
</script>

<template>
  <div class="home">
    <!-- Tag filter bar -->
    <div class="tag-bar">
      <button
        @click="selectTag('')"
        class="tag-btn"
        :class="{ active: selectedTag === '' }"
      >전체</button>
      <button
        v-for="tag in popularTags"
        :key="tag"
        @click="selectTag(tag)"
        class="tag-btn"
        :class="{ active: selectedTag === tag }"
      >{{ tag }}</button>
    </div>

    <!-- Main layout -->
    <div class="layout">
      <!-- Posts grid -->
      <div class="main-col">
        <div v-if="postStore.loading" class="empty-state">
          <div class="loading-dots">
            <span></span><span></span><span></span>
          </div>
          <p>불러오는 중...</p>
        </div>
        <div v-else-if="postStore.posts.length === 0" class="empty-state">
          <span class="empty-emoji">📭</span>
          <p>게시글이 없습니다.</p>
          <span class="empty-sub">첫 번째 글의 주인공이 되어보세요!</span>
        </div>
        <div v-else class="posts-grid">
          <PostCard v-for="post in postStore.posts" :key="post.postId" :post="post" />
        </div>
        <Pagination v-model:currentPage="currentPage" :totalPages="postStore.totalPages" />
      </div>

      <!-- Trending sidebar -->
      <aside class="sidebar">
        <div class="sidebar-card">
          <div class="sidebar-title">
            <svg viewBox="0 0 20 20" fill="currentColor" class="trend-icon">
              <path fill-rule="evenodd" d="M12 7a1 1 0 110-2h5a1 1 0 011 1v5a1 1 0 11-2 0V8.414l-4.293 4.293a1 1 0 01-1.414 0L8 10.414l-4.293 4.293a1 1 0 01-1.414-1.414l5-5a1 1 0 011.414 0L11 10.586 14.586 7H12z" clip-rule="evenodd"/>
            </svg>
            트렌딩
          </div>
          <div v-if="trendingPosts.length === 0" class="sidebar-empty">아직 게시글이 없어요.</div>
          <ol v-else class="trending-list">
            <li v-for="(post, idx) in trendingPosts" :key="post.postId" class="trending-item">
              <span class="trend-num" :class="`num-${idx}`">{{ idx + 1 }}</span>
              <div class="trend-info">
                <router-link
                  :to="{ name: 'PostDetail', params: { id: post.postId } }"
                  class="trend-title"
                >{{ post.title }}</router-link>
                <p class="trend-meta">
                  {{ post.authorNickname }}
                  <span>·</span>
                  <svg viewBox="0 0 20 20" fill="currentColor" class="eye-icon"><path d="M10 12a2 2 0 100-4 2 2 0 000 4z"/><path fill-rule="evenodd" d="M.458 10C1.732 5.943 5.522 3 10 3s8.268 2.943 9.542 7c-1.274 4.057-5.064 7-9.542 7S1.732 14.057.458 10zM14 10a4 4 0 11-8 0 4 4 0 018 0z" clip-rule="evenodd"/></svg>
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

<style scoped>
/* ── Tag bar ── */
.tag-bar {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  flex-wrap: wrap;
  margin-bottom: 1.75rem;
  padding-bottom: 1.25rem;
  border-bottom: 1px solid #E8EDF8;
}

.tag-btn {
  padding: 0.4rem 1rem;
  border-radius: 100px;
  font-size: 0.8rem;
  font-weight: 500;
  border: 1.5px solid #E8EDF8;
  background: white;
  color: #64748B;
  cursor: pointer;
  transition: all 0.18s ease;
  font-family: 'Noto Sans KR', sans-serif;
}
.tag-btn:hover {
  border-color: #C7D2FE;
  color: #4F46E5;
  background: #EEF2FF;
}
.tag-btn.active {
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  border-color: transparent;
  color: white;
  box-shadow: 0 2px 10px rgba(71,118,230,0.28);
}

/* ── Layout ── */
.layout {
  display: grid;
  grid-template-columns: 1fr 280px;
  gap: 2rem;
  align-items: start;
}

@media (max-width: 1024px) {
  .layout { grid-template-columns: 1fr; }
  .sidebar { display: none; }
}

.main-col { min-width: 0; }

/* ── Empty / loading state ── */
.empty-state {
  text-align: center;
  padding: 4rem 1rem;
  color: #94A3B8;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}
.empty-emoji { font-size: 2.5rem; }
.empty-state p { font-size: 0.9rem; color: #64748B; margin: 0; font-weight: 500; }
.empty-sub { font-size: 0.78rem; color: #CBD5E1; }

.loading-dots {
  display: flex;
  gap: 6px;
  justify-content: center;
  margin-bottom: 0.5rem;
}
.loading-dots span {
  width: 8px; height: 8px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  border-radius: 50%;
  animation: dot-bounce 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.15s; }
.loading-dots span:nth-child(3) { animation-delay: 0.3s; }
@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* ── Posts grid ── */
.posts-grid {
  display: grid;
  grid-template-columns: repeat(2, 1fr);
  gap: 1.25rem;
  margin-bottom: 2rem;
}
@media (max-width: 640px) {
  .posts-grid { grid-template-columns: 1fr; }
}

/* ── Sidebar ── */
.sidebar { position: sticky; top: 5.5rem; }

.sidebar-card {
  background: white;
  border-radius: 16px;
  border: 1px solid #E8EDF8;
  padding: 1.25rem;
  box-shadow: 0 2px 12px rgba(15,23,42,0.05);
}
.sidebar-title {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  font-size: 0.82rem;
  font-weight: 700;
  color: #0F172A;
  margin-bottom: 1rem;
}
.trend-icon {
  width: 14px; height: 14px;
  color: #4776E6;
}
.sidebar-empty {
  font-size: 0.8rem;
  color: #94A3B8;
  text-align: center;
  padding: 1rem 0;
}

.trending-list {
  list-style: none;
  padding: 0;
  margin: 0;
  display: flex;
  flex-direction: column;
  gap: 0.9rem;
}
.trending-item {
  display: flex;
  align-items: flex-start;
  gap: 0.65rem;
}
.trend-num {
  font-size: 0.9rem;
  font-weight: 800;
  width: 20px;
  flex-shrink: 0;
  line-height: 1.3;
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.num-0 { color: #4776E6; }
.num-1 { color: #8E54E9; }
.num-2 { color: #06B6D4; }
.num-3 { color: #10B981; }

.trend-info { min-width: 0; }
.trend-title {
  display: block;
  font-size: 0.82rem;
  font-weight: 600;
  color: #0F172A;
  text-decoration: none;
  line-height: 1.4;
  overflow: hidden;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  transition: color 0.15s;
}
.trend-title:hover { color: #4776E6; }
.trend-meta {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.7rem;
  color: #94A3B8;
  margin-top: 0.25rem;
}
.eye-icon { width: 11px; height: 11px; }
</style>