<template>
  <div id="appChatPage">
    <!-- 顶部栏 -->
    <div class="header-bar">
      <div class="header-left">
        <h1 class="app-name">{{ appInfo?.appName || '网站生成器' }}</h1>
        <a-tag v-if="appInfo?.codeGenType" color="blue" class="code-gen-type-tag">
          {{ formatCodeGenType(appInfo.codeGenType) }}
        </a-tag>
      </div>
      <div class="header-right">
        <!-- 协作相关按钮 -->
        <template v-if="isCreator">
          <a-button
            v-if="!isCollaborating"
            type="primary"
            @click="startCollaboration"
            :loading="collaborationLoading"
          >
            <template #icon>
              <TeamOutlined />
            </template>
            开始协作
          </a-button>
          <template v-else>
            <a-button
              type="primary"
              @click="showAddCollaboratorModal"
              :loading="collaborationLoading"
            >
              <template #icon>
                <UserAddOutlined />
              </template>
              添加协作者
            </a-button>
            <a-button
              type="default"
              @click="showCollaboratorsModal"
              :loading="collaborationLoading"
            >
              <template #icon>
                <TeamOutlined />
              </template>
              查看协作者
            </a-button>
            <a-button
              type="default"
              danger
              @click="exitCollaboration"
              :loading="collaborationLoading"
            >
              <template #icon>
                <CloseOutlined />
              </template>
              退出协作
            </a-button>
          </template>
        </template>
        <a-button
          type="default"
          @click="switchToPreview"
          :class="{ 'view-mode-active': viewMode === 'preview' }"
        >
          <template #icon>
            <EyeOutlined />
          </template>
          查看预览
        </a-button>
        <a-button
          type="default"
          @click="switchToSource"
          :class="{ 'view-mode-active': viewMode === 'source' }"
        >
          <template #icon>
            <FileCodeOutlined />
          </template>
          查看源码
        </a-button>
        <a-button type="default" @click="showAppDetail">
          <template #icon>
            <InfoCircleOutlined />
          </template>
          应用详情
        </a-button>
        <a-button
          type="primary"
          ghost
          @click="downloadCode"
          :loading="downloading"
          :disabled="!isOwner"
        >
          <template #icon>
            <DownloadOutlined />
          </template>
          下载代码
        </a-button>
        <a-button type="primary" @click="deployApp" :loading="deploying" v-if="isCreator">
          <template #icon>
            <CloudUploadOutlined />
          </template>
          部署
        </a-button>
        <a-button
          type="default"
          @click="toggleVersionSidebar"
          :class="{ 'version-sidebar-active': showVersionSidebar }"
        >
          <template #icon>
            <HistoryOutlined />
          </template>
          版本历史
        </a-button>
      </div>
    </div>

    <!-- 主要内容区域 -->
    <div class="main-content">
      <!-- 版本侧边栏 -->
      <div class="version-sidebar" v-if="showVersionSidebar">
        <div class="version-sidebar-header">
          <HistoryOutlined />
          <span>版本历史</span>
          <a-button type="text" size="small" @click="showVersionSidebar = false">
            <CloseOutlined />
          </a-button>
        </div>
        <div class="version-sidebar-content">
          <a-spin :spinning="versionsLoading">
            <div v-if="versions.length === 0" class="version-empty">
              <HistoryOutlined />
              <p>暂无版本记录</p>
            </div>
            <div v-else class="version-list">
              <div
                v-for="version in versions"
                :key="version.id"
                class="version-item"
                :class="{ 'version-current': version.isCurrent === 1 }"
                @click="handleVersionClick(version)"
              >
                <div class="version-header">
                  <span class="version-name">{{ version.versionName }}</span>
                  <a-tag v-if="version.isCurrent === 1" color="green" size="small">当前</a-tag>
                </div>
                <div class="version-info">
                  <div class="version-desc">{{ version.versionDescription || '无描述' }}</div>
                  <div class="version-meta">
                    <span class="version-time">{{ formatTime(version.createTime) }}</span>
                    <span class="version-user">{{ version.createUserName }}</span>
                  </div>
                </div>
                <div class="version-actions" v-if="version.isCurrent !== 1 && isCreator">
                  <a-button
                    type="link"
                    size="small"
                    @click.stop="handleRollbackVersion(version.id)"
                  >
                    <template #icon>
                      <RollbackOutlined />
                    </template>
                    回退
                  </a-button>
                  <a-popconfirm
                    title="确定要删除这个版本吗？"
                    @confirm="handleDeleteVersion(version.id)"
                    @click.stop
                  >
                    <a-button type="link" size="small" danger @click.stop>
                      <template #icon>
                        <DeleteOutlined />
                      </template>
                      删除
                    </a-button>
                  </a-popconfirm>
                </div>
              </div>
            </div>
          </a-spin>
        </div>
      </div>

      <!-- 左侧对话区域 -->
      <div class="chat-section">
        <!-- 消息区域 -->
        <div class="messages-container" ref="messagesContainer">
          <!-- 加载更多按钮 -->
          <div v-if="hasMoreHistory" class="load-more-container">
            <a-button type="link" @click="loadMoreHistory" :loading="loadingHistory" size="small">
              加载更多历史消息
            </a-button>
          </div>
          <div v-for="(message, index) in messages" :key="index" class="message-item">
            <div v-if="message.type === 'user'" class="user-message">
              <div class="message-content">{{ message.content }}</div>
              <div class="message-avatar">
                <a-avatar :src="message.userAvatar" />
              </div>
            </div>
            <div v-else class="ai-message">
              <div class="message-avatar">
                <a-avatar :src="aiAvatar" />
              </div>
              <div class="message-content">
                <MarkdownRenderer v-if="message.content" :content="message.content" />
                <div v-if="message.loading" class="loading-indicator">
                  <a-spin size="small" />
                  <span>AI 正在思考...</span>
                </div>
              </div>
            </div>
          </div>
        </div>

        <!-- 选中元素信息展示 -->
        <a-alert
          v-if="selectedElementInfo"
          class="selected-element-alert"
          type="info"
          closable
          @close="clearSelectedElement"
        >
          <template #message>
            <div class="selected-element-info">
              <div class="element-header">
                <span class="element-tag">
                  选中元素：{{ selectedElementInfo.tagName.toLowerCase() }}
                </span>
                <span v-if="selectedElementInfo.id" class="element-id">
                  #{{ selectedElementInfo.id }}
                </span>
                <span v-if="selectedElementInfo.className" class="element-class">
                  .{{ selectedElementInfo.className.split(' ').join('.') }}
                </span>
              </div>
              <div class="element-details">
                <div v-if="selectedElementInfo.textContent" class="element-item">
                  内容: {{ selectedElementInfo.textContent.substring(0, 50) }}
                  {{ selectedElementInfo.textContent.length > 50 ? '...' : '' }}
                </div>
                <div v-if="selectedElementInfo.pagePath" class="element-item">
                  页面路径: {{ selectedElementInfo.pagePath }}
                </div>
                <div class="element-item">
                  选择器:
                  <code class="element-selector-code">{{ selectedElementInfo.selector }}</code>
                </div>
              </div>
            </div>
          </template>
        </a-alert>

        <!-- 用户消息输入框 -->
        <div class="input-container">
          <div class="input-wrapper">
            <a-tooltip v-if="!isOwner" title="无法在别人的作品下对话哦~" placement="top">
              <a-textarea
                v-model:value="userInput"
                :placeholder="getInputPlaceholder()"
                :rows="4"
                :maxlength="1000"
                @keydown.enter.prevent="sendMessage"
                :disabled="isGenerating || !isOwner"
              />
            </a-tooltip>
            <a-textarea
              v-else
              v-model:value="userInput"
              :placeholder="getInputPlaceholder()"
              :rows="4"
              :maxlength="1000"
              @keydown.enter.prevent="sendMessage"
              :disabled="isGenerating"
            />
            <div class="input-actions">
              <a-button
                type="primary"
                @click="sendMessage"
                :loading="isGenerating"
                :disabled="!isOwner"
              >
                <template #icon>
                  <SendOutlined />
                </template>
              </a-button>
            </div>
          </div>
        </div>
      </div>
      <!-- 右侧网页展示区域 -->
      <div class="preview-section">
        <div class="preview-header">
          <h3>生成后的网页展示</h3>
          <div class="preview-actions">
            <!-- 设备切换按钮 -->
            <div class="device-switcher">
              <a-tooltip title="手机" placement="top">
                <a-button
                  type="text"
                  :class="{ 'device-active': currentDevice === 'mobile' }"
                  @click="switchDevice('mobile')"
                  size="small"
                >
                  📱
                </a-button>
              </a-tooltip>
              <a-tooltip title="平板" placement="top">
                <a-button
                  type="text"
                  :class="{ 'device-active': currentDevice === 'tablet' }"
                  @click="switchDevice('tablet')"
                  size="small"
                >
                  💻
                </a-button>
              </a-tooltip>
              <a-tooltip title="PC" placement="top">
                <a-button
                  type="text"
                  :class="{ 'device-active': currentDevice === 'desktop' }"
                  @click="switchDevice('desktop')"
                  size="small"
                >
                  🖥️
                </a-button>
              </a-tooltip>
            </div>

            <!-- 现有编辑模式按钮 -->
            <a-button
              v-if="isOwner && previewUrl"
              type="link"
              :danger="isEditMode"
              @click="toggleEditMode"
              :class="{ 'edit-mode-active': isEditMode }"
              style="padding: 0; height: auto; margin-right: 12px"
            >
              <template #icon>
                <EditOutlined />
              </template>
              {{ isEditMode ? '退出编辑' : '编辑模式' }}
            </a-button>

            <!-- 添加直接修改按钮 -->
            <a-button
              v-if="isOwner && previewUrl && isEditMode"
              type="link"
              :danger="isDirectEditMode"
              @click="toggleDirectEditMode"
              :class="{ 'direct-edit-active': isDirectEditMode }"
              style="padding: 0; height: auto; margin-right: 12px"
            >
              <template #icon>
                <EditOutlined />
              </template>
              {{ isDirectEditMode ? '退出直接修改' : '直接修改' }}
            </a-button>

            <!-- 添加保存直接修改按钮 -->
            <a-button
              v-if="isOwner && previewUrl && isDirectEditMode"
              type="primary"
              size="small"
              @click="saveDirectEditContent"
              :loading="isSaving"
              style="padding: 0 12px; height: auto; margin-right: 12px"
            >
              <template #icon>
                <SaveOutlined />
              </template>
              保存修改
            </a-button>

            <!-- 现有新窗口打开按钮 -->
            <a-button v-if="previewUrl" type="link" @click="openInNewTab">
              <template #icon>
                <ExportOutlined />
              </template>
              新窗口打开
            </a-button>
          </div>
        </div>
        <div class="preview-content" :class="deviceClass">
          <template v-if="viewMode === 'preview'">
            <div v-if="!previewUrl && !isGenerating" class="preview-placeholder">
              <div class="placeholder-icon">🌐</div>
              <p>网站文件生成完成后将在这里展示</p>
            </div>
            <div v-else-if="isGenerating" class="preview-loading">
              <a-spin size="large" />
              <p>正在生成网站...</p>
            </div>
            <div v-else class="device-preview-container">
              <iframe
                :src="previewUrl"
                class="preview-iframe"
                frameborder="0"
                @load="onIframeLoad"
              ></iframe>
            </div>
          </template>
          <template v-else>
            <div class="source-code-view">
              <div class="source-code-sidebar">
                <div class="sidebar-header">
                  <FileOutlined />
                  <span>文件列表</span>
                </div>
                <div class="file-tree">
                  <div v-if="sourceDirLoading" class="file-tree-loading">
                    <a-spin size="small" />
                  </div>
                  <template v-else>
                    <FileTreeNode
                      v-for="node in sourceDirTree"
                      :key="node.path"
                      :node="node"
                      :selected-file="selectedFilePath"
                      @select="handleFileSelect"
                    />
                  </template>
                </div>
              </div>
              <div class="source-code-main">
                <div v-if="sourceCodeLoading" class="preview-loading">
                  <a-spin size="large" />
                  <p>正在加载源码...</p>
                </div>
                <template v-else-if="!selectedFilePath">
                  <div class="source-code-empty">
                    <FileTextOutlined />
                    <p>选择一个文件查看源码</p>
                  </div>
                </template>
                <template v-else>
                  <div class="source-code-header">
                    <FileTextOutlined />
                    <span>{{ selectedFileName }}</span>
                    <div class="source-code-actions">
                      <a-button
                        v-if="!sourceCodeEditMode"
                        type="primary"
                        size="small"
                        @click="enableSourceEditMode"
                      >
                        <template #icon>
                          <EditOutlined />
                        </template>
                        编辑
                      </a-button>
                      <template v-else>
                        <a-button
                          type="primary"
                          size="small"
                          @click="saveSourceFileContent"
                          :loading="sourceCodeSaving"
                        >
                          <template #icon>
                            <SaveOutlined />
                          </template>
                          保存
                        </a-button>
                        <a-button
                          size="small"
                          @click="cancelSourceEditMode"
                          style="margin-left: 8px"
                        >
                          取消
                        </a-button>
                      </template>
                    </div>
                  </div>
                  <div class="source-code-content" :class="{ 'edit-mode': sourceCodeEditMode }">
                    <template v-if="!sourceCodeEditMode">
                      <div class="source-code-with-lines">
                        <div class="line-numbers">
                          <span v-for="n in sourceCodeLineCount" :key="n">{{ n }}</span>
                        </div>
                        <pre class="source-code-viewer"><code :class="sourceCodeLanguageClass"
                                                              v-html="highlightedSourceCode"></code></pre>
                      </div>
                    </template>
                    <template v-else>
                      <a-textarea
                        v-model:value="sourceCodeEditContent"
                        class="source-code-editor"
                        placeholder="编辑文件内容..."
                        :auto-size="{ minRows: 10, maxRows: 50 }"
                      />
                    </template>
                  </div>
                </template>
              </div>
            </div>
          </template>
        </div>
      </div>
    </div>

    <!-- 应用详情弹窗 -->
    <AppDetailModal
      v-model:open="appDetailVisible"
      :app="appInfo"
      :show-actions="isOwner || isAdmin"
      @edit="editApp"
      @delete="deleteApp"
    />

    <!-- 部署成功弹窗 -->
    <DeploySuccessModal
      v-model:open="deployModalVisible"
      :deploy-url="deployUrl"
      @open-site="openDeployedSite"
    />

    <!-- 添加协作者弹窗 -->
    <FriendSelector
      v-model:visible="addCollaboratorModalVisible"
      :collaboration-id="collaborationId"
      :app-id="appId"
      :app-name="appInfo?.appName || null"
      @add-collaborators="handleAddCollaborators"
    />

    <!-- 查看协作者弹窗 -->
    <a-modal
      v-model:open="collaboratorsModalVisible"
      title="协作者列表"
      @ok="collaboratorsModalVisible = false"
      @cancel="collaboratorsModalVisible = false"
      width="600px"
      :ok-text="'确定'"
      :cancel-text="'关闭'"
    >
      <a-table
        :columns="collaboratorsColumns"
        :data-source="collaboratorsList"
        :pagination="false"
        :scroll="{ y: 300 }"
        row-key="id"
        bordered
        :loading="collaboratorsLoading"
      >
        <template #bodyCell="{ column, record }">
          <template v-if="column.key === 'joinTime'">
            {{ new Date(record.joinTime || '').toLocaleString() }}
          </template>
          <template v-else-if="column.key === 'createTime'">
            {{ new Date(record.createTime || '').toLocaleString() }}
          </template>
        </template>
      </a-table>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, nextTick, onUnmounted, computed } from 'vue'
