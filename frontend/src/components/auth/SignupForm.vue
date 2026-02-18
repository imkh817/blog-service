<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../../stores/auth'

const router = useRouter()
const auth = useAuthStore()

const email = ref('')
const password = ref('')
const passwordConfirm = ref('')
const nickname = ref('')
const error = ref('')
const loading = ref(false)

async function handleSubmit() {
  error.value = ''
  if (password.value !== passwordConfirm.value) {
    error.value = '비밀번호가 일치하지 않습니다.'
    return
  }
  if (password.value.length < 8) {
    error.value = '비밀번호는 8자 이상이어야 합니다.'
    return
  }
  loading.value = true
  try {
    await auth.signup(email.value, password.value, nickname.value)
    router.push({ name: 'Login', query: { registered: 'true' } })
  } catch (e) {
    error.value = e.response?.data?.message || '회원가입에 실패했습니다.'
  } finally {
    loading.value = false
  }
}
</script>

<template>
  <form @submit.prevent="handleSubmit" class="space-y-4">
    <div v-if="error" class="p-3 bg-red-50 text-red-600 text-sm rounded-lg">
      {{ error }}
    </div>
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-1">이메일</label>
      <input
        v-model="email"
        type="email"
        required
        class="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
        placeholder="이메일을 입력하세요"
      />
    </div>
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-1">닉네임</label>
      <input
        v-model="nickname"
        type="text"
        required
        minlength="2"
        maxlength="20"
        class="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
        placeholder="닉네임을 입력하세요"
      />
    </div>
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-1">비밀번호</label>
      <input
        v-model="password"
        type="password"
        required
        minlength="8"
        class="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
        placeholder="8자 이상 입력하세요"
      />
    </div>
    <div>
      <label class="block text-sm font-medium text-gray-700 mb-1">비밀번호 확인</label>
      <input
        v-model="passwordConfirm"
        type="password"
        required
        class="w-full px-4 py-2 border border-gray-300 rounded-lg text-sm focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
        placeholder="비밀번호를 다시 입력하세요"
      />
    </div>
    <button
      type="submit"
      :disabled="loading"
      class="w-full py-2.5 bg-gray-900 text-white text-sm font-medium rounded-lg hover:bg-gray-700 disabled:opacity-50 cursor-pointer"
    >
      {{ loading ? '가입 중...' : '회원가입' }}
    </button>
  </form>
</template>
