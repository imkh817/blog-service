<script setup>
import { ref, onMounted, watch } from 'vue'
import { useRoute } from 'vue-router'

function useDebounce(fn, delay) {
  let timer = null
  return (...args) => {
    clearTimeout(timer)
    timer = setTimeout(() => fn(...args), delay)
  }
}
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import BlogHeader from '../components/common/BlogHeader.vue'
import PostCard from '../components/post/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'

const router = useRouter()
const route  = useRoute()

const auth = useAuthStore()

const activeTab     = ref(route.query.tab || 'PUBLISHED')
const posts         = ref([])
const loading       = ref(false)
const currentPage   = ref(1)
const totalPages    = ref(0)
const totalCount    = ref(0)
const searchKeyword = ref('')

const tabs = [
  { value: 'PUBLISHED', label: '발행됨' },
  { value: 'DRAFT',     label: '임시저장' },
  { value: 'HIDDEN',    label: '숨김' },
]

async function fetchPosts() {
  loading.value = true
  try {
    const res = await postApi.getMyPosts({
      postStatuses: activeTab.value,
      keyword: searchKeyword.value || undefined,
      page: currentPage.value - 1,
      size: 10,
      sort: 'createdAt,desc',
    })
    posts.value = res.data.data || []
    const total = parseInt(res.headers['x-total-count'] || '0')
    totalCount.value = total
    totalPages.value = Math.ceil(total / 10)
  } finally {
    loading.value = false
  }
}

function switchTab(val) {
  activeTab.value = val
  currentPage.value = 1
}

const debouncedSearch = useDebounce((val) => {
  searchKeyword.value = val
  currentPage.value = 1
}, 300)

watch([activeTab, currentPage, searchKeyword], fetchPosts)
onMounted(fetchPosts)

function handleSearch(keyword) {
  router.push({ name: 'Home', query: { keyword } })
}

function handleTagSelect(tag) {
  router.push({ name: 'Home', query: { tag } })
}
</script>

<template>
  <div>
  <BlogHeader @search="handleSearch" @tag-select="handleTagSelect" />
  <div class="mypage">

    <!-- Profile header -->
    <div class="profile-header">
      <div class="profile-avatar">
        {{ auth.user?.nickname?.charAt(0)?.toUpperCase() || 'U' }}
      </div>
      <div class="profile-info">
        <h1 class="profile-name">{{ auth.user?.nickname || '사용자' }}</h1>
        <p class="profile-email">{{ auth.user?.email }}</p>
      </div>
      <router-link to="/write" class="write-shortcut">
        <svg viewBox="0 0 20 20" fill="currentColor"><path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"/></svg>
        새 글 쓰기
      </router-link>
    </div>

    <!-- Tab bar + Search -->
    <div class="tab-row">
      <div class="tab-bar">
        <button
          v-for="tab in tabs"
          :key="tab.value"
          @click="switchTab(tab.value)"
          class="tab-btn"
          :class="{ active: activeTab === tab.value }"
        >
          {{ tab.label }}
        </button>
      </div>

      <div class="search-box">
        <svg class="search-icon" viewBox="0 0 20 20" fill="none">
          <circle cx="8.5" cy="8.5" r="5.5" stroke="#94A3B8" stroke-width="1.6"/>
          <path d="M13 13l3.5 3.5" stroke="#94A3B8" stroke-width="1.6" stroke-linecap="round"/>
        </svg>
        <input
          type="text"
          class="search-input"
          placeholder="내 글 검색..."
          @input="debouncedSearch($event.target.value)"
        />
      </div>
    </div>

    <!-- Search result -->
    <p v-if="searchKeyword && !loading" class="search-result-text">
      총 <strong>{{ totalCount }}</strong>개의 포스트를 찾았습니다.
    </p>

    <!-- Content -->
    <div v-if="loading" class="loading-wrap">
      <div class="loading-dots"><span></span><span></span><span></span></div>
    </div>
    <div v-else-if="posts.length === 0" class="empty-state">
      <svg class="empty-icon" viewBox="0 0 48 48" fill="none">
        <rect x="8" y="6" width="32" height="36" rx="4" stroke="#CBD5E1" stroke-width="2.5"/>
        <path d="M16 16h16M16 22h16M16 28h10" stroke="#CBD5E1" stroke-width="2.5" stroke-linecap="round"/>
      </svg>
      <p>{{ searchKeyword ? '검색 결과가 없습니다.' : '게시글이 없습니다.' }}</p>
      <router-link v-if="!searchKeyword && activeTab === 'PUBLISHED'" to="/write" class="empty-cta">첫 글 써보기 →</router-link>
    </div>
    <div v-else class="posts-grid">
      <PostCard v-for="post in posts" :key="post.postId" :post="post" />
    </div>

    <Pagination v-model:currentPage="currentPage" :totalPages="totalPages" />
  </div>
  </div>
