<template>
  <div class="app-card" :class="{ 'app-card--featured': featured }">
    <!-- 预览区域 -->
    <div class="app-preview">
      <img v-if="app.cover" :src="app.cover" :alt="app.appName" />
      <div v-else class="app-placeholder">
        <div class="placeholder-icon">🤖</div>
        <div class="placeholder-text">AI生成应用</div>
      </div>
      
      <!-- 悬停遮罩 -->
      <div class="app-overlay">
        <div class="overlay-content">
          <a-space direction="vertical" size="large">
            <a-button 
              type="primary" 
              @click="handleViewChat"
              size="large"
              class="action-btn primary-btn"
            >
              <MessageOutlined />
              查看对话
            </a-button>
            <a-button 
              v-if="app.deployKey" 
              type="default" 
              @click="handleViewWork"
              size="large"
              class="action-btn secondary-btn"
            >
              <EyeOutlined />
              查看作品
            </a-button>
          </a-space>
        </div>
      </div>
      
      <!-- 特色标签 -->
      <div v-if="featured" class="featured-badge">
        <StarFilled />
        精选
      </div>
    </div>
    
    <!-- 信息区域 -->
    <div class="app-info">
      <div class="app-info-top">
        <div class="app-meta">
          <a-avatar 
            :src="app.user?.userAvatar" 
            :size="32"
            class="author-avatar"
          >
            {{ app.user?.userName?.charAt(0) || 'U' }}
          </a-avatar>
          <div class="author-info">
            <p class="app-author">
              {{ app.user?.userName || (featured ? '官方' : '未知用户') }}
            </p>
            <div class="app-status">
              <div v-if="app.deployKey" class="status-badge deployed">已部署</div>
              <div v-else class="status-badge developing">开发中</div>
            </div>
          </div>
        </div>
      </div>
      
      <h3 class="app-title">{{ app.appName || '未命名应用' }}</h3>
      
      <!-- 描述信息 -->
      <div class="app-description">
        <p class="description-text">
          {{ getAppDescription(app) }}
        </p>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { MessageOutlined, EyeOutlined, StarFilled } from '@ant-design/icons-vue'

interface Props {
  app: API.AppVO
  featured?: boolean
}

interface Emits {
  (e: 'view-chat', appId: string | number | undefined): void
  (e: 'view-work', app: API.AppVO): void
}

const props = withDefaults(defineProps<Props>(), {
  featured: false,
})

const emit = defineEmits<Emits>()

const handleViewChat = () => {
  emit('view-chat', props.app.id)
}

const handleViewWork = () => {
  emit('view-work', props.app)
}

// 获取应用描述
const getAppDescription = (app: API.AppVO) => {
  if (app.initPrompt) {
    return app.initPrompt.length > 60 ? app.initPrompt.substring(0, 60) + '...' : app.initPrompt
  }
  return '这是一个由AI生成的应用，提供丰富的功能和优秀的用户体验。'
}
</script>

<style scoped>
.app-card {
  background: rgba(255, 255, 255, 0.98);
  border-radius: 20px;
  overflow: hidden;
  box-shadow: 
    0 4px 20px rgba(0, 0, 0, 0.08),
    0 0 0 1px rgba(255, 255, 255, 0.1);
  backdrop-filter: blur(20px);
  border: 1px solid rgba(102, 126, 234, 0.1);
  transition: all 0.4s cubic-bezier(0.4, 0, 0.2, 1);
  cursor: pointer;
  position: relative;
  overflow: hidden;
}

.app-card::before {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.02) 0%, rgba(118, 75, 162, 0.02) 100%);
  opacity: 0;
  transition: opacity 0.4s ease;
  pointer-events: none;
}

.app-card:hover::before {
  opacity: 1;
}

.app-card:hover {
  transform: translateY(-8px) scale(1.02);
  box-shadow: 
    0 20px 40px rgba(0, 0, 0, 0.15),
    0 0 0 1px rgba(102, 126, 234, 0.2);
}

