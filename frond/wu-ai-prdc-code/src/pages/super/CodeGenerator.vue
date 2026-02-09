<template>
  <div class="code-generator">
    <div class="generator-content">
      <div class="input-section">
        <a-card class="input-card">
          <template #title>
            <span class="card-title">
              <span class="title-dot"></span>
              需求描述
            </span>
          </template>

          <a-textarea
            v-model:value="requirement"
            placeholder="例如：创建一个现代化的个人博客网站首页，包含响应式导航栏、英雄区域、特性介绍、团队成员展示和页脚..."
            :rows="6"
            :maxlength="3000"
            class="requirement-input"
            :show-count="true"
          />

          <div class="options-row">
            <div class="option-item">
              <span class="option-label">技术栈：</span>
              <a-select
                v-model:value="techStack"
                style="width: 160px"
                class="tech-select"
              >
                <a-select-option value="vue">Vue 3</a-select-option>
                <a-select-option value="react">React</a-select-option>
                <a-select-option value="html">HTML/CSS/JS</a-select-option>
              </a-select>
            </div>

            <div class="option-item">
              <span class="option-label">UI 框架：</span>
              <a-select
                v-model:value="uiFramework"
                style="width: 160px"
                class="tech-select"
              >
                <a-select-option value="antd">Ant Design</a-select-option>
                <a-select-option value="element">Element Plus</a-select-option>
                <a-select-option value="tailwind">Tailwind CSS</a-select-option>
              </a-select>
            </div>
          </div>

          <div class="action-row">
            <a-button
              type="primary"
              size="large"
              :loading="isGenerating"
              :disabled="!requirement.trim()"
              class="generate-btn"
              @click="generateCode"
            >
              <template #icon>
                <CodeOutlined />
              </template>
              {{ isGenerating ? '正在生成...' : '生成代码' }}
            </a-button>
          </div>
        </a-card>
      </div>

      <div class="preview-section">
        <a-card class="preview-card">
          <template #title>
            <div class="preview-header">
              <span class="card-title">
                <span class="title-dot preview"></span>
                代码预览
              </span>
              <div class="preview-actions">
                <a-button
                  type="text"
                  size="small"
                  @click="copyCode"
                  :disabled="!generatedCode"
                >
                  <template #icon>
                    <CopyOutlined />
                  </template>
                  复制
                </a-button>
                <a-button
                  type="text"
                  size="small"
                  @click="downloadCode"
                  :disabled="!generatedCode"
                >
                  <template #icon>
                    <DownloadOutlined />
                  </template>
                  下载
                </a-button>
              </div>
            </div>
          </template>

          <div class="code-preview">
            <div v-if="!generatedCode" class="empty-state">
              <CodeOutlined class="empty-icon" />
              <p>输入需求描述，点击生成按钮开始生成代码</p>
            </div>

            <a-tabs v-else v-model:activeKey="activeTab" class="code-tabs">
              <a-tab-pane key="vue" tab="App.vue">
                <pre class="code-block"><code>{{ generatedCode }}</code></pre>
              </a-tab-pane>
              <a-tab-pane key="css" tab="App.css">
                <pre class="code-block"><code>{{ generatedCSS }}</code></pre>
              </a-tab-pane>
            </a-tabs>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { message } from 'ant-design-vue'
import {
  CodeOutlined,
  CopyOutlined,
  DownloadOutlined
} from '@ant-design/icons-vue'

const requirement = ref('')
const techStack = ref('vue')
const uiFramework = ref('antd')
const isGenerating = ref(false)
const generatedCode = ref('')
const generatedCSS = ref('')
const activeTab = ref('vue')

