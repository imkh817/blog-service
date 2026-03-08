import api from './index'

export const postApi = {
  getPresignedUrl(fileName, contentType, imageType) {
    return api.post('/images/presigned-url', { fileName, contentType, imageType })
  },

  create(memberId, data) {
    return api.post(`/posts?memberId=${memberId}`, data)
  },

  update(memberId, data) {
    return api.put(`/posts?memberId=${memberId}`, data)
  },

  getById(postId) {
    return api.get(`/posts/${postId}`)
  },

  getList(params) {
    return api.get('/posts', { params })
  },

  search(params) {
    return api.get('/posts/search', { params })
  },

  saveDraft(data) {
    return api.post('/posts/draft', data)
  },

  getMyPosts(params) {
    return api.get('/posts/my', { params })
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

  like(postId) {
    return api.post(`/posts/${postId}/likes`)
  },

  unlike(postId) {
    return api.delete(`/posts/${postId}/likes`)
  },
}
