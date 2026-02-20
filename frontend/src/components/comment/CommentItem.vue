<script setup>
import { ref } from 'vue'
import { useAuthStore } from '../../stores/auth'
import CommentForm from './CommentForm.vue'

const props = defineProps({
  comment:    { type: Object, required: true },
  replies:    { type: Array,  default: () => [] },
  getReplies: { type: Function, default: () => [] },
})

const emit = defineEmits(['reply'])
const auth = useAuthStore()
const showReplyForm = ref(false)

function handleReply(content) {
  emit('reply', { parentId: props.comment.commentId, content })
  showReplyForm.value = false
}

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('ko-KR', { year: 'numeric', month: 'short', day: 'numeric' })
}
</script>

<template>
  <div class="comment-item">
    <!-- Comment header -->
    <div class="comment-header">
      <div class="comment-author">
        <div class="comment-avatar">U</div>
        <div>
          <span class="author-name">사용자 {{ comment.authorId }}</span>
          <span class="comment-date">{{ formatDate(comment.createdAt) }}</span>
        </div>
      </div>
      <button
        v-if="auth.isLoggedIn"
        @click="showReplyForm = !showReplyForm"
        class="reply-btn"
        :class="{ active: showReplyForm }"
      >
        <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M7.707 3.293a1 1 0 010 1.414L5.414 7H11a7 7 0 017 7v2a1 1 0 11-2 0v-2a5 5 0 00-5-5H5.414l2.293 2.293a1 1 0 11-1.414 1.414l-4-4a1 1 0 010-1.414l4-4a1 1 0 011.414 0z" clip-rule="evenodd"/></svg>
        답글
      </button>
    </div>

    <!-- Comment body -->
    <p class="comment-body">{{ comment.content }}</p>

    <!-- Reply form -->
    <div v-if="showReplyForm" class="reply-form-wrap">
      <CommentForm
        :parent-id="comment.commentId"
        placeholder="답글을 작성하세요..."
        @submit="handleReply"
        @cancel="showReplyForm = false"
      />
    </div>

    <!-- Replies -->
    <div v-if="replies.length > 0" class="replies">
      <CommentItem
        v-for="reply in replies"
        :key="reply.commentId"
        :comment="reply"
        :replies="getReplies(reply.commentId)"
        :get-replies="getReplies"
        @reply="(d) => emit('reply', d)"
      />
    </div>
  </div>
</template>

<style scoped>
.comment-item {
  padding: 1.1rem 0;
  border-bottom: 1px solid #F1F5F9;
}
.comment-item:last-child { border-bottom: none; }

.comment-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 0.6rem;
}
.comment-author {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}
.comment-avatar {
  width: 30px; height: 30px;
  border-radius: 8px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  font-size: 0.72rem;
  font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.author-name {
  display: block;
  font-size: 0.82rem;
  font-weight: 600;
  color: #0F172A;
  line-height: 1.3;
}
.comment-date {
  display: block;
  font-size: 0.7rem;
  color: #94A3B8;
  line-height: 1.3;
}

.reply-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.3rem 0.65rem;
  background: none;
  border: 1.5px solid #E2E8F0;
  border-radius: 100px;
  font-size: 0.72rem;
  color: #94A3B8;
  cursor: pointer;
  font-family: 'Noto Sans KR', sans-serif;
  transition: all 0.18s;
}
.reply-btn svg { width: 11px; height: 11px; }
.reply-btn:hover, .reply-btn.active {
  border-color: #4776E6;
  color: #4776E6;
  background: #EEF2FF;
}

.comment-body {
  font-size: 0.875rem;
  color: #334155;
  line-height: 1.75;
  white-space: pre-wrap;
  margin: 0 0 0.75rem;
  padding-left: 2.4rem;
}

.reply-form-wrap {
  padding-left: 2.4rem;
  margin-bottom: 0.75rem;
}

/* Replies indent */
.replies {
  padding-left: 2.4rem;
  border-left: 2px solid #EEF2FF;
  margin-top: 0.25rem;
}
</style>
