<template>
  <div class="requirement-generator">
    <div class="generator-content">
      <div class="input-section">
        <a-card class="input-card">
          <template #title>
            <span class="card-title">
              <span class="title-dot"></span>
              项目描述
            </span>
          </template>

          <a-textarea
            v-model:value="projectDescription"
            placeholder="描述您的项目需求，例如：我们需要开发一个在线教育平台，包含用户注册登录、课程管理、视频播放、在线测试、作业提交等功能..."
            :rows="6"
            :maxlength="3000"
            class="description-input"
            :show-count="true"
          />

          <div class="project-type-section">
            <span class="section-label">项目类型：</span>
            <div class="type-tags">
              <a-tag
                v-for="type in projectTypes"
                :key="type.value"
                :class="['type-tag', { active: selectedType === type.value }]"
                @click="selectedType = type.value"
              >
                <span class="type-icon">{{ type.icon }}</span>
                {{ type.label }}
              </a-tag>
            </div>
          </div>

          <div class="action-row">
            <a-button
              type="primary"
              size="large"
              :loading="isGenerating"
              :disabled="!projectDescription.trim()"
              class="generate-btn"
              @click="generateRequirement"
            >
              <template #icon>
                <FileTextOutlined />
              </template>
              {{ isGenerating ? '正在分析...' : '生成需求文档' }}
            </a-button>
          </div>
        </a-card>
      </div>

      <div class="output-section">
        <a-card class="output-card">
          <template #title>
            <div class="output-header">
              <span class="card-title">
                <span class="title-dot output"></span>
                需求文档
              </span>
              <div class="output-actions">
                <a-button
                  type="primary"
                  size="small"
                  class="generate-app-btn"
                >
                  生成应用
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  @click="copyDocument"
                  :disabled="!generatedDocument"
                >
                  <template #icon>
                    <CopyOutlined />
                  </template>
                  复制
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  @click="downloadDocument"
                  :disabled="!generatedDocument"
                >
                  <template #icon>
                    <DownloadOutlined />
                  </template>
                  下载
                </a-button>
              </div>
            </div>
          </template>

          <div class="document-preview">
            <div v-if="!generatedDocument" class="empty-state">
              <FileTextOutlined class="empty-icon" />
              <p>输入项目描述，点击生成按钮创建需求文档</p>
            </div>

            <div v-else class="document-content">
              <template v-for="(section, index) in documentSections" :key="index">
                <div class="document-section">
                  <h3 class="section-title">
                    <span class="section-number">{{ index + 1 }}</span>
                    {{ section.title }}
                  </h3>
                  <div class="section-content">
                    <p v-for="(point, pIndex) in section.points" :key="pIndex" class="section-point">
                      <span class="point-bullet">•</span>
                      {{ point }}
                    </p>
                  </div>
                </div>
              </template>
            </div>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive } from 'vue'
