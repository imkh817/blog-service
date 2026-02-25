import api from './index'

export const notificationApi = {
  getAll(params) {
    return api.get('/notifications', { params })
  },
  getUnreadCount() {
    return api.get('/notifications/unread-count')
  },
  markAsRead(id) {
    return api.patch(`/notifications/${id}/read`)
  },
  markAllAsRead() {
    return api.patch('/notifications/read-all')
  },
}