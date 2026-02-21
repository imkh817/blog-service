<script setup>
import { ref, watch } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()
const showDropdown = ref(false)

function goSearch() {
  router.push({ name: 'Search' })
}

function goWrite() {
  router.push({ name: 'PostWrite' })
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
</script>

<template>
  <header class="site-header">
    <div class="header-inner">
      <!-- Brand -->
      <router-link to="/" class="brand">blog</router-link>

      <!-- Center nav -->
      <nav class="nav-links">
        <router-link to="/" class="nav-link">Blog</router-link>
      </nav>

      <!-- Right actions -->
      <div class="actions">
        <!-- Search button -->
        <button class="search-btn" @click="goSearch">
          <svg viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
            <circle cx="9" cy="9" r="6"/>
            <path d="m17 17-3.5-3.5"/>
          </svg>
          Search
        </button>

        <template v-if="auth.isLoggedIn">
          <!-- Write button -->
          <button class="write-btn" @click="goWrite">새 글 작성</button>

          <!-- Avatar dropdown -->
          <div class="avatar-wrap">
            <button class="avatar-btn" @click="showDropdown = !showDropdown">
              {{ auth.user?.nickname?.charAt(0)?.toUpperCase() || 'U' }}
            </button>
            <Transition name="dropdown">
              <div v-if="showDropdown" class="dropdown" @click.stop>
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
  </header>
</template>

<style scoped>
.site-header {
  position: sticky;
  top: 0;
  z-index: 50;
  background: #fff;
  border-bottom: 1px solid #e5e7eb;
}

.header-inner {
  max-width: 72rem;
  margin: 0 auto;
  padding: 0 1.5rem;
  height: 3.5rem;
  display: flex;
  align-items: center;
  gap: 2rem;
}

/* Brand */
.brand {
  font-size: 1rem;
  font-weight: 700;
  color: #111;
  text-decoration: none;
  letter-spacing: -0.02em;
  flex-shrink: 0;
}

/* Center nav */
.nav-links {
  display: flex;
  align-items: center;
  gap: 1.5rem;
  flex: 1;
}

.nav-link {
  font-size: 0.875rem;
  font-weight: 500;
  color: #374151;
  text-decoration: none;
  transition: color 0.15s;
}
.nav-link:hover,
.nav-link.router-link-active {
  color: #111;
}

/* Right actions */
.actions {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  margin-left: auto;
}

/* Search button */
.search-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.45rem 1rem;
  background: #111;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  transition: background 0.15s;
  font-family: inherit;
}
.search-btn svg {
  width: 14px;
  height: 14px;
}
.search-btn:hover {
  background: #1f2937;
}

/* Write button */
.write-btn {
  padding: 0.45rem 1rem;
  background: none;
  border: 1.5px solid #d1d5db;
  border-radius: 6px;
  font-size: 0.82rem;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  transition: all 0.15s;
  font-family: inherit;
}
.write-btn:hover {
  border-color: #9ca3af;
  color: #111;
}

/* Avatar */
.avatar-wrap {
  position: relative;
}
.avatar-btn {
  width: 32px;
  height: 32px;
  border-radius: 50%;
  background: #111;
  color: #fff;
  font-size: 0.78rem;
  font-weight: 700;
  border: none;
  cursor: pointer;
  display: flex;
  align-items: center;
  justify-content: center;
  transition: transform 0.15s;
}
.avatar-btn:hover {
  transform: scale(1.05);
}

/* Dropdown */
.dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 200px;
  background: #fff;
  border-radius: 10px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  padding: 0.4rem;
}
.dropdown-header {
  padding: 0.5rem 0.6rem 0.4rem;
}
.dropdown-name {
  font-size: 0.85rem;
  font-weight: 700;
  color: #111;
  margin: 0 0 0.1rem;
}
.dropdown-email {
  font-size: 0.72rem;
  color: #9ca3af;
  margin: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.dropdown-divider {
  height: 1px;
  background: #f3f4f6;
  margin: 0.3rem 0;
}
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
.dropdown-item svg {
  width: 14px;
  height: 14px;
  flex-shrink: 0;
  color: #9ca3af;
}
.dropdown-item:hover {
  background: #f9fafb;
}
.dropdown-item--danger {
  color: #ef4444;
}
.dropdown-item--danger svg {
  color: #ef4444;
}
.dropdown-item--danger:hover {
  background: #fef2f2;
}

/* Ghost / CTA buttons */
.ghost-btn {
  padding: 0.45rem 0.85rem;
  color: #6b7280;
  font-size: 0.82rem;
  font-weight: 500;
  text-decoration: none;
  border-radius: 6px;
  transition: all 0.15s;
}
.ghost-btn:hover {
  color: #111;
  background: #f9fafb;
}
.cta-btn {
  padding: 0.45rem 1rem;
  background: #111;
  color: #fff;
  font-size: 0.82rem;
  font-weight: 600;
  text-decoration: none;
  border-radius: 6px;
  transition: background 0.15s;
}
.cta-btn:hover {
  background: #1f2937;
}

/* Dropdown transition */
.dropdown-enter-active,
.dropdown-leave-active {
  transition: all 0.15s ease;
}
.dropdown-enter-from,
.dropdown-leave-to {
  opacity: 0;
  transform: translateY(-4px) scale(0.97);
}
</style>
