import { defineStore } from 'pinia'
import { ref } from 'vue'
import { postApi } from '../api/postApi'

export const usePostStore = defineStore('post', () => {
  const posts = ref([])
  const currentPost = ref(null)
  const totalPages = ref(0)
  const loading = ref(false)

  async function fetchPosts(params = {}) {
    loading.value = true
    try {
      const res = await postApi.search(params)
      posts.value = res.data.data
      const totalCount = parseInt(res.headers['x-total-count'] || '0')
      const size = params.size || 10
      totalPages.value = Math.ceil(totalCount / size)
    } finally {
      loading.value = false
    }
  }

  async function fetchPost(id) {
    loading.value = true
    try {
      const res = await postApi.getById(id)
      currentPost.value = res.data.data
      return currentPost.value
    } finally {
      loading.value = false
    }
  }

  return { posts, currentPost, totalPages, loading, fetchPosts, fetchPost }
})
