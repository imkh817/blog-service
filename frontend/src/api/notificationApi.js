import api from './index'

export const notificationApi = {
  getUnread() {
    return api.get('/notifications/unread')
  },
  getAll() {
    return api.get('/notifications')
  },
  markAsRead(id) {
    return api.patch(`/notifications/${id}/read`)
  },
}