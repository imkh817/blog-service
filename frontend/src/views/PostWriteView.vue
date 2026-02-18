<script setup>
import { ref } from 'vue'
import { useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import PostEditor from '../components/post/PostEditor.vue'
import TagInput from '../components/post/TagInput.vue'

const router = useRouter()
const auth = useAuthStore()

const title = ref('')
const content = ref('')
const tags = ref([])
const error = ref('')
const submitting = ref(false)

async function submit(status) {
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
    const res = await postApi.create(auth.user?.id, {
      title: title.value,
      content: content.value,
      postStatus: status,
      tagNames: tags.value,
    })
    router.push({ name: 'PostDetail', params: { id: res.data.data.postId } })
  } catch (e) {
    error.value = e.response?.data?.message || '저장에 실패했습니다.'
  } finally {
    submitting.value = false
  }
}
</script>

<template>
  <div class="max-w-3xl mx-auto">
    <h1 class="text-2xl font-bold mb-6">새 글 작성</h1>

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
          @click="submit('DRAFT')"
          :disabled="submitting"
          class="px-6 py-2.5 border border-gray-300 text-sm rounded-lg hover:bg-gray-50 disabled:opacity-50 cursor-pointer"
        >
          임시저장
        </button>
        <button
          @click="submit('PUBLISHED')"
          :disabled="submitting"
          class="px-6 py-2.5 bg-gray-900 text-white text-sm rounded-lg hover:bg-gray-700 disabled:opacity-50 cursor-pointer"
        >
          발행
        </button>
      </div>
    </div>
  </div>
</template>
