<script setup>
import { computed } from 'vue'

const props = defineProps({
  post: { type: Object, required: true },
})

const thumbStyle = computed(() => {
  const palettes = [
    ['#818cf8', '#a78bfa'],
    ['#60a5fa', '#818cf8'],
    ['#34d399', '#60a5fa'],
    ['#f472b6', '#818cf8'],
    ['#fb923c', '#f472b6'],
    ['#a3e635', '#34d399'],
    ['#38bdf8', '#818cf8'],
    ['#e879f9', '#818cf8'],
  ]
  const [from, to] = palettes[(props.post.postId || 0) % palettes.length]
  return { background: `linear-gradient(135deg, ${from} 0%, ${to} 100%)` }
})

const formattedDate = computed(() => {
  if (!props.post.createdAt) return ''
  const d = new Date(props.post.createdAt)
  return `${d.getFullYear()}. ${String(d.getMonth() + 1).padStart(2, '0')}. ${String(d.getDate()).padStart(2, '0')}.`
})
</script>

<template>
  <router-link
    :to="{ name: 'PostDetail', params: { id: post.postId } }"
    class="post-item"
  >
    <!-- 본문 영역 -->
    <div class="item-body">
      <!-- 상단: 작성자 + 날짜 -->
      <div class="item-top">
        <div class="item-author">
          <div class="item-avatar">{{ post.authorNickname?.charAt(0)?.toUpperCase() || 'U' }}</div>
          <span class="item-name">{{ post.authorNickname || '익명' }}</span>
        </div>
        <span class="item-date">{{ formattedDate }}</span>
      </div>

      <!-- 제목 -->
      <h2 class="item-title">{{ post.title }}</h2>

      <!-- 요약 -->
      <p class="item-excerpt">{{ post.content }}</p>

      <!-- 하단: 태그 + 통계 -->
      <div class="item-bottom">
        <div class="item-tags" v-if="post.tags?.length">
          <span
            v-for="tag in post.tags.slice(0, 3)"
            :key="tag"
            class="item-tag"
          >{{ tag }}</span>
        </div>
        <div class="item-stats">
          <span class="item-stat">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M20.84 4.61a5.5 5.5 0 0 0-7.78 0L12 5.67l-1.06-1.06a5.5 5.5 0 0 0-7.78 7.78l1.06 1.06L12 21.23l7.78-7.78 1.06-1.06a5.5 5.5 0 0 0 0-7.78z"/></svg>
            {{ post.likeCount ?? 0 }}
          </span>
          <span class="item-stat">
            <svg width="12" height="12" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="2"><path d="M21 15a2 2 0 0 1-2 2H7l-4 4V5a2 2 0 0 1 2-2h14a2 2 0 0 1 2 2z"/></svg>
            {{ post.commentCount ?? 0 }}
          </span>
        </div>
      </div>
    </div>

    <!-- 썸네일 -->
    <div class="item-thumb" :style="thumbStyle">
      <span class="item-thumb-letter">{{ post.title?.charAt(0)?.toUpperCase() || 'B' }}</span>
    </div>
  </router-link>
</template>

<style scoped>
.post-item {
  display: flex;
  align-items: flex-start;
  gap: 2rem;
  padding: 2.25rem 0;
  border-bottom: 1px solid #f3f4f6;
  text-decoration: none;
  cursor: pointer;
  transition: none;
}
.post-item:last-child {
  border-bottom: none;
}

/* 본문 */
.item-body {
  flex: 1;
  min-width: 0;
  display: flex;
  flex-direction: column;
  gap: 0.55rem;
}

/* 상단 행 */
.item-top {
  display: flex;
  align-items: center;
  justify-content: space-between;
}

.item-author {
  display: flex;
  align-items: center;
  gap: 0.5rem;
}

.item-avatar {
  width: 22px;
  height: 22px;
  border-radius: 50%;
  background: #e5e7eb;
  color: #6b7280;
  font-size: 0.65rem;
  font-weight: 700;
  display: flex;
  align-items: center;
  justify-content: center;
  flex-shrink: 0;
}

.item-name {
  font-size: 0.8125rem;
  color: #6b7280;
  font-weight: 500;
}

.item-date {
  font-size: 0.8125rem;
  color: #9ca3af;
  flex-shrink: 0;
}

/* 제목 */
.item-title {
  font-size: 1.375rem;
  font-weight: 700;
  color: #111827;
  margin: 0;
  line-height: 1.4;
  letter-spacing: -0.015em;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  transition: color 0.15s;
}

.post-item:hover .item-title {
  color: #4776E6;
}

/* 요약 */
.item-excerpt {
  font-size: 0.9375rem;
  color: #6b7280;
  line-height: 1.75;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
}

/* 하단 행 */
.item-bottom {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-top: 0.25rem;
}

.item-tags {
  display: flex;
  align-items: center;
  gap: 0.4rem;
  flex-wrap: wrap;
}

.item-tag {
  display: inline-block;
  padding: 0.15rem 0.6rem;
  border: 1px solid #e5e7eb;
  border-radius: 999px;
  font-size: 0.75rem;
  color: #6b7280;
  font-weight: 500;
  transition: all 0.15s;
}
.post-item:hover .item-tag {
  border-color: #c7d2fe;
  color: #4776E6;
}

.item-stats {
  display: flex;
  align-items: center;
  gap: 0.75rem;
}

.item-stat {
  display: flex;
  align-items: center;
  gap: 0.3rem;
  font-size: 0.8125rem;
  color: #9ca3af;
}

/* 썸네일 */
.item-thumb {
  flex-shrink: 0;
  width: 140px;
  height: 88px;
  border-radius: 6px;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 0.25rem;
  opacity: 0.72;
  transition: opacity 0.2s;
}
.post-item:hover .item-thumb {
  opacity: 0.88;
}
.item-thumb-letter {
  font-size: 2.25rem;
  font-weight: 900;
  color: rgba(255, 255, 255, 0.22);
  user-select: none;
}

@media (max-width: 640px) {
  .item-thumb {
    width: 96px;
    height: 64px;
  }
  .item-title {
    font-size: 1.125rem;
  }
}
</style>
