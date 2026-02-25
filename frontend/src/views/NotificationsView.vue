<script setup>
import { ref, computed, onMounted } from 'vue'
import { useRouter } from 'vue-router'
import BlogHeader from '../components/common/BlogHeader.vue'
import { useNotificationStore } from '../stores/notification'

const router     = useRouter()
const notifStore = useNotificationStore()

const filter      = ref('all')  // 'all' | 'unread'
const pageSize    = 15
let   currentPage = 0

// ── Filtered list ─────────────────────────────────────────────────────────
const filtered = computed(() =>
  filter.value === 'unread'
    ? notifStore.notifications.filter(n => !n.isRead)
    : notifStore.notifications
)

// ── Date grouping ─────────────────────────────────────────────────────────
const today    = new Date(); today.setHours(0, 0, 0, 0)
const weekAgo  = new Date(today); weekAgo.setDate(weekAgo.getDate() - 7)

const groups = computed(() => {
  const todayItems  = []
  const weekItems   = []
  const olderItems  = []

  for (const n of filtered.value) {
    const d = new Date(n.createdAt)
    if (d >= today)   todayItems.push(n)
    else if (d >= weekAgo) weekItems.push(n)
    else olderItems.push(n)
  }

  return [
    { label: '오늘',    items: todayItems  },
    { label: '이번 주', items: weekItems   },
    { label: '이전',    items: olderItems  },
  ].filter(g => g.items.length > 0)
})

// ── Load / paginate ───────────────────────────────────────────────────────
async function load(page = 0) {
  currentPage = page
  await notifStore.fetchNotifications(page, pageSize)
}

async function loadMore() {
  await load(currentPage + 1)
}

const hasMore = computed(() =>
  currentPage + 1 < notifStore.totalPages && filter.value === 'all'
)

// ── Actions ───────────────────────────────────────────────────────────────
async function markAll() {
  await notifStore.markAllAsRead()
}

async function handleItem(n) {
  if (!n.isRead) notifStore.markAsRead(n.id)
  if (n.postId) router.push({ name: 'PostDetail', params: { id: n.postId } })
}

function handleSearch(keyword) {
  router.push({ name: 'Home', query: { keyword } })
}

function handleTagSelect(tag) {
  router.push({ name: 'Home', query: { tag } })
}

// ── Helpers ───────────────────────────────────────────────────────────────
const TYPE = {
  COMMENT: { color: '#4776E6', bg: '#EFF4FF', label: '댓글' },
  LIKE:    { color: '#ef4444', bg: '#FFF1F1', label: '좋아요' },
  FOLLOW:  { color: '#10b981', bg: '#F0FDF4', label: '팔로우' },
}
function typeStyle(type) {
  return TYPE[type] || { color: '#9ca3af', bg: '#f3f4f6', label: '알림' }
}

function timeAgo(dateStr) {
  const diff = Math.floor((Date.now() - new Date(dateStr)) / 1000)
  if (diff < 60)     return '방금 전'
  if (diff < 3600)   return `${Math.floor(diff / 60)}분 전`
  if (diff < 86400)  return `${Math.floor(diff / 3600)}시간 전`
  if (diff < 604800) return `${Math.floor(diff / 86400)}일 전`
  return new Date(dateStr).toLocaleDateString('ko-KR', { year: 'numeric', month: 'long', day: 'numeric' })
}

onMounted(() => load(0))
</script>

