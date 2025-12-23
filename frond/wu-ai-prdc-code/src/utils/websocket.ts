/**
 * WebSocket管理服务
 * 封装WebSocket连接、断开、重连、心跳等功能
 */

// WebSocket事件类型
type WebSocketEvent = 'open' | 'message' | 'close' | 'error' | 'reconnect'

// WebSocket事件监听器类型
type WebSocketEventListener = (event: Event | MessageEvent | CloseEvent) => void

class WebSocketService {
  private ws: WebSocket | null = null
  private url: string
  private reconnectTimeout: number = 3000 // 重连间隔
  private reconnectAttempts: number = 0 // 重连尝试次数
  private maxReconnectAttempts: number = 5 // 最大重连次数
  private heartbeatInterval: number = 10000 // 心跳间隔（毫秒），缩短为10秒
  private heartbeatTimer: number | null = null // 心跳定时器
  private listeners: Map<WebSocketEvent, Set<WebSocketEventListener>> = new Map()
  private isConnecting: boolean = false
  private isClosing: boolean = false

  constructor(url: string) {
    // 根据当前页面协议自动选择ws或wss
    const protocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
    this.url = url.replace(/^ws?:/, protocol)
  }

  // 连接WebSocket
  connect(): void {
    if (this.ws?.readyState === WebSocket.OPEN || this.isConnecting || this.isClosing) {
      return
    }

    this.isConnecting = true
    this.reconnectAttempts = 0

    try {
      this.ws = new WebSocket(this.url)

      this.ws.onopen = (event) => {
        this.isConnecting = false
        this.reconnectAttempts = 0
        this.startHeartbeat()
        this.emit('open', event)
        console.log('WebSocket连接已建立')
      }

      this.ws.onmessage = (event) => {
        this.emit('message', event)
      }

      this.ws.onclose = (event) => {
        this.isConnecting = false
        this.stopHeartbeat()
        this.emit('close', event)
        console.log('WebSocket连接已关闭')

        // 如果不是主动关闭，尝试重连
        if (!this.isClosing && this.reconnectAttempts < this.maxReconnectAttempts) {
          this.attemptReconnect()
        }
      }

      this.ws.onerror = (event) => {
        this.isConnecting = false
        this.emit('error', event)
        console.error('WebSocket错误:', event)
      }
    } catch (error) {
      this.isConnecting = false
      console.error('WebSocket连接失败:', error)
      // 只有在非主动关闭的情况下才尝试重连
      if (!this.isClosing && this.reconnectAttempts < this.maxReconnectAttempts) {
        this.attemptReconnect()
      }
    }
  }

  // 断开WebSocket连接
  disconnect(): void {
    this.isClosing = true
    this.stopHeartbeat()

    if (this.ws) {
      // 移除所有事件监听器，避免在关闭过程中触发错误
      this.ws.onopen = null
      this.ws.onmessage = null
      this.ws.onclose = null
      this.ws.onerror = null

      try {
        this.ws.close()
      } catch (error) {
        console.error('关闭WebSocket连接时出错:', error)
      }
      this.ws = null
    }

    this.isClosing = false
    console.log('WebSocket连接已主动关闭')
  }

  // 发送消息
  send(data: string | ArrayBufferLike | Blob | ArrayBufferView): void {
    if (this.ws?.readyState === WebSocket.OPEN) {
      this.ws.send(data)
    } else {
      console.error('WebSocket连接未建立，无法发送消息')
    }
  }

  // 添加事件监听器
  on(event: WebSocketEvent, listener: WebSocketEventListener): void {
    if (!this.listeners.has(event)) {
      this.listeners.set(event, new Set())
    }
    this.listeners.get(event)?.add(listener)
  }

  // 移除事件监听器
  off(event: WebSocketEvent, listener: WebSocketEventListener): void {
    this.listeners.get(event)?.delete(listener)
  }

  // 触发事件
  private emit(event: WebSocketEvent, data: Event | MessageEvent | CloseEvent): void {
    this.listeners.get(event)?.forEach(listener => {
      try {
        listener(data)
      } catch (error) {
        console.error('WebSocket事件监听器执行错误:', error)
      }
    })
  }

  // 尝试重连
  private attemptReconnect(): void {
    this.reconnectAttempts++
    console.log(`尝试重连WebSocket... (${this.reconnectAttempts}/${this.maxReconnectAttempts})`)

    setTimeout(() => {
      this.connect()
      this.emit('reconnect', new Event('reconnect'))
    }, this.reconnectTimeout)
  }

  // 开始心跳
  private startHeartbeat(): void {
    this.stopHeartbeat()

    this.heartbeatTimer = window.setInterval(() => {
      if (this.ws?.readyState === WebSocket.OPEN) {
        // 发送心跳消息，类型为"heartbeat"
        const heartbeatMsg = JSON.stringify({
          type: "heartbeat",
          timestamp: Date.now()
        });
        this.send(heartbeatMsg);
        console.log("发送心跳消息");
      }
    }, this.heartbeatInterval)
  }

  // 停止心跳
  private stopHeartbeat(): void {
    if (this.heartbeatTimer !== null) {
      clearInterval(this.heartbeatTimer)
      this.heartbeatTimer = null
    }
  }

  // 获取WebSocket连接状态
  get readyState(): number | null {
    return this.ws?.readyState || null
  }

  // 判断WebSocket是否连接
  get isConnected(): boolean {
    return this.ws?.readyState === WebSocket.OPEN
  }
}

// 导出WebSocketService单例
// 从API_BASE_URL中提取后端服务地址，确保WebSocket连接到正确的后端服务
import { API_BASE_URL } from '@/config/env'

// 从API_BASE_URL提取主机和端口，处理可能的URL格式问题
let wsUrl: string

try {
  // 解析API_BASE_URL获取基础URL
  const apiUrl = new URL(API_BASE_URL)
  const wsProtocol = apiUrl.protocol === 'https:' ? 'wss:' : 'ws:'
  // 构建WebSocket URL，注意要加上/api前缀，因为后端配置了context-path
  wsUrl = `${wsProtocol}//${apiUrl.host}/api/ws`
} catch (error) {
  // 如果API_BASE_URL格式不正确，使用默认配置
  console.error('API_BASE_URL格式不正确，使用默认WebSocket配置:', error)
  const defaultProtocol = window.location.protocol === 'https:' ? 'wss:' : 'ws:'
  wsUrl = `${defaultProtocol}//${window.location.hostname}:8234/api/ws`
}

export const webSocketService = new WebSocketService(wsUrl)
