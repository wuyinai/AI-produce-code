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
      name: '首页',
      component: HomeView,
    },
    {
      path: '/user/login',
      name: '登录页',
      component: UserLogin,
    },
    {
      path: '/user/register',
      name: '注册页',
      component: UserRegister,
    },
    {
      path: '/admin/userlist',
      name: '用户信息页',
      component: UserManage,
    },
  ],
})

export default router