import { useRoute, useRouter } from 'vue-router'
import { message } from 'ant-design-vue'
import { useLoginUserStore } from '@/stores/loginUser'
import { useWebSocketStore } from '@/stores/websocket'
import { webSocketService } from '@/utils/websocket'
import {
  getAppVoById,
  deployApp as deployAppApi,
  deleteApp as deleteAppApi,
  saveDirectEdit,
  getAppSourceDir,
  getAppSourceFile,
  saveSourceFile
} from '@/api/appController'
import { listAppChatHistory } from '@/api/chatHistoryController'
import { getCollaborationMembers, getCollaboratorsByAppId } from '@/api/collaborationController'
import { getUserVoById } from '@/api/userController'
import { listVersions, rollbackToVersion, deleteVersion } from '@/api/appVersionController'
import { CodeGenTypeEnum, formatCodeGenType } from '@/utils/codeGenTypes'
import request from '@/request'

import MarkdownRenderer from '@/components/MarkdownRenderer.vue'
import AppDetailModal from '@/components/AppDetailModal.vue'
import DeploySuccessModal from '@/components/DeploySuccessModal.vue'
import FriendSelector from '@/components/FriendSelector.vue'
import FileTreeNode from '@/components/FileTreeNode.vue'
import aiAvatar from '@/assets/aiAvatar.png'
import { API_BASE_URL, getStaticPreviewUrl } from '@/config/env'
import { VisualEditor, type ElementInfo } from '@/utils/visualEditor'
import hljs from 'highlight.js'

