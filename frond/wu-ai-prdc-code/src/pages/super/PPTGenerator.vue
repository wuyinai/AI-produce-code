<template>
  <div class="ppt-generator">
    <div class="generator-content">
      <div class="config-section">
        <a-card class="config-card">
          <template #title>
            <span class="card-title">
              <span class="title-dot"></span>
              演示文稿配置
            </span>
          </template>

          <a-form layout="vertical" class="config-form">
            <a-form-item label="演示主题" required>
              <a-input
                v-model:value="pptConfig.topic"
                placeholder="例如：产品发布介绍、项目汇报、技术分享"
                size="large"
              />
            </a-form-item>

            <a-form-item label="演示时长">
              <a-radio-group v-model:value="pptConfig.duration" button-style="solid">
                <a-radio-button value="5min">5分钟</a-radio-button>
                <a-radio-button value="10min">10分钟</a-radio-button>
                <a-radio-button value="15min">15分钟</a-radio-button>
                <a-radio-button value="30min">30分钟</a-radio-button>
              </a-radio-group>
            </a-form-item>

            <a-form-item label="幻灯片数量">
              <a-slider
                v-model:value="pptConfig.slides"
                :min="5"
                :max="30"
                :marks="slideMarks"
                class="slide-slider"
              />
            </a-form-item>

            <a-form-item label="风格主题">
              <div class="theme-grid">
                <div
                  v-for="theme in themes"
                  :key="theme.id"
                  :class="['theme-item', { active: pptConfig.theme === theme.id }]"
                  @click="pptConfig.theme = theme.id"
                >
                  <div class="theme-preview" :style="{ background: theme.gradient }"></div>
                  <span class="theme-name">{{ theme.name }}</span>
                </div>
              </div>
            </a-form-item>

            <a-form-item label="详细描述（可选）">
              <a-textarea
                v-model:value="pptConfig.description"
                placeholder="添加更多细节，如重点内容、目标受众等..."
                :rows="4"
              />
            </a-form-item>

            <a-form-item>
              <a-button
                type="primary"
                size="large"
                :loading="isGenerating"
                :disabled="!pptConfig.topic.trim()"
                class="generate-btn"
                @click="generatePPT"
              >
                <template #icon>
                  <FilePptOutlined />
                </template>
                {{ isGenerating ? '正在生成...' : '生成 PPT' }}
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </div>

      <div class="preview-section">
        <a-card class="preview-card">
          <template #title>
            <div class="preview-header">
              <span class="card-title">
                <span class="title-dot preview"></span>
                幻灯片预览
              </span>
              <div class="preview-actions">
                <a-button
                  type="primary"
                  ghost
                  size="small"
                  :disabled="!generatedSlides.length"
                  @click="downloadPPT"
                >
                  <template #icon>
                    <DownloadOutlined />
                  </template>
                  下载 PPTX
                </a-button>
              </div>
            </div>
          </template>

          <div class="slides-preview">
            <div v-if="!generatedSlides.length" class="empty-state">
              <FilePptOutlined class="empty-icon" />
              <p>配置演示文稿参数，点击生成按钮创建 PPT</p>
            </div>

            <div v-else class="slides-list">
              <div
                v-for="(slide, index) in generatedSlides"
                :key="index"
                class="slide-item"
              >
                <div class="slide-number">{{ index + 1 }}</div>
                <div class="slide-content">
                  <h4 class="slide-title">{{ slide.title }}</h4>
                  <p class="slide-points">
                    {{ slide.content.substring(0, 100) }}{{ slide.content.length > 100 ? '...' : '' }}
                  </p>
                </div>
              </div>
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
  FilePptOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'

interface Slide {
  title: string
  content: string
}

const pptConfig = reactive({
  topic: '',
  duration: '10min',
  slides: 10,
  theme: 'modern',
  description: ''
})

const slideMarks = {
  5: '5',
  10: '10',
  15: '15',
  20: '20',
  25: '25',
  30: '30'
}

