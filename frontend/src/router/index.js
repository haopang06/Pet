import { createRouter, createWebHistory } from 'vue-router'

const routes = [
  {
    path: '/',
    name: 'Home',
    component: () => import('../views/Home.vue')
  },
  {
    path: '/login',
    name: 'Login',
    component: () => import('../views/Login.vue')
  },
  {
    path: '/register',
    name: 'Register',
    component: () => import('../views/Register.vue')
  },
  {
    path: '/pets',
    name: 'Pets',
    component: () => import('../views/Pets.vue')
  },
  {
    path: '/feeding',
    name: 'Feeding',
    component: () => import('../views/Feeding.vue')
  },
  {
    path: '/health',
    name: 'Health',
    component: () => import('../views/Health.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router