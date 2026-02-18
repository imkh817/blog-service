<script setup>
import { useRoute } from 'vue-router'
import LoginForm from '../components/auth/LoginForm.vue'

const route = useRoute()
</script>

<template>
  <div class="max-w-md mx-auto mt-10">
    <h1 class="text-2xl font-bold text-center mb-6">로그인</h1>
    <div v-if="route.query.registered" class="mb-4 p-3 bg-green-50 text-green-600 text-sm rounded-lg text-center">
      회원가입이 완료되었습니다. 로그인하세요.
    </div>
    <div class="bg-white rounded-xl border border-gray-200 p-8">
      <LoginForm />
      <p class="mt-6 text-center text-sm text-gray-500">
        계정이 없으신가요?
        <router-link to="/signup" class="text-gray-900 font-medium underline">회원가입</router-link>
      </p>
    </div>
  </div>
</template>
