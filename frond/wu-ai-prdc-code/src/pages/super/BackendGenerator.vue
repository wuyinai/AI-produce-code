<template>
  <div class="backend-generator">
    <div class="page-header">
      <div class="header-content">
        <div class="header-icon">⚙️</div>
        <div class="header-text">
          <h2>后端代码生成</h2>
          <p>快速生成高质量的后端 API 和服务代码</p>
        </div>
      </div>
    </div>

    <div class="generator-content">
      <div class="input-section">
        <a-card class="input-card">
          <template #title>
            <span class="card-title">
              <span class="title-dot"></span>
              API 设计
            </span>
          </template>

          <a-form layout="vertical" class="api-form">
            <a-form-item label="API 名称" required>
              <a-input
                v-model:value="apiConfig.name"
                placeholder="例如：用户管理 API"
                size="large"
              />
            </a-form-item>

            <a-form-item label="API 描述" required>
              <a-textarea
                v-model:value="apiConfig.description"
                placeholder="描述 API 的功能，如：提供用户的增删改查功能，包括用户注册、登录、信息修改等..."
                :rows="3"
              />
            </a-form-item>

            <a-form-item label="技术栈">
              <div class="tech-stack-grid">
                <a-tag
                  v-for="tech in techStackOptions"
                  :key="tech.value"
                  :class="['tech-tag', { active: apiConfig.techStack.includes(tech.value) }]"
                  @click="toggleTechStack(tech.value)"
                >
                  {{ tech.label }}
                </a-tag>
              </div>
            </a-form-item>

            <a-form-item label="数据库">
              <a-select
                v-model:value="apiConfig.database"
                style="width: 100%"
                placeholder="选择数据库类型"
              >
                <a-select-option value="mysql">MySQL</a-select-option>
                <a-select-option value="postgresql">PostgreSQL</a-select-option>
                <a-select-option value="mongodb">MongoDB</a-select-option>
                <a-select-option value="redis">Redis</a-select-option>
              </a-select>
            </a-form-item>

            <a-form-item label="API 接口">
              <div class="endpoint-list">
                <div
                  v-for="(endpoint, index) in apiConfig.endpoints"
                  :key="index"
                  class="endpoint-item"
                >
                  <a-select
                    v-model:value="endpoint.method"
                    style="width: 120px"
                    size="small"
                  >
                    <a-select-option value="GET">GET</a-select-option>
                    <a-select-option value="POST">POST</a-select-option>
                    <a-select-option value="PUT">PUT</a-select-option>
                    <a-select-option value="DELETE">DELETE</a-select-option>
                  </a-select>
                  <a-input
                    v-model:value="endpoint.path"
                    placeholder="/api/users"
                    size="small"
                  />
                  <a-button
                    type="text"
                    danger
                    size="small"
                    @click="removeEndpoint(index)"
                    v-if="apiConfig.endpoints.length > 1"
                  >
                    <template #icon>
                      <DeleteOutlined />
                    </template>
                  </a-button>
                </div>
                <a-button type="dashed" block @click="addEndpoint">
                  <template #icon>
                    <PlusOutlined />
                  </template>
                  添加接口
                </a-button>
              </div>
            </a-form-item>

            <a-form-item>
              <a-button
                type="primary"
                size="large"
                :loading="isGenerating"
                :disabled="!canGenerate"
                class="generate-btn"
                @click="generateBackend"
              >
                <template #icon>
                  <CodeOutlined />
                </template>
                {{ isGenerating ? '正在生成...' : '生成代码' }}
              </a-button>
            </a-form-item>
          </a-form>
        </a-card>
      </div>

      <div class="output-section">
        <a-card class="output-card">
          <template #title>
            <div class="output-header">
              <span class="card-title">
                <span class="title-dot output"></span>
                代码预览
              </span>
              <div class="output-actions">
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
            <a-tabs v-model:activeKey="activeTab" class="code-tabs">
              <a-tab-pane key="controller" tab="Controller">
                <pre class="code-block" v-if="generatedCode.controller"><code>{{ generatedCode.controller }}</code></pre>
              </a-tab-pane>
              <a-tab-pane key="service" tab="Service">
                <pre class="code-block" v-if="generatedCode.service"><code>{{ generatedCode.service }}</code></pre>
              </a-tab-pane>
              <a-tab-pane key="mapper" tab="Mapper">
                <pre class="code-block" v-if="generatedCode.mapper"><code>{{ generatedCode.mapper }}</code></pre>
              </a-tab-pane>
            </a-tabs>

            <div v-if="!generatedCode.controller" class="empty-state">
              <CodeOutlined class="empty-icon" />
              <p>配置 API 信息，点击生成按钮创建后端代码</p>
            </div>
          </div>
        </a-card>
      </div>
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref, reactive, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  CodeOutlined,
  CopyOutlined,
  DownloadOutlined,
  PlusOutlined,
  DeleteOutlined
} from '@ant-design/icons-vue'

interface Endpoint {
  method: string
  path: string
}

const apiConfig = reactive({
  name: '',
  description: '',
  techStack: ['spring-boot'],
  database: 'mysql',
  endpoints: [
    { method: 'GET', path: '/api/users' },
    { method: 'POST', path: '/api/users' }
  ]
})

const techStackOptions = [
  { value: 'spring-boot', label: 'Spring Boot' },
  { value: 'nodejs', label: 'Node.js' },
  { value: 'python', label: 'Python' },
  { value: 'go', label: 'Go' }
]

