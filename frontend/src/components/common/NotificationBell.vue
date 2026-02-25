<script setup>
import { ref, watch, onMounted, onBeforeUnmount } from 'vue'
import { useRouter } from 'vue-router'
import { useNotificationStore } from '../../stores/notification'

const router       = useRouter()
const notifStore   = useNotificationStore()

const open    = ref(false)
const loading = ref(false)

// ── Open / close ──────────────────────────────────────────────────────────
async function toggle() {
  open.value = !open.value
  if (open.value) {
    loading.value = true
    await notifStore.fetchNotifications(0, 5)
    loading.value = false
  }
}

function close() {
  open.value = false
}

// ── Item click ────────────────────────────────────────────────────────────
async function handleItem(n) {
  close()
  if (!n.isRead) notifStore.markAsRead(n.id)
  if (n.postId) router.push({ name: 'PostDetail', params: { id: n.postId } })
}

// ── Actions ───────────────────────────────────────────────────────────────
async function markAll() {
  await notifStore.markAllAsRead()
}

function viewAll() {
  close()
  router.push({ name: 'Notifications' })
}

// ── Outside click ─────────────────────────────────────────────────────────
function onOutside(e) {
  if (!e.target.closest('.nb-wrap')) close()
}
watch(open, (val) => {
  if (val) document.addEventListener('click', onOutside)
  else     document.removeEventListener('click', onOutside)
})

// ── Polling lifecycle ─────────────────────────────────────────────────────
onMounted(() => notifStore.startPolling())
onBeforeUnmount(() => {
  notifStore.stopPolling()
  document.removeEventListener('click', onOutside)
})

// ── Helpers ───────────────────────────────────────────────────────────────
const TYPE = {
  COMMENT: { color: '#4776E6', bg: '#EFF4FF' },
  LIKE:    { color: '#ef4444', bg: '#FFF1F1' },
  FOLLOW:  { color: '#10b981', bg: '#F0FDF4' },
}
function typeStyle(type) {
  return TYPE[type] || { color: '#9ca3af', bg: '#f3f4f6' }
}

function timeAgo(dateStr) {
  const diff = Math.floor((Date.now() - new Date(dateStr)) / 1000)
  if (diff < 60)    return '방금 전'
  if (diff < 3600)  return `${Math.floor(diff / 60)}분 전`
  if (diff < 86400) return `${Math.floor(diff / 3600)}시간 전`
  if (diff < 604800) return `${Math.floor(diff / 86400)}일 전`
  return new Date(dateStr).toLocaleDateString('ko-KR', { month: 'short', day: 'numeric' })
}

const badgeCount = ref('')
watch(() => notifStore.unreadCount, (n) => {
  badgeCount.value = n > 99 ? '99+' : n > 0 ? String(n) : ''
}, { immediate: true })
</script>

