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
const editorRef = ref(null)

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
    const resolvedContent = await editorRef.value?.resolveImages(content.value) ?? content.value
    const res = await postApi.create(auth.user?.id, {
      title: title.value,
      content: resolvedContent,
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
  <div class="min-h-screen bg-[#F2F2F2] flex flex-col">
    <!-- Custom editor header -->
    <header class="bg-white border-b border-gray-200 px-6 h-14 flex items-center justify-between sticky top-0 z-10">
      <button
        @click="router.back()"
        class="flex items-center gap-1.5 text-gray-500 hover:text-gray-700 text-sm cursor-pointer transition"
      >
        <svg class="w-4 h-4" fill="none" stroke="currentColor" viewBox="0 0 24 24">
          <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M15 19l-7-7 7-7"/>
        </svg>
        나가기
      </button>

      <div class="flex items-center gap-2">
        <button
          @click="submit('DRAFT')"
          :disabled="submitting"
          class="px-4 py-1.5 border border-gray-300 text-gray-600 text-sm rounded-lg hover:bg-gray-50 disabled:opacity-50 cursor-pointer transition"
        >
          임시저장
        </button>
        <button
          @click="submit('PUBLISHED')"
          :disabled="submitting"
          class="px-5 py-1.5 bg-blue-400 text-white text-sm rounded-full hover:bg-blue-500 disabled:opacity-50 cursor-pointer transition font-medium"
        >
          출간하기
        </button>
      </div>
    </header>

    <!-- Editor content -->
    <div class="flex-1 max-w-5xl w-full mx-auto px-6 pt-10 pb-16">
      <div v-if="error" class="mb-4 p-3 bg-red-50 text-red-600 border border-red-200 text-sm rounded-lg">
        {{ error }}
      </div>

      <!-- Title -->
      <input
        v-model="title"
        type="text"
        placeholder="제목을 입력하세요"
        maxlength="100"
        class="w-full text-4xl font-bold text-gray-800 bg-transparent placeholder-gray-300 border-none outline-none mb-4 leading-tight"
      />

      <hr class="border-gray-200 mb-4" />

      <!-- Tags -->
      <TagInput v-model="tags" />

      <hr class="border-gray-200 mt-4 mb-4" />

      <!-- Editor -->
      <PostEditor ref="editorRef" v-model="content" />
    </div>
  </div>
</template>
