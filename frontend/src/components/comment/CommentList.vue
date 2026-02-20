<script setup>
import { ref, onMounted, computed } from 'vue'
import { useAuthStore } from '../../stores/auth'
import { commentApi } from '../../api/commentApi'
import CommentForm from './CommentForm.vue'
import CommentItem from './CommentItem.vue'

const props = defineProps({
  postId: { type: [Number, String], required: true },
})

const auth     = useAuthStore()
const comments = ref([])
const loading  = ref(false)

const rootComments = computed(() => comments.value.filter(c => !c.parentId))
function getReplies(parentId) { return comments.value.filter(c => c.parentId === parentId) }

async function fetchComments() {
  loading.value = true
  try {
    const res = await commentApi.getByPostId(props.postId)
    comments.value = res.data.data || []
  } catch { comments.value = [] }
  finally  { loading.value = false }
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
  <section class="comment-section">
    <div class="comment-section-header">
      <h3 class="comment-count">
        <svg viewBox="0 0 20 20" fill="currentColor" class="bubble-icon">
          <path fill-rule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clip-rule="evenodd"/>
        </svg>
        댓글 {{ comments.length }}개
      </h3>
    </div>

    <!-- Comment form -->
    <div v-if="auth.isLoggedIn" class="comment-form-wrap">
      <div class="form-avatar">{{ auth.user?.nickname?.charAt(0)?.toUpperCase() || 'U' }}</div>
      <div class="form-content">
        <CommentForm @submit="handleSubmit" />
      </div>
    </div>
    <div v-else class="login-prompt">
      <svg viewBox="0 0 20 20" fill="currentColor" class="lock-icon"><path fill-rule="evenodd" d="M5 9V7a5 5 0 0110 0v2a2 2 0 012 2v5a2 2 0 01-2 2H5a2 2 0 01-2-2v-5a2 2 0 012-2zm8-2v2H7V7a3 3 0 016 0z" clip-rule="evenodd"/></svg>
      댓글을 달려면&nbsp;
      <router-link to="/login" class="login-link">로그인</router-link>
      이 필요해요.
    </div>

    <!-- Comments list -->
    <div v-if="loading" class="comment-loading">
      <div class="loading-dots"><span></span><span></span><span></span></div>
    </div>
    <div v-else-if="rootComments.length === 0" class="no-comments">
      <span>🗨️</span>
      <p>아직 댓글이 없어요. 첫 댓글을 남겨보세요!</p>
    </div>
    <div v-else class="comments-list">
      <CommentItem
        v-for="comment in rootComments"
        :key="comment.commentId"
        :comment="comment"
        :replies="getReplies(comment.commentId)"
        :get-replies="getReplies"
        @reply="handleReply"
      />
    </div>
  </section>
</template>

<style scoped>
.comment-section {
  margin-top: 3rem;
  padding-top: 2rem;
  border-top: 2px solid #E8EDF8;
}

.comment-section-header { margin-bottom: 1.5rem; }

.comment-count {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  font-family: 'Plus Jakarta Sans', sans-serif;
  font-size: 1rem;
  font-weight: 700;
  color: #0F172A;
  margin: 0;
}
.bubble-icon { width: 18px; height: 18px; color: #4776E6; }

/* Write form row */
.comment-form-wrap {
  display: flex;
  gap: 0.75rem;
  margin-bottom: 1.5rem;
}
.form-avatar {
  width: 32px; height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  font-size: 0.8rem;
  font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
  margin-top: 0.2rem;
}
.form-content { flex: 1; }

/* Login prompt */
.login-prompt {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.9rem 1.1rem;
  background: #F8F9FF;
  border: 1px solid #E8EDF8;
  border-radius: 12px;
  font-size: 0.82rem;
  color: #64748B;
  margin-bottom: 1.5rem;
}
.lock-icon { width: 14px; height: 14px; color: #94A3B8; }
.login-link {
  color: #4776E6;
  font-weight: 600;
  text-decoration: none;
}
.login-link:hover { text-decoration: underline; }

/* Loading */
.comment-loading {
  display: flex;
  justify-content: center;
  padding: 2rem 0;
}
.loading-dots {
  display: flex;
  gap: 5px;
}
.loading-dots span {
  width: 7px; height: 7px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  border-radius: 50%;
  animation: db 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.15s; }
.loading-dots span:nth-child(3) { animation-delay: 0.3s; }
@keyframes db {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

/* No comments */
.no-comments {
  text-align: center;
  padding: 2.5rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
  font-size: 1.75rem;
}
.no-comments p {
  font-size: 0.85rem;
  color: #94A3B8;
  margin: 0;
}

.comments-list { margin-top: 0.5rem; }
</style>
