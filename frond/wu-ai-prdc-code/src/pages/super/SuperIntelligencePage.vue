<template>
  <div class="super-intelligence-page">
    <a-layout class="si-layout">
      <!-- 左侧垂直导航栏 -->
      <a-layout-sider
        class="si-sider"
        :width="siderWidth"
        :collapsed="isCollapsed"
        :collapsed-width="collapsedWidth"
        :trigger="null"
        collapsible
        :style="{ overflow: 'auto', height: '100vh', position: 'fixed', left: 0, top: '64px', bottom: 0 }"
      >
        <div class="sider-header">
          <div class="ai-avatar">
            <span class="avatar-icon">🧠</span>
          </div>
          <transition name="fade">
            <div v-if="!isCollapsed" class="header-info">
              <h3 class="ai-title">全栈生成</h3>
              <p class="ai-subtitle">AI 驱动的智能助手</p>
            </div>
          </transition>
        </div>

        <a-menu
          v-model:selectedKeys="selectedKeys"
          v-model:openKeys="openKeys"
          mode="inline"
          :items="menuItems"
          :inline-collapsed="isCollapsed"
          class="si-menu"
          @click="handleMenuClick"
        />

        <div class="sider-footer">
          <a-button
            type="text"
            class="collapse-btn"
            @click="toggleCollapse"
          >
            <template #icon>
              <MenuFoldOutlined v-if="!isCollapsed" />
              <MenuUnfoldOutlined v-else />
            </template>
            <span v-if="!isCollapsed" class="collapse-text">收起</span>
          </a-button>
        </div>
      </a-layout-sider>

      <!-- 主内容区域 -->
      <a-layout
        class="si-main-layout"
        :style="{ marginLeft: isCollapsed ? collapsedWidth + 'px' : siderWidth + 'px' }"
      >
        <a-layout-content class="si-content">
          <!-- 欢迎区域 -->
          <div v-if="!currentComponent" class="welcome-section">
            <div class="welcome-card">
              <div class="welcome-icon">
                <span>🚀</span>
              </div>
              <h2 class="welcome-title">欢迎使用全栈生成</h2>
              <p class="welcome-description">
                选择左侧菜单栏中的功能，开始体验 AI 驱动的智能服务
              </p>
              <div class="feature-cards">
                <div
                  v-for="feature in quickFeatures"
                  :key="feature.key"
                  class="feature-card"
                  @click="navigateTo(feature.key)"
                >
                  <div class="feature-icon">{{ feature.icon }}</div>
                  <h4 class="feature-title">{{ feature.label }}</h4>
                  <p class="feature-desc">{{ feature.description }}</p>
                </div>
              </div>
            </div>
          </div>

          <!-- 动态内容区域 -->
          <transition name="slide-fade" mode="out-in">
            <component :is="currentComponent" v-if="currentComponent" />
          </transition>
        </a-layout-content>
      </a-layout>
    </a-layout>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, h, markRaw } from 'vue'
import { useRouter } from 'vue-router'
import {
  MenuFoldOutlined,
  MenuUnfoldOutlined,
  RocketOutlined,
  BulbOutlined,
  CodeOutlined,
  FilePptOutlined,
  CloudServerOutlined,
  ApiOutlined,
  SettingOutlined,
  ExperimentOutlined,
  BarChartOutlined,
  SafetyOutlined,
  GlobalOutlined
} from '@ant-design/icons-vue'
import type { MenuProps } from 'ant-design-vue'

import DatabaseConstruction from './DatabaseConstruction.vue'
import CodeGenerator from './CodeGenerator.vue'
import PPTGenerator from './PPTGenerator.vue'
import RequirementGenerator from './RequirementGenerator.vue'
import BackendGenerator from './BackendGenerator.vue'
import SystemMonitor from './SystemMonitor.vue'
import APITesting from './APITesting.vue'
import DataAnalysis from './DataAnalysis.vue'

const router = useRouter()

// 侧边栏状态
const isCollapsed = ref(false)
const siderWidth = ref(240)
const collapsedWidth = ref(80)

// 当前选中的菜单
const selectedKeys = ref<string[]>(['welcome'])
const openKeys = ref<string[]>([])

