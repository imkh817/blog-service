<script setup>
import { computed } from 'vue'

const props = defineProps({
  post: { type: Object, required: true },
})

const coverStyle = computed(() => {
  const palettes = [
    ['#818cf8', '#a78bfa'],
    ['#60a5fa', '#818cf8'],
    ['#34d399', '#60a5fa'],
    ['#f472b6', '#818cf8'],
    ['#fb923c', '#f472b6'],
    ['#a3e635', '#34d399'],
    ['#38bdf8', '#818cf8'],
    ['#e879f9', '#818cf8'],
    ['#f59e0b', '#f472b6'],
    ['#4776E6', '#8E54E9'],
  ]
  const [from, to] = palettes[(props.post.postId || 0) % palettes.length]
  return { background: `linear-gradient(135deg, ${from} 0%, ${to} 100%)` }
})

const category = computed(() => props.post.tags?.[0] ?? null)

const formattedDate = computed(() => {
  if (!props.post.createdAt) return ''
  const d = new Date(props.post.createdAt)
  return `${d.getFullYear()}. ${String(d.getMonth() + 1).padStart(2, '0')}. ${String(d.getDate()).padStart(2, '0')}.`
})
</script>

<template>
  <router-link
    :to="{ name: 'PostDetail', params: { id: post.postId } }"
    class="post-card"
  >
    <!-- 커버 이미지 -->
    <div class="post-card__cover" :style="coverStyle">
      <span class="post-card__cover-letter">
        {{ post.title?.charAt(0)?.toUpperCase() || 'B' }}
      </span>
    </div>

    <!-- 바디 -->
    <div class="post-card__body">
      <!-- 카테고리 (첫 번째 태그) -->
      <p v-if="category" class="post-card__category">{{ category }}</p>

      <!-- 제목 -->
      <h2 class="post-card__title">{{ post.title }}</h2>

      <!-- 작성자 · 날짜 -->
      <p class="post-card__meta">
        <span>{{ post.authorNickname || '익명' }}</span>
        <span class="meta-dot">·</span>
        <span>{{ formattedDate }}</span>
      </p>

      <!-- 통계 -->
      <div class="post-card__stats">
        <span class="post-card__stat">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/>
          </svg>
          {{ post.likeCount ?? 0 }}
        </span>
        <span class="post-card__stat">
          <svg width="11" height="11" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2">
            <path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/>
          </svg>
          {{ post.commentCount ?? 0 }}
        </span>
      </div>
    </div>
  </router-link>
</template>

<style scoped>
.post-card {
  display: block;
  background: #fff;
  border-radius: 8px;
  overflow: hidden;
  box-shadow: 0 1px 3px rgba(0, 0, 0, 0.07), 0 1px 2px rgba(0, 0, 0, 0.04);
  text-decoration: none;
  transition: box-shadow 0.2s, transform 0.2s;
}
.post-card:hover {
  box-shadow: 0 8px 24px rgba(0, 0, 0, 0.12);
  transform: translateY(-3px);
}

/* 커버 */
.post-card__cover {
  width: 100%;
  aspect-ratio: 16 / 9;
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
}
.post-card__cover-letter {
  font-size: 3.5rem;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.18);
  user-select: none;
  line-height: 1;
}

/* 바디 */
.post-card__body {
  padding: 14px 16px 16px;
  display: flex;
  flex-direction: column;
  gap: 6px;
}

/* 카테고리 */
.post-card__category {
  margin: 0;
  font-size: 0.75rem;
  font-weight: 600;
  color: #0d9488;
  text-transform: lowercase;
}

/* 제목 */
.post-card__title {
  margin: 0;
  font-size: 1rem;
  font-weight: 700;
  color: #111827;
  line-height: 1.5;
  letter-spacing: -0.01em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.15s;
}
.post-card:hover .post-card__title {
  color: #4776E6;
}

/* 메타 */
.post-card__meta {
  margin: 0;
  display: flex;
  align-items: center;
  gap: 0.35rem;
  font-size: 0.75rem;
  color: #9ca3af;
}
.meta-dot {
  color: #d1d5db;
}

/* 통계 */
.post-card__stats {
  display: flex;
  align-items: center;
  gap: 0.6rem;
  margin-top: 2px;
}
.post-card__stat {
  display: flex;
  align-items: center;
  gap: 3px;
  font-size: 0.72rem;
  color: #d1d5db;
}
</style>
