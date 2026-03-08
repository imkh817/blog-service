<script setup>
import { ref, onMounted, computed, watch } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { useAuthStore } from '../stores/auth'
import { postApi } from '../api/postApi'
import BlogHeader from '../components/common/BlogHeader.vue'
import PostContent from '../components/post/PostContent.vue'
import PostStatusBadge from '../components/post/PostStatusBadge.vue'
import CommentList from '../components/comment/CommentList.vue'
import { extractToc } from '../utils/markdown'

const route  = useRoute()
const router = useRouter()
const auth   = useAuthStore()

const post         = ref(null)
const loading      = ref(true)
const relatedPosts = ref([])
const liked        = ref(false)
const likeCount    = ref(0)
const commentCount = ref(0)
const likeLoading       = ref(false)
const showDeleteDialog  = ref(false)

const isAuthor = () => auth.user?.id === post.value?.authorId

const readTime = computed(() => {
  if (!post.value?.content) return 1
  return Math.max(1, Math.round(post.value.content.split(/\s+/).length / 200))
})

const formattedDate = computed(() => {
  if (!post.value?.createdAt) return ''
  const d = new Date(post.value.createdAt)
  return `${d.getFullYear()}. ${d.getMonth() + 1}. ${d.getDate()}.`
})

const toc = computed(() => extractToc(post.value?.content || ''))

async function fetchPost() {
  loading.value = true
  try {
    const res = await postApi.getById(route.params.id)
    post.value = res.data.data
    likeCount.value    = post.value.likeCount ?? 0
    liked.value        = post.value.isLikedByMe ?? false
    commentCount.value = post.value.commentCounts ?? 0
  } finally {
    loading.value = false
  }
}

async function handleLike() {
  if (!auth.isLoggedIn) {
    router.push({ name: 'Login' })
    return
  }
  if (likeLoading.value) return
  likeLoading.value = true
  const prev = liked.value
  liked.value = !prev
  likeCount.value += prev ? -1 : 1
  try {
    if (prev) {
      const res = await postApi.unlike(post.value.postId)
      likeCount.value = res.data.data.likeCount
      liked.value = false
    } else {
      const res = await postApi.like(post.value.postId)
      likeCount.value = res.data.data.likeCount
      liked.value = true
    }
  } catch {
    liked.value = prev
    likeCount.value += prev ? 1 : -1
  } finally {
    likeLoading.value = false
  }
}

async function loadRelated() {
  try {
    const res = await postApi.getList({ page: 0, size: 6, sort: 'createdAt,desc' })
    const all = res.data.data || []
    relatedPosts.value = all.filter(p => p.postId !== post.value?.postId).slice(0, 2)
  } catch {
    relatedPosts.value = []
  }
}

async function handlePublish() { await postApi.publish(post.value.postId); await fetchPost() }
async function handleHide()    { await postApi.hide(post.value.postId);    await fetchPost() }
function handleDelete() {
  showDeleteDialog.value = true
}

async function confirmDelete() {
  await postApi.delete(post.value.postId)
  router.back()
}

function handleShare() {
  if (navigator.share) {
    navigator.share({ title: post.value?.title, url: location.href })
  } else {
    navigator.clipboard.writeText(location.href)
    alert('링크가 복사되었습니다.')
  }
}

function scrollToHeading(id) {
  const el = document.getElementById(id)
  if (el) el.scrollIntoView({ behavior: 'smooth', block: 'start' })
}

onMounted(async () => {
  await fetchPost()
  loadRelated()
})

watch(() => route.params.id, async (newId) => {
  if (!newId) return
  await fetchPost()
  loadRelated()
})
</script>

