<script setup>
import { ref, watch, nextTick } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import NotificationBell from './NotificationBell.vue'

const props = defineProps({
  tags: { type: Array, default: () => [] },
})
const emit = defineEmits(['search', 'tag-select'])

const router = useRouter()
const auth   = useAuthStore()

const showDropdown = ref(false)
const showSearch   = ref(false)
const searchQuery  = ref('')
const searchInput  = ref(null)

function toggleSearch() {
  showSearch.value = !showSearch.value
  if (showSearch.value) {
    nextTick(() => searchInput.value?.focus())
  } else {
    searchQuery.value = ''
  }
}

function handleSearch() {
  emit('search', searchQuery.value.trim())
  showSearch.value = false
  searchQuery.value = ''
}

function handleTagClick(tag) {
  emit('tag-select', tag)
  showSearch.value = false
  searchQuery.value = ''
}

function handleLogout() {
  auth.logout()
  showDropdown.value = false
}

function onOutsideClick(e) {
  if (!e.target.closest('.avatar-wrap')) showDropdown.value = false
}

watch(showDropdown, (val) => {
  if (val) document.addEventListener('click', onOutsideClick)
  else document.removeEventListener('click', onOutsideClick)
})
</script>

<template>
  <header class="blog-header">
    <div class="header-inner">

      <!-- 로고 -->
      <router-link to="/" class="brand">blog</router-link>

      <!-- 우측 액션 -->
      <div class="header-actions">

        <!-- Search 버튼 -->
        <button class="search-btn" :class="{ active: showSearch }" @click="toggleSearch">
          <svg viewBox="0 0 20 20" fill="currentColor" class="search-icon">
            <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
          </svg>
          Search
        </button>

        <template v-if="auth.isLoggedIn">
          <button class="write-btn" @click="router.push({ name: 'PostWrite' })">
            <svg viewBox="0 0 20 20" fill="currentColor" class="write-icon">
              <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"/>
            </svg>
            새 글 작성
          </button>
          <NotificationBell />
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
          <router-link to="/login" class="login-btn">로그인</router-link>
          <router-link to="/signup" class="signup-btn">시작하기</router-link>
        </template>
      </div>

    </div>

    <!-- ── 검색 패널 (슬라이드 다운) ── -->
    <Transition name="search-panel">
      <div v-if="showSearch" class="search-panel">
        <div class="search-panel-inner">

          <!-- 검색 입력 -->
          <form class="search-form" @submit.prevent="handleSearch">
            <div class="search-input-wrap">
              <svg viewBox="0 0 20 20" fill="currentColor" class="input-icon">
                <path fill-rule="evenodd" d="M8 4a4 4 0 100 8 4 4 0 000-8zM2 8a6 6 0 1110.89 3.476l4.817 4.817a1 1 0 01-1.414 1.414l-4.816-4.816A6 6 0 012 8z" clip-rule="evenodd"/>
              </svg>
              <input
                ref="searchInput"
                v-model="searchQuery"
                type="text"
                class="search-input"
                placeholder="검색어를 입력하세요..."
                @keyup.esc="toggleSearch"
              />
              <button v-if="searchQuery" type="button" class="clear-btn" @click="searchQuery = ''">
                <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M4.293 4.293a1 1 0 011.414 0L10 8.586l4.293-4.293a1 1 0 111.414 1.414L11.414 10l4.293 4.293a1 1 0 01-1.414 1.414L10 11.414l-4.293 4.293a1 1 0 01-1.414-1.414L8.586 10 4.293 5.707a1 1 0 010-1.414z" clip-rule="evenodd"/></svg>
              </button>
            </div>
            <button type="submit" class="search-submit">
              Search
              <svg viewBox="0 0 20 20" fill="currentColor">
                <path fill-rule="evenodd" d="M10.293 3.293a1 1 0 011.414 0l6 6a1 1 0 010 1.414l-6 6a1 1 0 01-1.414-1.414L14.586 11H3a1 1 0 110-2h11.586l-4.293-4.293a1 1 0 010-1.414z" clip-rule="evenodd"/>
              </svg>
            </button>
          </form>

          <!-- 태그 목록 -->
          <div v-if="tags.length" class="search-tags">
            <button
              v-for="tag in tags"
              :key="tag"
              class="search-tag"
              @click="handleTagClick(tag)"
            >{{ tag }}</button>
          </div>

        </div>
      </div>
    </Transition>

  </header>
