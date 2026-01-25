import { createApp } from 'vue'
import { createPinia } from 'pinia'

import App from './App.vue'
import router from './router'

import Antd from 'ant-design-vue'
import 'ant-design-vue/dist/reset.css'
import 'highlight.js/styles/atom-one-dark.css'

import '@/access'

const app = createApp(App)

app.use(createPinia())
app.use(router)
app.use(Antd)

app.mount('#app')
/**
 * 这段代码是 Vue 3 应用的入口文件，
 * 负责初始化应用并挂载到 DOM 上。
 * 它集成了状态管理(Pinia)、路由管理和 UI 组件库(Ant Design Vue)等核心功能。
 */
