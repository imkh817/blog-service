import api from './index'

export const subscriptionApi = {
  subscribe(targetId) {
    return api.post(`/subscription/${targetId}`)
  },
  unsubscribe(targetId) {
    return api.delete(`/subscription/${targetId}`)
  },
}