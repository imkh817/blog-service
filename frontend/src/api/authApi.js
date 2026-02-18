import api from './index'

export const authApi = {
  login(data) {
    return api.post('/auth/login', data)
  },

  signup(data) {
    return api.post('/members', data)
  },

  getMe() {
    return api.get('/members/me')
  },

  refresh() {
    return api.post('/auth/refresh')
  },

  logout(accessToken) {
    return api.post('/auth/logout', null, {
      headers: { Authorization: `Bearer ${accessToken}` },
    })
  },
}
