<script setup>
import { ref, onMounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import PostContent from '../components/post/PostContent.vue'
import PostStatusBadge from '../components/post/PostStatusBadge.vue'
import CommentList from '../components/comment/CommentList.vue'

const route  = useRoute()
const router = useRouter()
const auth   = useAuthStore()

const post    = ref(null)
const loading = ref(true)

const isAuthor = () => auth.user?.id === post.value?.authorId

const readTime = computed(() => {
  if (!post.value?.content) return 1
  return Math.max(1, Math.round(post.value.content.split(/\s+/).length / 200))
})

const formattedDate = computed(() => {
  if (!post.value?.createdAt) return ''
  const d = new Date(post.value.createdAt)
  return `${d.getFullYear()}년 ${d.getMonth() + 1}월 ${d.getDate()}일`
})

async function fetchPost() {
  loading.value = true
  try {
    const res = await postApi.getById(route.params.id)
    post.value = res.data.data
  } finally {
    loading.value = false
  }
}

async function handlePublish() { await postApi.publish(post.value.postId); await fetchPost() }
async function handleHide()    { await postApi.hide(post.value.postId);    await fetchPost() }
async function handleDelete() {
  if (!confirm('정말 삭제하시겠습니까?')) return
  await postApi.delete(post.value.postId)
  router.push({ name: 'Home' })
}

onMounted(fetchPost)
</script>

<template>
  <!-- Loading -->
  <div v-if="loading" class="loading-wrap">
    <div class="loading-dots">
      <span></span><span></span><span></span>
    </div>
  </div>

  <article v-else-if="post" class="article">

    <!-- Back button -->
    <button @click="router.back()" class="back-btn">
      <svg viewBox="0 0 20 20" fill="currentColor">
        <path fill-rule="evenodd" d="M9.707 16.707a1 1 0 01-1.414 0l-6-6a1 1 0 010-1.414l6-6a1 1 0 011.414 1.414L5.414 9H17a1 1 0 110 2H5.414l4.293 4.293a1 1 0 010 1.414z" clip-rule="evenodd"/>
      </svg>
      돌아가기
    </button>

    <!-- Header -->
    <header class="post-header">
      <PostStatusBadge v-if="post.postStatus !== 'PUBLISHED'" :status="post.postStatus" class="mb-3" />

      <h1 class="post-title">{{ post.title }}</h1>

      <!-- Author row -->
      <div class="author-row">
        <div class="avatar">{{ post.authorNickname?.charAt(0)?.toUpperCase() || 'U' }}</div>
        <div class="author-info">
          <p class="author-name">{{ post.authorNickname || '익명' }}</p>
          <p class="author-meta">{{ formattedDate }} · {{ readTime }}분 읽기</p>
        </div>
      </div>

      <!-- Tags -->
      <div v-if="post.tags?.length" class="tags">
        <router-link
          v-for="tag in post.tags"
          :key="tag"
          :to="{ name: 'TagPosts', params: { tagName: tag } }"
          class="tag-link"
        >#{{ tag }}</router-link>
      </div>

      <div class="header-divider"></div>
    </header>

    <!-- Content -->
    <div class="post-content">
      <PostContent :content="post.content" />
    </div>

    <!-- Action bar -->
    <div class="action-bar">
      <button class="action-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
        </svg>
        좋아요 {{ post.viewCount || 0 }}
      </button>
      <button class="action-btn">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
          <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
          <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/><line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
        </svg>
        공유하기
      </button>
    </div>

    <!-- Author card -->
    <div class="author-card">
      <div class="author-card-avatar">{{ post.authorNickname?.charAt(0)?.toUpperCase() || 'U' }}</div>
      <div class="author-card-info">
        <p class="author-card-name">{{ post.authorNickname || '익명' }}</p>
        <p class="author-card-handle">@user{{ post.authorId }}</p>
        <p class="author-card-bio">글 쓰는 것을 좋아합니다.</p>
      </div>
    </div>

    <!-- Author actions -->
    <div v-if="isAuthor()" class="author-actions">
      <router-link :to="{ name: 'PostEdit', params: { id: post.postId } }" class="edit-btn">
        <svg viewBox="0 0 20 20" fill="currentColor"><path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"/></svg>
        수정
      </router-link>
      <button
        v-if="post.postStatus === 'HIDDEN' || post.postStatus === 'DRAFT'"
        @click="handlePublish"
        class="publish-btn"
      >
        <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M10 18a8 8 0 100-16 8 8 0 000 16zm3.707-9.293a1 1 0 00-1.414-1.414L9 10.586 7.707 9.293a1 1 0 00-1.414 1.414l2 2a1 1 0 001.414 0l4-4z" clip-rule="evenodd"/></svg>
        발행
      </button>
      <button v-if="post.postStatus === 'PUBLISHED'" @click="handleHide" class="hide-btn">숨기기</button>
      <button @click="handleDelete" class="delete-btn">
        <svg viewBox="0 0 20 20" fill="currentColor"><path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>
        삭제
      </button>
    </div>

    <!-- Comments -->
    <CommentList :post-id="post.postId" />

  </article>
</template>

<style scoped>
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 5rem 0;
}
.loading-dots {
  display: flex;
  gap: 6px;
  align-items: center;
}
.loading-dots span {
  width: 8px; height: 8px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  border-radius: 50%;
  animation: dot-bounce 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.15s; }