// 菜单配置
const menuItems = computed<MenuProps['items']>(() => [
  {
    key: 'welcome',
    icon: () => h(RocketOutlined),
    label: '首页',
    title: '首页',
  },
  {
    key: 'ai-chat',
    icon: () => h(BulbOutlined),
    label: '数据库生成',
    title: '数据库生成',
  },
  {
    key: 'requirement',
    icon: () => h(FileTextOutlined),
    label: '需求生成',
    title: '需求生成',
  },
  {
    key: 'code-gen',
    icon: () => h(CodeOutlined),
    label: '前端代码生成',
    title: '前端代码生成',
  },
  {
    key: 'backend-gen',
    icon: () => h(CloudServerOutlined),
    label: '后端代码生成',
    title: '后端代码生成',
  },
  {
    key: 'ppt-gen',
    icon: () => h(FilePptOutlined),
    label: 'PPT 生成',
    title: 'PPT 生成',
  },
  {
    key: 'monitor',
    icon: () => h(BarChartOutlined),
    label: '系统监控',
    title: '系统监控',
  },
  {
    key: 'api-test',
    icon: () => h(ApiOutlined),
    label: 'API 测试',
    title: 'API 测试',
  },
  {
    key: 'analysis',
    icon: () => h(ExperimentOutlined),
    label: '数据分析',
    title: '数据分析',
  },
])

// 快速功能卡片
const quickFeatures = [
  {
    key: 'ai-chat',
    icon: '💬',
    label: 'AI 对话',
    description: '智能对话，解答疑问'
  },
  {
    key: 'code-gen',
    icon: '🎨',
    label: '代码生成',
    description: '快速生成前端代码'
  },
  {
    key: 'requirement',
    icon: '📝',
    label: '需求生成',
    description: '智能分析生成需求文档'
  },
  {
    key: 'ppt-gen',
    icon: '📊',
    label: 'PPT 生成',
    description: '自动生成演示文稿'
  }
]

// 组件映射
const componentMap: Record<string, any> = {
  'ai-chat': markRaw(DatabaseConstruction),
  'code-gen': markRaw(CodeGenerator),
  'ppt-gen': markRaw(PPTGenerator),
  'requirement': markRaw(RequirementGenerator),
  'backend-gen': markRaw(BackendGenerator),
  'monitor': markRaw(SystemMonitor),
  'api-test': markRaw(APITesting),
  'analysis': markRaw(DataAnalysis)
}

// 当前显示的组件
const currentComponent = computed(() => {
  return selectedKeys.value[0] && selectedKeys.value[0] !== 'welcome'
    ? componentMap[selectedKeys.value[0]]
    : null
})

// 切换侧边栏
const toggleCollapse = () => {
  isCollapsed.value = !isCollapsed.value
}

// 导航到指定功能
const navigateTo = (key: string) => {
  selectedKeys.value = [key]
  if (key !== 'welcome') {
    openKeys.value = []
  }
}

// 处理菜单点击
const handleMenuClick: MenuProps['onClick'] = (e) => {
  const key = e.key as string
  selectedKeys.value = [key]
  if (key !== 'welcome') {
    openKeys.value = []
  }
}
</script>

<script lang="ts">
// 导入额外的图标
import { FileTextOutlined } from '@ant-design/icons-vue'
</script>

<style scoped>
.super-intelligence-page {
  min-height: 100vh;
  background: #f5f7fa;
}

.si-layout {
  min-height: calc(100vh - 64px);
}

