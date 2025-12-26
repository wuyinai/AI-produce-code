import { defineStore } from 'pinia'
import { ref, computed, watch } from 'vue'
import { webSocketService } from '@/utils/websocket'
import { useLoginUserStore } from './loginUser'

/**
 * WebSocket状态管理
 */
export const useWebSocketStore = defineStore('websocket', () => {
  // WebSocket连接状态
  const isConnected = ref(false)
  // 重连状态
  const isReconnecting = ref(false)
  // 连接错误信息
  const error = ref<string | null>(null)

  // 获取登录用户信息
  const loginUserStore = useLoginUserStore()
  const isLoggedIn = computed(() => loginUserStore.loginUser.id !== undefined)

  // 连接WebSocket
  async function connect() {
    if (!isLoggedIn.value) {
      return
    }

    try {
      webSocketService.connect()
    } catch (err) {
      error.value = err instanceof Error ? err.message : 'WebSocket连接失败'
      console.error('WebSocket连接失败:', err)
    }
  }

  // 断开WebSocket
  function disconnect() {
    webSocketService.disconnect()
  }

  // 监听登录状态变化
  watch(isLoggedIn, (newVal) => {
    if (newVal) {
      // 用户已登录，连接WebSocket
      connect()
    } else {
      // 用户未登录，断开WebSocket
      disconnect()
    }
  }, { immediate: true })

  // 协作邀请消息
  const collaborationInvites = ref<Array<{
    id: string
    senderId: number
    senderName?: string
    appId: number
    appName: string
    collaborationId: number
    timestamp: number
  }>>([])

  // 初始化WebSocket事件监听
  function initWebSocketListeners() {
    // 连接成功
    webSocketService.on('open', () => {
      isConnected.value = true
      isReconnecting.value = false
      error.value = null
      console.log('WebSocket连接已建立')
    })

    // 连接关闭
    webSocketService.on('close', () => {
      isConnected.value = false
      console.log('WebSocket连接已关闭')
    })

    // 连接错误
    webSocketService.on('error', (event) => {
      error.value = 'WebSocket连接错误'
      console.error('WebSocket错误:', event)
    })

    // 重连中
    webSocketService.on('reconnect', () => {
      isReconnecting.value = true
      console.log('WebSocket正在重连...')
    })

    // 消息处理
    webSocketService.on('message', (event) => {
      if (event instanceof MessageEvent) {
        try {
          const messageData = JSON.parse(event.data)
          console.log('收到WebSocket消息========:', messageData)
          const messageType = messageData.type

          // 处理心跳消息
          if (messageType === 'heartbeat') {
            console.log('收到心跳响应')
          }
          // 处理协作邀请消息
          else if (messageType === 'collaboration_invite') {
            handleCollaborationInvite(messageData)
          }
          // 处理协作邀请接受消息
          else if (messageType === 'collaboration_accept') {
            handleCollaborationAccept(messageData)
          }
          // 处理协作邀请拒绝消息
          else if (messageType === 'collaboration_reject') {
            handleCollaborationReject(messageData)
          }
          // 处理协作记录状态变更消息
          else if (messageType === 'collaboration_status_change') {
            handleCollaborationStatusChange(messageData)
          }
        } catch (error) {
          console.error('解析WebSocket消息失败:', error)
        }
      }
    })
  }

  // 处理协作邀请
  function handleCollaborationInvite(messageData: any) {

    const invite = {
      id: `${messageData.senderId}-${messageData.timestamp}`,
      senderId: messageData.senderId,
      appId: messageData.appId,
      appName: messageData.appName,
      collaborationId: messageData.collaborationId,
      userName: messageData.userName,
      senderAvatar: messageData.senderAvatar,
      timestamp: messageData.timestamp
    }
    collaborationInvites.value.push(invite)
  }

  // 处理协作邀请接受
  function handleCollaborationAccept(messageData: any) {
    console.log('收到协作邀请接受:', messageData)
  }

  // 处理协作邀请拒绝
  function handleCollaborationReject(messageData: any) {
    console.log('收到协作邀请拒绝:', messageData)
  }

  // 处理协作记录状态变更
  function handleCollaborationStatusChange(messageData: any) {
    console.log('收到协作记录状态变更:', messageData)
    // 触发自定义事件，通知相关组件更新
    const event = new CustomEvent('collaboration-status-change', {
      detail: messageData
    })
    window.dispatchEvent(event)
  }

  // 接受协作邀请
  function acceptCollaborationInvite(inviteId: string) {
    const invite = collaborationInvites.value.find(i => i.id === inviteId)
    if (invite) {
      // 发送接受消息
      sendCollaborationAccept(invite)
      // 移除邀请
      collaborationInvites.value = collaborationInvites.value.filter(i => i.id !== inviteId)
    }
  }

  // 拒绝协作邀请
  function rejectCollaborationInvite(inviteId: string) {
    const invite = collaborationInvites.value.find(i => i.id === inviteId)
    if (invite) {
      // 发送拒绝消息
      sendCollaborationReject(invite)
      // 移除邀请
      collaborationInvites.value = collaborationInvites.value.filter(i => i.id !== inviteId)
    }
  }

  // 发送协作邀请接受消息
  function sendCollaborationAccept(invite: any) {
    const message = JSON.stringify({
      type: 'collaboration_accept',
      senderId: invite.senderId,
      receiverId: loginUserStore.loginUser.id,
      collaborationId: invite.collaborationId,
      timestamp: Date.now()
    })
    webSocketService.send(message)
  }

  // 发送协作邀请拒绝消息
  function sendCollaborationReject(invite: any) {
    const message = JSON.stringify({
      type: 'collaboration_reject',
      senderId: invite.senderId,
      receiverId: loginUserStore.loginUser.id,
      collaborationId: invite.collaborationId,
      reason: '用户拒绝了您的协作邀请',
      timestamp: Date.now()
    })
    webSocketService.send(message)
  }

  // 初始化WebSocket监听
  initWebSocketListeners()

  return {
    isConnected,
    isReconnecting,
    error,
    collaborationInvites,
    connect,
    disconnect,
    acceptCollaborationInvite,
    rejectCollaborationInvite
  }
})