const isGenerating = ref(false)
const activeTab = ref('controller')
const generatedCode = reactive({
  controller: '',
  service: '',
  mapper: ''
})

const canGenerate = computed(() => {
  return apiConfig.name.trim() && apiConfig.description.trim()
})

const toggleTechStack = (tech: string) => {
  const index = apiConfig.techStack.indexOf(tech)
  if (index > -1) {
    apiConfig.techStack.splice(index, 1)
  } else {
    apiConfig.techStack.push(tech)
  }
}

const addEndpoint = () => {
  apiConfig.endpoints.push({ method: 'GET', path: '/api/' })
}

const removeEndpoint = (index: number) => {
  apiConfig.endpoints.splice(index, 1)
}

const generateBackend = async () => {
  if (!canGenerate.value) {
    message.warning('请填写 API 名称和描述')
    return
  }

  isGenerating.value = true

  await new Promise(resolve => setTimeout(resolve, 2000))

  const className = apiConfig.name.replace(/\s+/g, '')

  generatedCode.controller = `package com.example.api.controller;

import com.example.api.common.Result;
import com.example.api.entity.${className};
import com.example.api.service.${className}Service;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/${apiConfig.name.toLowerCase().replace(/\s+/g, '-')}")
public class ${className}Controller {

    private final ${className}Service ${className.toLowerCase()}Service;

    public ${className}Controller(${className}Service ${className.toLowerCase()}Service) {
        this.${className.toLowerCase()}Service = ${className.toLowerCase()}Service;
    }

    @GetMapping
    public Result<List<${className}>> list() {
        return Result.success(${className.toLowerCase()}Service.list());
    }

    @GetMapping("/{id}")
    public Result<${className}> getById(@PathVariable Long id) {
        return Result.success(${className.toLowerCase()}Service.getById(id));
    }

    @PostMapping
    public Result<Void> save(@RequestBody ${className} ${className.toLowerCase()}) {
        ${className.toLowerCase()}Service.save(${className.toLowerCase()});
        return Result.success();
    }

    @PutMapping("/{id}")
    public Result<Void> update(@PathVariable Long id, @RequestBody ${className} ${className.toLowerCase()}) {
        ${className.toLowerCase()}.setId(id);
        ${className.toLowerCase()}Service.update(${className.toLowerCase()});
        return Result.success();
    }

    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        ${className.toLowerCase()}Service.delete(id);
        return Result.success();
    }
}`

  generatedCode.service = `package com.example.api.service;

import com.example.api.common.Result;
import com.example.api.entity.${className};
import com.example.api.mapper.${className}Mapper;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ${className}Service {

    private final ${className}Mapper ${className.toLowerCase()}Mapper;

    public ${className}Service(${className}Mapper ${className.toLowerCase()}Mapper) {
        this.${className.toLowerCase()}Mapper = ${className.toLowerCase()}Mapper;
    }

    public List<${className}> list() {
        return ${className.toLowerCase()}Mapper.selectList(null);
    }

    public ${className} getById(Long id) {
        return ${className.toLowerCase()}Mapper.selectById(id);
    }

    public void save(${className} ${className.toLowerCase()}) {
        ${className.toLowerCase()}Mapper.insert(${className.toLowerCase()});
    }

    public void update(${className} ${className.toLowerCase()}) {
        ${className.toLowerCase()}Mapper.updateById(${className.toLowerCase()});
    }

    public void delete(Long id) {
        ${className.toLowerCase()}Mapper.deleteById(id);
    }
}`

  generatedCode.mapper = `package com.example.api.mapper;

import com.example.api.entity.${className};
import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface ${className}Mapper extends BaseMapper<${className}> {
}`

  isGenerating.value = false
  message.success('后端代码生成成功')
}

const copyCode = async () => {
  const code = generatedCode[activeTab.value as keyof typeof generatedCode]
  if (!code) return

  try {
    await navigator.clipboard.writeText(code)
    message.success('代码已复制到剪贴板')
  } catch {
    message.error('复制失败')
  }
}

const downloadCode = () => {
  const code = generatedCode[activeTab.value as keyof typeof generatedCode]
  if (!code) return

  const extensions: Record<string, string> = {
    controller: 'java',
    service: 'java',
    mapper: 'java'
  }

  const blob = new Blob([code], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${className}${activeTab.value.charAt(0).toUpperCase() + activeTab.value.slice(1)}.${extensions[activeTab.value]}`
  a.click()
  URL.revokeObjectURL(url)
  message.success('文件下载成功')
}
</script>

<style scoped>
.backend-generator {
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

.api-form {
  margin-top: 16px;
}

.tech-stack-grid {
  display: flex;
  gap: 12px;
  flex-wrap: wrap;
}

.tech-tag {
  cursor: pointer;
  border-radius: 8px;
  padding: 6px 16px;
  border: 2px solid #e2e8f0;
  transition: all 0.3s ease;
}

.tech-tag:hover {
  border-color: #667eea;
}

.tech-tag.active {
  border-color: #667eea;
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.endpoint-list {
  display: flex;
  flex-direction: column;
  gap: 12px;
}

.endpoint-item {
  display: flex;
  gap: 12px;
  align-items: center;
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

.output-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.output-actions {
  display: flex;
  gap: 8px;
}

.code-preview {
  flex: 1;
  overflow: auto;
  background: #1e1e1e;
  border-radius: 12px;
  position: relative;
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
  padding: 20px;
  color: #d4d4d4;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
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
