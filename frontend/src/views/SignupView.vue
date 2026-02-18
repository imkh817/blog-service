<script setup>
import SignupForm from '../components/auth/SignupForm.vue'
</script>

<template>
  <div class="max-w-md mx-auto mt-10">
    <h1 class="text-2xl font-bold text-center mb-6">회원가입</h1>
    <div class="bg-white rounded-xl border border-gray-200 p-8">
      <SignupForm />
      <p class="mt-6 text-center text-sm text-gray-500">
        이미 계정이 있으신가요?
        <router-link to="/login" class="text-gray-900 font-medium underline">로그인</router-link>
      </p>
    </div>
  </div>
</template>