<template>
  <div>
    <BlogHeader @search="handleSearch" @tag-select="handleTagSelect" />

    <div class="notif-page">

      <!-- ── Page header ── -->
      <div class="page-head">
        <div class="page-head-left">
          <h1 class="page-title">알림</h1>
          <span v-if="notifStore.unreadCount" class="unread-pill">
            {{ notifStore.unreadCount }}개 읽지 않음
          </span>
        </div>
        <button
          v-if="notifStore.unreadCount"
          class="mark-all-btn"
          @click="markAll"
        >
          <svg viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M16.707 5.293a1 1 0 010 1.414l-8 8a1 1 0 01-1.414 0l-4-4a1 1 0 011.414-1.414L8 12.586l7.293-7.293a1 1 0 011.414 0z" clip-rule="evenodd"/>
          </svg>
          모두 읽음 처리
        </button>
      </div>

      <!-- ── Filter tabs ── -->
      <div class="filter-bar">
        <button
          :class="['filter-btn', { active: filter === 'all' }]"
          @click="filter = 'all'"
        >전체</button>
        <button
          :class="['filter-btn', { active: filter === 'unread' }]"
          @click="filter = 'unread'"
        >
          안 읽음
          <span v-if="notifStore.unreadCount" class="filter-badge">
            {{ notifStore.unreadCount }}
          </span>
        </button>
      </div>

      <!-- ── Loading (initial) ── -->
      <div v-if="notifStore.loading && notifStore.notifications.length === 0" class="loading-wrap">
        <div class="loading-dots"><span/><span/><span/></div>
      </div>

      <!-- ── Empty state ── -->
      <div v-else-if="!groups.length" class="empty-state">
        <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="empty-icon">
          <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke-linecap="round" stroke-linejoin="round"/>
          <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke-linecap="round" stroke-linejoin="round"/>
        </svg>
        <p class="empty-title">
          {{ filter === 'unread' ? '읽지 않은 알림이 없습니다' : '알림이 없습니다' }}
        </p>
        <p class="empty-sub">새로운 활동이 생기면 여기에 표시됩니다.</p>
      </div>

      <!-- ── Grouped list ── -->
      <template v-else>
        <section v-for="group in groups" :key="group.label" class="group">
          <h2 class="group-label">{{ group.label }}</h2>

          <div class="notif-card">
            <div
              v-for="(n, idx) in group.items"
              :key="n.id"
              :class="['notif-item', { 'notif-item--unread': !n.isRead, 'notif-item--last': idx === group.items.length - 1 }]"
              @click="handleItem(n)"
            >
              <!-- Type icon -->
              <span
                class="type-icon"
                :style="{ background: typeStyle(n.type).bg, color: typeStyle(n.type).color }"
              >
                <!-- COMMENT -->
                <svg v-if="n.type === 'COMMENT'" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M18 10c0 3.866-3.582 7-8 7a8.841 8.841 0 01-4.083-.98L2 17l1.338-3.123C2.493 12.767 2 11.434 2 10c0-3.866 3.582-7 8-7s8 3.134 8 7zM7 9H5v2h2V9zm8 0h-2v2h2V9zM9 9h2v2H9V9z" clip-rule="evenodd"/>
                </svg>
                <!-- LIKE -->
                <svg v-else-if="n.type === 'LIKE'" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M3.172 5.172a4 4 0 015.656 0L10 6.343l1.172-1.171a4 4 0 115.656 5.656L10 17.657l-6.828-6.829a4 4 0 010-5.656z" clip-rule="evenodd"/>
                </svg>
                <!-- FOLLOW -->
                <svg v-else-if="n.type === 'FOLLOW'" viewBox="0 0 20 20" fill="currentColor">
                  <path d="M8 9a3 3 0 100-6 3 3 0 000 6zM8 11a6 6 0 016 6H2a6 6 0 016-6zM16 7a1 1 0 10-2 0v1h-1a1 1 0 100 2h1v1a1 1 0 102 0v-1h1a1 1 0 100-2h-1V7z"/>
                </svg>
                <!-- Default -->
                <svg v-else viewBox="0 0 20 20" fill="currentColor">
                  <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z"/>
                </svg>
              </span>

              <!-- Body -->
              <div class="notif-body">
                <p class="notif-msg">{{ n.message }}</p>
                <div class="notif-meta">
                  <span class="notif-type-label">{{ typeStyle(n.type).label }}</span>
                  <span class="notif-sep">·</span>
                  <span class="notif-time">{{ timeAgo(n.createdAt) }}</span>
                </div>
              </div>

              <!-- Right side -->
              <div class="notif-right">
                <span v-if="!n.isRead" class="unread-dot" />
                <svg v-if="n.postId" class="chevron" viewBox="0 0 20 20" fill="currentColor">
                  <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
                </svg>
              </div>
            </div>
          </div>
        </section>

        <!-- Load more -->
        <div v-if="hasMore" class="load-more-wrap">
          <button class="load-more-btn" :disabled="notifStore.loading" @click="loadMore">
            <svg v-if="notifStore.loading" class="loading-spin" viewBox="0 0 24 24" fill="none">
              <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" style="opacity:.2"/>
              <path d="M4 12a8 8 0 018-8" stroke="currentColor" stroke-width="3" stroke-linecap="round"/>
            </svg>
            {{ notifStore.loading ? '불러오는 중...' : '더 보기' }}
          </button>
        </div>
      </template>

    </div>
  </div>
</template>

<style scoped>
.notif-page {
  max-width: 640px;
  margin: 0 auto;
  padding: 2.5rem 1.25rem 4rem;
}

/* ── Page header ── */
.page-head {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 1.25rem;
  gap: 1rem;
}
.page-head-left {
  display: flex;
  align-items: center;
  gap: 0.6rem;
}
.page-title {
  font-size: 1.4rem;
  font-weight: 800;
  color: #0f172a;
  margin: 0;
  letter-spacing: -0.02em;
}
.unread-pill {
  padding: 0.2rem 0.6rem;
  background: #eff4ff;
  color: #4776E6;
  font-size: 0.72rem;
  font-weight: 600;
  border-radius: 999px;
}
.mark-all-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.3rem;
  padding: 0.45rem 0.9rem;
  background: none;
  border: 1.5px solid #e5e7eb;
  border-radius: 8px;
  font-size: 0.78rem;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  font-family: inherit;
  white-space: nowrap;
  transition: all 0.14s;
}
.mark-all-btn svg { width: 13px; height: 13px; }
.mark-all-btn:hover {
  border-color: #4776E6;
  color: #4776E6;
  background: #f5f8ff;
}

