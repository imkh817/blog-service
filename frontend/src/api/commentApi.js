import api from './index'

export const commentApi = {
  // GET /api/v1/posts/{postId}/comments
  getByPostId(postId, params = {}) {
    return api.get(`/posts/${postId}/comments`, { params })
  },

  // GET /api/v1/posts/{postId}/comments/{commentId}
  getById(postId, commentId) {
    return api.get(`/posts/${postId}/comments/${commentId}`)
  },

  // POST /api/v1/posts/{postId}/comments
  create(postId, data) {
    return api.post(`/posts/${postId}/comments`, data)
  },
}
