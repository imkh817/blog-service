<script setup>
defineProps({
  name: { type: String, required: true },
  clickable: { type: Boolean, default: true },
})
</script>

<template>
  <router-link
    v-if="clickable"
    :to="{ name: 'TagPosts', params: { tagName: name } }"
    class="inline-block px-3 py-1 bg-gray-100 text-gray-700 text-xs rounded-full hover:bg-gray-200 transition no-underline"
  >
    #{{ name }}
  </router-link>
  <span
    v-else
    class="inline-block px-3 py-1 bg-gray-100 text-gray-700 text-xs rounded-full"
  >
    #{{ name }}
  </span>
</template>