<template>
  <div class="detail-page">

    <!-- 공통 헤더 -->
    <BlogHeader />

    <!-- 로딩 -->
    <div v-if="loading" class="loading-wrap">
      <div class="loading-dots"><span></span><span></span><span></span></div>
    </div>

    <!-- 본문 -->
    <div v-else-if="post" class="detail-body">
      <div class="detail-container">
        <div class="detail-layout">

          <!-- ── 왼쪽: 작성자 프로필 ── -->
          <aside class="author-sidebar">
            <div class="author-card">
              <!-- 아바타 -->
              <div class="author-avatar">
                {{ post.authorNickname?.charAt(0)?.toUpperCase() || 'U' }}
              </div>
              <!-- 이름 -->
              <p class="author-name">{{ post.authorNickname || '익명' }}</p>
              <!-- 날짜 + 읽기시간 -->
              <p class="author-meta">{{ formattedDate }}</p>
              <p class="author-readtime">{{ readTime }}분 읽기</p>

              <div class="author-divider"></div>

              <!-- 구독 버튼 -->
              <button class="action-subscribe">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
                  <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
                </svg>
                구독하기
              </button>
            </div>
          </aside>

          <!-- ── 중앙: 본문 ── -->
          <article class="article-main">

            <!-- 작성자 액션 툴바 (상단 우측) -->
            <div v-if="isAuthor()" class="author-actions">
              <router-link :to="{ name: 'PostEdit', params: { id: post.postId } }" class="action-btn action-edit">
                <svg viewBox="0 0 20 20" fill="currentColor" class="action-icon"><path d="M13.586 3.586a2 2 0 112.828 2.828l-.793.793-2.828-2.828.793-.793zM11.379 5.793L3 14.172V17h2.828l8.38-8.379-2.83-2.828z"/></svg>
                수정
              </router-link>
              <button v-if="post.postStatus === 'HIDDEN'" @click="handlePublish" class="action-btn action-publish">
                <svg viewBox="0 0 20 20" fill="currentColor" class="action-icon"><path d="M10 2a.75.75 0 01.75.75v8.614l2.72-2.72a.75.75 0 111.06 1.062l-4.5 4.5a.75.75 0 01-1.06 0l-4.5-4.5a.75.75 0 111.06-1.06l2.72 2.72V2.75A.75.75 0 0110 2zM4.75 15.25a.75.75 0 000 1.5h10.5a.75.75 0 000-1.5H4.75z"/></svg>
                발행
              </button>
              <button v-if="post.postStatus === 'PUBLISHED'" @click="handleHide" class="action-btn action-hide">
                <svg viewBox="0 0 20 20" fill="currentColor" class="action-icon"><path d="M3.707 2.293a1 1 0 00-1.414 1.414l14 14a1 1 0 001.414-1.414l-1.473-1.473A10.014 10.014 0 0019.542 10C18.268 5.943 14.478 3 10 3a9.958 9.958 0 00-4.512 1.074l-1.78-1.781zm4.261 4.26l1.514 1.515a2.003 2.003 0 012.45 2.45l1.514 1.514a4 4 0 00-5.478-5.478z"/><path d="M12.454 16.697L9.75 13.992a4 4 0 01-3.742-3.741L2.335 6.578A9.98 9.98 0 00.458 10c1.274 4.057 5.065 7 9.542 7 .847 0 1.669-.105 2.454-.303z"/></svg>
                숨기기
              </button>
              <div class="action-divider"></div>
              <button @click="handleDelete" class="action-btn action-delete" title="삭제">
                <svg viewBox="0 0 20 20" fill="currentColor" class="action-icon"><path fill-rule="evenodd" d="M9 2a1 1 0 00-.894.553L7.382 4H4a1 1 0 000 2v10a2 2 0 002 2h8a2 2 0 002-2V6a1 1 0 100-2h-3.382l-.724-1.447A1 1 0 0011 2H9zM7 8a1 1 0 012 0v6a1 1 0 11-2 0V8zm5-1a1 1 0 00-1 1v6a1 1 0 102 0V8a1 1 0 00-1-1z" clip-rule="evenodd"/></svg>
              </button>
            </div>

            <!-- 상태 배지 -->
            <PostStatusBadge
              v-if="post.postStatus !== 'PUBLISHED'"
              :status="post.postStatus"
              class="mb-3"
            />

            <!-- 썸네일 이미지 -->
            <div v-if="post.thumbnailUrl" class="post-thumbnail">
              <img :src="post.thumbnailUrl" :alt="post.title" />
            </div>

            <!-- 제목 -->
            <h1 class="post-title">{{ post.title }}</h1>

            <!-- 태그 -->
            <div v-if="post.tags?.length" class="post-tags">
              <router-link
                v-for="tag in post.tags"
                :key="tag"
                :to="{ name: 'TagPosts', params: { tagName: tag } }"
                class="tag-link"
              >#{{ tag }}</router-link>
            </div>

            <!-- 콘텐츠 -->
            <div class="post-content">
              <PostContent :content="post.content" />
            </div>

            <!-- 반응 바 (좋아요 + 공유) -->
            <div class="post-reactions">
              <button
                class="reaction-like"
                :class="{ liked }"
                :disabled="likeLoading"
                @click="handleLike"
              >
                <svg viewBox="0 0 24 24" :fill="liked ? 'currentColor' : 'none'" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
                </svg>
                <span>{{ likeCount }}</span>
              </button>
              <button class="reaction-share" @click="handleShare">
                <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
                  <circle cx="18" cy="5" r="3"/><circle cx="6" cy="12" r="3"/><circle cx="18" cy="19" r="3"/>
                  <line x1="8.59" y1="13.51" x2="15.42" y2="17.49"/>
                  <line x1="15.41" y1="6.51" x2="8.59" y2="10.49"/>
                </svg>
                공유하기
              </button>
            </div>

            <!-- 댓글 -->
            <CommentList :post-id="post.postId" />

          </article>

          <!-- ── 오른쪽: 목차 + 관련 글 ── -->
          <aside class="toc-sidebar">
            <div v-if="toc.length" class="toc-card">
              <p class="toc-label">목차</p>
              <ol class="toc-list">
                <li
                  v-for="(item, idx) in toc"
                  :key="idx"
                  class="toc-item"
                  :class="`toc-level-${item.level}`"
                >
                  <button class="toc-link" @click="scrollToHeading(item.id)">
                    {{ item.text }}
                  </button>
                </li>
              </ol>
            </div>

            <!-- 관련 글 -->
            <div v-if="relatedPosts.length" class="related-card">
              <p class="toc-label">관련 글</p>
              <ul class="related-list">
                <li v-for="(rp, i) in relatedPosts" :key="rp.postId" class="related-item">
                  <span class="related-num">{{ String(i + 1).padStart(2, '0') }}</span>
                  <router-link :to="{ name: 'PostDetail', params: { id: rp.postId } }" class="related-link">{{ rp.title }}</router-link>
                  <svg class="related-arrow" viewBox="0 0 20 20" fill="none" stroke="currentColor" stroke-width="2"><path stroke-linecap="round" stroke-linejoin="round" d="M9 5l7 7-7 7"/></svg>
                </li>
              </ul>
            </div>
          </aside>

        </div>
      </div>
    </div>

    <!-- 글 없음 -->
    <div v-else class="error-wrap">
      <p>글을 찾을 수 없습니다.</p>
      <button class="back-home-btn" @click="router.push({ name: 'Home' })">홈으로</button>
    </div>

  </div>

  <!-- 삭제 확인 다이얼로그 -->
  <Teleport to="body">
    <div
      v-if="showDeleteDialog"
      class="fixed inset-0 z-50 flex items-center justify-center bg-black/40"
      style="backdrop-filter: blur(2px)"
      @click.self="showDeleteDialog = false"
    >
      <div class="bg-white rounded-2xl shadow-2xl w-full max-w-sm mx-4 p-6">
        <div class="w-10 h-10 bg-red-100 rounded-xl flex items-center justify-center mb-4">
          <svg class="w-5 h-5 text-red-500" fill="none" stroke="currentColor" viewBox="0 0 24 24">
            <path stroke-linecap="round" stroke-linejoin="round" stroke-width="2" d="M19 7l-.867 12.142A2 2 0 0116.138 21H7.862a2 2 0 01-1.995-1.858L5 7m5 4v6m4-6v6m1-10V4a1 1 0 00-1-1h-4a1 1 0 00-1 1v3M4 7h16"/>
          </svg>
        </div>
        <h2 class="text-sm font-semibold text-gray-900 mb-1">게시글을 삭제하시겠어요?</h2>
        <p class="text-xs text-gray-500 leading-relaxed">삭제된 게시글은 복구할 수 없습니다.</p>
        <div class="flex gap-2 mt-5">
          <button
            @click="showDeleteDialog = false"
            class="flex-1 px-4 py-2 text-xs font-medium border border-gray-300 text-gray-700 rounded-lg hover:bg-gray-50 cursor-pointer transition-colors"
          >취소</button>
          <button
            @click="confirmDelete"
            class="flex-1 px-4 py-2 text-xs font-semibold bg-red-500 text-white rounded-lg hover:bg-red-600 cursor-pointer transition-colors"
          >삭제</button>
        </div>
      </div>
    </div>
  </Teleport>