const themes = [
  { id: 'modern', name: '现代简约', gradient: 'linear-gradient(135deg, #667eea, #764ba2)' },
  { id: 'business', name: '商务蓝', gradient: 'linear-gradient(135deg, #0c3483, #a2b6df)' },
  { id: 'nature', name: '自然清新', gradient: 'linear-gradient(135deg, #11998e, #38ef7d)' },
  { id: 'elegant', name: '优雅紫色', gradient: 'linear-gradient(135deg, #8e2de2, #4a00e0)' }
]

const generatedSlides = ref<Slide[]>([])
const isGenerating = ref(false)

const generatePPT = async () => {
  if (!pptConfig.topic.trim()) {
    message.warning('请输入演示主题')
    return
  }

  isGenerating.value = true

  await new Promise(resolve => setTimeout(resolve, 2000))

  const slideTitles = [
    '封面',
    '目录',
    '背景介绍',
    '主要内容',
    '核心亮点',
    '案例分析',
    '数据支持',
    '实施方案',
    '预期效果',
    '总结展望'
  ]

  generatedSlides.value = slideTitles.map((title, index) => ({
    title: `${index + 1}. ${title}`,
    content: `${pptConfig.topic} - ${title}部分内容。AI 自动生成的演示文稿，包含专业的排版和内容结构。`
  }))

  isGenerating.value = false
  message.success(`成功生成 ${generatedSlides.value.length} 页幻灯片`)
}

const downloadPPT = () => {
  message.success('PPT 文件下载中...')
  setTimeout(() => {
    message.success('PPT 下载功能演示，实际会生成 .pptx 文件')
  }, 1000)
}
</script>

<style scoped>
.ppt-generator {
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
  grid-template-columns: 1fr 1fr;
  gap: 24px;
  min-height: 0;
}

.config-section,
.preview-section {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.config-card,
.preview-card {
  height: 100%;
  display: flex;
  flex-direction: column;
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

.title-dot.preview {
  background: #10b981;
}

.config-form {
  margin-top: 16px;
}

.slide-slider {
  padding: 8px 0;
}

.theme-grid {
  display: grid;
  grid-template-columns: repeat(4, 1fr);
  gap: 16px;
}

.theme-item {
  cursor: pointer;
  border-radius: 12px;
  overflow: hidden;
  border: 3px solid transparent;
  transition: all 0.3s ease;
}

.theme-item:hover {
  transform: translateY(-4px);
}

.theme-item.active {
  border-color: #667eea;
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.3);
}

.theme-preview {
  height: 60px;
  border-radius: 8px 8px 0 0;
}

.theme-name {
  display: block;
  padding: 8px;
  text-align: center;
  font-size: 12px;
  background: #f8fafc;
  color: #64748b;
}

.generate-btn {
  width: 100%;
  height: 48px;
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

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.slides-preview {
  flex: 1;
  overflow: auto;
  padding: 8px;
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

.slides-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.slide-item {
  display: flex;
  gap: 16px;
  padding: 16px;
  background: #f8fafc;
  border-radius: 12px;
  transition: all 0.3s ease;
}

.slide-item:hover {
  background: #f1f5f9;
  transform: translateX(4px);
}

.slide-number {
  width: 32px;
  height: 32px;
  border-radius: 8px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  display: flex;
  align-items: center;
  justify-content: center;
  font-weight: 600;
  font-size: 14px;
  flex-shrink: 0;
}

.slide-content {
  flex: 1;
  min-width: 0;
}

.slide-title {
  margin: 0 0 8px;
  font-size: 15px;
  font-weight: 600;
  color: #1e293b;
}

.slide-points {
  margin: 0;
  font-size: 13px;
  color: #64748b;
  line-height: 1.5;
}

@media (max-width: 992px) {
  .generator-content {
    grid-template-columns: 1fr;
  }

  .theme-grid {
    grid-template-columns: repeat(2, 1fr);
  }
}
</style>
