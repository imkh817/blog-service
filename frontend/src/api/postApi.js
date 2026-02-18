import api from './index'

export const postApi = {
  create(memberId, data) {
    return api.post(`/posts?memberId=${memberId}`, data)
  },

  update(memberId, data) {
    return api.put(`/posts?memberId=${memberId}`, data)
  },

  getById(postId) {
    return api.get(`/posts/${postId}`)
  },

  search(params) {
    return api.get('/posts', { params })
  },

  publish(postId) {
    return api.post(`/posts/${postId}/publish`)
  },

  hide(postId) {
    return api.post(`/posts/${postId}/hide`)
  },

  delete(postId) {
    return api.post(`/posts/${postId}/delete`)
  },
}