import { message } from 'ant-design-vue'
import {
  FileTextOutlined,
  CopyOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'

interface DocumentSection {
  title: string
  points: string[]
}

const projectDescription = ref('')
const selectedType = ref('web')
const isGenerating = ref(false)
const generatedDocument = ref('')
const documentSections = ref<DocumentSection[]>([])

const projectTypes = [
  { value: 'web', label: 'Web 应用', icon: '🌐' },
  { value: 'mobile', label: '移动应用', icon: '📱' },
  { value: 'api', label: 'API 服务', icon: '🔌' },
  { value: 'e-commerce', label: '电商平台', icon: '🛒' }
]

const generateRequirement = async () => {
  if (!projectDescription.value.trim()) {
    message.warning('请输入项目描述')
    return
  }

  isGenerating.value = true

  await new Promise(resolve => setTimeout(resolve, 2000))

  const typeLabel = projectTypes.find(t => t.value === selectedType.value)?.label || '应用'

  documentSections.value = [
    {
      title: '项目概述',
      points: [
        `项目名称：AI 生成的 ${typeLabel} 项目`,
        `项目类型：${typeLabel}`,
        `描述：${projectDescription.value.substring(0, 200)}...`,
        `目标用户：面向广大用户提供在线服务`
      ]
    },
    {
      title: '功能需求',
      points: [
        '用户注册与登录功能',
        '个人中心管理',
        '内容浏览与搜索',
        '数据管理与分析',
        '消息通知系统'
      ]
    },
    {
      title: '非功能需求',
      points: [
        '系统响应时间 < 2秒',
        '支持 1000+ 并发用户',
        '数据备份与恢复机制',
        '完善的用户权限控制',
        '跨浏览器兼容性'
      ]
    },
    {
      title: '技术选型',
      points: [
        '前端：Vue 3 + Ant Design',
        '后端：Spring Boot',
        '数据库：MySQL',
        '部署：Docker + Nginx',
        '版本控制：Git'
      ]
    },
    {
      title: '项目计划',
      points: [
        '第一阶段：需求分析与设计（1周）',
        '第二阶段：核心功能开发（3周）',
        '第三阶段：测试与优化（1周）',
        '第四阶段：部署与上线（1周）'
      ]
    }
  ]

  generatedDocument.value = documentSections.value
    .map((section, index) => `${index + 1}. ${section.title}\n${section.points.map(p => '  - ' + p).join('\n')}`)
    .join('\n\n')

  isGenerating.value = false
  message.success('需求文档生成成功')
}

const copyDocument = async () => {
  try {
    await navigator.clipboard.writeText(generatedDocument.value)
    message.success('文档已复制到剪贴板')
  } catch {
    message.error('复制失败')
  }
}

const downloadDocument = () => {
  const blob = new Blob([generatedDocument.value], { type: 'text/markdown' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = '需求文档.md'
  a.click()
  URL.revokeObjectURL(url)
  message.success('文档下载成功')
}
</script>

<style scoped>
.requirement-generator {
  height: calc(100vh - 112px);
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.page-header {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 4px 16px rgba(0, 0, 0, 0.06);
}

.header-content {
  display: flex;
  align-items: center;
  gap: 20px;
}

.header-icon {
  width: 64px;
  height: 64px;
  border-radius: 16px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 32px;
}

.header-text h2 {
  margin: 0 0 8px;
  font-size: 24px;
  font-weight: 600;
  color: #1e293b;
}

.header-text p {
  margin: 0;
  color: #64748b;
  font-size: 14px;
}

.generator-content {
  flex: 1;
  display: grid;
  grid-template-columns: 30% 70%;
  gap: 24px;
  min-height: 0;
}

.input-section,
.output-section {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.input-card,
.output-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  max-height: calc(100vh - 280px);
}

.card-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 16px;
  font-weight: 600;
}

.title-dot {
  width: 8px;
  height: 8px;
  border-radius: 50%;
  background: #667eea;
}

.title-dot.output {
  background: #10b981;
}

.description-input {
  border-radius: 12px;
  padding: 16px;
  font-size: 15px;
  resize: none;
  margin-bottom: 24px;
}

.project-type-section {
  margin-bottom: 24px;
}

.section-label {
  display: block;
  font-size: 14px;
  color: #64748b;
  margin-bottom: 12px;
}

.type-tags {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.type-tag {
  cursor: pointer;
  border-radius: 12px;
  padding: 8px 16px;
  border: 2px solid #e2e8f0;
  background: white;
  transition: all 0.3s ease;
  font-size: 14px;
}

.type-tag:hover {
  border-color: #667eea;
}

.type-tag.active {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.type-icon {
  margin-right: 6px;
}

.action-row {
  display: flex;
  justify-content: flex-end;
}

.generate-btn {
  height: 48px;
  padding: 0 32px;
  border-radius: 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  font-size: 16px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.generate-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}

.output-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.output-actions {
  display: flex;
  gap: 12px;
}

.generate-app-btn {
  background: linear-gradient(135deg, #667eea, #764ba2);
  border: none;
  border-radius: 8px;
  font-weight: 600;
  transition: all 0.3s ease;
}

.generate-app-btn:hover {
  transform: translateY(-2px);
  box-shadow: 0 4px 12px rgba(102, 126, 234, 0.4);
}

.document-preview {
  flex: 1;
  overflow-y: auto;
  overflow-x: hidden;
  background: #f8fafc;
  border-radius: 12px;
  padding: 20px;
  max-height: calc(100vh - 360px);
}

.empty-state {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  height: 100%;
  color: #64748b;
}

.empty-icon {
  font-size: 48px;
  margin-bottom: 16px;
  color: #667eea;
}

.empty-state p {
  font-size: 14px;
}

.document-content {
  display: flex;
  flex-direction: column;
  gap: 24px;
  max-height: calc(100vh - 420px);
  overflow-y: auto;
  padding-right: 8px;
}

.document-section {
  background: white;
  border-radius: 16px;
  padding: 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.04);
}

.section-title {
  display: flex;
  align-items: center;
  gap: 12px;
  margin: 0 0 16px;
  font-size: 18px;
  font-weight: 600;
  color: #1e293b;
}

.section-number {
  width: 28px;
  height: 28px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-size: 14px;
}

.section-content {
  padding-left: 40px;
}

.section-point {
  display: flex;
  align-items: flex-start;
  gap: 10px;
  margin: 0 0 10px;
  font-size: 14px;
  color: #475569;
  line-height: 1.6;
}

.point-bullet {
  color: #667eea;
  font-weight: bold;
}

@media (max-width: 992px) {
  .generator-content {
    grid-template-columns: 1fr;
  }
}
</style>