</template>

<style scoped>
.mypage {
  max-width: 72rem;
  width: 100%;
  margin: 0 auto;
  padding: 2rem 1.5rem;
}

/* ── Profile header ── */
.profile-header {
  display: flex;
  align-items: center;
  gap: 1rem;
  margin-bottom: 2rem;
  padding: 1.5rem;
  background: white;
  border-radius: 16px;
  border: 1px solid #E8EDF8;
  box-shadow: 0 2px 12px rgba(15,23,42,0.04);
}
.profile-avatar {
  width: 56px; height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  font-size: 1.4rem;
  font-weight: 800;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.profile-info { flex: 1; min-width: 0; }
.profile-name {
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 1.2rem;
  font-weight: 800;
  color: #0F172A;
  margin: 0 0 0.2rem;
  letter-spacing: -0.01em;
}
.profile-email {
  font-size: 0.8rem;
  color: #94A3B8;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.write-shortcut {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.55rem 1.1rem;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  text-decoration: none;
  border-radius: 10px;
  font-size: 0.82rem;
  font-weight: 600;
  white-space: nowrap;
  transition: all 0.2s;
  box-shadow: 0 2px 10px rgba(71,118,230,0.28);
}
.write-shortcut svg { width: 14px; height: 14px; }
.write-shortcut:hover { opacity: 0.9; transform: translateY(-1px); }

/* ── Tab row ── */
.tab-row {
  display: flex;
  align-items: flex-end;
  justify-content: space-between;
  margin-bottom: 1.75rem;
  border-bottom: 1.5px solid #E8EDF8;
  gap: 1rem;
}
.tab-bar {
  display: flex;
  gap: 0;
  flex-shrink: 0;
}
.tab-btn {
  padding: 0.7rem 1.25rem;
  border: none;
  border-bottom: 2px solid transparent;
  background: none;
  font-size: 0.875rem;
  font-weight: 500;
  color: #94A3B8;
  cursor: pointer;
  font-family: 'Noto Sans KR', sans-serif;
  transition: all 0.18s;
  margin-bottom: -1.5px;
  white-space: nowrap;
}
.tab-btn:hover { color: #334155; }
.tab-btn.active {
  color: #4776E6;
  font-weight: 700;
  border-bottom-color: #4776E6;
}

/* ── Search ── */
.search-result-text {
  font-size: 0.82rem;
  color: #64748B;
  margin: 0 0 1.25rem;
}
.search-result-text strong {
  color: #4776E6;
  font-weight: 700;
}
.search-box {
  position: relative;
  width: 220px;
}
.search-icon {
  position: absolute;
  left: 0.75rem;
  top: 50%;
  transform: translateY(-50%);
  width: 15px;
  height: 15px;
  pointer-events: none;
}
.search-input {
  width: 100%;
  padding: 0.5rem 0.8rem 0.5rem 2.1rem;
  border: 1.5px solid #E8EDF8;
  border-radius: 8px;
  font-size: 0.82rem;
  font-family: 'Noto Sans KR', sans-serif;
  color: #0F172A;
  background: white;
  outline: none;
  transition: border-color 0.18s, box-shadow 0.18s;
  box-sizing: border-box;
}
.search-input::placeholder { color: #CBD5E1; }
.search-input:focus {
  border-color: #4776E6;
  box-shadow: 0 0 0 3px rgba(71,118,230,0.12);
}

/* ── States ── */
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 4rem 0;
}
.loading-dots {
  display: flex;
  gap: 6px;
}
.loading-dots span {
  width: 8px; height: 8px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  border-radius: 50%;
  animation: db 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.15s; }
.loading-dots span:nth-child(3) { animation-delay: 0.3s; }
@keyframes db {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.empty-state {
  text-align: center;
  padding: 4rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}
.empty-icon { width: 48px; height: 48px; margin-bottom: 0.25rem; }
.empty-state p { font-size: 0.9rem; color: #64748B; margin: 0; font-weight: 500; }
.empty-cta {
  margin-top: 0.5rem;
  padding: 0.55rem 1.25rem;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  text-decoration: none;
  border-radius: 10px;
  font-size: 0.82rem;
  font-weight: 600;
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
  .profile-header { flex-wrap: wrap; }
}
</style>
