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
</script>

<template>
  <header class="bg-white border-b border-gray-200 sticky top-0 z-50 shadow-sm">
    <div class="max-w-6xl mx-auto px-4 h-14 flex items-center justify-between">
      <router-link to="/" class="text-xl font-bold text-gray-900 hover:text-gray-600 transition no-underline">
        velog
      </router-link>

      <div class="flex items-center gap-2">
        <button
          @click="showSearch = !showSearch"
          class="p-2 text-gray-500 hover:text-gray-900 transition cursor-pointer rounded-full hover:bg-gray-100"
        >
          <svg class="w-5 h-5" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
              d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
          </svg>
        </button>

        <template v-if="auth.isLoggedIn">
          <button
            @click="goWrite"
            class="px-4 py-1.5 bg-gray-900 text-white text-sm font-medium rounded-full hover:bg-gray-700 transition cursor-pointer"
          >
            새 글 작성
          </button>
          <div class="relative">
            <button
              @click="showDropdown = !showDropdown"
              class="w-9 h-9 bg-gray-200 rounded-full flex items-center justify-center text-sm font-bold text-gray-700 hover:bg-gray-300 transition cursor-pointer"
            >
              {{ auth.user?.nickname?.charAt(0)?.toUpperCase() || 'U' }}
            </button>
            <div
              v-if="showDropdown"
              class="absolute right-0 mt-2 w-48 bg-white rounded-xl shadow-lg border border-gray-100 py-1"
            >
              <router-link
                to="/my"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition no-underline"
                @click="showDropdown = false"
              >
                마이페이지
              </router-link>
              <button
                @click="handleLogout"
                class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 transition cursor-pointer"
              >
                로그아웃
              </button>
            </div>
          </div>
        </template>
        <template v-else>
          <router-link
            to="/login"
            class="px-3 py-1.5 text-sm text-gray-600 hover:text-gray-900 transition no-underline"
          >
            로그인
          </router-link>
          <router-link
            to="/signup"
            class="px-4 py-1.5 bg-gray-900 text-white text-sm font-medium rounded-full hover:bg-gray-700 transition no-underline"
          >
            회원가입
          </router-link>
        </template>
      </div>
    </div>

    <!-- 검색창 (토글) -->
    <div v-if="showSearch" class="border-t border-gray-100 px-4 py-3 bg-white">
      <form @submit.prevent="handleSearch" class="max-w-6xl mx-auto relative">
        <input
          v-model="searchKeyword"
          type="text"
          placeholder="검색어를 입력하세요..."
          autofocus
          class="w-full pl-10 pr-4 py-2 bg-gray-100 border border-gray-200 rounded-lg text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent"
        />
        <svg
          class="absolute left-3 top-1/2 -translate-y-1/2 w-4 h-4 text-gray-400"
          fill="none" stroke="currentColor" viewBox="0 0 24 24"
        >
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2"
            d="M21 21l-6-6m2-5a7 7 0 11-14 0 7 7 0 0114 0z" />
        </svg>
      </form>
    </div>
  </header>
</template>
