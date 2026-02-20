<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const showDropdown = ref(false)
const showSearch = ref(false)
const searchKeyword = ref('')

function handleSearch() {
  const trimmed = searchKeyword.value.trim()
  if (trimmed) {
    router.push({ name: 'Search', query: { keyword: trimmed } })
    showSearch.value = false
    searchKeyword.value = ''
  }
}

function goWrite() {
  router.push({ name: 'PostWrite' })
}

function handleLogout() {
  auth.logout()
  showDropdown.value = false
  router.push({ name: 'Home' })
}

function closeDropdown(e) {
  if (!e.target.closest('.avatar-wrap')) {
    showDropdown.value = false
  }
}
</script>

<template>
  <header class="site-header">
    <div class="header-inner">
      <!-- Brand -->
      <router-link to="/" class="brand">
        <span class="brand-dot"></span>
        <span class="brand-name">Blog</span>
      </router-link>

      <!-- Right actions -->
      <div class="actions">
        <!-- Search toggle -->
        <button class="icon-btn" @click="showSearch = !showSearch" :class="{ active: showSearch }" title="검색">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
        </button>

        <template v-if="auth.isLoggedIn">
          <!-- Write button -->
          <button class="write-btn" @click="goWrite">
            <svg viewBox="0 0 20 20" fill="currentColor" class="write-icon">
              <path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"/>
            </svg>
            새 글 작성
          </button>

          <!-- Avatar dropdown -->
          <div class="avatar-wrap" v-click-outside="() => showDropdown = false">
            <button class="avatar-btn" @click="showDropdown = !showDropdown">
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

    <!-- Search bar slide-down -->
    <Transition name="search-slide">
      <div v-if="showSearch" class="search-bar">
        <form @submit.prevent="handleSearch" class="search-form">
          <svg class="search-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <circle cx="11" cy="11" r="8"/>
            <path d="m21 21-4.35-4.35"/>
          </svg>
          <input
            v-model="searchKeyword"
            type="text"
            placeholder="검색어를 입력하세요..."
            autofocus
            class="search-input"
          />
          <button v-if="searchKeyword" type="button" class="search-clear" @click="searchKeyword = ''">
            <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zM8.707 7.293a1 1 0 00-1.414 1.414L8.586 10l-1.293 1.293a1 1 0 101.414 1.414L10 11.414l1.293 1.293a1 1 0 001.414-1.414L11.414 10l1.293-1.293a1 1 0 00-1.414-1.414L10 8.586 8.707 7.293z" clip-rule="evenodd"/></svg>
          </button>
          <button type="submit" class="search-submit">검색</button>
        </form>
      </div>
    </Transition>
  </header>
</template>

<style scoped>
@import url('https://fonts.googleapis.com/css2?family=Plus+Jakarta+Sans:wght@700;800&display=swap');

.site-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: rgba(255, 255, 255, 0.92);
  backdrop-filter: blur(12px);
  -webkit-backdrop-filter: blur(12px);
  border-bottom: 1px solid #E8EDF8;
}

.header-inner {
  max-width: 72rem;
  margin: 0 auto;
  padding: 0 1.25rem;
  height: 3.75rem;
  display: flex;
  align-items: center;
  justify-content: space-between;
}

/* ── Brand ── */
.brand {
  display: flex;
  align-items: center;
  gap: 0.45rem;
  text-decoration: none;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 1.05rem;
  font-weight: 800;
  color: #0F172A;
  letter-spacing: -0.02em;
}
.brand-dot {
  width: 8px; height: 8px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  border-radius: 50%;
  box-shadow: 0 0 8px rgba(99,102,241,0.55);
}
.brand-name { color: #0F172A; }

/* ── Right actions ── */
.actions {
  display: flex;
  align-items: center;
  gap: 0.4rem;
}

/* Icon button (search) */
.icon-btn {
  width: 36px; height: 36px;
  display: flex; align-items: center; justify-content: center;
  border-radius: 10px;
  background: none;
  border: none;
  color: #64748B;
  cursor: pointer;
  transition: all 0.18s ease;
}
.icon-btn svg { width: 18px; height: 18px; }
.icon-btn:hover, .icon-btn.active {
  background: #EEF2FF;
  color: #4F46E5;
}

/* Write button */
.write-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.45rem 1rem;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.2s ease;
  font-family: 'Noto Sans KR', sans-serif;
  box-shadow: 0 2px 10px rgba(71,118,230,0.3);
}
.write-btn:hover {
  opacity: 0.9;
  transform: translateY(-1px);
  box-shadow: 0 4px 16px rgba(71,118,230,0.4);
}
.write-icon { width: 14px; height: 14px; }

