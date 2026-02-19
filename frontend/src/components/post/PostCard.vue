<script setup>
import { computed } from 'vue'
import PostStatusBadge from './PostStatusBadge.vue'

const props = defineProps({
  post: { type: Object, required: true },
})

const gradientStyle = computed(() => {
  const palettes = [
    ['#6366f1', '#8b5cf6'],
    ['#ec4899', '#f43f5e'],
    ['#14b8a6', '#06b6d4'],
    ['#f97316', '#f59e0b'],
    ['#ef4444', '#f97316'],
    ['#10b981', '#34d399'],
    ['#eab308', '#f97316'],
    ['#6366f1', '#a855f7'],
    ['#0ea5e9', '#6366f1'],
    ['#84cc16', '#10b981'],
  ]
  const [from, to] = palettes[(props.post.postId || 0) % palettes.length]
  return { background: `linear-gradient(135deg, ${from} 0%, ${to} 100%)` }
})

function formatDate(dateStr) {
  if (!dateStr) return ''
  const d = new Date(dateStr)
  return d.toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}
</script>

<template>
  <router-link
    :to="{ name: 'PostDetail', params: { id: post.postId } }"
    class="post-card"
  >
    <!-- 커버 이미지 -->
    <div class="post-card__cover" :style="gradientStyle">
      <span class="post-card__cover-letter">
        {{ post.title?.charAt(0)?.toUpperCase() || 'B' }}
      </span>
    </div>

    <!-- 콘텐츠 영역 -->
    <div class="post-card__body">
      <!-- 제목 -->
      <h2 class="post-card__title">{{ post.title }}</h2>

      <!-- 본문 요약 -->
      <p class="post-card__excerpt">{{ post.content }}</p>

      <!-- 태그 -->
      <div v-if="(post.tags || []).length" class="post-card__tags">
        <span
          v-for="tag in (post.tags || []).slice(0, 3)"
          :key="tag"
          class="post-card__tag"
        >
          {{ tag }}
        </span>
      </div>

      <!-- 날짜 -->
      <p class="post-card__date">{{ formatDate(post.createdAt) }}</p>

      <!-- 구분선 -->
      <hr class="post-card__divider" />

      <!-- 하단: 작성자 + 통계 -->
      <div class="post-card__footer">
        <div class="post-card__author">
          <div class="post-card__avatar">
            {{ post.authorNickname?.charAt(0)?.toUpperCase() || 'U' }}
          </div>
          <span class="post-card__nickname">{{ post.authorNickname }}</span>
        </div>

        <div class="post-card__stats">
          <span class="post-card__stat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
            </svg>
            {{ post.likeCount ?? 0 }}
          </span>
          <span class="post-card__stat">
            <svg width="14" height="14" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2" stroke-linecap="round" stroke-linejoin="round">
              <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
            </svg>
            {{ post.commentCount ?? 0 }}
          </span>
        </div>
      </div>

      <PostStatusBadge
        v-if="post.postStatus !== 'PUBLISHED'"
        :status="post.postStatus"
        class="mt-2"
      />
    </div>
  </router-link>
</template>

<style scoped>
.post-card {
  display: block;
  background: #ffffff;
  border-radius: 16px;
  box-shadow: 0 2px 12px rgba(0, 0, 0, 0.07);
  overflow: hidden;
  text-decoration: none;
  transition: transform 0.22s ease, box-shadow 0.22s ease;
}

.post-card:hover {
  transform: translateY(-5px);
  box-shadow: 0 10px 28px rgba(0, 0, 0, 0.13);
}

/* ── 커버 ── */
.post-card__cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  border-radius: 16px 16px 0 0;
}

.post-card__cover-letter {
  font-size: 4.5rem;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.22);
  user-select: none;
  line-height: 1;
}

/* ── 바디 ── */
.post-card__body {
  padding: 20px 20px 18px;
}

/* ── 제목 ── */
.post-card__title {
  font-size: 1.0625rem;
  font-weight: 700;
  color: #111827;
  margin: 0 0 10px;
  line-height: 1.5;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.18s;
}

.post-card:hover .post-card__title {
  color: #4f46e5;
}

/* ── 본문 요약 ── */
.post-card__excerpt {
  font-size: 0.875rem;
  color: #6b7280;
  line-height: 1.65;
  margin: 0 0 14px;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* ── 태그 ── */
.post-card__tags {
  display: flex;
  flex-wrap: wrap;
  gap: 6px;
  margin-bottom: 10px;
}

.post-card__tag {
  display: inline-block;
  padding: 3px 11px;
  background: #f3f4f6;
  color: #6b7280;
  font-size: 0.72rem;
  font-weight: 500;
  border-radius: 999px;
}

/* ── 날짜 ── */
.post-card__date {
  font-size: 0.75rem;
  color: #9ca3af;
  margin: 0 0 14px;
}

/* ── 구분선 ── */
.post-card__divider {
  border: none;
  border-top: 1px solid #f0f0f0;
  margin: 0 0 14px;
}

/* ── 푸터 ── */
.post-card__footer {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.post-card__author {
  display: flex;
  align-items: center;
  gap: 8px;
  min-width: 0;
}

.post-card__avatar {
  width: 28px;
  height: 28px;
  border-radius: 50%;
  background: #e5e7eb;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 0.75rem;
  font-weight: 700;
  color: #6b7280;
  flex-shrink: 0;
}

.post-card__nickname {
  font-size: 0.8125rem;
  color: #374151;
  white-space: nowrap;
  overflow: hidden;
  text-overflow: ellipsis;
}

.post-card__stats {
  display: flex;
  align-items: center;
  gap: 10px;
  flex-shrink: 0;
}

.post-card__stat {
  display: flex;
  align-items: center;
  gap: 4px;
  font-size: 0.75rem;
  color: #9ca3af;
}
</style>