import {
  CloudUploadOutlined,
  SendOutlined,
  ExportOutlined,
  InfoCircleOutlined,
  DownloadOutlined,
  EditOutlined,
  SaveOutlined,
  TeamOutlined,
  UserAddOutlined,
  CloseOutlined,
  EyeOutlined,
  FileOutlined,
  FileTextOutlined,
  HistoryOutlined,
  RollbackOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'

const route = useRoute()
const router = useRouter()
const loginUserStore = useLoginUserStore()
const webSocketStore = useWebSocketStore()

// 应用信息
const appInfo = ref<API.AppVO>()
const appId = ref<any>()

// 对话相关
interface Message {
  type: 'user' | 'ai'
  content: string
  loading?: boolean
  createTime?: string
  userId?: number
  userAvatar?: string
  sessionId?: string // 用于关联流式消息
}

const messages = ref<Message[]>([]) // 消息列表
const userInput = ref('')
const isGenerating = ref(false)
const messagesContainer = ref<HTMLElement>()

// 用户信息缓存，用于存储已经查询过的用户信息
const userInfoCache = ref<Map<number, API.UserVO>>(new Map())

// 跟踪正在进行的AI流式会话，用于合并消息
const streamingSessions = ref<
  Map<
    string,
    {
      messageIndex: number
      accumulatedContent: string
      senderId?: number
      senderName?: string
    }
  >
>(new Map())

// 对话历史相关
const loadingHistory = ref(false) // 加载状态
const hasMoreHistory = ref(false) // 是否还有更多历史
const lastCreateTime = ref<string>() //游标：最后一条消息的创建时间
const historyLoaded = ref(false)

// 版本相关
const showVersionSidebar = ref(false)
const versions = ref<API.AppVersionVO[]>([])
const versionsLoading = ref(false)

// 预览相关
const previewUrl = ref('')
const previewReady = ref(false)

// 部署相关
const deploying = ref(false)
const deployModalVisible = ref(false)
const deployUrl = ref('')

// 下载相关
const downloading = ref(false)

// 可视化编辑相关
const isEditMode = ref(false)
const isDirectEditMode = ref(false) // 添加直接修改模式状态
const isSaving = ref(false) // 添加保存状态
const selectedElementInfo = ref<ElementInfo | null>(null)
const visualEditor = new VisualEditor({
  onElementSelected: (elementInfo: ElementInfo) => {
    selectedElementInfo.value = elementInfo
  }
})

// 设备预览相关
const currentDevice = ref('desktop') // 默认桌面模式

// 查看模式相关
type ViewMode = 'preview' | 'source'
const viewMode = ref<ViewMode>('preview')
const sourceCode = ref('')
const sourceCodeLoading = ref(false)
const sourceDirTree = ref<API.SourceCodeFileDTO[]>([])
const sourceDirLoading = ref(false)
const selectedFilePath = ref('')
const selectedFileName = ref('')
const sourceCodeEditMode = ref(false)
const sourceCodeEditContent = ref('')
const sourceCodeSaving = ref(false)

// 设备切换函数
const switchDevice = (device: 'mobile' | 'tablet' | 'desktop') => {
  currentDevice.value = device
}

// 切换到预览模式
const switchToPreview = () => {
  viewMode.value = 'preview'
}

// 切换到源码模式
const switchToSource = async () => {
  viewMode.value = 'source'
  if (sourceDirTree.value.length === 0) {
    await fetchSourceDir()
  }
  if (!selectedFilePath.value && sourceDirTree.value.length > 0) {
    const firstFile = findFirstFile(sourceDirTree.value)
    if (firstFile) {
      await fetchSourceFile(firstFile.path, firstFile.name)
    }
  }
}

// 查找第一个文件
const findFirstFile = (nodes: API.SourceCodeFileDTO[]): API.SourceCodeFileDTO | null => {
  for (const node of nodes) {
    if (!node.isDir) {
      return node
    }
    if (node.children && node.children.length > 0) {
      const found = findFirstFile(node.children)
      if (found) return found
    }
  }
  return null
}

// 获取源码目录
const fetchSourceDir = async () => {
  if (!appId.value) return

  sourceDirLoading.value = true
  try {
    const res = await getAppSourceDir({ appId: appId.value as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      sourceDirTree.value = res.data.data
    } else {
      message.error(res.data.message || '获取文件目录失败')
    }
  } catch (error) {
    console.error('获取文件目录失败：', error)
    message.error('获取文件目录失败，请重试')
  } finally {
    sourceDirLoading.value = false
  }
}

// 选择文件
const handleFileSelect = async (file: API.SourceCodeFileDTO) => {
  if (file.isDir) return
  await fetchSourceFile(file.path, file.name)
}

// 获取指定文件源码
const fetchSourceFile = async (filePath: string, fileName: string) => {
  if (!appId.value || !filePath) return

  selectedFilePath.value = filePath
  selectedFileName.value = fileName
  sourceCodeLoading.value = true
  sourceCode.value = ''

  try {
    const res = await getAppSourceFile({
      appId: appId.value as unknown as number,
      filePath: filePath
    })
    if (res.data.code === 0 && res.data.data) {
      sourceCode.value = res.data.data
    } else {
      message.error(res.data.message || '获取文件源码失败')
    }
  } catch (error) {
    console.error('获取文件源码失败：', error)
    message.error('获取文件源码失败，请重试')
  } finally {
    sourceCodeLoading.value = false
  }
}

// 获取源码语言类型
const sourceCodeLanguageClass = computed(() => {
  const ext = selectedFileName.value?.split('.').pop()?.toLowerCase()
  switch (ext) {
    case 'html':
    case 'htm':
      return 'language-html'
    case 'css':
      return 'language-css'
    case 'js':
    case 'javascript':
      return 'language-javascript'
    case 'json':
      return 'language-json'
    case 'vue':
      return 'language-html'
    default:
      return 'language-html'
  }
})

// 启用源码编辑模式
const enableSourceEditMode = () => {
  sourceCodeEditContent.value = sourceCode.value
  sourceCodeEditMode.value = true
}

// 取消源码编辑
const cancelSourceEditMode = () => {
  sourceCodeEditContent.value = ''
  sourceCodeEditMode.value = false
}

// 保存源码文件
const saveSourceFileContent = async () => {
  if (!appId.value || !selectedFilePath.value) return

  sourceCodeSaving.value = true
  try {
    const res = await saveSourceFile({
      appId: appId.value as unknown as number,
      filePath: selectedFilePath.value,
      content: sourceCodeEditContent.value
    })
    if (res.data.code === 0) {
      sourceCode.value = sourceCodeEditContent.value
      sourceCodeEditMode.value = false
      message.success('保存成功')
    } else {
      message.error(res.data.message || '保存失败')
    }
  } catch (error) {
    console.error('保存文件失败：', error)
    message.error('保存失败，请重试')
  } finally {
    sourceCodeSaving.value = false
  }
}

// 获取源码
const fetchSourceCode = async () => {
  if (!appId.value) return

  sourceCodeLoading.value = true
  try {
    const API_BASE_URL = request.defaults.baseURL || ''
    const url = `${API_BASE_URL}/app/source/${appId.value}`
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include'
    })
    if (!response.ok) {
      throw new Error(`获取源码失败: ${response.status}`)
    }
    const data = await response.json()
    if (data.code === 0 && data.data) {
      sourceCode.value = data.data
    } else {
      message.error(data.message || '获取源码失败')
    }
  } catch (error) {
    console.error('获取源码失败：', error)
    message.error('获取源码失败，请重试')
  } finally {
    sourceCodeLoading.value = false
  }
}