/* Avatar */
.avatar-wrap { position: relative; }
.avatar-btn {
  width: 34px; height: 34px;
  border-radius: 10px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  font-size: 0.8rem;
  font-weight: 700;
  border: none;
  cursor: pointer;
  display: flex; align-items: center; justify-content: center;
  transition: transform 0.18s ease;
  font-family: 'Plus Jakarta Sans', sans-serif;
}
.avatar-btn:hover { transform: scale(1.05); }

/* Dropdown */
.dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 200px;
  background: white;
  border-radius: 14px;
  border: 1px solid #E8EDF8;
  box-shadow: 0 8px 32px rgba(15,23,42,0.12);
  overflow: hidden;
  padding: 0.5rem;
}
.dropdown-header {
  padding: 0.6rem 0.65rem 0.5rem;
}
.dropdown-name {
  font-size: 0.85rem;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 0.15rem;
}
.dropdown-email {
  font-size: 0.72rem;
  color: #94A3B8;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dropdown-divider {
  height: 1px;
  background: #F1F5F9;
  margin: 0.35rem 0;
}
.dropdown-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  width: 100%;
  padding: 0.55rem 0.65rem;
  border-radius: 8px;
  font-size: 0.82rem;
  color: #374151;
  text-decoration: none;
  background: none;
  border: none;
  cursor: pointer;
  font-family: 'Noto Sans KR', sans-serif;
  transition: background 0.15s;
  text-align: left;
}
.dropdown-item svg { width: 14px; height: 14px; flex-shrink: 0; color: #9CA3AF; }
.dropdown-item:hover { background: #F8F9FF; }
.dropdown-item--danger { color: #EF4444; }
.dropdown-item--danger svg { color: #EF4444; }
.dropdown-item--danger:hover { background: #FEF2F2; }

/* Ghost / CTA buttons */
.ghost-btn {
  padding: 0.45rem 0.85rem;
  color: #64748B;
  font-size: 0.82rem;
  font-weight: 500;
  text-decoration: none;
  border-radius: 10px;
  transition: all 0.18s;
}
.ghost-btn:hover { color: #0F172A; background: #F1F5F9; }

.cta-btn {
  padding: 0.45rem 1rem;
  background: #0F172A;
  color: white;
  font-size: 0.82rem;
  font-weight: 600;
  text-decoration: none;
  border-radius: 10px;
  transition: all 0.2s;
}
.cta-btn:hover { background: #1E293B; transform: translateY(-1px); }

/* ── Search bar ── */
.search-bar {
  border-top: 1px solid #F1F5F9;
  padding: 0.75rem 1.25rem;
  background: white;
}
.search-form {
  max-width: 72rem;
  margin: 0 auto;
  position: relative;
  display: flex;
  align-items: center;
  gap: 0.5rem;
  background: #F8F9FF;
  border: 1.5px solid #E8EDF8;
  border-radius: 12px;
  padding: 0.1rem 0.5rem;
  transition: border-color 0.2s, box-shadow 0.2s;
}
.search-form:focus-within {
  border-color: #6366F1;
  box-shadow: 0 0 0 3px rgba(99,102,241,0.1);
  background: white;
}
.search-icon {
  width: 16px; height: 16px;
  color: #94A3B8;
  flex-shrink: 0;
  margin-left: 0.25rem;
}
.search-input {
  flex: 1;
  padding: 0.6rem 0;
  background: none;
  border: none;
  outline: none;
  font-size: 0.875rem;
  color: #0F172A;
  font-family: 'Noto Sans KR', sans-serif;
}
.search-input::placeholder { color: #CBD5E1; }
.search-clear {
  width: 20px; height: 20px;
  display: flex; align-items: center; justify-content: center;
  background: none;
  border: none;
  cursor: pointer;
  color: #94A3B8;
  padding: 0;
}
.search-clear svg { width: 16px; height: 16px; }
.search-clear:hover { color: #64748B; }
.search-submit {
  padding: 0.4rem 0.9rem;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  border: none;
  border-radius: 8px;
  font-size: 0.78rem;
  font-weight: 600;
  cursor: pointer;
  font-family: 'Noto Sans KR', sans-serif;
  white-space: nowrap;
}

/* Transitions */
.dropdown-enter-active, .dropdown-leave-active {
  transition: all 0.18s ease;
}
.dropdown-enter-from, .dropdown-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.97);
}

.search-slide-enter-active, .search-slide-leave-active {
  transition: all 0.22s ease;
}
.search-slide-enter-from, .search-slide-leave-to {
  opacity: 0;
  transform: translateY(-6px);
}
</style>