/* ── Filter bar ── */
.filter-bar {
  display: flex;
  gap: 0.25rem;
  padding: 0.3rem;
  background: #f3f4f6;
  border-radius: 10px;
  width: fit-content;
  margin-bottom: 1.75rem;
}
.filter-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.35rem;
  padding: 0.4rem 1rem;
  border-radius: 7px;
  border: none;
  background: none;
  font-size: 0.82rem;
  font-weight: 500;
  color: #6b7280;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.14s;
}
.filter-btn.active {
  background: #fff;
  color: #111827;
  font-weight: 600;
  box-shadow: 0 1px 4px rgba(0,0,0,0.08);
}
.filter-badge {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 16px;
  height: 16px;
  padding: 0 3px;
  background: #4776E6;
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  border-radius: 999px;
}

/* ── Loading ── */
.loading-wrap {
  display: flex;
  justify-content: center;
  padding: 4rem 0;
}
.loading-dots {
  display: flex;
  gap: 5px;
  align-items: center;
}
.loading-dots span {
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: linear-gradient(135deg, #4776E6, #8E54E9);
  animation: pulse 1.2s ease-in-out infinite;
}
.loading-dots span:nth-child(2) { animation-delay: 0.15s; }
.loading-dots span:nth-child(3) { animation-delay: 0.3s; }
@keyframes pulse {
  0%, 80%, 100% { transform: scale(0.5); opacity: 0.4; }
  40%           { transform: scale(1);   opacity: 1; }
}

/* ── Empty state ── */
.empty-state {
  text-align: center;
  padding: 5rem 1rem;
  display: flex;
  flex-direction: column;
  align-items: center;
  gap: 0.5rem;
}
.empty-icon {
  width: 40px;
  height: 40px;
  color: #d1d5db;
  margin-bottom: 0.5rem;
}
.empty-title {
  font-size: 0.95rem;
  font-weight: 600;
  color: #374151;
  margin: 0;
}
.empty-sub {
  font-size: 0.82rem;
  color: #9ca3af;
  margin: 0;
}

/* ── Groups ── */
.group {
  margin-bottom: 1.75rem;
}
.group-label {
  font-size: 0.72rem;
  font-weight: 700;
  color: #9ca3af;
  text-transform: uppercase;
  letter-spacing: 0.07em;
  margin: 0 0 0.5rem;
}

/* ── Notification card ── */
.notif-card {
  background: #fff;
  border: 1px solid #e5e7eb;
  border-radius: 12px;
  overflow: hidden;
}

.notif-item {
  display: flex;
  align-items: center;
  gap: 0.75rem;
  padding: 0.85rem 1rem;
  cursor: pointer;
  transition: background 0.12s;
  border-bottom: 1px solid #f3f4f6;
  position: relative;
}
.notif-item--last {
  border-bottom: none;
}
.notif-item:hover {
  background: #f9fafb;
}
.notif-item--unread {
  background: #f8faff;
}
.notif-item--unread:hover {
  background: #eff4ff;
}
/* Left accent for unread items */
.notif-item--unread::before {
  content: '';
  position: absolute;
  left: 0;
  top: 0;
  bottom: 0;
  width: 3px;
  background: linear-gradient(180deg, #4776E6, #8E54E9);
  border-radius: 0 2px 2px 0;
}

/* ── Type icon ── */
.type-icon {
  flex-shrink: 0;
  width: 36px;
  height: 36px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
}
.type-icon svg {
  width: 16px;
  height: 16px;
}

/* ── Notification body ── */
.notif-body {
  flex: 1;
  min-width: 0;
}
.notif-msg {
  font-size: 0.84rem;
  color: #1f2937;
  margin: 0 0 0.25rem;
  line-height: 1.45;
}
.notif-meta {
  display: flex;
  align-items: center;
  gap: 0.3rem;
}
.notif-type-label {
  font-size: 0.7rem;
  font-weight: 600;
  color: #9ca3af;
}
.notif-sep {
  font-size: 0.7rem;
  color: #d1d5db;
}
.notif-time {
  font-size: 0.7rem;
  color: #9ca3af;
}

/* ── Right side ── */
.notif-right {
  display: flex;
  align-items: center;
  gap: 0.35rem;
  flex-shrink: 0;
}
.unread-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #4776E6;
}
.chevron {
  width: 14px;
  height: 14px;
  color: #d1d5db;
}

/* ── Load more ── */
.load-more-wrap {
  display: flex;
  justify-content: center;
  margin-top: 1rem;
}
.load-more-btn {
  display: inline-flex;
  align-items: center;
  gap: 0.4rem;
  padding: 0.6rem 1.75rem;
  background: #fff;
  border: 1.5px solid #e5e7eb;
  border-radius: 10px;
  font-size: 0.82rem;
  font-weight: 600;
  color: #374151;
  cursor: pointer;
  font-family: inherit;
  transition: all 0.14s;
}
.load-more-btn:hover:not(:disabled) {
  border-color: #4776E6;
  color: #4776E6;
}
.load-more-btn:disabled {
  opacity: 0.6;
  cursor: default;
}
.loading-spin {
  width: 14px;
  height: 14px;
  animation: nb-rotate 0.8s linear infinite;
}
@keyframes nb-rotate { to { transform: rotate(360deg); } }
</style>