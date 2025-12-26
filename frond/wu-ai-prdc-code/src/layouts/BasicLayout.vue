<template>
  <a-layout class="basic-layout">
    <!-- 顶部导航栏 -->
    <GlobalHeader />
    <!-- 主要内容区域 -->
    <a-layout-content class="main-content">
      <router-view />
    </a-layout-content>
    <!-- 底部版权信息 -->
    <GlobalFooter />

    <!-- 全局协作邀请弹窗 -->
    <div v-if="showInviteModal" class="invite-modal-overlay">
      <div class="invite-modal-content">
        <div class="invite-modal-header">
          <h3>收到协作邀请</h3>
          <a-button type="text" @click="handleCloseModal" class="close-btn">
            <CloseOutlined />
          </a-button>
        </div>
        <div class="invite-modal-body">
          <div class="invite-info">
            <a-avatar :src="currentInvite?.senderAvatar || ''" size="large" class="sender-avatar" />
            <div class="invite-details">
              <p class="invite-title">
                <span class="sender-name">{{ currentInvite?.userName || '未知用户' }}</span>
                邀请您协作开发
              </p>
              <p class="app-name">{{ currentInvite?.appName || '' }}</p>
            </div>
          </div>
        </div>
        <div class="invite-modal-footer">
          <a-button @click="handleRejectInvite" class="reject-btn">拒绝</a-button>
          <a-button type="primary" @click="handleAcceptInvite" class="accept-btn">接受</a-button>
        </div>
      </div>
    </div>
  </a-layout>
</template>

<script setup lang="ts">
import { ref, watch, computed, onMounted } from 'vue'
import { message } from 'ant-design-vue'
import { useRouter } from 'vue-router'
import { CloseOutlined } from '@ant-design/icons-vue'
import GlobalHeader from '@/components/GlobalHeader.vue'
import GlobalFooter from '@/components/GlobalFooter.vue'
// 引入WebSocket store，自动初始化WebSocket连接
import { useWebSocketStore } from '@/stores/websocket'
import { useLoginUserStore } from '@/stores/loginUser'

// 初始化状态
const router = useRouter()
const loginUserStore = useLoginUserStore()
const webSocketStore = useWebSocketStore()

// 协作邀请状态
const showInviteModal = ref(false)
const currentInvite = ref<any>(null)
const isProcessingInvite = ref(false)

// 监听协作邀请列表变化
watch(
  () => webSocketStore.collaborationInvites,
  (newInvites) => {
    // 如果有新邀请且当前没有显示弹窗，显示第一个邀请
    if (newInvites.length > 0 && !showInviteModal.value) {
      currentInvite.value = newInvites[0]
      showInviteModal.value = true
    }
  },
  { deep: true }
)

// 关闭弹窗
const handleCloseModal = () => {
  showInviteModal.value = false
  currentInvite.value = null
}

// 接受邀请
const handleAcceptInvite = async () => {
  if (!currentInvite.value) return

  // 保存邀请ID，防止在调用过程中currentInvite变为null
  const inviteId = currentInvite.value.id
  console.log('接受邀请ID====：', inviteId)
  console.log('接受邀请====：', currentInvite.value)

  isProcessingInvite.value = true
  try {
    // 调用WebSocket store的接受邀请方法
    webSocketStore.acceptCollaborationInvite(inviteId)
    // 跳转到协作应用页面
    router.push(`/app/chat/${currentInvite.value.appId}?view=1`)
    message.success('接受协作邀请成功')
    handleCloseModal()
  } catch (error) {
    console.error('接受协作邀请失败：', error)
    message.error('接受协作邀请失败，请重试')
  } finally {
    isProcessingInvite.value = false
  }
}

// 拒绝邀请
const handleRejectInvite = async () => {
  if (!currentInvite.value) return

  // 保存邀请ID，防止在调用过程中currentInvite变为null
  const inviteId = currentInvite.value.id
  isProcessingInvite.value = true
  try {
    // 调用WebSocket store的拒绝邀请方法
    webSocketStore.rejectCollaborationInvite(inviteId)
    message.success('拒绝协作邀请成功')
    handleCloseModal()
  } catch (error) {
    console.error('拒绝协作邀请失败：', error)
    message.error('拒绝协作邀请失败，请重试')
  } finally {
    isProcessingInvite.value = false
  }
}
</script>

<style scoped>
.basic-layout {
  background: none;
}

.main-content {
  width: 100%;
  padding: 0;
  background: none;
  margin: 0;
}

/* 全局协作邀请弹窗样式 */
.invite-modal-overlay {
  position: fixed;
  top: 0;
  left: 0;
  right: 0;
  bottom: 0;
  background: rgba(0, 0, 0, 0.5);
  backdrop-filter: blur(4px);
  display: flex;
  align-items: center;
  justify-content: center;
  z-index: 9999;
  animation: fadeIn 0.3s ease;
}

.invite-modal-content {
  background: white;
  border-radius: 12px;
  box-shadow: 0 8px 32px rgba(0, 0, 0, 0.15);
  width: 90%;
  max-width: 500px;
  overflow: hidden;
  animation: slideUp 0.3s ease;
}

.invite-modal-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 20px 24px;
  border-bottom: 1px solid #f0f0f0;
  background: linear-gradient(135deg, #fafafa, #f5f5f5);
}

.invite-modal-header h3 {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.close-btn {
  padding: 4px;
  font-size: 16px;
  color: #64748b;
  transition: all 0.3s ease;
}

.close-btn:hover {
  color: #334155;
  background: rgba(100, 116, 139, 0.1);
}

.invite-modal-body {
  padding: 24px;
}

.invite-info {
  display: flex;
  align-items: flex-start;
  gap: 16px;
}

.sender-avatar {
  margin-top: 4px;
}

.invite-details {
  flex: 1;
}

.invite-title {
  margin: 0 0 8px 0;
  font-size: 16px;
  color: #334155;
}

.sender-name {
  font-weight: 600;
  color: #1e293b;
}

.app-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #667eea;
}

.invite-modal-footer {
  display: flex;
  justify-content: flex-end;
  gap: 12px;
  padding: 16px 24px;
  border-top: 1px solid #f0f0f0;
  background: #fafafa;
}

.reject-btn {
  padding: 8px 20px;
  font-size: 14px;
  font-weight: 500;
}

.accept-btn {
  padding: 8px 28px;
  font-size: 14px;
  font-weight: 500;
}

/* 动画效果 */
@keyframes fadeIn {
  from {
    opacity: 0;
  }
  to {
    opacity: 1;
  }
}

@keyframes slideUp {
  from {
    opacity: 0;
    transform: translateY(20px);
  }
  to {
    opacity: 1;
    transform: translateY(0);
  }
}
</style>