const generateCode = async () => {
  if (!requirement.value.trim()) {
    message.warning('请输入需求描述')
    return
  }

  isGenerating.value = true

  await new Promise(resolve => setTimeout(resolve, 2000))

  generatedCode.value = `&lt;!-- App.vue --&gt;
&lt;template&gt;
  &lt;div class="app-container"&gt;
    &lt;header class="app-header"&gt;
      &lt;nav class="navbar"&gt;
        &lt;div class="logo"&gt;
          &lt;span class="logo-icon"&gt;🚀&lt;/span&gt;
          &lt;span class="logo-text"&gt;AI 应用&lt;/span&gt;
        &lt;/div&gt;
        &lt;ul class="nav-links"&gt;
          &lt;li&gt;&lt;a href="#"&gt;首页&lt;/a&gt;&lt;/li&gt;
          &lt;li&gt;&lt;a href="#"&gt;功能&lt;/a&gt;&lt;/li&gt;
          &lt;li&gt;&lt;a href="#"&gt;关于&lt;/a&gt;&lt;/li&gt;
        &lt;/ul&gt;
      &lt;/nav&gt;
    &lt;/header&gt;

    &lt;main class="app-main"&gt;
      &lt;section class="hero-section"&gt;
        &lt;h1&gt;欢迎使用 AI 代码生成&lt;/h1&gt;
        &lt;p&gt;${requirement.value}&lt;/p&gt;
        &lt;button class="cta-button"&gt;开始使用&lt;/button&gt;
      &lt;/section&gt;
    &lt;/main&gt;

    &lt;footer class="app-footer"&gt;
      &lt;p&gt;© 2024 AI 应用生成平台&lt;/p&gt;
    &lt;/footer&gt;
  &lt;/div&gt;
&lt;/template&gt;

&lt;script setup&gt;
import { ref } from 'vue'

const message = () => {
  console.log('AI Code Generator')
}
&lt;/script&gt;`

  generatedCSS.value = `/* App.css */
* {
  margin: 0;
  padding: 0;
  box-sizing: border-box;
}

.app-container {
  min-height: 100vh;
  display: flex;
  flex-direction: column;
}

.app-header {
  background: rgba(255, 255, 255, 0.95);
  backdrop-filter: blur(10px);
  padding: 0 24px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.06);
}

.navbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  max-width: 1200px;
  margin: 0 auto;
  height: 64px;
}

.logo {
  display: flex;
  align-items: center;
  gap: 8px;
  font-size: 20px;
  font-weight: 700;
  background: linear-gradient(135deg, #667eea, #764ba2);
  -webkit-background-clip: text;
  -webkit-text-fill-color: transparent;
}

.nav-links {
  display: flex;
  gap: 32px;
  list-style: none;
}

.nav-links a {
  text-decoration: none;
  color: #64748b;
  font-weight: 500;
  transition: color 0.3s ease;
}

.nav-links a:hover {
  color: #667eea;
}

.cta-button {
  background: linear-gradient(135deg, #667eea, #764ba2);
  color: white;
  border: none;
  padding: 12px 32px;
  border-radius: 8px;
  font-weight: 600;
  cursor: pointer;
  transition: all 0.3s ease;
}

.cta-button:hover {
  transform: translateY(-2px);
  box-shadow: 0 8px 24px rgba(102, 126, 234, 0.4);
}`

  isGenerating.value = false
  message.success('代码生成成功')
}

const copyCode = async () => {
  try {
    await navigator.clipboard.writeText(
      activeTab.value === 'vue' ? generatedCode.value : generatedCSS.value
    )
    message.success('代码已复制到剪贴板')
  } catch {
    message.error('复制失败')
  }
}

const downloadCode = () => {
  const content = activeTab.value === 'vue' ? generatedCode.value : generatedCSS.value
  const blob = new Blob([content], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = activeTab.value === 'vue' ? 'App.vue' : 'App.css'
  a.click()
  URL.revokeObjectURL(url)
  message.success('文件下载成功')
}
</script>

<style scoped>
.code-generator {
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

.input-section,
.preview-section {
  display: flex;
  flex-direction: column;
  min-height: 0;
}

.input-card,
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

.requirement-input {
  border-radius: 12px;
  padding: 16px;
  font-size: 15px;
  resize: none;
  margin-bottom: 20px;
}

.options-row {
  display: flex;
  gap: 24px;
  margin-bottom: 24px;
}

.option-item {
  display: flex;
  align-items: center;
  gap: 12px;
}

.option-label {
  font-size: 14px;
  color: #64748b;
  white-space: nowrap;
}

.tech-select {
  border-radius: 8px;
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

.preview-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.preview-actions {
  display: flex;
  gap: 8px;
}

.code-preview {
  flex: 1;
  overflow: auto;
  background: #1e1e1e;
  border-radius: 12px;
  padding: 16px;
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

.code-block {
  margin: 0;
  padding: 16px;
  background: transparent;
  color: #d4d4d4;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 14px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.code-tabs {
  height: 100%;
}

.code-tabs :deep(.ant-tabs-content) {
  height: calc(100% - 46px);
}

.code-tabs :deep(.ant-tabs-tabpane) {
  height: 100%;
}

@media (max-width: 992px) {
  .generator-content {
    grid-template-columns: 1fr;
  }
}
</style>
