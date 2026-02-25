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
    } catch { /* best-effort */ }
    const item = notifications.value.find(n => n.id === id)
    if (item && !item.isRead) {
      item.isRead = true
      unreadCount.value = Math.max(0, unreadCount.value - 1)
    }
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
  }
})