// 计算当前设备的样式类
const deviceClass = computed(() => {
  return `device-${currentDevice.value}`
})

// 高亮源码
const highlightedSourceCode = computed(() => {
  if (!sourceCode.value) return ''
  try {
    return hljs.highlight(sourceCode.value, { language: 'html' }).value
  } catch (error) {
    console.error('高亮源码失败：', error)
    return sourceCode.value
  }
})

// 源码行数
const sourceCodeLineCount = computed(() => {
  if (!sourceCode.value) return 0
  return sourceCode.value.split('\n').length
})

// 直接修改模式切换函数
const toggleDirectEditMode = () => {
  // 检查iframe是否已经加载
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (!iframe) {
    message.warning('请等待页面加载完成')
    return
  }
  // 确保visualEditor已初始化
  if (!previewReady.value) {
    message.warning('请等待页面加载完成')
    return
  }
  // 切换直接修改模式
  const newDirectEditMode = visualEditor.toggleDirectEditMode()
  isDirectEditMode.value = newDirectEditMode
}

// 保存直接修改的内容
const saveDirectEditContent = async () => {
  // 检查iframe是否已经加载
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (!iframe) {
    message.warning('请等待页面加载完成')
    return
  }
  // 确保visualEditor已初始化
  if (!previewReady.value) {
    message.warning('请等待页面加载完成')
    return
  }

  try {
    isSaving.value = true

    // 调用visualEditor的保存方法，获取修改的文件内容
    const modifiedFiles = await visualEditor.saveDirectEdit()

    if (!modifiedFiles || modifiedFiles.length === 0) {
      message.info('没有需要保存的修改')
      return
    }

    // 调用后端API保存修改
    const res = await saveDirectEdit({
      appId: appId.value,
      files: modifiedFiles
    })

    if (res.data.code === 0) {
      message.success('修改保存成功')
    } else {
      message.error('修改保存失败：' + res.data.message)
    }
  } catch (error) {
    console.error('保存直接修改内容失败：', error)
    message.error('保存失败，请重试')
  } finally {
    isSaving.value = false
  }
}

// 权限相关
const isOwner = computed(() => {
  console.log('真假===', isCollaborator.value)
  return appInfo.value?.userId === loginUserStore.loginUser.id || isCollaborator.value
})

const isCreator = computed(() => {
  return appInfo.value?.userId === loginUserStore.loginUser.id
})

// 协作者列表 - 用于判断当前用户是否为协作者
const collaboratorsByApp = ref<API.CollaborationMember[]>([])

// 检查当前用户是否为协作者
const isCollaborator = computed(() => {
  return collaboratorsByApp.value.some(
    (collaborator) => collaborator.userId === loginUserStore.loginUser.id
  )
})

// 更新isAdmin逻辑：包含管理员、应用创建者、协作者
const isAdmin = computed(() => {
  return loginUserStore.loginUser.userRole === 'admin' || isOwner.value || isCollaborator.value
})

// 获取用户信息，优先从缓存中获取，缓存中没有则调用API
const getUserInfo = async (userId: number) => {
  // 如果是当前登录用户，直接返回当前用户信息
  if (userId === loginUserStore.loginUser.id) {
    return loginUserStore.loginUser
  }

  // 检查缓存中是否已有该用户信息
  if (userInfoCache.value.has(userId)) {
    return userInfoCache.value.get(userId)!
  }

  try {
    // 调用API获取用户信息
    const res = await getUserVoById({ id: userId })
    if (res.data.code === 0 && res.data.data) {
      const userInfo = res.data.data
      // 将用户信息存入缓存
      userInfoCache.value.set(userId, userInfo)
      return userInfo
    }
  } catch (error) {
    console.error('获取用户信息失败：', error)
  }
  return null
}

// 应用详情相关
const appDetailVisible = ref(false)

// 显示应用详情
const showAppDetail = () => {
  appDetailVisible.value = true
}

// 协作相关状态
const isCollaborating = ref(false)
const collaborationId = ref<number | null>(null)
const collaborationLoading = ref(false)
const addCollaboratorModalVisible = ref(false)
const collaboratorsModalVisible = ref(false)
const collaboratorsList = ref<API.CollaborationMember[]>([])
const collaboratorsLoading = ref(false)

// 协作者列表列定义
const collaboratorsColumns = [
  {
    title: '用户昵称',
    dataIndex: 'userName',
    key: 'userName',
    ellipsis: true
  },
  {
    title: '加入时间',
    key: 'joinTime',
    width: 200,
    ellipsis: true
  },
  {
    title: '创建时间',
    key: 'createTime',
    width: 200,
    ellipsis: true
  }
]

// 开始协作
const startCollaboration = async () => {
  collaborationLoading.value = true
  try {
    // 调用API开始协作
    const res = await request.post(`/collaboration/start/${appId.value}`)
    if (res.data.code === 0) {
      collaborationId.value = res.data.data
      isCollaborating.value = true
      message.success('开始协作成功')
    } else {
      message.error('开始协作失败：' + res.data.message)
    }
  } catch (error) {
    console.error('开始协作失败：', error)
    message.error('开始协作失败，请重试')
  } finally {
    collaborationLoading.value = false
  }
}

// 显示添加协作者弹窗
const showAddCollaboratorModal = () => {
  addCollaboratorModalVisible.value = true
}

// 显示协作者列表弹窗
const showCollaboratorsModal = async () => {
  if (!collaborationId.value) return

  await fetchCollaboratorsList()
  collaboratorsModalVisible.value = true
}