.app-card--featured {
  border: 2px solid transparent;
  background: linear-gradient(white, white) padding-box,
              linear-gradient(135deg, #667eea, #764ba2) border-box;
}

.app-card--featured::after {
  content: '';
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  pointer-events: none;
}

/* 预览区域 */
.app-preview {
  height: 200px;
  background: linear-gradient(135deg, #f8fafc 0%, #e2e8f0 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  overflow: hidden;
  position: relative;
}

.app-preview img {
  width: 100%;
  height: 100%;
  object-fit: cover;
  transition: transform 0.4s ease;
}

.app-card:hover .app-preview img {
  transform: scale(1.1);
}

.app-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #94a3b8;
  background: linear-gradient(135deg, rgba(102, 126, 234, 0.05) 0%, rgba(118, 75, 162, 0.05) 100%);
  height: 100%;
  width: 100%;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
  background-clip: text;
}

.placeholder-text {
  font-size: 14px;
  font-weight: 500;
  color: #64748b;
}

/* 悬停遮罩 */
.app-overlay {
  position: absolute;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: linear-gradient(135deg, rgba(0, 0, 0, 0.7) 0%, rgba(0, 0, 0, 0.5) 100%);
  display: flex;
  align-items: center;
  justify-content: center;
  opacity: 0;
  transition: all 0.3s ease;
  backdrop-filter: blur(4px);
}

.app-card:hover .app-overlay {
  opacity: 1;
}

.overlay-content {
  text-align: center;
}

.action-btn {
  border-radius: 12px;
  font-weight: 500;
  height: 42px;
  min-width: 120px;
  backdrop-filter: blur(20px);
  transition: all 0.3s ease;
}

.primary-btn {
  background: rgba(102, 126, 234, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.2);
  color: white;
}

.primary-btn:hover {
  background: rgba(102, 126, 234, 1);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(102, 126, 234, 0.4);
}

.secondary-btn {
  background: rgba(255, 255, 255, 0.9);
  border: 1px solid rgba(255, 255, 255, 0.3);
  color: #374151;
}

.secondary-btn:hover {
  background: rgba(255, 255, 255, 1);
  transform: translateY(-2px);
  box-shadow: 0 8px 25px rgba(0, 0, 0, 0.15);
}

/* 特色标签 */
.featured-badge {
  position: absolute;
  top: 12px;
  right: 12px;
  background: linear-gradient(135deg, #f59e0b, #f97316);
  color: white;
  padding: 6px 12px;
  border-radius: 20px;
  font-size: 12px;
  font-weight: 600;
  display: flex;
  align-items: center;
  gap: 4px;
  box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
  animation: pulse 2s infinite;
}

@keyframes pulse {
  0%, 100% {
    box-shadow: 0 4px 12px rgba(245, 158, 11, 0.3);
  }
  50% {
    box-shadow: 0 6px 20px rgba(245, 158, 11, 0.5);
  }
}

/* 信息区域 */
.app-info {
  padding: 20px;
  position: relative;
  z-index: 2;
}

.app-info-top {
  margin-bottom: 12px;
}

.app-meta {
  display: flex;
  align-items: center;
  gap: 12px;
}

.author-avatar {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  font-weight: 600;
  flex-shrink: 0;
}

.author-info {
  flex: 1;
  min-width: 0;
}

.app-author {
  font-size: 13px;
  color: #64748b;
  margin: 0 0 4px 0;
  font-weight: 500;
}

.app-status {
  display: flex;
  align-items: center;
}

.status-badge {
  font-size: 11px;
  padding: 2px 8px;
  border-radius: 12px;
  font-weight: 500;
  border: 1px solid;
}

.status-badge.deployed {
  background: rgba(34, 197, 94, 0.1);
  color: #22c55e;
  border-color: rgba(34, 197, 94, 0.2);
}

.status-badge.developing {
  background: rgba(59, 130, 246, 0.1);
  color: #3b82f6;
  border-color: rgba(59, 130, 246, 0.2);
}

.app-title {
  font-size: 18px;
  font-weight: 700;
  margin: 0 0 12px 0;
  color: #1e293b;
  line-height: 1.3;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

.app-description {
  margin-top: 8px;
}

.description-text {
  font-size: 14px;
  color: #64748b;
  line-height: 1.5;
  margin: 0;
  display: -webkit-box;
  -webkit-line-clamp: 2;
  -webkit-box-orient: vertical;
  overflow: hidden;
  text-overflow: ellipsis;
}

/* 响应式设计 */
@media (max-width: 768px) {
  .app-preview {
    height: 160px;
  }
  
  .app-info {
    padding: 16px;
  }
  
  .app-title {
    font-size: 16px;
  }
  
  .action-btn {
    min-width: 100px;
    height: 38px;
    font-size: 14px;
  }
}
</style>
