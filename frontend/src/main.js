import { createApp } from 'vue'
import { createPinia } from 'pinia'
import router from './router'
import App from './App.vue'
import './style.css'
import 'highlight.js/styles/github.css'
import { useAuthStore } from './stores/auth'

const app = createApp(App)
const pinia = createPinia()
app.use(pinia)

const auth = useAuthStore()
auth.initialize().finally(() => {
  app.use(router)
  app.mount('#app')
})