.loading-dots span:nth-child(3) { animation-delay: 0.3s; }
@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.4; }
  40% { transform: scale(1); opacity: 1; }
}

.article { max-width: 44rem; margin: 0 auto; }

/* ── Back btn ── */
.back-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  color: #94A3B8;
  font-size: 0.82rem;
  font-weight: 500;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.4rem 0.8rem;
  border-radius: 8px;
  margin-bottom: 1.75rem;
  transition: all 0.18s;
  margin-left: -0.8rem;
}
.back-btn svg { width: 14px; height: 14px; }
.back-btn:hover { color: #0F172A; background: #F1F5F9; }

/* ── Post header ── */
.post-header { margin-bottom: 1.5rem; }

.post-title {
  font-family: 'Plus Jakarta Sans', 'Noto Sans KR', sans-serif;
  font-size: clamp(1.6rem, 4vw, 2.25rem);
  font-weight: 800;
  color: #0F172A;
  line-height: 1.2;
  letter-spacing: -0.02em;
  margin: 0 0 1.25rem;
}

.author-row {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  margin-bottom: 1rem;
}
.avatar {
  width: 38px; height: 38px;
  border-radius: 12px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  font-size: 0.9rem;
  font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.author-name {
  font-size: 0.875rem;
  font-weight: 600;
  color: #0F172A;
  margin: 0 0 0.1rem;
}
.author-meta {
  font-size: 0.75rem;
  color: #94A3B8;
  margin: 0;
}

.tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-bottom: 1.25rem;
}
.tag-link {
  display: inline-block;
  padding: 0.3rem 0.75rem;
  background: #EEF2FF;
  color: #4F46E5;
  font-size: 0.75rem;
  font-weight: 600;
  border-radius: 100px;
  text-decoration: none;
  transition: all 0.18s;
}
.tag-link:hover { background: #E0E7FF; color: #4338CA; }

.header-divider {
  height: 1px;
  background: #E8EDF8;
  margin: 1.25rem 0 2rem;
}

/* ── Content ── */
.post-content { margin-bottom: 2.5rem; }

/* ── Action bar ── */
.action-bar {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 1.25rem 0;
  border-top: 1px solid #E8EDF8;
  border-bottom: 1px solid #E8EDF8;
  margin-bottom: 2rem;
}
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.6rem 1.2rem;
  border: 1.5px solid #E2E8F0;
  border-radius: 100px;
  background: white;
  font-size: 0.82rem;
  color: #64748B;
  font-weight: 500;
  cursor: pointer;
  transition: all 0.18s;
  font-family: 'Noto Sans KR', sans-serif;
}
.action-btn svg { width: 15px; height: 15px; }
.action-btn:hover {
  border-color: #4776E6;
  color: #4776E6;
  background: #EEF2FF;
}

/* ── Author card ── */
.author-card {
  background: white;
  border: 1px solid #E8EDF8;
  border-radius: 16px;
  padding: 1.5rem;
  display: flex;
  align-items: flex-start;
  gap: 1rem;
  margin-bottom: 1.5rem;
  box-shadow: 0 2px 12px rgba(15,23,42,0.04);
}
.author-card-avatar {
  width: 52px; height: 52px;
  border-radius: 14px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: white;
  font-size: 1.2rem;
  font-weight: 700;
  display: flex; align-items: center; justify-content: center;
  flex-shrink: 0;
}
.author-card-name {
  font-size: 0.95rem;
  font-weight: 700;
  color: #0F172A;
  margin: 0 0 0.2rem;
}
.author-card-handle {
  font-size: 0.78rem;
  color: #94A3B8;
  margin: 0 0 0.4rem;
}
.author-card-bio {
  font-size: 0.82rem;
  color: #64748B;
  margin: 0;
}

/* ── Author actions ── */
.author-actions {
  display: flex;
  gap: 0.6rem;
  margin-bottom: 2rem;
  flex-wrap: wrap;
}
.edit-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.55rem 1.1rem;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  color: #374151;
  font-size: 0.82rem;
  font-weight: 600;
  text-decoration: none;
  background: white;
  transition: all 0.18s;
}
.edit-btn svg { width: 13px; height: 13px; }
.edit-btn:hover { border-color: #4776E6; color: #4776E6; background: #EEF2FF; }

.publish-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.55rem 1.1rem;
  background: linear-gradient(135deg, #10B981, #059669);
  color: white;
  border: none;
  border-radius: 10px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  font-family: 'Noto Sans KR', sans-serif;
  transition: opacity 0.18s;
}
.publish-btn svg { width: 13px; height: 13px; }
.publish-btn:hover { opacity: 0.9; }

.hide-btn {
  padding: 0.55rem 1.1rem;
  border: 1.5px solid #E2E8F0;
  border-radius: 10px;
  color: #374151;
  font-size: 0.82rem;
  font-weight: 600;
  background: white;
  cursor: pointer;
  font-family: 'Noto Sans KR', sans-serif;
  transition: all 0.18s;
}
.hide-btn:hover { background: #F1F5F9; }

.delete-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.55rem 1.1rem;
  background: #FEF2F2;
  color: #DC2626;
  border: 1.5px solid #FECACA;
  border-radius: 10px;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  font-family: 'Noto Sans KR', sans-serif;
  transition: all 0.18s;
}
.delete-btn svg { width: 13px; height: 13px; }
.delete-btn:hover { background: #FEE2E2; border-color: #FCA5A5; }
</style>