<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../../stores/auth'
import CommentForm from './CommentForm.vue'

const props = defineProps({
  comment: { type: Object, required: true },
  replies: { type: Array, default: () => [] },
})

const emit = defineEmits(['reply'])
const auth = useAuthStore()
const showReplyForm = ref(false)

function handleReply(content) {
  emit('reply', { parentId: props.comment.commentId, content })
  showReplyForm.value = false
}
</script>

<template>
  <div class="py-4">
    <div class="flex items-center justify-between mb-2">
      <div class="flex items-center gap-2">
        <div class="w-7 h-7 bg-gray-200 rounded-full flex items-center justify-center text-xs font-bold text-gray-600">
          U
        </div>
        <span class="text-sm font-medium text-gray-800">사용자 {{ comment.authorId }}</span>
      </div>
      <button
        v-if="auth.isLoggedIn"
        @click="showReplyForm = !showReplyForm"
        class="text-xs text-gray-400 hover:text-gray-700 transition cursor-pointer"
      >
        답글
      </button>
    </div>
    <p class="text-sm text-gray-700 whitespace-pre-wrap ml-9">{{ comment.content }}</p>

    <div v-if="showReplyForm" class="mt-3 ml-9">
      <CommentForm
        :parent-id="comment.commentId"
        placeholder="답글을 작성하세요..."
        @submit="handleReply"
        @cancel="showReplyForm = false"
      />
    </div>

    <div v-if="replies.length > 0" class="ml-9 mt-2 border-l-2 border-gray-100 pl-4">
      <CommentItem
        v-for="reply in replies"
        :key="reply.commentId"
        :comment="reply"
        :replies="[]"
        @reply="(data) => emit('reply', data)"
      />
    </div>
  </div>
</template>
