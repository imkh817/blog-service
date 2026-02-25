<script setup>
import { useRouter } from 'vue-router'
import BlogHeader from '../components/common/BlogHeader.vue'

const router = useRouter()

function handleSearch(keyword) {
  router.push({ name: 'Home', query: { keyword } })
}

function handleTagSelect(tag) {
  router.push({ name: 'Home', query: { tag } })
}
</script>

<template>
  <div>
    <BlogHeader @search="handleSearch" @tag-select="handleTagSelect" />
    <div class="content">
      <p class="text-lg font-medium mb-2 text-gray-400">사용자 프로필 페이지</p>
      <p class="text-sm text-gray-400">해당 기능은 현재 준비 중입니다.</p>
    </div>
  </div>
</template>

<style scoped>
.content {
  max-width: 72rem;
  margin: 0 auto;
  padding: 5rem 1.5rem;
  text-align: center;
}
</style>