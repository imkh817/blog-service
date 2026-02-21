<script setup>
import { ref, onMounted, watch, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import PostListItem from '../components/post/PostListItem.vue'
import Pagination from '../components/common/Pagination.vue'

const route  = useRoute()
const router = useRouter()
const auth   = useAuthStore()

const posts       = ref([])
const loading     = ref(false)
const currentPage = ref(1)
const totalPages  = ref(0)
const allTags     = ref([])
const showDropdown = ref(false)

const keyword   = ref(route.query.keyword || '')
const tagFilter = ref(route.query.tag || '')
const sortType  = ref('CREATED_AT')

const popularTags = computed(() => allTags.value.slice(0, 12))

async function loadPopularTags() {
  try {
    const res = await postApi.search({ postStatuses: 'PUBLISHED', page: 0, size: 50, sort: 'createdAt,desc' })
    const all = res.data.data || []
    const tagCount = {}
    for (const post of all) {
      for (const tag of (post.tags || [])) {
        tagCount[tag] = (tagCount[tag] || 0) + 1
      }
    }
    allTags.value = Object.entries(tagCount)
      .sort((a, b) => b[1] - a[1])
      .map(([name]) => name)
  } catch {
    allTags.value = []
  }
}

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

function selectTag(tag) {
  tagFilter.value = tagFilter.value === tag ? '' : tag
  currentPage.value = 1
  search()
}

function handleLogout() {
  auth.logout()
  showDropdown.value = false
  router.push({ name: 'Home' })
}

function onOutsideClick(e) {
  if (!e.target.closest('.avatar-wrap')) showDropdown.value = false
}

watch(showDropdown, (val) => {
  if (val) document.addEventListener('click', onOutsideClick)
  else document.removeEventListener('click', onOutsideClick)
})

watch([currentPage, sortType], search)
watch(() => route.query.keyword, (val) => {
  if (val !== undefined) { keyword.value = val || ''; search() }
})

onMounted(() => { search(); loadPopularTags() })
</script>

<template>
  <div class="search-page">

    <!-- ── Dark Header ── -->
    <div class="search-header">
      <div class="search-header-inner">

        <!-- Brand -->
        <router-link to="/" class="brand">blog</router-link>

        <!-- Search bar -->
        <form class="header-search" @submit.prevent="handleSearch">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
          <input
            v-model="keyword"
            type="text"
            placeholder="검색어를 입력하세요..."
            class="search-input"
            autofocus
          />
          <button v-if="keyword" type="button" class="search-clear" @click="keyword = ''">
            <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>
          </button>
          <button type="submit" class="search-submit">
            <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
          </button>
        </form>

        <!-- Auth actions -->
        <div class="header-actions">
          <template v-if="auth.isLoggedIn">
            <button class="write-btn" @click="router.push({ name: 'PostWrite' })">새 글 작성</button>
            <div class="avatar-wrap">
              <button class="avatar-btn" @click.stop="showDropdown = !showDropdown">
                {{ auth.user?.nickname?.charAt(0)?.toUpperCase() || 'U' }}
              </button>
              <Transition name="dropdown">
                <div v-if="showDropdown" class="dropdown">
                  <div class="dropdown-header">
                    <p class="dropdown-name">{{ auth.user?.nickname }}</p>
                    <p class="dropdown-email">{{ auth.user?.email }}</p>
                  </div>
                  <div class="dropdown-divider"></div>
                  <router-link to="/my" class="dropdown-item" @click="showDropdown = false">
                    <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 9a3 3 0 100-6 3 3 0 000 6zm-7 9a7 7 0 1114 0H3z" clip-rule="evenodd"/></svg>
                    마이페이지
                  </router-link>
                  <button class="dropdown-item dropdown-item--danger" @click="handleLogout">
                    <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M3 3a1 1 0 00-1 1v12a1 1 0 102 0V4a1 1 0 00-1-1zm10.293 9.293a1 1 0 001.414 1.414l3-3a1 1 0 000-1.414l-3-3a1 1 0 10-1.414 1.414L14.586 9H7a1 1 0 100 2h7.586l-1.293 1.293z" clip-rule="evenodd"/></svg>
                    로그아웃
                  </button>
                </div>
              </Transition>
            </div>
          </template>
          <template v-else>
            <router-link to="/login" class="ghost-btn">로그인</router-link>
            <router-link to="/signup" class="cta-btn">시작하기</router-link>
          </template>
        </div>

      </div>

      <!-- Tag chips -->
      <div class="tag-row">
        <div class="tag-row-inner">
          <button
            v-for="tag in popularTags"
            :key="tag"
            class="tag-chip"
            :class="{ active: tagFilter === tag }"
            @click="selectTag(tag)"
          >{{ tag }}</button>
        </div>
      </div>
    </div>

    <!-- ── Results section ── -->
    <div class="results-content">
      <div class="results-inner">

        <!-- Results bar -->
        <div class="results-bar">
          <p class="results-label">
            <template v-if="keyword">
              <span class="results-keyword">"{{ keyword }}"</span> 검색 결과
            </template>
            <template v-else-if="tagFilter">
              <span class="results-keyword">#{{ tagFilter }}</span> 태그 결과
            </template>
            <template v-else>전체 검색</template>
          </p>
          <select v-model="sortType" class="sort-select">
            <option value="CREATED_AT">최신순</option>
            <option value="VIEW_COUNT">조회순</option>
          </select>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="state-box">
          <div class="loading-dots"><span></span><span></span><span></span></div>
          <p>검색 중...</p>
        </div>

        <!-- Empty -->
        <div v-else-if="posts.length === 0" class="state-box">
          <span class="state-emoji">🔍</span>
          <p>검색 결과가 없습니다.</p>
          <button class="reset-btn" @click="keyword = ''; tagFilter = ''; handleSearch()">초기화</button>
        </div>

        <!-- List -->
        <div v-else class="post-list">
          <PostListItem v-for="post in posts" :key="post.postId" :post="post" />
        </div>

        <Pagination v-model:currentPage="currentPage" :totalPages="totalPages" />
      </div>
    </div>

  </div>
</template>

<style scoped>
.search-page {
  display: flex;
  flex-direction: column;
  flex: 1;
}

/* ── Dark Header ── */
.search-header {
  background: #18181b;
  position: sticky;
  top: 0;
  z-index: 50;
}

.search-header-inner {
  max-width: 72rem;
  margin: 0 auto;
  padding: 0 1.5rem;
  height: 3.5rem;
  display: flex;
  align-items: center;
  gap: 1rem;
}

.brand {
  font-size: 0.95rem;
  font-weight: 700;
  color: #fff;
  text-decoration: none;
  letter-spacing: -0.02em;
  flex-shrink: 0;
}

.header-search {
  flex: 1;
  display: flex;
  align-items: center;
  background: #27272a;
  border: 1.5px solid #3f3f46;
  border-radius: 8px;
  padding: 0 0.75rem;
  transition: border-color 0.2s;
}
.header-search:focus-within {
  border-color: #71717a;
  background: #2d2d30;
}

.search-icon {
  width: 15px;
  height: 15px;
  color: #71717a;
  flex-shrink: 0;
}

.search-input {
  flex: 1;
  padding: 0.55rem 0.6rem;
  background: transparent;
  border: none;
  outline: none;
  font-size: 0.875rem;
  color: #f4f4f5;
  font-family: inherit;
}
.search-input::placeholder { color: #52525b; }

.search-clear {
  width: 18px; height: 18px;
  display: flex; align-items: center; justify-content: center;
  background: none; border: none; cursor: pointer;
  color: #71717a; padding: 0; flex-shrink: 0;
}
.search-clear svg { width: 14px; height: 14px; }
.search-clear:hover { color: #a1a1aa; }

.search-submit {
  display: flex;
  align-items: center;
  justify-content: center;
  width: 26px; height: 26px;
  background: #fff;
  color: #18181b;
  border: none;
  border-radius: 5px;
  cursor: pointer;
  flex-shrink: 0;
  margin: 0 0.1rem;
  transition: opacity 0.15s;
}
.search-submit svg { width: 13px; height: 13px; }
.search-submit:hover { opacity: 0.85; }

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  flex-shrink: 0;
}

.write-btn {
  padding: 0.4rem 0.85rem;
  background: none;
  border: 1.5px solid #3f3f46;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  color: #d4d4d8;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}
.write-btn:hover { border-color: #71717a; color: #fff; }

.avatar-wrap { position: relative; }
.avatar-btn {
  width: 30px; height: 30px;
  border-radius: 50%;
  background: #3f3f46;
  color: #fff;
  font-size: 0.75rem; font-weight: 700;
  border: none; cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: background 0.15s;
}
.avatar-btn:hover { background: #52525b; }

.dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 200px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e4e4e7;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  padding: 0.4rem;
}
.dropdown-header { padding: 0.5rem 0.6rem 0.4rem; }
.dropdown-name { font-size: 0.85rem; font-weight: 700; color: #18181b; margin: 0 0 0.1rem; }
.dropdown-email { font-size: 0.72rem; color: #a1a1aa; margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dropdown-divider { height: 1px; background: #f4f4f5; margin: 0.3rem 0; }
.dropdown-item {
  display: flex; align-items: center; gap: 0.5rem;
  width: 100%; padding: 0.5rem 0.6rem;
  border-radius: 6px; font-size: 0.82rem; color: #3f3f46;
  text-decoration: none; background: none; border: none;
  cursor: pointer; font-family: inherit; transition: background 0.15s; text-align: left;
}
.dropdown-item svg { width: 14px; height: 14px; flex-shrink: 0; color: #a1a1aa; }
.dropdown-item:hover { background: #f4f4f5; }
.dropdown-item--danger { color: #ef4444; }
.dropdown-item--danger svg { color: #ef4444; }
.dropdown-item--danger:hover { background: #fef2f2; }

.ghost-btn {
  padding: 0.4rem 0.8rem;
  color: #a1a1aa; font-size: 0.8rem; font-weight: 500;
  text-decoration: none; border-radius: 6px; transition: all 0.15s;
}
.ghost-btn:hover { color: #fff; background: #27272a; }
.cta-btn {
  padding: 0.4rem 0.85rem;
  background: #fff; color: #18181b;
  font-size: 0.8rem; font-weight: 700;
  text-decoration: none; border-radius: 6px; transition: opacity 0.15s;
}
.cta-btn:hover { opacity: 0.85; }

.tag-row {
  background: #111113;
  border-top: 1px solid #27272a;
  padding: 0.6rem 0;
}
.tag-row-inner {
  max-width: 72rem;
  margin: 0 auto;
  padding: 0 1.5rem;
  display: flex;
  align-items: center;
  gap: 0.4rem;
  flex-wrap: wrap;
}
.tag-chip {
  padding: 0.25rem 0.8rem;
  border-radius: 999px;
  font-size: 0.75rem;
  font-weight: 500;
  border: 1.5px solid #27272a;
  background: transparent;
  color: #71717a;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}
.tag-chip:hover { border-color: #52525b; color: #d4d4d8; }
.tag-chip.active { background: #fff; border-color: #fff; color: #18181b; }

/* ── Results ── */
.results-content { flex: 1; }
.results-inner {
  max-width: 72rem;
  margin: 0 auto;
  padding: 1.5rem;
}

.results-bar {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1rem;
  padding-bottom: 1rem;
  border-bottom: 1px solid #e4e4e7;
}
.results-label { font-size: 0.875rem; color: #71717a; margin: 0; }
.results-keyword { font-weight: 700; color: #18181b; }

.sort-select {
  padding: 0.3rem 0.7rem;
  background: #fff;
  border: 1.5px solid #e4e4e7;
  border-radius: 6px;
  font-size: 0.78rem;
  color: #3f3f46;
  font-family: inherit;
  cursor: pointer;
  outline: none;
}
.sort-select:focus { border-color: #a1a1aa; }

.state-box {
  text-align: center;
  padding: 4rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.75rem;
}
.state-emoji { font-size: 2.5rem; }
.state-box p { font-size: 0.9rem; color: #71717a; margin: 0; }

.loading-dots { display: flex; gap: 5px; margin-bottom: 0.5rem; }
.loading-dots span {
  width: 7px; height: 7px;
  background: #18181b;
  border-radius: 50%;
  animation: db 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.15s; }
.loading-dots span:nth-child(3) { animation-delay: 0.3s; }
@keyframes db {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.35; }
  40% { transform: scale(1); opacity: 1; }
}

.reset-btn {
  padding: 0.4rem 1rem;
  background: #18181b;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s;
}
.reset-btn:hover { background: #27272a; }

.post-list {
  background: #fff;
  border: 1px solid #e4e4e7;
  border-radius: 12px;
  overflow: hidden;
  margin-bottom: 1.5rem;
}

/* Dropdown transition */
.dropdown-enter-active, .dropdown-leave-active { transition: all 0.15s ease; }
.dropdown-enter-from, .dropdown-leave-to { opacity: 0; transform: translateY(-4px); }
</style>