// 获取协作者列表
const fetchCollaboratorsList = async () => {
  if (!collaborationId.value) return

  collaboratorsLoading.value = true
  try {
    const res = await getCollaborationMembers({ collaborationId: collaborationId.value })
    if (res.data.code === 0 && res.data.data) {
      collaboratorsList.value = res.data.data
    } else {
      message.error('获取协作者列表失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取协作者列表失败：', error)
    message.error('获取协作者列表失败，请重试')
  } finally {
    collaboratorsLoading.value = false
  }
}

// 退出协作
const exitCollaboration = async () => {
  if (!collaborationId.value) return

  collaborationLoading.value = true
  try {
    // 调用API退出协作
    const res = await request.post(`/collaboration/exit/${collaborationId.value}`)
    if (res.data.code === 0) {
      isCollaborating.value = false
      collaborationId.value = null
      message.success('退出协作成功')
    } else {
      message.error('退出协作失败：' + res.data.message)
    }
  } catch (error) {
    console.error('退出协作失败：', error)
    message.error('退出协作失败，请重试')
  } finally {
    collaborationLoading.value = false
  }
}

// 处理添加协作者
const handleAddCollaborators = async (friendIds: number[]) => {
  if (!collaborationId.value) return

  collaborationLoading.value = true
  try {
    // 批量添加协作者
    for (const friendId of friendIds) {
      await request.post(`/collaboration/add/${collaborationId.value}`, { userId: friendId })
    }
    message.success('添加协作者成功')
  } catch (error) {
    console.error('添加协作者失败：', error)
    message.error('添加协作者失败，请重试')
  } finally {
    collaborationLoading.value = false
  }
}

// 加载对话历史
const loadChatHistory = async (isLoadMore = false) => {
  if (!appId.value || loadingHistory.value) return
  loadingHistory.value = true
  try {
    const params: API.listAppChatHistoryParams = {
      appId: appId.value,
      pageSize: 10
    }
    // 如果是加载更多，传递最后一条消息的创建时间作为游标
    if (isLoadMore && lastCreateTime.value) {
      params.lastCreateTime = lastCreateTime.value
    }
    const res = await listAppChatHistory(params)
    if (res.data.code === 0 && res.data.data) {
      const chatHistories = res.data.data.records || []
      if (chatHistories.length > 0) {
        // 将对话历史转换为消息格式，并按时间正序排列（老消息在前）
        const historyMessages: Message[] = []
        for (const chat of chatHistories) {
          const messageType = (chat.messageType === 'user' ? 'user' : 'ai') as 'user' | 'ai'
          const message: Message = {
            type: messageType,
            content: chat.message || '',
            createTime: chat.createTime,
            userId: chat.userId,
            userAvatar: undefined
          }

          // 如果是用户消息，获取用户头像
          if (messageType === 'user') {
            // 如果是当前登录用户，直接使用当前用户头像
            if (chat.userId === loginUserStore.loginUser.id) {
              message.userAvatar = loginUserStore.loginUser.userAvatar
            } else {
              // 否则，调用API获取用户头像
              const userInfo = await getUserInfo(chat.userId!)
              if (userInfo) {
                message.userAvatar = userInfo.userAvatar
              }
            }
          }

          historyMessages.push(message)
        }

        // 反转数组，让老消息在前
        historyMessages.reverse()

        if (isLoadMore) {
          // 加载更多时，将历史消息添加到开头
          messages.value.unshift(...historyMessages)
        } else {
          // 初始加载，直接设置消息列表
          messages.value = historyMessages
        }
        // 更新游标
        lastCreateTime.value = chatHistories[chatHistories.length - 1]?.createTime
        // 检查是否还有更多历史
        hasMoreHistory.value = chatHistories.length === 10
      } else {
        hasMoreHistory.value = false
      }
      historyLoaded.value = true
    }
  } catch (error) {
    console.error('加载对话历史失败：', error)
    message.error('加载对话历史失败')
  } finally {
    loadingHistory.value = false
  }
}

// 加载更多历史消息
const loadMoreHistory = async () => {
  await loadChatHistory(true)
}

// 获取应用信息
const fetchAppInfo = async () => {
  const id = route.params.id as string
  if (!id) {
    message.error('应用ID不存在')
    router.push('/')
    return
  }

  appId.value = id

  try {
    const res = await getAppVoById({ id: id as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      appInfo.value = res.data.data
      // 查询应用的协作者列表
      await fetchCollaboratorsByAppId()
      // 先加载对话历史
      await loadChatHistory()
      // 检查URL参数，如果有view=1，则直接展示预览
      const viewParam = route.query.view
      // 如果有至少2条对话记录，或者URL中有view=1参数，展示对应的网站
      if (messages.value.length >= 2 || viewParam === '1') {
        updatePreview()
      }
      // 检查是否需要自动发送初始提示词
      // 只有在是自己的应用且没有对话历史时才自动发送
      if (
        appInfo.value.initPrompt &&
        isOwner.value &&
        messages.value.length === 0 &&
        historyLoaded.value
      ) {
        await sendInitialMessage(appInfo.value.initPrompt)
      }

      // 检查是否存在协作记录
      await checkCollaborationRecord()
    } else {
      message.error('获取应用信息失败')
      router.push('/')
    }
  } catch (error) {
    console.error('获取应用信息失败：', error)
    message.error('获取应用信息失败')
    router.push('/')
  }
}

// 根据应用ID获取协作者列表
const fetchCollaboratorsByAppId = async () => {
  if (!appId.value) return

  try {
    const res = await getCollaboratorsByAppId({ appId: appId.value as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      collaboratorsByApp.value = res.data.data
    }
  } catch (error) {
    console.error('获取协作者列表失败：', error)
    // 获取协作者列表失败不影响页面正常显示，仅记录日志
  }
}

// 检查是否存在协作记录
const checkCollaborationRecord = async () => {
  try {
    const res = await import('@/api/collaborationController').then((module) =>
      module.getCollaborationRecordByAppId({ appId: appId.value as unknown as number })
    )
    if (res.data.code === 0 && res.data.data && res.data.data.id) {
      // 存在协作记录，恢复协作状态
      const record = res.data.data
      isCollaborating.value = true
      collaborationId.value = record.id || null
    }
  } catch (error) {
    console.error('检查协作记录失败：', error)
    // 检查失败不影响页面正常显示，仅记录日志
  }
}

// 发送初始消息
const sendInitialMessage = async (prompt: string) => {
  // 添加用户消息
  messages.value.push({
    type: 'user',
    content: prompt,
    userId: loginUserStore.loginUser.id,
    userAvatar: loginUserStore.loginUser.userAvatar
  })

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(prompt, aiMessageIndex)
}

// 发送消息
const sendMessage = async () => {
  if (!userInput.value.trim() || isGenerating.value) {
    return
  }

  let message = userInput.value.trim()
  // 如果有选中的元素，将元素信息添加到提示词中
  if (selectedElementInfo.value) {
    let elementContext = `\n\n选中元素信息：`
    if (selectedElementInfo.value.pagePath) {
      elementContext += `\n- 页面路径: ${selectedElementInfo.value.pagePath}`
    }
    elementContext += `\n- 标签: ${selectedElementInfo.value.tagName.toLowerCase()}\n- 选择器: ${selectedElementInfo.value.selector}`
    if (selectedElementInfo.value.textContent) {
      elementContext += `\n- 当前内容: ${selectedElementInfo.value.textContent.substring(0, 100)}`
    }
    message += elementContext
  }
  userInput.value = ''
  // 添加用户消息（包含元素信息）
  messages.value.push({
    type: 'user',
    content: message,
    userId: loginUserStore.loginUser.id,
    userAvatar: loginUserStore.loginUser.userAvatar
  })

  // 广播消息给协作者
  if (appId.value) {
    webSocketStore.sendCollaborationMessage(message, Number(appId.value))
  }

  // 发送消息后，清除选中元素并退出编辑模式
  if (selectedElementInfo.value) {
    clearSelectedElement()
    if (isEditMode.value) {
      toggleEditMode()
    }
  }

  // 添加AI消息占位符
  const aiMessageIndex = messages.value.length
  messages.value.push({
    type: 'ai',
    content: '',
    loading: true
  })

  await nextTick()
  scrollToBottom()

  // 开始生成
  isGenerating.value = true
  await generateCode(message, aiMessageIndex)
}

// 生成代码 - 使用 EventSource 处理流式响应
const generateCode = async (userMessage: string, aiMessageIndex: number) => {
  let eventSource: EventSource | null = null
  let streamCompleted = false

  try {
    // 获取 axios 配置的 baseURL
    const baseURL = request.defaults.baseURL || API_BASE_URL
    // 构建URL参数
    const params = new URLSearchParams({
      appId: String(appId.value || ''),
      message: userMessage
    })

    const url = `${baseURL}/app/chat/gen/code?${params}`

    // 创建 EventSource 连接
    eventSource = new EventSource(url, {
      withCredentials: true
    })

    let fullContent = ''

    // 处理接收到的消息
    eventSource.onmessage = function(event) {
      if (streamCompleted) return

      try {
        // 解析JSON包装的数据
        const parsed = JSON.parse(event.data)
        const content = parsed.d

        // 拼接内容
        if (content !== undefined && content !== null) {
          fullContent += content
          messages.value[aiMessageIndex].content = fullContent
          messages.value[aiMessageIndex].loading = false
          scrollToBottom()
        }
      } catch (error) {
        console.error('解析消息失败:', error)
        handleError(error, aiMessageIndex)
      }
    }

    // 处理done事件
    eventSource.addEventListener('done', function() {
      if (streamCompleted) return

      streamCompleted = true
      isGenerating.value = false
      eventSource?.close()

      // 延迟更新预览，确保后端已完成处理
      setTimeout(async () => {
        await fetchAppInfo()
        updatePreview()
      }, 1000)
    })
    // 处理business-error事件（后端限流等错误）
    eventSource.addEventListener('business-error', function(event: MessageEvent) {
      if (streamCompleted) return

      try {
        const errorData = JSON.parse(event.data)
        console.error('SSE业务错误事件:', errorData)

        // 显示具体的错误信息
        const errorMessage = errorData.message || '生成过程中出现错误'
        messages.value[aiMessageIndex].content = `❌ ${errorMessage}`
        messages.value[aiMessageIndex].loading = false
        message.error(errorMessage)

        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()
      } catch (parseError) {
        console.error('解析错误事件失败:', parseError, '原始数据:', event.data)
        handleError(new Error('服务器返回错误'), aiMessageIndex)
      }
    })
    // 处理错误
    eventSource.onerror = function() {
      if (streamCompleted || !isGenerating.value) return
      // 检查是否是正常的连接关闭
      if (eventSource?.readyState === EventSource.CONNECTING) {
        streamCompleted = true
        isGenerating.value = false
        eventSource?.close()

        setTimeout(async () => {
          await fetchAppInfo()
          updatePreview()
        }, 1000)
      } else {
        handleError(new Error('SSE连接错误'), aiMessageIndex)
      }
    }
  } catch (error) {
    console.error('创建 EventSource 失败：', error)
    handleError(error, aiMessageIndex)
  }
}

// 错误处理函数
const handleError = (error: unknown, aiMessageIndex: number) => {
  console.error('生成代码失败：', error)
  messages.value[aiMessageIndex].content = '抱歉，生成过程中出现了错误，请重试。'
  messages.value[aiMessageIndex].loading = false
  message.error('生成失败，请重试')
  isGenerating.value = false
}

// 更新预览
const updatePreview = (forceRefresh = false) => {
  if (appId.value) {
    const codeGenType = appInfo.value?.codeGenType || CodeGenTypeEnum.HTML
    let newPreviewUrl = getStaticPreviewUrl(codeGenType, String(appId.value))
    
    // 如果需要强制刷新，添加时间戳参数
    if (forceRefresh) {
      const separator = newPreviewUrl.includes('?') ? '&' : '?'
      newPreviewUrl = `${newPreviewUrl}${separator}_t=${Date.now()}`
    }
    
    previewUrl.value = newPreviewUrl
    previewReady.value = true
  }
}

// 滚动到底部
const scrollToBottom = () => {
  if (messagesContainer.value) {
    messagesContainer.value.scrollTop = messagesContainer.value.scrollHeight
  }
}

// 下载代码
const downloadCode = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }
  downloading.value = true
  try {
    const API_BASE_URL = request.defaults.baseURL || ''
    const url = `${API_BASE_URL}/app/download/${appId.value}`
    const response = await fetch(url, {
      method: 'GET',
      credentials: 'include'
    })
    if (!response.ok) {
      throw new Error(`下载失败: ${response.status}`)
    }
    // 获取文件名
    const contentDisposition = response.headers.get('Content-Disposition')
    const fileName = contentDisposition?.match(/filename="(.+)"/)?.[1] || `app-${appId.value}.zip`
    // 下载文件
    const blob = await response.blob()
    const downloadUrl = URL.createObjectURL(blob)
    const link = document.createElement('a')
    link.href = downloadUrl
    link.download = fileName
    link.click()
    // 清理
    URL.revokeObjectURL(downloadUrl)
    message.success('代码下载成功')
  } catch (error) {
    console.error('下载失败：', error)
    message.error('下载失败，请重试')
  } finally {
    downloading.value = false
  }
}

// 部署应用
const deployApp = async () => {
  if (!appId.value) {
    message.error('应用ID不存在')
    return
  }

  deploying.value = true
  try {
    const res = await deployAppApi({
      appId: appId.value as unknown as number
    })

    if (res.data.code === 0 && res.data.data) {
      deployUrl.value = res.data.data
      deployModalVisible.value = true
      message.success('部署成功')
    } else {
      message.error('部署失败：' + res.data.message)
    }
  } catch (error) {
    console.error('部署失败：', error)
    message.error('部署失败，请重试')
  } finally {
    deploying.value = false
  }
}

// 在新窗口打开预览
const openInNewTab = () => {
  if (previewUrl.value) {
    window.open(previewUrl.value, '_blank')
  }
}

// 打开部署的网站
const openDeployedSite = () => {
  if (deployUrl.value) {
    window.open(deployUrl.value, '_blank')
  }
}

// iframe加载完成
const onIframeLoad = () => {
  previewReady.value = true
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (iframe) {
    visualEditor.init(iframe)
    visualEditor.onIframeLoad()
  }
}

// 编辑应用
const editApp = () => {
  if (appInfo.value?.id) {
    router.push(`/app/edit/${appInfo.value.id}`)
  }
}

// 删除应用
const deleteApp = async () => {
  if (!appInfo.value?.id) return

  try {
    const res = await deleteAppApi({ id: appInfo.value.id })
    if (res.data.code === 0) {
      message.success('删除成功')
      appDetailVisible.value = false
      router.push('/')
    } else {
      message.error('删除失败：' + res.data.message)
    }
  } catch (error) {
    console.error('删除失败：', error)
    message.error('删除失败')
  }
}

// 可视化编辑相关函数
const toggleEditMode = () => {
  // 检查 iframe 是否已经加载
  const iframe = document.querySelector('.preview-iframe') as HTMLIFrameElement
  if (!iframe) {
    message.warning('请等待页面加载完成')
    return
  }
  // 确保 visualEditor 已初始化
  if (!previewReady.value) {
    message.warning('请等待页面加载完成')
    return
  }

  const newEditMode = visualEditor.toggleEditMode()
  isEditMode.value = newEditMode

  // 如果退出编辑模式，同时退出直接修改模式
  if (!newEditMode && isDirectEditMode.value) {
    visualEditor.toggleDirectEditMode()
    isDirectEditMode.value = false
  }
}

const clearSelectedElement = () => {
  selectedElementInfo.value = null
  visualEditor.clearSelection()
}

const getInputPlaceholder = () => {
  if (selectedElementInfo.value) {
    return `正在编辑 ${selectedElementInfo.value.tagName.toLowerCase()} 元素，描述您想要的修改...`
  }
  return '请描述你想生成的网站，越详细效果越好哦'
}

// 页面加载时获取应用信息
onMounted(async () => {
  await fetchAppInfo()

  // 监听 iframe 消息
  window.addEventListener('message', (event) => {
    visualEditor.handleIframeMessage(event)
  })

  // 监听AI流式回答消息（分块，用于合并显示）
  window.addEventListener('ai-answer-stream', async (event: Event) => {
    const data = (event as CustomEvent).detail
    // 检查是否是当前应用的消息
    if (data.appId && Number(data.appId) === Number(appId.value)) {
      await handleAiStreamChunk(data)
    }
  })

  // 监听AI流式回答结束消息（显示完整内容）
  window.addEventListener('ai-answer-stream-end', async (event: Event) => {
    const data = (event as CustomEvent).detail
    // 检查是否是当前应用的消息
    if (data.appId && Number(data.appId) === Number(appId.value)) {
      await handleAiStreamEnd(data)
    }
  })

  // 兼容旧版AI回答共享消息（单条消息）
  window.addEventListener('ai-answer-share', async (event: Event) => {
    const data = (event as CustomEvent).detail
    // 检查是否是当前应用的消息
    if (data.appId && Number(data.appId) === Number(appId.value)) {
      // 添加AI消息
      await addRealTimeAiMessage(data.message, data.senderId, data.senderName)
    }
  })

  // 监听协作用户消息（协作者实时消息）
  window.addEventListener('collaboration_message', async (event: Event) => {
    const data = (event as CustomEvent).detail
    // 检查是否是当前应用的消息
    if (data.appId && Number(data.appId) === Number(appId.value)) {
      // 直接使用后端发送的发送者信息（包含头像）
      messages.value.push({
        type: 'user',
        content: data.message,
        userId: data.senderId,
        userAvatar: data.senderAvatar || loginUserStore.loginUser.userAvatar
      })

      await nextTick()
      scrollToBottom()
    }
  })
})

// 处理AI流式消息分块
const handleAiStreamChunk = async (data: {
  sessionId: string
  chunk: string
  appId: number
  senderId?: number
  senderName?: string
}) => {
  const { sessionId, chunk } = data

  // 检查是否已有该会话的消息
  if (streamingSessions.value.has(sessionId)) {
    // 累积内容
    const session = streamingSessions.value.get(sessionId)!
    session.accumulatedContent += chunk
    // 更新消息内容
    messages.value[session.messageIndex].content = session.accumulatedContent
  } else {
    // 创建新的AI消息占位符
    const messageIndex = messages.value.length
    messages.value.push({
      type: 'ai',
      content: chunk,
      loading: true,
      userId: data.senderId,
      userAvatar: aiAvatar,
      sessionId: sessionId
    })

    // 记录会话信息
    streamingSessions.value.set(sessionId, {
      messageIndex: messageIndex,
      accumulatedContent: chunk,
      senderId: data.senderId,
      senderName: data.senderName
    })
  }

  await nextTick()
  scrollToBottom()
}

// 处理AI流式消息结束
const handleAiStreamEnd = async (data: {
  sessionId: string
  fullContent: string
  appId: number
  senderId?: number
  senderName?: string
}) => {
  const { sessionId, fullContent } = data

  // 检查是否有该会话的消息
  if (streamingSessions.value.has(sessionId)) {
    const session = streamingSessions.value.get(sessionId)!

    // 更新消息内容为完整内容
    messages.value[session.messageIndex].content = fullContent
    messages.value[session.messageIndex].loading = false

    // 清理会话
    streamingSessions.value.delete(sessionId)

    await nextTick()
    scrollToBottom()

    // 更新预览
    updatePreview()
  }
}

// 版本相关函数
const toggleVersionSidebar = async () => {
  showVersionSidebar.value = !showVersionSidebar.value
  if (showVersionSidebar.value) {
    await fetchVersions()
  }
}

const fetchVersions = async () => {
  if (!appId.value) return

  versionsLoading.value = true
  try {
    const res = await listVersions({ appId: appId.value as unknown as number })
    if (res.data.code === 0 && res.data.data) {
      versions.value = res.data.data
    } else {
      message.error(res.data.message || '获取版本列表失败')
    }
  } catch (error) {
    console.error('获取版本列表失败：', error)
    message.error('获取版本列表失败，请重试')
  } finally {
    versionsLoading.value = false
  }
}

const handleVersionClick = (version: API.AppVersionVO) => {
  if (version.isCurrent === 1) {
    message.info('当前已经是最新版本')
    return
  }
  message.info(`查看版本 ${version.versionName} 的详细信息`)
}
const handleRollbackVersion = async (versionId: number) => {
  try {
    const res = await rollbackToVersion({ versionId: versionId as unknown as number })
    if (res.data.code === 0) {
      message.success('已回退到指定版本')
      await fetchVersions()
      await fetchAppInfo()
      // 强制刷新预览，添加时间戳参数
      updatePreview(true)
    } else {
      message.error(res.data.message || '版本回退失败')
    }
  } catch (error) {
    console.error('版本回退失败：', error)
    message.error('版本回退失败，请重试')
  }
}

const handleDeleteVersion = async (versionId: number) => {
  try {
    const res = await deleteVersion({ versionId: versionId as unknown as number })
    if (res.data.code === 0) {
      message.success('版本删除成功')
      await fetchVersions()
    } else {
      message.error(res.data.message || '版本删除失败')
    }
  } catch (error) {
    console.error('版本删除失败：', error)
    message.error('版本删除失败，请重试')
  }
}
const formatTime = (time: string) => {
  if (!time) return ''
  const date = new Date(time)
  const now = new Date()
  const diff = now.getTime() - date.getTime()
  const minutes = Math.floor(diff / 60000)
  const hours = Math.floor(diff / 3600000)
  const days = Math.floor(diff / 86400000)

  if (minutes < 1) return '刚刚'
  if (minutes < 60) return `${minutes}分钟前`
  if (hours < 24) return `${hours}小时前`
  if (days < 7) return `${days}天前`
  return date.toLocaleDateString()
}

// 添加实时AI消息（旧版兼容）
const addRealTimeAiMessage = async (content: string) => {
  // 添加AI消息占位符
  messages.value.push({
    type: 'ai',
    content: content,
    loading: false,
    userAvatar: aiAvatar
  })

  await nextTick()
  scrollToBottom()

  // 更新预览
  updatePreview()
}

// 清理资源
onUnmounted(() => {
  // 移除事件监听器
  window.removeEventListener('message', (event) => {
    visualEditor.handleIframeMessage(event)
  })
  window.removeEventListener('ai-answer-stream', (event: Event) => {
    const data = (event as CustomEvent).detail
    if (data.appId && Number(data.appId) === Number(appId.value)) {
      // 消息处理逻辑
    }
  })
  window.removeEventListener('ai-answer-stream-end', (event: Event) => {
    const data = (event as CustomEvent).detail
    if (data.appId && Number(data.appId) === Number(appId.value)) {
      // 消息处理逻辑
    }
  })
  window.removeEventListener('ai-answer-share', (event: Event) => {
    const data = (event as CustomEvent).detail
    if (data.appId && Number(data.appId) === Number(appId.value)) {
      // 消息处理逻辑
    }
  })
  window.removeEventListener('collaboration-message', (event: Event) => {
    const data = (event as CustomEvent).detail
    if (data.appId && Number(data.appId) === Number(appId.value)) {
      // 消息处理逻辑
    }
  })
  // 清理流式会话
  streamingSessions.value.clear()
  // EventSource 会在组件卸载时自动清理
})
</script>

<style scoped>
#appChatPage {
  height: 100vh;
  display: flex;
  flex-direction: column;
  padding: 16px;
  background: #fdfdfd;
}