<template>
  <div class="nb-wrap">
    <!-- Bell button -->
    <button class="nb-btn" @click.stop="toggle" :class="{ active: open }" :aria-label="`알림${notifStore.unreadCount ? ` (${notifStore.unreadCount}개 읽지 않음)` : ''}`">
      <svg class="nb-icon" viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.8" stroke-linecap="round" stroke-linejoin="round">
        <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9"/>
        <path d="M13.73 21a2 2 0 0 1-3.46 0"/>
      </svg>
      <span v-if="badgeCount" class="nb-badge">{{ badgeCount }}</span>
    </button>

    <!-- Dropdown -->
    <Transition name="nb-drop">
      <div v-if="open" class="nb-dropdown">

        <!-- Header -->
        <div class="nb-header">
          <span class="nb-title">
            알림
            <span v-if="notifStore.unreadCount" class="nb-count-pill">{{ notifStore.unreadCount }}</span>
          </span>
          <button v-if="notifStore.unreadCount" class="nb-read-all" @click="markAll">
            모두 읽음
          </button>
        </div>

        <!-- Loading -->
        <div v-if="loading" class="nb-state">
          <svg class="nb-spin" viewBox="0 0 24 24" fill="none">
            <circle cx="12" cy="12" r="10" stroke="currentColor" stroke-width="3" class="nb-spin-track"/>
            <path d="M4 12a8 8 0 018-8" stroke="currentColor" stroke-width="3" stroke-linecap="round" class="nb-spin-arc"/>
          </svg>
        </div>

        <!-- Empty -->
        <div v-else-if="!notifStore.notifications.length" class="nb-state">
          <svg viewBox="0 0 24 24" fill="none" stroke="currentColor" stroke-width="1.5" class="nb-empty-icon">
            <path d="M18 8A6 6 0 0 0 6 8c0 7-3 9-3 9h18s-3-2-3-9" stroke-linecap="round" stroke-linejoin="round"/>
            <path d="M13.73 21a2 2 0 0 1-3.46 0" stroke-linecap="round" stroke-linejoin="round"/>
          </svg>
          <p>새 알림이 없습니다</p>
        </div>

        <!-- List -->
        <ul v-else class="nb-list">
          <li
            v-for="n in notifStore.notifications"
            :key="n.id"
            class="nb-item"
            :class="{ 'nb-item--unread': !n.isRead }"
            @click="handleItem(n)"
          >
            <!-- Type icon -->
            <span
              class="nb-type-icon"
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
              <!-- Default bell -->
              <svg v-else viewBox="0 0 20 20" fill="currentColor">
                <path d="M10 2a6 6 0 00-6 6v3.586l-.707.707A1 1 0 004 14h12a1 1 0 00.707-1.707L16 11.586V8a6 6 0 00-6-6zM10 18a3 3 0 01-3-3h6a3 3 0 01-3 3z"/>
              </svg>
            </span>

            <!-- Content -->
            <div class="nb-content">
              <p class="nb-msg">{{ n.message }}</p>
              <span class="nb-time">{{ timeAgo(n.createdAt) }}</span>
            </div>

            <!-- Unread dot -->
            <span v-if="!n.isRead" class="nb-dot" />
          </li>
        </ul>

        <!-- Footer -->
        <button class="nb-footer" @click="viewAll">
          전체 알림 보기
          <svg viewBox="0 0 20 20" fill="currentColor">
            <path fill-rule="evenodd" d="M7.293 14.707a1 1 0 010-1.414L10.586 10 7.293 6.707a1 1 0 011.414-1.414l4 4a1 1 0 010 1.414l-4 4a1 1 0 01-1.414 0z" clip-rule="evenodd"/>
          </svg>
        </button>
      </div>
    </Transition>
  </div>
</template>

<style scoped>
/* ── Wrapper ── */
.nb-wrap {
  position: relative;
}

/* ── Bell button ── */
.nb-btn {
  position: relative;
  display: inline-flex;
  align-items: center;
  justify-content: center;
  width: 34px;
  height: 34px;
  border-radius: 8px;
  border: 1.5px solid #e5e7eb;
  background: transparent;
  color: #6b7280;
  cursor: pointer;
  transition: all 0.15s;
}
.nb-btn:hover,
.nb-btn.active {
  border-color: #4776E6;
  color: #4776E6;
  background: rgba(71, 118, 230, 0.06);
}
.nb-icon {
  width: 16px;
  height: 16px;
}

/* ── Unread badge ── */
.nb-badge {
  position: absolute;
  top: -5px;
  right: -5px;
  min-width: 16px;
  height: 16px;
  padding: 0 3px;
  background: #ef4444;
  color: #fff;
  font-size: 9px;
  font-weight: 700;
  border-radius: 999px;
  display: flex;
  align-items: center;
  justify-content: center;
  line-height: 1;
  border: 1.5px solid #fff;
}

/* ── Dropdown shell ── */
.nb-dropdown {
  position: absolute;
  right: 0;
  top: calc(100% + 8px);
  width: 320px;
  background: #fff;
  border-radius: 14px;
  border: 1px solid #e5e7eb;
  box-shadow: 0 12px 36px rgba(0, 0, 0, 0.12);
  overflow: hidden;
  z-index: 999;
}

