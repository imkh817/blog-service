<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'
import SearchBar from './SearchBar.vue'

const router = useRouter()
const auth = useAuthStore()
const showDropdown = ref(false)

function handleSearch(keyword) {
  router.push({ name: 'Search', query: { keyword } })
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
  <header class="bg-white border-b border-gray-200 sticky top-0 z-50">
    <div class="max-w-6xl mx-auto px-4 h-16 flex items-center justify-between">
      <router-link to="/" class="text-xl font-bold text-gray-900 no-underline">
        Blog
      </router-link>

      <SearchBar class="hidden sm:block flex-1 max-w-md mx-8" @search="handleSearch" />

      <div class="flex items-center gap-3">
        <template v-if="auth.isLoggedIn">
          <button
            @click="goWrite"
            class="px-4 py-2 bg-gray-900 text-white text-sm rounded-lg hover:bg-gray-700 transition cursor-pointer"
          >
            새 글 작성
          </button>
          <div class="relative">
            <button
              @click="showDropdown = !showDropdown"
              class="w-9 h-9 bg-gray-200 rounded-full flex items-center justify-center text-sm font-medium hover:bg-gray-300 transition cursor-pointer"
            >
              {{ auth.user?.nickname?.charAt(0) || 'U' }}
            </button>
            <div
              v-if="showDropdown"
              class="absolute right-0 mt-2 w-48 bg-white rounded-lg shadow-lg border border-gray-200 py-1"
            >
              <router-link
                to="/my"
                class="block px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 no-underline"
                @click="showDropdown = false"
              >
                마이페이지
              </router-link>
              <button
                @click="handleLogout"
                class="w-full text-left px-4 py-2 text-sm text-gray-700 hover:bg-gray-50 cursor-pointer"
              >
                로그아웃
              </button>
            </div>
          </div>
        </template>
        <template v-else>
          <router-link
            to="/login"
            class="px-4 py-2 text-sm text-gray-700 hover:text-gray-900 no-underline"
          >
            로그인
          </router-link>
          <router-link
            to="/signup"
            class="px-4 py-2 bg-gray-900 text-white text-sm rounded-lg hover:bg-gray-700 no-underline"
          >
            회원가입
          </router-link>
        </template>
      </div>
    </div>
  </header>
</template>
