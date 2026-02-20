<script setup>
import { ref, onMounted, watch } from 'vue'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import PostCard from '../components/post/PostCard.vue'
import Pagination from '../components/common/Pagination.vue'

const auth = useAuthStore()

const activeTab   = ref('PUBLISHED')
const posts       = ref([])
const loading     = ref(false)
const currentPage = ref(1)
const totalPages  = ref(0)

const tabs = [
  { value: 'PUBLISHED', label: '발행됨',  icon: '✅' },
  { value: 'DRAFT',     label: '임시저장', icon: '📝' },
  { value: 'HIDDEN',    label: '숨김',    icon: '🔒' },
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
    const total = parseInt(res.headers['x-total-count'] || '0')
    totalPages.value = Math.ceil(total / 10)
  } finally {
    loading.value = false
  }
}

function switchTab(val) {
  activeTab.value = val
  currentPage.value = 1
}

watch([activeTab, currentPage], fetchPosts)
onMounted(fetchPosts)
</script>

<template>
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

    <!-- Tab bar -->
    <div class="tab-bar">
      <button
        v-for="tab in tabs"
        :key="tab.value"
        @click="switchTab(tab.value)"
        class="tab-btn"
        :class="{ active: activeTab === tab.value }"
      >
        <span class="tab-icon">{{ tab.icon }}</span>
        {{ tab.label }}
      </button>
    </div>

    <!-- Content -->
    <div v-if="loading" class="loading-wrap">
      <div class="loading-dots"><span></span><span></span><span></span></div>
    </div>
    <div v-else-if="posts.length === 0" class="empty-state">
      <span class="empty-emoji">{{ tabs.find(t => t.value === activeTab)?.icon }}</span>
      <p>게시글이 없습니다.</p>
      <router-link v-if="activeTab === 'PUBLISHED'" to="/write" class="empty-cta">첫 글 써보기 →</router-link>
    </div>
    <div v-else class="posts-grid">
      <PostCard v-for="post in posts" :key="post.postId" :post="post" />
    </div>

    <Pagination v-model:currentPage="currentPage" :totalPages="totalPages" />
  </div>
</template>

<style scoped>
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

/* ── Tab bar ── */
.tab-bar {
  display: flex;
  gap: 0.35rem;
  margin-bottom: 1.5rem;
  padding: 0.35rem;
  background: white;
  border-radius: 12px;
  border: 1px solid #E8EDF8;
  width: fit-content;
}
.tab-btn {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.5rem 1rem;
  border-radius: 8px;
  border: none;
  background: none;
  font-size: 0.82rem;
  font-weight: 500;
  color: #64748B;
  cursor: pointer;
  font-family: 'Noto Sans KR', sans-serif;
  transition: all 0.18s;
}
.tab-btn:hover { background: #F8F9FF; color: #0F172A; }
.tab-btn.active {
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  font-weight: 600;
}
.tab-icon { font-size: 0.9rem; }

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
.empty-emoji { font-size: 2.5rem; }
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