</template>

<style scoped>
.blog-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.96);
  backdrop-filter: blur(8px);
  -webkit-backdrop-filter: blur(8px);
  border-bottom: 1px solid #e5e7eb;
}

.header-inner {
  max-width: 72rem;
  margin: 0 auto;
  padding: 0 2rem;
  height: 3.5rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.brand {
  font-size: 1.0625rem;
  font-weight: 800;
  color: #111827;
  text-decoration: none;
  letter-spacing: -0.03em;
}

.header-actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

/* Search 버튼 */
.search-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.4rem 0.85rem;
  background: transparent;
  color: #6b7280;
  font-size: 0.8rem;
  font-weight: 500;
  font-family: inherit;
  border: 1.5px solid #e5e7eb;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.search-btn:hover,
.search-btn.active {
  border-color: #4776E6;
  color: #4776E6;
  background: rgba(71, 118, 230, 0.06);
}
.search-icon { width: 12px; height: 12px; }

/* 검색 패널 */
.search-panel {
  background: linear-gradient(135deg, #3a1fa8 0%, #4776E6 60%, #8E54E9 100%);
  border-top: 1px solid rgba(255,255,255,0.1);
  overflow: hidden;
}
.search-panel-inner {
  max-width: 72rem;
  margin: 0 auto;
  padding: 1.5rem 2rem 1.75rem;
}

/* 검색 폼 */
.search-form {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}
.search-input-wrap {
  flex: 1;
  position: relative;
  display: flex;
  align-items: center;
}
.input-icon {
  position: absolute;
  left: 1rem;
  width: 16px;
  height: 16px;
  color: #9ca3af;
  pointer-events: none;
  flex-shrink: 0;
}
.search-input {
  width: 100%;
  padding: 0.75rem 2.75rem 0.75rem 2.75rem;
  font-size: 0.9375rem;
  font-family: inherit;
  color: #111827;
  background: #fff;
  border: none;
  border-radius: 10px;
  outline: none;
  box-shadow: 0 2px 12px rgba(0,0,0,0.15);
  transition: box-shadow 0.15s;
}
.search-input::placeholder { color: #9ca3af; }
.search-input:focus { box-shadow: 0 4px 20px rgba(0,0,0,0.2); }

.clear-btn {
  position: absolute;
  right: 0.75rem;
  display: flex;
  align-items: center;
  background: none;
  border: none;
  cursor: pointer;
  color: #9ca3af;
  padding: 0.25rem;
  border-radius: 4px;
  transition: color 0.15s;
}
.clear-btn:hover { color: #374151; }
.clear-btn svg { width: 14px; height: 14px; }

.search-submit {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.75rem 1.25rem;
  background: rgba(255,255,255,0.15);
  color: #fff;
  font-size: 0.875rem;
  font-weight: 600;
  font-family: inherit;
  border: 1.5px solid rgba(255,255,255,0.3);
  border-radius: 10px;
  cursor: pointer;
  white-space: nowrap;
  transition: all 0.15s;
  backdrop-filter: blur(4px);
  flex-shrink: 0;
}
.search-submit:hover { background: rgba(255,255,255,0.25); border-color: rgba(255,255,255,0.5); }
.search-submit svg { width: 13px; height: 13px; }

/* 태그 목록 */
.search-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.45rem;
  margin-top: 1rem;
}
.search-tag {
  padding: 0.25rem 0.75rem;
  background: rgba(255,255,255,0.12);
  color: rgba(255,255,255,0.85);
  font-size: 0.775rem;
  font-family: inherit;
  border: 1px solid rgba(255,255,255,0.2);
  border-radius: 999px;
  cursor: pointer;
  transition: all 0.15s;
  white-space: nowrap;
}
.search-tag:hover { background: rgba(255,255,255,0.25); color: #fff; border-color: rgba(255,255,255,0.4); }

/* 슬라이드 다운 트랜지션 */
.search-panel-enter-active,
.search-panel-leave-active {
  transition: all 0.28s cubic-bezier(0.4, 0, 0.2, 1);
  max-height: 240px;
}
.search-panel-enter-from,
.search-panel-leave-to {
  max-height: 0;
  opacity: 0;
  transform: translateY(-8px);
}
.search-panel-enter-to,
.search-panel-leave-from {
  max-height: 240px;
  opacity: 1;
  transform: translateY(0);
}

/* 새 글 작성 버튼 */
.write-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.45rem 1rem;
  background: #4776E6;
  color: #fff;
  border: none;
  border-radius: 8px;
  font-size: 0.8rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s, transform 0.15s;
  box-shadow: 0 2px 8px rgba(71, 118, 230, 0.22);
}
.write-btn:hover { background: #3d67d8; transform: translateY(-1px); }
.write-icon { width: 12px; height: 12px; }

/* 아바타 */
.avatar-wrap { position: relative; }
.avatar-btn {
  width: 30px;
  height: 30px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: #fff;
  font-size: 0.72rem;
  font-weight: 700;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s;
}
.avatar-btn:hover { transform: scale(1.06); }

/* 드롭다운 */
.dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 200px;
  background: #fff;
  border-radius: 12px;
  border: 1px solid #f3f4f6;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.08);
  overflow: hidden;
  padding: 0.4rem;
}
.dropdown-header { padding: 0.5rem 0.6rem 0.4rem; }
.dropdown-name { font-size: 0.85rem; font-weight: 700; color: #111827; margin: 0 0 0.1rem; }
.dropdown-email { font-size: 0.72rem; color: #9ca3af; margin: 0; overflow: hidden; text-overflow: ellipsis; white-space: nowrap; }
.dropdown-divider { height: 1px; background: #f9fafb; margin: 0.3rem 0; }

.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.5rem 0.6rem;
  border-radius: 6px;
  font-size: 0.82rem;
  color: #374151;
  text-decoration: none;
  background: none;
  border: none;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.15s;
  text-align: left;
}
.dropdown-item svg { width: 13px; height: 13px; flex-shrink: 0; color: #9ca3af; }
.dropdown-item:hover { background: #f9fafb; }
.dropdown-item--danger { color: #ef4444; }
.dropdown-item--danger svg { color: #ef4444; }
.dropdown-item--danger:hover { background: #fef2f2; }

/* 로그인/시작하기 */
.login-btn {
  padding: 0.4rem 0.85rem;
  font-size: 0.8rem;
  font-weight: 500;
  color: #6b7280;
  text-decoration: none;
  border-radius: 8px;
  transition: all 0.15s;
}
.login-btn:hover { color: #111827; background: #f9fafb; }

.signup-btn {
  display: inline-flex;
  padding: 0.4rem 1rem;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: #fff;
  font-size: 0.8rem;
  font-weight: 600;
  text-decoration: none;
  border-radius: 8px;
  transition: opacity 0.15s, transform 0.15s;
  box-shadow: 0 2px 8px rgba(71, 118, 230, 0.22);
}
.signup-btn:hover { opacity: 0.9; transform: translateY(-1px); }

/* 드롭다운 트랜지션 */
.dropdown-enter-active, .dropdown-leave-active { transition: all 0.15s ease; }
.dropdown-enter-from, .dropdown-leave-to { opacity: 0; transform: translateY(-4px); }
</style>