</template>

<style scoped>
/* ── 페이지 ── */
.detail-page {
  display: flex;
  flex-direction: column;
  flex: 1;
  background: #f5f5f5;
}

/* ── 로딩 ── */
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 6rem 0;
}
.loading-dots { display: flex; gap: 6px; align-items: center; }
.loading-dots span {
  width: 8px; height: 8px;
  background: #4776E6;
  border-radius: 50%;
  animation: dot-bounce 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.15s; }
.loading-dots span:nth-child(3) { animation-delay: 0.3s; }
@keyframes dot-bounce {
  0%, 80%, 100% { transform: scale(0.6); opacity: 0.35; }
  40% { transform: scale(1); opacity: 1; }
}

/* ── 컨테이너 ── */
.detail-body { flex: 1; }

.detail-container {
  max-width: 76rem;
  margin: 0 auto;
  padding: 2.5rem 2rem 5rem;
}

/* ── 3컬럼 그리드 ── */
.detail-layout {
  display: grid;
  grid-template-columns: 200px 1fr 240px;
  gap: 2.5rem;
  align-items: start;
}

/* ── 왼쪽: 작성자 프로필 ── */
.author-sidebar {
  position: sticky;
  top: 5.5rem;
}

.author-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 1.5rem 1.25rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.4rem;
  text-align: center;
}

