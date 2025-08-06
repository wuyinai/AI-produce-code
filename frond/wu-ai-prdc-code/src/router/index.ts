import { createRouter, createWebHistory } from 'vue-router'
import HomeView from '../views/HomeView.vue'
import UserLogin from '../views/user/UserLogin.vue'
import UserRegister from '../views/user/UserRegister.vue'
import UserManage from '@/views/admin/UserManage.vue'

const router = createRouter({
  history: createWebHistory(import.meta.env.BASE_URL),
  routes: [
    {
      path: '/',
      name: 'home',
      component: HomeView,
    },
    {
      path: '/user/login',
      name: 'user-login',
      component: UserLogin,
    },
    {
      path: '/user/register',
      name: 'user-register',
      component: UserRegister,
    },
    {
      path: '/admin/userlist',
      name: 'user-manage',
      component: UserManage,
    },
  ],
})

export default router
