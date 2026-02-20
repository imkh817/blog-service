<script setup>
import { computed } from 'vue'
import { useRoute } from 'vue-router'
import AppHeader from './components/common/AppHeader.vue'
import AppFooter from './components/common/AppFooter.vue'

const route = useRoute()
const isEditorRoute = computed(() => ['PostWrite', 'PostEdit', 'Signup', 'Login'].includes(route.name))
</script>

<template>
  <template v-if="isEditorRoute">
    <router-view />
  </template>
  <div v-else class="min-h-screen flex flex-col text-gray-900" style="background: #F8F9FF;">
    <AppHeader />
    <main class="flex-1 max-w-6xl w-full mx-auto px-4 py-8">
      <router-view />
    </main>
    <AppFooter />
  </div>
</template>
