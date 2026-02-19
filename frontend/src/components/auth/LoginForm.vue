<script setup>
import { ref } from 'vue'
import { useRouter, useRoute } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const route = useRoute()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const error = ref('')
const loading = ref(false)

async function handleSubmit() {
  error.value = ''
  loading.value = true
  try {
    await auth.login(email.value, password.value)
    const redirect = route.query.redirect || '/'
    router.push(redirect)
  } catch (e) {
    error.value = e.response?.data?.message || '로그인에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <form @submit.prevent="handleSubmit" class="space-y-4">
    <div v-if="error" class="p-3 bg-red-50 text-red-600 border border-red-200 text-sm rounded-lg">
      {{ error }}
    </div>
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-1">이메일</label>
      <input
        v-model="email"
        type="email"
        required
        class="w-full px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent"
        placeholder="이메일을 입력하세요"
      />
    </div>
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
      <input
        v-model="password"
        type="password"
        required
        class="w-full px-4 py-2 bg-white border border-gray-300 rounded-lg text-sm text-gray-900 placeholder-gray-400 focus:outline-none focus:ring-2 focus:ring-gray-300 focus:border-transparent"
        placeholder="비밀번호를 입력하세요"
      />
    </div>
    <button
      type="submit"
      :disabled="loading"
      class="w-full py-2.5 bg-gray-900 text-white text-sm font-medium rounded-lg hover:bg-gray-700 disabled:opacity-50 cursor-pointer transition"
    >
      {{ loading ? '로그인 중...' : '로그인' }}
    </button>
  </form>
</template>