/* 侧边栏样式 */
.si-sider {
  background: linear-gradient(180deg, #1a1a2e 0%, #16213e 100%);
  border-right: 1px solid rgba(255, 255, 255, 0.08);
  box-shadow: 4px 0 20px rgba(0, 0, 0, 0.15);
  z-index: 100;
}

.sider-header {
  padding: 24px 16px;
  text-align: center;
  border-bottom: 1px solid rgba(255, 255, 255, 0.08);
  margin-bottom: 16px;
}

.ai-avatar {
  width: 56px;
  height: 56px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 12px;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  animation: pulse-glow 3s ease-in-out infinite;
}

@keyframes pulse-glow {
  0%, 100% {
    box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
  }
  50% {
    box-shadow: 0 8px 32px rgba(102, 126, 234, 0.6);
  }
}

.avatar-icon {
  font-size: 28px;
}

.header-info {
  color: white;
}

.ai-title {
  font-size: 18px;
  font-weight: 600;
  margin: 0 0 4px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.ai-subtitle {
  font-size: 12px;
  color: rgba(255, 255, 255, 0.6);
  margin: 0;
}

/* 菜单样式 */
.si-menu {
  background: transparent !important;
  border-right: none !important;
  padding: 8px;
}

.si-menu :deep(.ant-menu-item) {
  border-radius: 12px;
  margin-bottom: 4px;
  color: rgba(255, 255, 255, 0.75);
  transition: all 0.3s ease;
  height: 48px;
  line-height: 48px;
}

.si-menu :deep(.ant-menu-item:hover) {
  background: rgba(102, 126, 234, 0.2);
  color: white;
}

.si-menu :deep(.ant-menu-item-selected) {
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%) !important;
  color: white !important;
  font-weight: 600;
  box-shadow: 0 4px 15px rgba(102, 126, 234, 0.4);
}

.si-menu :deep(.ant-menu-item-selected::after) {
  display: none;
}

.si-menu :deep(.ant-menu-item svg) {
  font-size: 18px;
}

.sider-footer {
  padding: 16px;
  border-top: 1px solid rgba(255, 255, 255, 0.08);
  margin-top: auto;
}

.collapse-btn {
  width: 100%;
  color: rgba(255, 255, 255, 0.75);
  border-radius: 12px;
  height: 40px;
  display: flex;
  align-items: center;
  justify-content: center;
  gap: 8px;
  transition: all 0.3s ease;
}

.collapse-btn:hover {
  background: rgba(102, 126, 234, 0.2);
  color: white;
}

.collapse-text {
  font-size: 14px;
}

/* 主内容区域 */
.si-main-layout {
  transition: margin-left 0.3s ease;
  min-height: calc(100vh - 64px);
}

.si-content {
  padding: 24px;
  min-height: calc(100vh - 64px);
  background: #f5f7fa;
}

/* 欢迎区域 */
.welcome-section {
  display: flex;
  justify-content: center;
  align-items: center;
  min-height: calc(100vh - 160px);
}

.welcome-card {
  text-align: center;
  max-width: 800px;
  padding: 48px;
}

.welcome-icon {
  width: 80px;
  height: 80px;
  border-radius: 24px;
  background: linear-gradient(135deg, #667eea 0%, #764ba2 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  margin: 0 auto 24px;
  box-shadow: 0 12px 40px rgba(102, 126, 234, 0.3);
  animation: float 6s ease-in-out infinite;
}

.welcome-icon span {
  font-size: 40px;
}

@keyframes float {
  0%, 100% {
    transform: translateY(0);
  }
  50% {
    transform: translateY(-10px);
  }
}

.welcome-title {
  font-size: 36px;
  font-weight: 700;
  color: #1e293b;
  margin: 0 0 16px;
}

.welcome-description {
  font-size: 18px;
  color: #64748b;
  margin: 0 0 48px;
  line-height: 1.6;
}

.feature-cards {
  display: grid;
  grid-template-columns: repeat(auto-fit, minmax(180px, 1fr));
  gap: 24px;
}

.feature-card {
  background: white;
  border-radius: 20px;
  padding: 32px 24px;
  text-align: center;
  cursor: pointer;
  transition: all 0.4s ease;
  border: 2px solid transparent;
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.06);
}

.feature-card:hover {
  transform: translateY(-8px);
  border-color: #667eea;
  box-shadow: 0 20px 40px rgba(102, 126, 234, 0.15);
}

.feature-icon {
  font-size: 40px;
  margin-bottom: 16px;
}

.feature-title {
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
  margin: 0 0 8px;
}

.feature-desc {
  font-size: 14px;
  color: #64748b;
  margin: 0;
  line-height: 1.5;
}

/* 过渡动画 */
.fade-enter-active,
.fade-leave-active {
  transition: opacity 0.3s ease;
}

.fade-enter-from,
.fade-leave-to {
  opacity: 0;
}

.slide-fade-enter-active {
  transition: all 0.3s ease-out;
}

.slide-fade-leave-active {
  transition: all 0.2s ease-in;
}

.slide-fade-enter-from {
  transform: translateX(20px);
  opacity: 0;
}

.slide-fade-leave-to {
  transform: translateX(-20px);
  opacity: 0;
}

/* 响应式设计 */
@media (max-width: 992px) {
  .si-sider {
    position: absolute !important;
    z-index: 1000;
  }

  .si-content {
    padding: 16px;
  }

  .welcome-card {
    padding: 32px 24px;
  }

  .welcome-title {
    font-size: 28px;
  }

  .welcome-description {
    font-size: 16px;
  }

  .feature-cards {
    grid-template-columns: repeat(2, 1fr);
  }
}

@media (max-width: 768px) {
  .feature-cards {
    grid-template-columns: 1fr;
  }

  .si-sider {
    width: 100% !important;
    max-width: 280px;
  }
}
</style>