/* ── Dropdown header ── */
.nb-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 0.75rem 1rem;
  border-bottom: 1px solid #f3f4f6;
}
.nb-title {
  font-size: 0.875rem;
  font-weight: 700;
  color: #111827;
  display: flex;
  align-items: center;
  gap: 0.4rem;
}
.nb-count-pill {
  display: inline-flex;
  align-items: center;
  justify-content: center;
  min-width: 18px;
  height: 18px;
  padding: 0 4px;
  background: #4776E6;
  color: #fff;
  font-size: 10px;
  font-weight: 700;
  border-radius: 999px;
}
.nb-read-all {
  font-size: 0.72rem;
  font-weight: 500;
  color: #6b7280;
  background: none;
  border: none;
  cursor: pointer;
  padding: 0.25rem 0.5rem;
  border-radius: 5px;
  font-family: inherit;
  transition: all 0.12s;
}
.nb-read-all:hover {
  background: #f3f4f6;
  color: #111827;
}

/* ── Loading / empty states ── */
.nb-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  gap: 0.5rem;
  padding: 2rem 1rem;
  color: #9ca3af;
}
.nb-empty-icon {
  width: 28px;
  height: 28px;
  opacity: 0.4;
}
.nb-state p {
  font-size: 0.8rem;
  margin: 0;
}

/* ── Spinner ── */
.nb-spin {
  width: 22px;
  height: 22px;
  animation: nb-rotate 0.8s linear infinite;
}
.nb-spin-track { opacity: 0.15; }
.nb-spin-arc   { color: #4776E6; }
@keyframes nb-rotate { to { transform: rotate(360deg); } }

/* ── Notification list ── */
.nb-list {
  list-style: none;
  margin: 0;
  padding: 0.3rem 0;
  max-height: 300px;
  overflow-y: auto;
}

.nb-item {
  display: flex;
  align-items: flex-start;
  gap: 0.65rem;
  padding: 0.65rem 1rem;
  cursor: pointer;
  transition: background 0.12s;
  position: relative;
}
.nb-item:hover {
  background: #f9fafb;
}
.nb-item--unread {
  background: #f8faff;
}
.nb-item--unread:hover {
  background: #f0f4ff;
}

/* ── Type icon circle ── */
.nb-type-icon {
  flex-shrink: 0;
  width: 32px;
  height: 32px;
  border-radius: 50%;
  display: flex;
  align-items: center;
  justify-content: center;
  margin-top: 1px;
}
.nb-type-icon svg {
  width: 14px;
  height: 14px;
}

/* ── Content ── */
.nb-content {
  flex: 1;
  min-width: 0;
}
.nb-msg {
  font-size: 0.78rem;
  color: #374151;
  margin: 0 0 0.2rem;
  line-height: 1.45;
  overflow: hidden;
  display: -webkit-box;
  -webkit-box-orient: vertical;
  -webkit-line-clamp: 2;
}
.nb-time {
  font-size: 0.7rem;
  color: #9ca3af;
}

/* ── Unread indicator dot ── */
.nb-dot {
  flex-shrink: 0;
  width: 7px;
  height: 7px;
  border-radius: 50%;
  background: #4776E6;
  margin-top: 5px;
}

/* ── Footer link ── */
.nb-footer {
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 0.25rem;
  width: 100%;
  padding: 0.65rem 1rem;
  border-top: 1px solid #f3f4f6;
  background: none;
  border-left: none;
  border-right: none;
  border-bottom: none;
  font-size: 0.78rem;
  font-weight: 600;
  color: #4776E6;
  cursor: pointer;
  font-family: inherit;
  transition: background 0.12s;
}
.nb-footer:hover {
  background: #f8faff;
}
.nb-footer svg {
  width: 13px;
  height: 13px;
}

/* ── Dropdown transition ── */
.nb-drop-enter-active,
.nb-drop-leave-active {
  transition: all 0.18s cubic-bezier(0.4, 0, 0.2, 1);
}
.nb-drop-enter-from,
.nb-drop-leave-to {
  opacity: 0;
  transform: translateY(-6px) scale(0.98);
}
</style>