<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/common/AppHeader.vue'
import AppFooter from './components/common/AppFooter.vue'

const route = useRoute()

// 에디터/인증 라우트: 헤더/푸터 완전 제거
const isEditorRoute = computed(() =>
  ['PostWrite', 'PostEdit', 'Signup', 'Login'].includes(route.name)
)

// 홈/검색/글상세/태그/마이페이지/프로필/알림 라우트: 자체 헤더 + 풀 너비
const isDiscoveryRoute = computed(() =>
  ['Home', 'Search', 'PostDetail', 'TagPosts', 'MyPage', 'UserProfile', 'Notifications'].includes(route.name)
)
</script>

<template>
  <!-- 에디터/인증 페이지 -->
  <template v-if="isEditorRoute">
    <router-view />
  </template>

  <!-- 홈/검색/글상세: 자체 헤더 + 풀 너비 -->
  <div v-else-if="isDiscoveryRoute" class="min-h-screen flex flex-col" style="background: #f5f5f5;">
    <router-view />
    <AppFooter />
  </div>

  <!-- 나머지 페이지 (마이페이지 등): 흰 헤더 + 컨테이너 -->
  <div v-else class="min-h-screen flex flex-col" style="background: #F8F9FF;">
    <AppHeader />
    <main class="flex-1 max-w-6xl w-full mx-auto px-4 py-8">
      <router-view />
    </main>
    <AppFooter />
  </div>
</template>
