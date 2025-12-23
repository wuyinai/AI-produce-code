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
  }

  // 初始化WebSocket监听
  initWebSocketListeners()

  return {
    isConnected,
    isReconnecting,
    error,
    connect,
    disconnect
  }
})
