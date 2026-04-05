import axios from 'axios'

const http = axios.create({
  baseURL: 'http://localhost:8080',
  timeout: 10000
})

http.interceptors.request.use((config) => {
  const token = localStorage.getItem('carbonfootprint-token')
  if (token) {
    config.headers.Authorization = `Bearer ${token}`
  }
  return config
})

http.interceptors.response.use(
  (response) => response.data,
  (error) => {
    const message = error.response?.data?.message || error.message || 'Request failed'
    return Promise.reject(new Error(message))
  }
)

export default http
