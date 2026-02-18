<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { commentApi } from '../../api/commentApi'
import CommentForm from './CommentForm.vue'
import CommentItem from './CommentItem.vue'

const props = defineProps({
  postId: { type: [Number, String], required: true },
})

const auth = useAuthStore()
const comments = ref([])
const loading = ref(false)

const rootComments = computed(() =>
  comments.value.filter((c) => !c.parentId)
)

function getReplies(parentId) {
  return comments.value.filter((c) => c.parentId === parentId)
}

async function fetchComments() {
  loading.value = true
  try {
    const res = await commentApi.getByPostId(props.postId)
    comments.value = res.data.data || []
  } catch {
    comments.value = []
  } finally {
    loading.value = false
  }
}

async function handleSubmit(content) {
  await commentApi.create(props.postId, { content })
  await fetchComments()
}

async function handleReply({ parentId, content }) {
  await commentApi.create(props.postId, { content, parentId })
  await fetchComments()
}

onMounted(fetchComments)
</script>

<template>
  <div class="mt-10">
    <h3 class="text-lg font-semibold mb-4">댓글 {{ comments.length }}개</h3>

    <div v-if="auth.isLoggedIn" class="mb-6">
      <CommentForm @submit="handleSubmit" />
    </div>
    <p v-else class="text-sm text-gray-500 mb-6">
      댓글을 작성하려면
      <router-link to="/login" class="text-gray-900 underline">로그인</router-link>
      하세요.
    </p>

    <div v-if="loading" class="text-center py-8 text-gray-400">불러오는 중...</div>
    <div v-else-if="rootComments.length === 0" class="text-center py-8 text-gray-400 text-sm">
      아직 댓글이 없습니다.
    </div>
    <div v-else class="divide-y divide-gray-100">
      <CommentItem
        v-for="comment in rootComments"
        :key="comment.commentId"
        :comment="comment"
        :replies="getReplies(comment.commentId)"
        @reply="handleReply"
      />
    </div>
  </div>
</template>
