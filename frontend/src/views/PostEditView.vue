<script setup>
import { ref, onMounted } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import PostEditor from '../components/post/PostEditor.vue'
import TagInput from '../components/post/TagInput.vue'

const route = useRoute()
const router = useRouter()
const auth = useAuthStore()

const title = ref('')
const content = ref('')
const tags = ref([])
const error = ref('')
const loading = ref(true)
const submitting = ref(false)

async function fetchPost() {
  loading.value = true
  try {
    const res = await postApi.getById(route.params.id)
    const post = res.data.data
    title.value = post.title
    content.value = post.content
    tags.value = post.tags || []
  } finally {
    loading.value = false
  }
}

async function handleSubmit() {
  if (!title.value.trim()) {
    error.value = '제목을 입력하세요.'
    return
  }
  if (!content.value.trim()) {
    error.value = '내용을 입력하세요.'
    return
  }
  if (tags.value.length === 0) {
    error.value = '태그를 최소 1개 입력하세요.'
    return
  }

  error.value = ''
  submitting.value = true
  try {
    await postApi.update(auth.user?.id, {
      postId: Number(route.params.id),
      title: title.value,
      content: content.value,
      tagNames: tags.value,
    })
    router.push({ name: 'PostDetail', params: { id: route.params.id } })
  } catch (e) {
    error.value = e.response?.data?.message || '수정에 실패했습니다.'
  } finally {
    submitting.value = false
  }
}

onMounted(fetchPost)
</script>

<template>
  <div class="max-w-3xl mx-auto">
    <h1 class="text-2xl font-bold mb-6">글 수정</h1>

    <div v-if="loading" class="text-center py-20 text-gray-400">불러오는 중...</div>

    <template v-else>
      <div v-if="error" class="mb-4 p-3 bg-red-50 text-red-600 text-sm rounded-lg">
        {{ error }}
      </div>

      <div class="space-y-4">
        <input
          v-model="title"
          type="text"
          placeholder="제목을 입력하세요"
          maxlength="100"
          class="w-full px-4 py-3 text-xl font-semibold border border-gray-300 rounded-lg focus:outline-none focus:ring-2 focus:ring-gray-900 focus:border-transparent"
        />

        <TagInput v-model="tags" />

        <PostEditor v-model="content" />

        <div class="flex justify-end gap-3 pt-4">
          <button
            @click="router.back()"
            class="px-6 py-2.5 border border-gray-300 text-sm rounded-lg hover:bg-gray-50 cursor-pointer"
          >
            취소
          </button>
          <button
            @click="handleSubmit"
            :disabled="submitting"
            class="px-6 py-2.5 bg-gray-900 text-white text-sm rounded-lg hover:bg-gray-700 disabled:opacity-50 cursor-pointer"
          >
            수정 완료
          </button>
        </div>
      </div>
    </template>
  </div>
</template>
