import api from './index'

export const commentApi = {
  getByPostId(postId) {
    return api.get(`/posts/${postId}/comments`)
  },

  create(postId, data) {
    return api.post(`/posts/${postId}/comments`, data)
  },
}