/* 顶部栏 */
.header-bar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
}

.header-left {
  display: flex;
  align-items: center;
  gap: 12px;
}

.code-gen-type-tag {
  font-size: 12px;
}

.app-name {
  margin: 0;
  font-size: 18px;
  font-weight: 600;
  color: #1a1a1a;
}

.header-right {
  display: flex;
  gap: 12px;
}

/* 主要内容区域 */
.main-content {
  flex: 1;
  display: flex;
  gap: 16px;
  padding: 8px;
  overflow: hidden;
}

/* 左侧对话区域 */
.chat-section {
  flex: 2;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.messages-container {
  flex: 0.9;
  padding: 16px;
  overflow-y: auto;
  scroll-behavior: smooth;
}

.message-item {
  margin-bottom: 12px;
}

.user-message {
  display: flex;
  justify-content: flex-end;
  align-items: flex-start;
  gap: 8px;
}

.ai-message {
  display: flex;
  justify-content: flex-start;
  align-items: flex-start;
  gap: 8px;
}

.message-content {
  max-width: 70%;
  padding: 12px 16px;
  border-radius: 12px;
  line-height: 1.5;
  word-wrap: break-word;
}

.user-message .message-content {
  background: #1890ff;
  color: white;
}

.ai-message .message-content {
  background: #f5f5f5;
  color: #1a1a1a;
  padding: 8px 12px;
}

.message-avatar {
  flex-shrink: 0;
}

.loading-indicator {
  display: flex;
  align-items: center;
  gap: 8px;
  color: #666;
}

/* 加载更多按钮 */
.load-more-container {
  text-align: center;
  padding: 8px 0;
  margin-bottom: 16px;
}

/* 输入区域 */
.input-container {
  padding: 16px;
  background: white;
}

.input-wrapper {
  position: relative;
}

.input-wrapper .ant-input {
  padding-right: 50px;
}

.input-actions {
  position: absolute;
  bottom: 8px;
  right: 8px;
}

/* 右侧预览区域 */
.preview-section {
  flex: 3;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 16px;
  border-bottom: 1px solid #e8e8e8;
}

.preview-header h3 {
  margin: 0;
  font-size: 16px;
  font-weight: 600;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.preview-content {
  flex: 1;
  position: relative;
  overflow: hidden;
}

.preview-placeholder {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
}

.placeholder-icon {
  font-size: 48px;
  margin-bottom: 16px;
}

.preview-loading {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #666;
}

.preview-loading p {
  margin-top: 16px;
}

.preview-iframe {
  width: 100%;
  height: 100%;
  border: none;
}

/* 源码查看器样式 */
.source-code-view {
  width: 100%;
  height: 100%;
  display: flex;
  background-color: #ffffff;
}

.source-code-sidebar {
  width: 220px;
  min-width: 180px;
  background-color: #f5f5f5;
  border-right: 1px solid #e8e8e8;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.sidebar-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  color: #333;
  font-size: 14px;
  font-weight: 600;
}

.file-tree {
  flex: 1;
  overflow-y: auto;
  padding: 8px 0;
}

.file-tree-loading {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 20px;
  color: #999;
}

.source-code-main {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.source-code-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  color: #333;
  font-size: 14px;
  font-weight: 500;
}

.source-code-actions {
  margin-left: auto;
  display: flex;
  gap: 8px;
}

.source-code-empty {
  flex: 1;
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  color: #999;
}

.source-code-empty .anticon {
  font-size: 48px;
  margin-bottom: 16px;
}

.source-code-empty p {
  margin: 0;
}

.source-code-content {
  flex: 1;
  overflow: auto;
  padding: 16px;
  background-color: #fafafa;
}

.source-code-content.edit-mode {
  padding: 8px;
}

.source-code-editor {
  width: 100%;
  height: 100%;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
}

.source-code-with-lines {
  display: flex;
  gap: 16px;
}

.line-numbers {
  display: flex;
  flex-direction: column;
  align-items: flex-end;
  padding-right: 8px;
  border-right: 1px solid #e8e8e8;
  color: #999;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
  user-select: none;
}

.line-numbers span {
  min-width: 24px;
  text-align: right;
}

.source-code-viewer {
  margin: 0;
  padding: 0;
  flex: 1;
  font-family: 'Monaco', 'Menlo', 'Ubuntu Mono', monospace;
  font-size: 14px;
  line-height: 1.5;
  color: #333;
  white-space: pre;
  overflow: visible;
}

.source-code-viewer code {
  font-family: inherit;
}

/* 查看模式按钮激活状态 */
.view-mode-active {
  background-color: #1890ff !important;
  border-color: #1890ff !important;
  color: white !important;
}

.view-mode-active:hover {
  background-color: #40a9ff !important;
  border-color: #40a9ff !important;
}

/* 设备切换按钮样式 */
.device-switcher {
  display: flex;
  gap: 8px;
  margin-right: 12px;
  border-right: 1px solid #e8e8e8;
  padding-right: 12px;
}

.device-switcher .ant-btn {
  border-radius: 6px;
  padding: 4px 12px;
  transition: all 0.3s ease;
}

.device-switcher .ant-btn:hover {
  background-color: #f0f0f0;
}

.device-active {
  background-color: #1890ff !important;
  color: white !important;
}

/* 设备预览容器样式 */
.preview-content {
  flex: 1;
  position: relative;
  overflow: hidden;
  display: flex;
  align-items: center;
  justify-content: center;
  background-color: #f5f5f5;
}

.device-preview-container {
  position: relative;
  transition: all 0.3s ease;
  box-shadow: 0 4px 20px rgba(0, 0, 0, 0.15);
  border-radius: 8px;
  overflow: hidden;
}

/* 桌面模式 */
.device-desktop .device-preview-container {
  width: 100%;
  height: 100%;
  max-width: 100%;
  max-height: 100%;
  border-radius: 0;
  box-shadow: none;
}

/* 平板模式 */
.device-tablet .device-preview-container {
  width: 768px;
  height: 1024px;
  max-width: 90%;
  max-height: 90%;
  background-color: white;
  border: 8px solid #333;
  border-radius: 24px;
}

/* 手机模式 */
.device-mobile .device-preview-container {
  width: 375px;
  height: 667px;
  max-width: 90%;
  max-height: 90%;
  background-color: white;
  border: 8px solid #333;
  border-radius: 24px;
}

.selected-element-alert {
  margin: 0 16px;
}

/* 响应式设计 */
@media (max-width: 1024px) {
  .main-content {
    flex-direction: column;
  }

  .chat-section,
  .preview-section {
    flex: none;
    height: 50vh;
  }
}

@media (max-width: 768px) {
  .header-bar {
    padding: 12px 16px;
  }

  .app-name {
    font-size: 16px;
  }

  .main-content {
    padding: 8px;
    gap: 8px;
  }

  .message-content {
    max-width: 85%;
  }

  /* 选中元素信息样式 */
  .selected-element-alert {
    margin: 0 16px;
  }

  .selected-element-info {
    line-height: 1.4;
  }

  .element-header {
    margin-bottom: 8px;
  }

  .element-details {
    margin-top: 8px;
  }

  .element-item {
    margin-bottom: 4px;
    font-size: 13px;
  }

  .element-item:last-child {
    margin-bottom: 0;
  }

  .element-tag {
    font-family: 'Monaco', 'Menlo', monospace;
    font-size: 14px;
    font-weight: 600;
    color: #007bff;
  }

  .element-id {
    color: #28a745;
    margin-left: 4px;
  }

  .element-class {
    color: #ffc107;
    margin-left: 4px;
  }

  .element-selector-code {
    font-family: 'Monaco', 'Menlo', monospace;
    background: #f6f8fa;
    padding: 2px 4px;
    border-radius: 3px;
    font-size: 12px;
    color: #d73a49;
    border: 1px solid #e1e4e8;
  }

  /* 编辑模式按钮样式 */
  .edit-mode-active {
    background-color: #52c41a !important;
    border-color: #52c41a !important;
    color: white !important;
  }

  .edit-mode-active:hover {
    background-color: #73d13d !important;
    border-color: #73d13d !important;
  }

  /* 直接修改按钮样式 */
  .direct-edit-active {
    background-color: #faad14 !important;
    border-color: #faad14 !important;
    color: white !important;
  }

  .direct-edit-active:hover {
    background-color: #fa8c16 !important;
    border-color: #fa8c16 !important;
  }
}

/* 版本侧边栏样式 */
.version-sidebar {
  width: 280px;
  min-width: 280px;
  display: flex;
  flex-direction: column;
  background: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
  border: 1px solid #e8e8e8;
}

.version-sidebar-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  padding: 12px 16px;
  border-bottom: 1px solid #e8e8e8;
  background: #fafafa;
  color: #333;
  font-size: 14px;
  font-weight: 600;
  gap: 8px;
}

.version-sidebar-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.version-empty {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 40px 20px;
  color: #999;
}

.version-empty .anticon {
  font-size: 48px;
  margin-bottom: 16px;
}

.version-empty p {
  margin: 0;
  font-size: 14px;
}

.version-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.version-item {
  padding: 12px;
  border: 1px solid #e8e8e8;
  border-radius: 6px;
  cursor: pointer;
  transition: all 0.2s ease;
  background: white;
}

.version-item:hover {
  border-color: #1890ff;
  box-shadow: 0 2px 8px rgba(24, 144, 255, 0.1);
}

.version-item.version-current {
  border-color: #52c41a;
  background: #f6ffed;
}

.version-header {
  display: flex;
  align-items: center;
  justify-content: space-between;
  margin-bottom: 8px;
}

.version-name {
  font-weight: 600;
  color: #333;
  font-size: 14px;
}

.version-info {
  margin-bottom: 8px;
}

.version-desc {
  font-size: 12px;
  color: #666;
  margin-bottom: 6px;
  line-height: 1.4;
}

.version-meta {
  display: flex;
  align-items: center;
  justify-content: space-between;
  font-size: 12px;
  color: #999;
}

.version-time {
  flex: 1;
}

.version-user {
  margin-left: 8px;
}

.version-actions {
  display: flex;
  gap: 8px;
  padding-top: 8px;
  border-top: 1px solid #f0f0f0;
}

.version-actions .ant-btn-link {
  padding: 0;
  height: auto;
  font-size: 12px;
}

.version-sidebar-active {
  background-color: #1890ff !important;
  border-color: #1890ff !important;
  color: white !important;
}

.version-sidebar-active:hover {
  background-color: #40a9ff !important;
  border-color: #40a9ff !important;
}

/* 响应式设计 - 版本侧边栏 */
@media (max-width: 1024px) {
  .version-sidebar {
    width: 240px;
    min-width: 240px;
  }
}

@media (max-width: 768px) {
  .version-sidebar {
    position: fixed;
    right: 0;
    top: 0;
    height: 100vh;
    z-index: 1000;
    width: 100%;
    min-width: 100%;
    border-radius: 0;
  }

  .version-sidebar-header {
    padding: 16px;
  }

  .version-item {
    padding: 16px;
  }
}
</style>
