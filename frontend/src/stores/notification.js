import { ref } from 'vue'
import { defineStore } from 'pinia'
import { notificationApi } from '../api/notificationApi'

export const useNotificationStore = defineStore('notification', () => {
  // ── State ────────────────────────────────────────────────────────────────
  const notifications = ref([])   // full list used by NotificationsView
  const unreadCount   = ref(0)    // badge count – polled every 30 s
  const totalPages    = ref(0)
  const loading       = ref(false)

  let pollingTimer = null
  let sseController = null

  // ── Actions ───────────────────────────────────────────────────────────────

  /** Poll-friendly: fetch only the unread count without touching the list */
  async function fetchUnreadCount() {
    try {
      const res = await notificationApi.getUnreadCount()
      unreadCount.value = res.data.data ?? 0
    } catch { /* silent – don't break the UI on poll failure */ }
  }

  /**
   * Fetch a page of notifications.
   * page = 0  → replaces the list (used by both bell preview and page initial load)
   * page > 0  → appends (used by "load more" in NotificationsView)
   */
  async function fetchNotifications(page = 0, size = 15) {
    loading.value = true
    try {
      const res = await notificationApi.getAll({ page, size, sort: 'createdAt,desc' })
      const items = res.data.data || []
      notifications.value = page === 0 ? items : [...notifications.value, ...items]
      const total = parseInt(res.headers['x-total-count'] || '0', 10)
      totalPages.value = Math.ceil(total / size)
    } finally {
      loading.value = false
    }
  }

  async function markAsRead(id) {
    try {
      await notificationApi.markAsRead(id)
      const item = notifications.value.find(n => n.id === id)
      if (item && !item.isRead) {
        item.isRead = true
        unreadCount.value = Math.max(0, unreadCount.value - 1)
      }
    } catch { /* 무시 */ }
  }

  async function markAllAsRead() {
    try {
      await notificationApi.markAllAsRead()
    } catch { /* best-effort */ }
    notifications.value.forEach(n => { n.isRead = true })
    unreadCount.value = 0
  }

  // ── Polling ───────────────────────────────────────────────────────────────

  function startPolling() {
    stopPolling()
    fetchUnreadCount()
    pollingTimer = setInterval(fetchUnreadCount, 30_000)
  }

  function stopPolling() {
    if (pollingTimer) { clearInterval(pollingTimer); pollingTimer = null }
  }

  // ── Unread 조회 ───────────────────────────────────────────────────────────

  async function loadUnread() {
    try {
      const res = await notificationApi.getUnread()
      const items = res.data.data ?? []
      notifications.value = items.map(n => ({ ...n, isRead: false }))
      unreadCount.value = items.length
    } catch { /* 무시 */ }
  }

  // ── SSE ───────────────────────────────────────────────────────────────────

  async function connectSse(token) {
    disconnectSse()
    sseController = new AbortController()

    try {
      const response = await fetch('/api/v1/notifications/subscribe', {
        headers: {
          Authorization: `Bearer ${token}`,
          Accept: 'text/event-stream',
        },
        credentials: 'include',
        signal: sseController.signal,
      })

      // SSE 연결 직후 미읽은 알림 로드
      await loadUnread()

      const reader = response.body.getReader()
      const decoder = new TextDecoder()
      let buffer = ''

      while (true) {
        const { done, value } = await reader.read()
        if (done) break

        buffer += decoder.decode(value, { stream: true })

        // SSE 메시지는 빈 줄(\n\n)로 구분됨
        const parts = buffer.split('\n\n')
        buffer = parts.pop()

        for (const part of parts) {
          const lines = part.split('\n')
          let eventName = 'message'
          let data = ''

          for (const line of lines) {
            if (line.startsWith('event:')) eventName = line.slice(6).trim()
            if (line.startsWith('data:'))  data      = line.slice(5).trim()
          }

          if (data && eventName !== 'connect') {
            handleSseMessage(eventName, data)
          }
        }
      }
    } catch (e) {
      if (e.name !== 'AbortError') {
        console.warn('SSE 연결 끊김', e)
      }
    }
  }

  function disconnectSse() {
    if (sseController) {
      sseController.abort()
      sseController = null
    }
  }

  function handleSseMessage(eventName, data) {
    if (eventName === 'SUBSCRIBED') {
      try {
        const payload = JSON.parse(data)
        unreadCount.value++
        notifications.value.unshift({
          id: payload.notificationId,  // 실제 DB ID
          type: 'FOLLOW',
          message: payload.message ?? '새 구독 알림',
          isRead: false,
          createdAt: payload.createdAt ?? new Date().toISOString(),
        })
      } catch { /* JSON 파싱 실패 무시 */ }
    }
  }

  return {
    notifications,
    unreadCount,
    totalPages,
    loading,
    fetchUnreadCount,
    fetchNotifications,
    markAsRead,
    markAllAsRead,
    startPolling,
    stopPolling,
    loadUnread,
    connectSse,
    disconnectSse,
  }
})