.author-avatar {
  width: 64px;
  height: 64px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: #fff;
  font-size: 1.5rem;
  font-weight: 800;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-bottom: 0.6rem;
  flex-shrink: 0;
}

.author-name {
  font-size: 0.9375rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
}

.author-meta {
  font-size: 0.78rem;
  color: #9ca3af;
  margin: 0;
}

.author-readtime {
  font-size: 0.75rem;
  color: #c4c9d4;
  margin: 0;
}

.author-divider {
  width: 100%;
  height: 1px;
  background: #f3f4f6;
  margin: 0.75rem 0 0.25rem;
}

/* 구독 버튼 */
.action-subscribe {
  width: 100%;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.4rem;
  padding: 0.6rem;
  border: none;
  border-radius: 8px;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  color: #fff;
  font-size: 0.82rem;
  font-weight: 600;
  cursor: pointer;
  font-family: inherit;
  transition: opacity 0.15s;
  margin-top: 0.25rem;
}
.action-subscribe svg { width: 15px; height: 15px; flex-shrink: 0; }
.action-subscribe:hover { opacity: 0.88; }

/* 반응 바 (좋아요 + 공유) */
.post-reactions {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.75rem;
  padding: 1.75rem 0 1.5rem;
  margin-bottom: 1.5rem;
}
.reaction-like {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.6rem 1.4rem;
  border: 1.5px solid #e5e7eb;
  border-radius: 999px;
  background: #fff;
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
}
.reaction-like svg { width: 18px; height: 18px; flex-shrink: 0; }
.reaction-like:hover { border-color: #ef4444; color: #ef4444; }
.reaction-like.liked { border-color: #ef4444; color: #ef4444; background: #fef2f2; }

.reaction-share {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.6rem 1.4rem;
  border: 1.5px solid #e5e7eb;
  border-radius: 999px;
  background: #fff;
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.15s;
}
.reaction-share svg { width: 18px; height: 18px; flex-shrink: 0; }
.reaction-share:hover { border-color: #4776E6; color: #4776E6; }

.reaction-comment {
  display: inline-flex;
  align-items: center;
  gap: 0.45rem;
  padding: 0.6rem 1.4rem;
  border: 1.5px solid #e5e7eb;
  border-radius: 999px;
  background: #fff;
  font-size: 0.875rem;
  color: #6b7280;
  font-weight: 500;
}
.reaction-comment svg { width: 18px; height: 18px; flex-shrink: 0; }

/* ── 중앙: 본문 ── */
.article-main {
  min-width: 0;
  background: #fff;
  border-radius: 12px;
  padding: 2.5rem 2.5rem 3rem;
  border: 1px solid #f0f0f0;
}

.post-title {
  font-size: clamp(1.6rem, 3.5vw, 2.25rem);
  font-weight: 800;
  color: #111827;
  line-height: 1.25;
  letter-spacing: -0.025em;
  margin: 0 0 1.25rem;
}

/* 태그 */
.post-tags {
  display: flex;
  flex-wrap: wrap;
  gap: 0.4rem;
  margin-bottom: 2rem;
}
.tag-link {
  display: inline-block;
  padding: 0.25rem 0.7rem;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 0.75rem;
  font-weight: 500;
  border-radius: 999px;
  text-decoration: none;
  transition: all 0.15s;
}
.tag-link:hover { background: #e5e7eb; color: #374151; }

/* 썸네일 */
.post-thumbnail {
  margin-bottom: 1.75rem;
  border-radius: 8px;
  overflow: hidden;
}
.post-thumbnail img {
  width: 100%;
  max-height: 420px;
  object-fit: cover;
  display: block;
}

/* 콘텐츠 */
.post-content { margin-bottom: 2.5rem; }

/* 작성자 액션 툴바 */
.author-actions {
  display: flex;
  align-items: center;
  justify-content: flex-end;
  gap: 0.35rem;
  margin-bottom: 1.75rem;
}
.action-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.45rem 0.9rem;
  border-radius: 8px;
  font-size: 0.78rem;
  font-weight: 600;
  font-family: inherit;
  cursor: pointer;
  transition: all 0.15s;
  text-decoration: none;
  white-space: nowrap;
  line-height: 1;
}
.action-icon { width: 13px; height: 13px; flex-shrink: 0; }

.action-edit {
  border: 1.5px solid #e5e7eb;
  background: #fff;
  color: #374151;
}
.action-edit:hover { border-color: #9ca3af; background: #f9fafb; color: #111827; }

.action-publish {
  border: 1.5px solid #e5e7eb;
  background: #fff;
  color: #374151;
}
.action-publish:hover { border-color: #9ca3af; background: #f9fafb; color: #111827; }

.action-hide {
  border: 1.5px solid #e5e7eb;
  background: #fff;
  color: #374151;
}
.action-hide:hover { border-color: #9ca3af; background: #f9fafb; color: #111827; }

.action-divider {
  width: 1px;
  height: 18px;
  background: #e5e7eb;
  margin: 0 0.1rem;
  flex-shrink: 0;
}

.action-delete {
  border: 1.5px solid transparent;
  background: transparent;
  color: #d1d5db;
  padding: 0.45rem;
  border-radius: 8px;
}
.action-delete:hover { border-color: #fecaca; background: #fef2f2; color: #dc2626; }

/* 관련 글 */
.related-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 1.25rem;
  margin-top: 0.75rem;
}
.related-list { list-style: none; padding: 0; margin: 0; display: flex; flex-direction: column; gap: 0.1rem; }
.related-item {
  display: flex;
  align-items: center;
  gap: 0.5rem;
  padding: 0.4rem 0.5rem;
  border-radius: 6px;
  transition: background 0.15s;
}
.related-item:hover { background: #f9fafb; }
.related-num { font-size: 0.68rem; font-weight: 700; color: #e5e7eb; flex-shrink: 0; }
.related-link {
  flex: 1;
  font-size: 0.78rem;
  color: #4b5563;
  text-decoration: none;
  line-height: 1.45;
  transition: color 0.15s;
  min-width: 0;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}
.related-item:hover .related-link { color: #111827; }
.related-arrow {
  width: 12px;
  height: 12px;
  color: #d1d5db;
  flex-shrink: 0;
  transition: color 0.15s;
}
.related-item:hover .related-arrow { color: #9ca3af; }

/* ── 오른쪽: TOC ── */
.toc-sidebar {
  position: sticky;
  top: 5.5rem;
  min-width: 0;
}

.toc-card {
  background: #fff;
  border: 1px solid #f0f0f0;
  border-radius: 12px;
  padding: 1.25rem;
}

.toc-label {
  font-size: 0.7rem;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.08em;
  margin: 0 0 0.75rem;
}

.toc-list {
  list-style: none;
  padding: 0;
  margin: 0;
  counter-reset: toc-counter;
  display: flex;
  flex-direction: column;
  gap: 0.1rem;
}

.toc-item { counter-increment: toc-counter; }

.toc-link {
  display: block;
  width: 100%;
  text-align: left;
  background: none;
  border: none;
  cursor: pointer;
  font-family: inherit;
  font-size: 0.8rem;
  color: #6b7280;
  line-height: 1.5;
  padding: 0.25rem 0.5rem;
  border-radius: 4px;
  transition: all 0.15s;
  word-break: keep-all;
}
.toc-link::before {
  content: counter(toc-counter) ". ";
  color: #d1d5db;
  font-weight: 700;
  font-size: 0.72rem;
}
.toc-link:hover { color: #111827; background: #f9fafb; }

.toc-level-2 .toc-link { padding-left: 1.25rem; font-size: 0.77rem; }
.toc-level-3 .toc-link { padding-left: 2rem; font-size: 0.75rem; }

/* ── 에러 ── */
.error-wrap {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 1rem;
  padding: 5rem 0;
  color: #9ca3af;
}
.back-home-btn {
  padding: 0.5rem 1.25rem;
  background: #111827;
  color: #fff;
  border: none;
  border-radius: 6px;
  font-size: 0.875rem;
  cursor: pointer;
  font-family: inherit;
}

/* ── 반응형 ── */
@media (max-width: 1200px) {
  .detail-layout {
    grid-template-columns: 180px 1fr 220px;
    gap: 2rem;
  }
}
@media (max-width: 960px) {
  .detail-layout {
    grid-template-columns: 1fr;
  }
  .author-sidebar {
    position: static;
    order: -1;
  }
  .author-card {
    flex-direction: row;
    text-align: left;
    padding: 1rem 1.25rem;
    flex-wrap: wrap;
    gap: 0.75rem;
  }
  .author-avatar { width: 48px; height: 48px; font-size: 1.1rem; margin-bottom: 0; }
  .author-divider { display: none; }
  .action-subscribe { width: auto; padding: 0.45rem 1rem; }
  .toc-sidebar { display: none; }
  .article-main { padding: 2rem 1.5rem; }
}
@media (max-width: 640px) {
  .detail-container { padding: 1.5rem 1rem 3rem; }
  .article-main { padding: 1.5rem 1.25rem; }
  .post-title { font-size: 1.5rem; }
}
</style>

