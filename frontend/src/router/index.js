import { createRouter, createWebHistory } from 'vue-router'

const requireAuth = (to, from, next) => {
  if (typeof window !== 'undefined' && window.localStorage.getItem('token') && window.localStorage.getItem('userId')) {
    next()
    return
  }

  next('/login')
}

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
    path: '/profile',
    name: 'Profile',
    component: () => import('../views/Profile.vue'),
    beforeEnter: requireAuth
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
  },
  {
    path: '/diary',
    name: 'Diary',
    component: () => import('../views/Diary.vue')
  }
]

const router = createRouter({
  history: createWebHistory(),
  routes
})

export default router
