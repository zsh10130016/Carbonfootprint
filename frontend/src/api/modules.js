import http from './http'

export const authApi = {
  register: (payload) => http.post('/api/auth/register', payload).then((res) => res.data),
  login: (payload) => http.post('/api/auth/login', payload).then((res) => res.data),
  resetPassword: (payload) => http.post('/api/auth/password/reset', payload).then((res) => res.data)
}

export const userApi = {
  getMe: () => http.get('/api/users/me').then((res) => res.data),
  updateMe: (payload) => http.put('/api/users/me', payload).then((res) => res.data),
  changePassword: (payload) => http.put('/api/users/password', payload).then((res) => res.data)
}

export const recordApi = {
  create: (payload) => http.post('/api/records', payload).then((res) => res.data),
  update: (id, payload) => http.put(`/api/records/${id}`, payload).then((res) => res.data),
  remove: (id) => http.delete(`/api/records/${id}`).then((res) => res.data),
  list: (params) => http.get('/api/records', { params }).then((res) => res.data),
  detail: (id) => http.get(`/api/records/${id}`).then((res) => res.data)
}

export const dashboardApi = {
  summary: () => http.get('/api/dashboard/summary').then((res) => res.data),
  trend: () => http.get('/api/dashboard/trend').then((res) => res.data),
  sourceRatio: () => http.get('/api/dashboard/source-ratio').then((res) => res.data),
  categories: () => http.get('/api/dashboard/categories').then((res) => res.data)
}

export const adviceApi = {
  list: () => http.get('/api/advice').then((res) => res.data)
}

export const communityApi = {
  rankings: () => http.get('/api/community/rankings').then((res) => res.data),
  me: () => http.get('/api/community/me').then((res) => res.data)
}

export const articleApi = {
  list: () => http.get('/api/articles').then((res) => res.data),
  detail: (id) => http.get(`/api/articles/${id}`).then((res) => res.data)
}

export const ocrApi = {
  parse: (payload) => http.post('/api/ocr/parse', payload).then((res) => res.data)
}
