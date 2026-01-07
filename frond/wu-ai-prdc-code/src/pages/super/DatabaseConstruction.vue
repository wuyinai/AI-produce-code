<template>
  <div class="db-designer">
    <!-- 顶部工具栏 -->
    <div class="toolbar">
      <div class="toolbar-left">
        <a-space>
          <a-select v-model:value="currentDbType" style="width: 160px" @change="onDbTypeChange">
            <a-select-option value="mysql">
              <span><DatabaseOutlined /> MySQL</span>
            </a-select-option>
            <a-select-option value="postgresql">
              <span><DatabaseOutlined /> PostgreSQL</span>
            </a-select-option>
            <a-select-option value="sqlserver">
              <span><WindowsOutlined /> SQL Server</span>
            </a-select-option>
            <a-select-option value="oracle">
              <span><GoldOutlined /> Oracle</span>
            </a-select-option>
          </a-select>
          
          <a-button-group>
            <a-tooltip title="新建项目">
              <a-button @click="newProject">
                <template #icon><FileAddOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="打开项目">
              <a-button @click="openProject">
                <template #icon><FolderOpenOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="保存项目">
              <a-button @click="saveProject" :disabled="!hasChanges">
                <template #icon><SaveOutlined /></template>
              </a-button>
            </a-tooltip>
          </a-button-group>
        </a-space>
      </div>
      
      <div class="toolbar-center">
        <a-radio-group v-model:value="currentMode" button-style="solid" size="small">
          <a-radio-button value="design">
            <LayoutOutlined /> 设计
          </a-radio-button>
          <a-radio-button value="sql">
            <CodeOutlined /> SQL预览
          </a-radio-button>
        </a-radio-group>
      </div>
      
      <div class="toolbar-right">
        <a-space>
          <a-tooltip title="导出SQL">
            <a-button @click="exportSQL" :disabled="tables.length === 0">
              <template #icon><DownloadOutlined /></template>
              导出SQL
            </a-button>
          </a-tooltip>
          <a-tooltip title="导入SQL">
            <a-button @click="importSQL">
              <template #icon><UploadOutlined /></template>
              导入SQL
            </a-button>
          </a-tooltip>
        </a-space>
      </div>
    </div>

    <!-- 主内容区域 -->
    <div class="main-content">
      <!-- 左侧组件面板 -->
      <div class="left-panel" :style="{ width: leftPanelWidth + 'px' }">
        <div class="panel-tabs">
          <a-tabs v-model:activeKey="leftPanelTab" size="small">
            <a-tab-pane key="tables" tab="数据表" />
            <a-tab-pane key="components" tab="组件" />
          </a-tabs>
        </div>
        
        <div class="panel-content">
          <!-- 数据表列表 -->
          <div v-if="leftPanelTab === 'tables'" class="tables-list">
            <div
              v-for="table in tables"
              :key="table.id"
              class="table-item"
              :class="{ active: selectedTable?.id === table.id }"
              @click="selectTable(table)"
              @dblclick="editTable(table)"
            >
              <TableOutlined class="table-icon" />
              <span class="table-name">{{ table.name }}</span>
              <a-badge :count="table.fields.length" class="field-count" />
            </div>
          </div>
          
          <!-- 可拖拽组件 -->
          <div v-else class="components-list">
            <div
              v-for="comp in draggableComponents"
              :key="comp.type"
              class="draggable-component"
              :draggable="true"
              @dragstart="onDragStart($event, comp)"
            >
              <span class="comp-icon">{{ comp.icon }}</span>
              <span class="comp-name">{{ comp.name }}</span>
            </div>
          </div>
        </div>
        
        <!-- 面板宽度调节 -->
        <div
          class="panel-resizer"
          @mousedown="startLeftPanelResize"
        ></div>
      </div>

      <!-- 画布区域 -->
      <div class="canvas-wrapper" ref="canvasWrapper">
        <!-- 画布工具栏 -->
        <div class="canvas-toolbar">
          <a-space>
            <a-tooltip title="放大">
              <a-button size="small" @click="zoomIn">
                <template #icon><ZoomInOutlined /></template>
              </a-button>
            </a-tooltip>
            <span class="zoom-level">{{ Math.round(zoomLevel * 100) }}%</span>
            <a-tooltip title="缩小">
              <a-button size="small" @click="zoomOut">
                <template #icon><ZoomOutOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-tooltip title="适应屏幕">
              <a-button size="small" @click="fitToScreen">
                <template #icon><FullscreenOutlined /></template>
              </a-button>
            </a-tooltip>
            <a-divider type="vertical" />
            <a-tooltip title="添加表">
              <a-button type="primary" size="small" @click="addTable">
                <template #icon><PlusOutlined /></template>
                添加表
              </a-button>
            </a-tooltip>
            <a-tooltip title="关系模式">
              <a-button
                size="small"
                :type="relationshipMode ? 'primary' : 'default'"
                @click="toggleRelationshipMode"
              >
                <template #icon><NodeIndexOutlined /></template>
                关系模式
              </a-button>
            </a-tooltip>
          </a-space>
        </div>

        <!-- 画布 -->
        <div
          class="canvas-area"
          :style="{ transform: `scale(${zoomLevel})` }"
          @dragover.prevent
          @drop="onDrop"
          @click="onCanvasClick"
          ref="canvasArea"
        >
          <!-- 关系线 -->
          <svg class="relationships-layer" v-if="relationships.length > 0">
            <defs>
              <marker id="arrowhead" markerWidth="10" markerHeight="7" refX="9" refY="3.5" orient="auto">
                <polygon points="0 0, 10 3.5, 0 7" fill="#667eea" />
              </marker>
            </defs>
            <line
              v-for="rel in relationships"
              :key="rel.id"
              :x1="rel.sourceX"
              :y1="rel.sourceY"
              :x2="rel.targetX"
              :y2="rel.targetY"
              stroke="#667eea"
              stroke-width="2"
              marker-end="url(#arrowhead)"
              class="relationship-line"
              @click.stop="editRelationship(rel)"
            />
            <text
              v-for="rel in relationships"
              :key="'label-' + rel.id"
              :x="(rel.sourceX + rel.targetX) / 2"
              :y="(rel.sourceY + rel.targetY) / 2 - 5"
              class="relationship-label"
              @click.stop="editRelationship(rel)"
            >
              {{ rel.type }}
            </text>
          </svg>

          <!-- 表卡片 -->
          <div
            v-for="table in tables"
            :key="table.id"
            class="table-card"
            :class="{ selected: selectedTable?.id === table.id }"
            :style="{ left: table.x + 'px', top: table.y + 'px' }"
            @mousedown="startDragTable($event, table)"
            @click.stop="selectTable(table)"
            @dblclick.stop="editTable(table)"
          >
            <div class="table-card-header">
              <TableOutlined />
              <span class="table-card-name">{{ table.name }}</span>
              <a-dropdown :trigger="['click']">
                <a-button type="text" size="small" class="table-menu-btn">
                  <template #icon><MoreOutlined /></template>
                </a-button>
                <template #overlay>
                  <a-menu @click="({ key }) => onTableMenuClick(key, table)">
                    <a-menu-item key="edit"><EditOutlined /> 编辑</a-menu-item>
                    <a-menu-item key="duplicate"><CopyOutlined /> 复制</a-menu-item>
                    <a-menu-item key="delete" danger><DeleteOutlined /> 删除</a-menu-item>
                  </a-menu>
                </template>
              </a-dropdown>
            </div>
            <div class="table-card-fields">
              <div
                v-for="field in table.fields.slice(0, 5)"
                :key="field.name"
                class="table-field-item"
              >
                <span class="field-name">{{ field.name }}</span>
                <span class="field-type">{{ field.type }}</span>
                <KeyOutlined v-if="field.primaryKey" class="primary-key-icon" />
              </div>
              <div v-if="table.fields.length > 5" class="more-fields">
                +{{ table.fields.length - 5 }} 个字段
              </div>
            </div>
          </div>

          <!-- 关系连接点 -->
          <div
            v-for="table in tables"
            :key="'connector-' + table.id"
            class="connection-point"
            :style="{ left: table.x + 100 + 'px', top: table.y - 10 + 'px' }"
            @mousedown="startRelationship($event, table)"
          >
            <PlusCircleOutlined />
          </div>
        </div>
      </div>

      <!-- SQL预览面板 -->
      <div class="sql-panel" :style="{ width: sqlPanelWidth + 'px' }">
        <div class="sql-panel-header">
          <span class="sql-panel-title">
            <CodeOutlined /> SQL预览
          </span>
          <a-space>
            <a-button size="small" @click="copySQL" :disabled="!generatedSQL">
              <template #icon><CopyOutlined /></template>
              复制
            </a-button>
            <a-button size="small" @click="downloadSQL" :disabled="!generatedSQL">
              <template #icon><DownloadOutlined /></template>
              下载
            </a-button>
          </a-space>
        </div>
        <div class="sql-panel-content">
          <pre class="sql-code"><code>{{ generatedSQL }}</code></pre>
        </div>
        <div class="panel-resizer-horizontal" @mousedown="startSqlPanelResize"></div>
      </div>
    </div>

    <!-- 表编辑弹窗 -->
    <a-modal
      v-model:open="editTableModalVisible"
      :title="editingTable ? '编辑表: ' + editingTable.name : '新建表'"
      width="900px"
      :footer="null"
    >
      <div class="table-editor">
        <!-- 表基本信息 -->
        <a-form :model="editingTable" layout="inline" class="table-basic-info">
          <a-form-item label="表名" required>
            <a-input v-model:value="editingTable.name" placeholder="输入表名" />
          </a-form-item>
          <a-form-item label="表注释">
            <a-input v-model:value="editingTable.comment" placeholder="表注释" />
          </a-form-item>
          <a-form-item label="表引擎">
            <a-select v-model:value="editingTable.engine" style="width: 120px" v-if="currentDbType === 'mysql'">
              <a-select-option value="InnoDB">InnoDB</a-select-option>
              <a-select-option value="MyISAM">MyISAM</a-select-option>
            </a-select>
          </a-form-item>
        </a-form>

        <!-- 字段列表 -->
        <div class="fields-editor">
          <div class="fields-header">
            <span class="fields-title">字段列表</span>
            <a-space>
              <a-button size="small" type="primary" @click="addField">
                <template #icon><PlusOutlined /></template>
                添加字段
              </a-button>
              <a-button size="small" @click="importFields">
                <template #icon><ImportOutlined /></template>
                导入
              </a-button>
              <a-button size="small" @click="exportFields">
                <template #icon><ExportOutlined /></template>
                导出
              </a-button>
            </a-space>
          </div>
          
          <a-table
            :columns="fieldColumns"
            :data-source="editingTable.fields"
            :pagination="false"
            size="small"
            :scroll="{ x: 900 }"
            row-key="name"
          >
            <template #bodyCell="{ column, record, index }">
              <template v-if="column.key === 'name'">
                <a-input v-model:value="record.name" size="small" placeholder="字段名" />
              </template>
              <template v-if="column.key === 'type'">
                <a-select
                  v-model:value="record.type"
                  size="small"
                  style="width: 140px"
                  show-search
                  :options="availableFieldTypes"
                />
              </template>
              <template v-if="column.key === 'length'">
                <a-input v-model:value="record.length" size="small" style="width: 60px" placeholder="长度" />
              </template>
              <template v-if="column.key === 'primaryKey'">
                <a-checkbox v-model:checked="record.primaryKey" @change="onPrimaryKeyChange(record)" />
              </template>
              <template v-if="column.key === 'nullable'">
                <a-checkbox v-model:checked="record.nullable" />
              </template>
              <template v-if="column.key === 'default'">
                <a-input v-model:value="record.default" size="small" style="width: 80px" placeholder="默认值" />
              </template>
              <template v-if="column.key === 'comment'">
                <a-input v-model:value="record.comment" size="small" style="width: 100px" placeholder="注释" />
              </template>
              <template v-if="column.key === 'actions'">
                <a-space>
                  <a-button size="small" type="text" @click="moveField(index, -1)" :disabled="index === 0">
                    <template #icon><UpOutlined /></template>
                  </a-button>
                  <a-button size="small" type="text" @click="moveField(index, 1)" :disabled="index === editingTable.fields.length - 1">
                    <template #icon><DownOutlined /></template>
                  </a-button>
                  <a-button size="small" type="text" @click="duplicateField(record)">
                    <template #icon><CopyOutlined /></template>
                  </a-button>
                  <a-button size="small" type="text" danger @click="deleteField(index)">
                    <template #icon><DeleteOutlined /></template>
                  </a-button>
                </a-space>
              </template>
            </template>
          </a-table>
        </div>

        <!-- 弹窗底部按钮 -->
        <div class="modal-footer">
          <a-button @click="previewSQL">预览SQL</a-button>
          <a-space>
            <a-button @click="editTableModalVisible = false">取消</a-button>
            <a-button type="primary" @click="saveTable">保存</a-button>
          </a-space>
        </div>
      </div>
    </a-modal>

    <!-- SQL预览弹窗 -->
    <a-modal
      v-model:open="sqlPreviewModalVisible"
      title="SQL预览"
      width="800px"
      :footer="null"
    >
      <pre class="sql-preview-modal"><code>{{ generatedSQL }}</code></pre>
      <div class="modal-footer">
        <a-space>
          <a-button @click="copySQLFromModal">复制</a-button>
          <a-button type="primary" @click="sqlPreviewModalVisible = false">关闭</a-button>
        </a-space>
      </div>
    </a-modal>

    <!-- 关系编辑弹窗 -->
    <a-modal
      v-model:open="relationshipModalVisible"
      title="编辑关系"
      width="500px"
      :footer="null"
    >
      <a-form :model="editingRelationship" layout="vertical">
        <a-form-item label="关系类型" required>
          <a-select v-model:value="editingRelationship.type">
            <a-select-option value="one-to-one">一对一 (1:1)</a-select-option>
            <a-select-option value="one-to-many">一对多 (1:N)</a-select-option>
            <a-select-option value="many-to-one">多对一 (N:1)</a-select-option>
            <a-select-option value="many-to-many">多对多 (N:M)</a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="源表字段">
          <a-select v-model:value="editingRelationship.sourceField" style="width: 100%">
            <a-select-option v-for="field in getTableFields(editingRelationship.sourceTable)" :key="field.name" :value="field.name">
              {{ field.name }} ({{ field.type }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="目标表字段">
          <a-select v-model:value="editingRelationship.targetField" style="width: 100%">
            <a-select-option v-for="field in getTableFields(editingRelationship.targetTable)" :key="field.name" :value="field.name">
              {{ field.name }} ({{ field.type }})
            </a-select-option>
          </a-select>
        </a-form-item>
        <a-form-item label="关系说明">
          <a-input v-model:value="editingRelationship.comment" placeholder="关系说明" />
        </a-form-item>
      </a-form>
      <div class="modal-footer">
        <a-space>
          <a-button danger @click="deleteRelationship">删除关系</a-button>
          <a-button @click="relationshipModalVisible = false">取消</a-button>
          <a-button type="primary" @click="saveRelationship">保存</a-button>
        </a-space>
      </div>
    </a-modal>

    <!-- 新建表弹窗 -->
    <a-modal
      v-model:open="newTableModalVisible"
      title="新建表"
      width="400px"
      :footer="null"
    >
      <a-form :model="newTableForm" layout="vertical">
        <a-form-item label="表名" required>
          <a-input v-model:value="newTableForm.name" placeholder="输入表名" />
        </a-form-item>
        <a-form-item label="表注释">
          <a-input v-model:value="newTableForm.comment" placeholder="表注释（可选）" />
        </a-form-item>
      </a-form>
      <div class="modal-footer">
        <a-button @click="newTableModalVisible = false">取消</a-button>
        <a-button type="primary" @click="confirmCreateTable" :disabled="!newTableForm.name">创建</a-button>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, computed, onMounted, onUnmounted } from 'vue'
import { message, type MenuProps } from 'ant-design-vue'
import {
  DatabaseOutlined,
  WindowsOutlined,
  GoldOutlined,
  FileAddOutlined,
  FolderOpenOutlined,
  SaveOutlined,
  DownloadOutlined,
  UploadOutlined,
  LayoutOutlined,
  CodeOutlined,
  ZoomInOutlined,
  ZoomOutOutlined,
  FullscreenOutlined,
  PlusOutlined,
  NodeIndexOutlined,
  TableOutlined,
  EditOutlined,
  CopyOutlined,
  DeleteOutlined,
  MoreOutlined,
  KeyOutlined,
  PlusCircleOutlined,
  ImportOutlined,
  ExportOutlined,
  UpOutlined,
  DownOutlined
} from '@ant-design/icons-vue'

// 类型定义
interface Field {
  name: string
  type: string
  length?: string
  primaryKey: boolean
  nullable: boolean
  default?: string
  comment?: string
  unsigned?: boolean
  autoIncrement?: boolean
}

interface Table {
  id: string
  name: string
  comment?: string
  engine?: string
  x: number
  y: number
  fields: Field[]
}

interface Relationship {
  id: string
  sourceTable: string
  targetTable: string
  sourceField: string
  targetField: string
  type: string
  comment?: string
  sourceX: number
  sourceY: number
  targetX: number
  targetY: number
}

interface Project {
  name: string
  dbType: string
  tables: Table[]
  relationships: Relationship[]
  createTime: string
  updateTime: string
}

// 数据库类型
const currentDbType = ref('mysql')
const currentMode = ref('design')

// 面板状态
const leftPanelTab = ref('tables')
const leftPanelWidth = ref(250)
const sqlPanelWidth = ref(350)
const zoomLevel = ref(1)
const relationshipMode = ref(false)

// 数据
const tables = ref<Table[]>([])
const relationships = ref<Relationship[]>([])
const selectedTable = ref<Table | null>(null)
const hasChanges = ref(false)

// 编辑相关
const editTableModalVisible = ref(false)
const sqlPreviewModalVisible = ref(false)
const relationshipModalVisible = ref(false)
const newTableModalVisible = ref(false)

const editingTable = ref<Table | null>(null)
const editingRelationship = ref<Partial<Relationship>>({})
const newTableForm = ref({ name: '', comment: '' })

// 可拖拽组件
const draggableComponents = ref([
  { type: 'table', name: '数据表', icon: '📋' },
  { type: 'view', name: '视图', icon: '👁️' },
  { type: 'index', name: '索引', icon: '📑' }
])

// 字段类型配置
const fieldTypeConfig: Record<string, { label: string; types: { value: string; label: string }[] }> = {
  mysql: {
    label: 'MySQL',
    types: [
      { value: 'TINYINT', label: 'TINYINT' },
      { value: 'SMALLINT', label: 'SMALLINT' },
      { value: 'MEDIUMINT', label: 'MEDIUMINT' },
      { value: 'INT', label: 'INT' },
      { value: 'BIGINT', label: 'BIGINT' },
      { value: 'FLOAT', label: 'FLOAT' },
      { value: 'DOUBLE', label: 'DOUBLE' },
      { value: 'DECIMAL', label: 'DECIMAL' },
      { value: 'CHAR', label: 'CHAR' },
      { value: 'VARCHAR', label: 'VARCHAR' },
      { value: 'TINYTEXT', label: 'TINYTEXT' },
      { value: 'TEXT', label: 'TEXT' },
      { value: 'MEDIUMTEXT', label: 'MEDIUMTEXT' },
      { value: 'LONGTEXT', label: 'LONGTEXT' },
      { value: 'TINYBLOB', label: 'TINYBLOB' },
      { value: 'BLOB', label: 'BLOB' },
      { value: 'MEDIUMBLOB', label: 'MEDIUMBLOB' },
      { value: 'LONGBLOB', label: 'LONGBLOB' },
      { value: 'DATE', label: 'DATE' },
      { value: 'TIME', label: 'TIME' },
      { value: 'DATETIME', label: 'DATETIME' },
      { value: 'TIMESTAMP', label: 'TIMESTAMP' },
      { value: 'YEAR', label: 'YEAR' },
      { value: 'BOOLEAN', label: 'BOOLEAN' },
      { value: 'JSON', label: 'JSON' }
    ]
  },
  postgresql: {
    label: 'PostgreSQL',
    types: [
      { value: 'SMALLINT', label: 'SMALLINT' },
      { value: 'INTEGER', label: 'INTEGER' },
      { value: 'BIGINT', label: 'BIGINT' },
      { value: 'REAL', label: 'REAL' },
      { value: 'DOUBLE PRECISION', label: 'DOUBLE PRECISION' },
      { value: 'NUMERIC', label: 'NUMERIC' },
      { value: 'DECIMAL', label: 'DECIMAL' },
      { value: 'CHAR', label: 'CHAR' },
      { value: 'VARCHAR', label: 'VARCHAR' },
      { value: 'TEXT', label: 'TEXT' },
      { value: 'BYTEA', label: 'BYTEA' },
      { value: 'DATE', label: 'DATE' },
      { value: 'TIME', label: 'TIME' },
      { value: 'TIMESTAMP', label: 'TIMESTAMP' },
      { value: 'TIMESTAMPTZ', label: 'TIMESTAMPTZ' },
      { value: 'INTERVAL', label: 'INTERVAL' },
      { value: 'BOOLEAN', label: 'BOOLEAN' },
      { value: 'ENUM', label: 'ENUM' },
      { value: 'JSON', label: 'JSON' },
      { value: 'JSONB', label: 'JSONB' },
      { value: 'UUID', label: 'UUID' },
      { value: 'ARRAY', label: 'ARRAY' }
    ]
  },
  sqlserver: {
    label: 'SQL Server',
    types: [
      { value: 'TINYINT', label: 'TINYINT' },
      { value: 'SMALLINT', label: 'SMALLINT' },
      { value: 'INT', label: 'INT' },
      { value: 'BIGINT', label: 'BIGINT' },
      { value: 'FLOAT', label: 'FLOAT' },
      { value: 'REAL', label: 'REAL' },
      { value: 'DECIMAL', label: 'DECIMAL' },
      { value: 'NUMERIC', label: 'NUMERIC' },
      { value: 'MONEY', label: 'MONEY' },
      { value: 'CHAR', label: 'CHAR' },
      { value: 'VARCHAR', label: 'VARCHAR' },
      { value: 'TEXT', label: 'TEXT' },
      { value: 'NCHAR', label: 'NCHAR' },
      { value: 'NVARCHAR', label: 'NVARCHAR' },
      { value: 'NTEXT', label: 'NTEXT' },
      { value: 'DATE', label: 'DATE' },
      { value: 'TIME', label: 'TIME' },
      { value: 'DATETIME', label: 'DATETIME' },
      { value: 'DATETIME2', label: 'DATETIME2' },
      { value: 'SMALLDATETIME', label: 'SMALLDATETIME' },
      { value: 'BIT', label: 'BIT' },
      { value: 'UNIQUEIDENTIFIER', label: 'UNIQUEIDENTIFIER' },
      { value: 'XML', label: 'XML' }
    ]
  },
  oracle: {
    label: 'Oracle',
    types: [
      { value: 'NUMBER', label: 'NUMBER' },
      { value: 'FLOAT', label: 'FLOAT' },
      { value: 'BINARY_FLOAT', label: 'BINARY_FLOAT' },
      { value: 'BINARY_DOUBLE', label: 'BINARY_DOUBLE' },
      { value: 'CHAR', label: 'CHAR' },
      { value: 'VARCHAR2', label: 'VARCHAR2' },
      { value: 'NCHAR', label: 'NCHAR' },
      { value: 'NVARCHAR2', label: 'NVARCHAR2' },
      { value: 'CLOB', label: 'CLOB' },
      { value: 'NCLOB', label: 'NCLOB' },
      { value: 'BLOB', label: 'BLOB' },
      { value: 'BFILE', label: 'BFILE' },
      { value: 'DATE', label: 'DATE' },
      { value: 'TIMESTAMP', label: 'TIMESTAMP' },
      { value: 'TIMESTAMP WITH TIME ZONE', label: 'TIMESTAMP WITH TIME ZONE' },
      { value: 'INTERVAL YEAR TO MONTH', label: 'INTERVAL YEAR TO MONTH' },
      { value: 'INTERVAL DAY TO SECOND', label: 'INTERVAL DAY TO SECOND' },
      { value: 'RAW', label: 'RAW' },
      { value: 'LONG RAW', label: 'LONG RAW' },
      { value: 'ROWID', label: 'ROWID' },
      { value: 'UROWID', label: 'UROWID' },
      { value: 'BOOLEAN', label: 'BOOLEAN' }
    ]
  }
}

const availableFieldTypes = computed(() => {
  return fieldTypeConfig[currentDbType.value]?.types || []
})

// 字段表格列配置
const fieldColumns = [
  { title: '字段名', key: 'name', width: 120 },
  { title: '类型', key: 'type', width: 150 },
  { title: '长度', key: 'length', width: 80 },
  { title: '主键', key: 'primaryKey', width: 60 },
  { title: '可空', key: 'nullable', width: 60 },
  { title: '默认值', key: 'default', width: 100 },
  { title: '注释', key: 'comment', width: 120 },
  { title: '操作', key: 'actions', width: 180, fixed: 'right' }
]

// SQL生成
const generatedSQL = computed(() => {
  if (tables.value.length === 0) return '-- 暂无表结构'

  let sql = ''

  tables.value.forEach(table => {
    sql += `-- 表: ${table.name}${table.comment ? ' - ' + table.comment : ''}\n`
    
    const dbType = currentDbType.value
    
    if (dbType === 'mysql') {
      sql += `CREATE TABLE ${table.name} (\n`
    } else if (dbType === 'postgresql') {
      sql += `CREATE TABLE ${table.name} (\n`
    } else if (dbType === 'sqlserver') {
      sql += `CREATE TABLE [dbo].[${table.name}] (\n`
    } else if (dbType === 'oracle') {
      sql += `CREATE TABLE "${table.name.toUpperCase()}" (\n`
    }

    const fieldLines: string[] = []
    table.fields.forEach(field => {
      let line = '  '
      
      if (dbType === 'oracle') {
        line += `"${field.name.toUpperCase()}"`
      } else if (dbType === 'sqlserver') {
        line += `[${field.name}]`
      } else {
        line += field.name
      }
      
      line += ` ${field.type}`
      if (field.length && !['DATE', 'TIME', 'DATETIME', 'TIMESTAMP', 'BOOLEAN', 'JSON', 'TEXT', 'INT'].some(t => field.type.includes(t))) {
        line += `(${field.length})`
      }
      
      if (field.primaryKey) {
        if (dbType === 'mysql' || dbType === 'oracle') {
          line += ' PRIMARY KEY'
        }
      }
      
      if (!field.nullable) {
        line += ' NOT NULL'
      }
      
      if (field.autoIncrement && dbType === 'mysql') {
        line += ' AUTO_INCREMENT'
      }
      
      if (field.default) {
        line += ` DEFAULT ${field.default}`
      }
      
      if (field.comment) {
        if (dbType === 'mysql') {
          line += ` COMMENT '${field.comment}'`
        }
      }
      
      fieldLines.push(line)
    })

    sql += fieldLines.join(',\n')
    
    if (dbType === 'sqlserver') {
      sql += '\n) ON [PRIMARY]'
    } else {
      sql += '\n)'
    }
    
    if (table.comment) {
      if (dbType === 'mysql') {
        sql += ` COMMENT='${table.comment}'`
      }
    }
    
    sql += ';\n\n'
  })

  // 生成外键关系
  relationships.value.forEach(rel => {
    const sourceTable = tables.value.find(t => t.id === rel.sourceTable)
    const targetTable = tables.value.find(t => t.id === rel.targetTable)
    
    if (sourceTable && targetTable) {
      sql += `-- 关系: ${sourceTable.name}.${rel.sourceField} -> ${targetTable.name}.${rel.targetField}\n`
      
      if (currentDbType.value === 'mysql') {
        sql += `ALTER TABLE ${sourceTable.name}\n`
        sql += `  ADD CONSTRAINT fk_${rel.sourceTable}_${rel.targetTable}\n`
        sql += `  FOREIGN KEY (${rel.sourceField}) REFERENCES ${targetTable.name}(${rel.targetField});\n\n`
      }
    }
  })

  return sql || '-- 暂无数据'
})

// 拖拽状态
let draggedComponent: any = null
let draggedTable: Table | null = null
let dragOffset = { x: 0, y: 0 }
let isResizingLeftPanel = false
let isResizingSqlPanel = false

// 组件引用
const canvasWrapper = ref<HTMLElement | null>(null)
const canvasArea = ref<HTMLElement | null>(null)

// 方法
const onDragStart = (event: DragEvent, component: any) => {
  draggedComponent = component
  if (event.dataTransfer) {
    event.dataTransfer.effectAllowed = 'copy'
  }
}

const onDrop = (event: DragEvent) => {
  if (!draggedComponent || !canvasArea.value) return

  const rect = canvasArea.value.getBoundingClientRect()
  const x = event.clientX - rect.left - 100
  const y = event.clientY - rect.top - 30

  if (draggedComponent.type === 'table') {
    newTableForm.value = { name: '', comment: '' }
    newTableModalVisible.value = true
    
    // 临时存储位置
    newTableForm.value.tempX = x
    newTableForm.value.tempY = y
  }

  draggedComponent = null
}

const confirmCreateTable = () => {
  const x = (newTableForm.value as any).tempX || 100
  const y = (newTableForm.value as any).tempY || 100

  const newTable: Table = {
    id: Date.now().toString(),
    name: newTableForm.value.name,
    comment: newTableForm.value.comment,
    engine: currentDbType.value === 'mysql' ? 'InnoDB' : undefined,
    x: Math.max(0, x),
    y: Math.max(0, y),
    fields: [
      {
        name: 'id',
        type: currentDbType.value === 'postgresql' ? 'INTEGER' : 'INT',
        primaryKey: true,
        nullable: false,
        autoIncrement: currentDbType.value === 'mysql'
      },
      {
        name: 'created_at',
        type: currentDbType.value === 'mysql' ? 'DATETIME' : 'TIMESTAMP',
        primaryKey: false,
        nullable: false,
        default: currentDbType.value === 'mysql' ? 'CURRENT_TIMESTAMP' : 'CURRENT_TIMESTAMP'
      },
      {
        name: 'updated_at',
        type: currentDbType.value === 'mysql' ? 'DATETIME' : 'TIMESTAMP',
        primaryKey: false,
        nullable: true
      }
    ]
  }

  tables.value.push(newTable)
  hasChanges.value = true
  newTableModalVisible.value = false
  editTable(newTable)
}

const startDragTable = (event: MouseEvent, table: Table) => {
  if (relationshipMode.value) return
  if (event.button !== 0) return

  draggedTable = table
  dragOffset = {
    x: event.clientX - table.x,
    y: event.clientY - table.y
  }

  document.addEventListener('mousemove', onDragTable)
  document.addEventListener('mouseup', stopDragTable)
}

const onDragTable = (event: MouseEvent) => {
  if (!draggedTable || !canvasArea.value) return

  const rect = canvasArea.value.getBoundingClientRect()
  const newX = event.clientX - rect.left - dragOffset.x + rect.left
  const newY = event.clientY - rect.top - dragOffset.y + rect.top

  draggedTable.x = Math.max(0, Math.min(newX, rect.width - 220))
  draggedTable.y = Math.max(0, Math.min(newY, rect.height - 100))

  updateRelationshipPositions()
}

const stopDragTable = () => {
  draggedTable = null
  document.removeEventListener('mousemove', onDragTable)
  document.removeEventListener('mouseup', stopDragTable)
}

const startLeftPanelResize = () => {
  isResizingLeftPanel = true
  document.addEventListener('mousemove', onLeftPanelResize)
  document.addEventListener('mouseup', stopLeftPanelResize)
}

const onLeftPanelResize = (event: MouseEvent) => {
  if (!isResizingLeftPanel) return
  leftPanelWidth.value = Math.max(180, Math.min(400, event.clientX))
}

const stopLeftPanelResize = () => {
  isResizingLeftPanel = false
  document.removeEventListener('mousemove', onLeftPanelResize)
  document.removeEventListener('mouseup', stopLeftPanelResize)
}

const startSqlPanelResize = () => {
  isResizingSqlPanel = true
  document.addEventListener('mousemove', onSqlPanelResize)
  document.addEventListener('mouseup', stopSqlPanelResize)
}

const onSqlPanelResize = (event: MouseEvent) => {
  if (!isResizingSqlPanel || !canvasWrapper.value) return
  const rect = canvasWrapper.value.getBoundingClientRect()
  sqlPanelWidth.value = Math.max(250, Math.min(600, rect.width - event.clientX + rect.right - 300))
}

const stopSqlPanelResize = () => {
  isResizingSqlPanel = false
  document.removeEventListener('mousemove', onSqlPanelResize)
  document.removeEventListener('mouseup', stopSqlPanelResize)
}

const selectTable = (table: Table) => {
  selectedTable.value = table
}

const editTable = (table: Table) => {
  editingTable.value = { ...table, fields: [...table.fields] }
  editTableModalVisible.value = true
}

const saveTable = () => {
  if (!editingTable.value) return

  const index = tables.value.findIndex(t => t.id === editingTable.value!.id)
  if (index !== -1) {
    tables.value[index] = { ...editingTable.value }
    hasChanges.value = true
  }

  editTableModalVisible.value = false
  editingTable.value = null
}

const deleteTable = (table: Table) => {
  const index = tables.value.findIndex(t => t.id === table.id)
  if (index !== -1) {
    tables.value.splice(index, 1)
    hasChanges.value = true
    
    // 删除关联的关系
    relationships.value = relationships.value.filter(
      rel => rel.sourceTable !== table.id && rel.targetTable !== table.id
    )
  }

  if (selectedTable.value?.id === table.id) {
    selectedTable.value = null
  }
}

const addTable = () => {
  newTableForm.value = { name: '', comment: '' }
  ;(newTableForm.value as any).tempX = 100
  ;(newTableForm.value as any).tempY = 100
  newTableModalVisible.value = true
}

const onTableMenuClick: MenuProps['onClick'] = ({ key }, table: Table) => {
  switch (key) {
    case 'edit':
      editTable(table)
      break
    case 'duplicate':
      duplicateTable(table)
      break
    case 'delete':
      deleteTable(table)
      break
  }
}

const duplicateTable = (table: Table) => {
  const newTable: Table = {
    ...table,
    id: Date.now().toString(),
    name: table.name + '_copy',
    x: table.x + 50,
    y: table.y + 50,
    fields: table.fields.map(f => ({ ...f }))
  }
  tables.value.push(newTable)
  hasChanges.value = true
}

const addField = () => {
  if (!editingTable.value) return
  
  editingTable.value.fields.push({
    name: '',
    type: 'VARCHAR',
    length: '255',
    primaryKey: false,
    nullable: true
  })
}

const deleteField = (index: number) => {
  if (!editingTable.value) return
  editingTable.value.fields.splice(index, 1)
}

const duplicateField = (field: Field) => {
  if (!editingTable.value) return
  const index = editingTable.value.fields.findIndex(f => f === field)
  if (index !== -1) {
    editingTable.value.fields.splice(index + 1, 0, { ...field, name: field.name + '_copy' })
  }
}

const moveField = (index: number, direction: number) => {
  if (!editingTable.value) return
  const newIndex = index + direction
  if (newIndex < 0 || newIndex >= editingTable.value.fields.length) return
  
  const fields = editingTable.value.fields
  ;[fields[index], fields[newIndex]] = [fields[newIndex], fields[index]]
}

const onPrimaryKeyChange = (field: Field) => {
  if (field.primaryKey && editingTable.value) {
    editingTable.value.fields.forEach(f => {
      if (f !== field) {
        f.primaryKey = false
      }
    })
  }
}

const importFields = () => {
  message.info('字段导入功能')
}

const exportFields = () => {
  if (!editingTable.value) return
  const data = JSON.stringify(editingTable.value.fields, null, 2)
  const blob = new Blob([data], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `${editingTable.value.name}_fields.json`
  a.click()
  URL.revokeObjectURL(url)
  message.success('字段导出成功')
}

const previewSQL = () => {
  if (editingTable.value) {
    const index = tables.value.findIndex(t => t.id === editingTable.value!.id)
    if (index !== -1) {
      tables.value[index] = { ...editingTable.value }
    }
  }
  sqlPreviewModalVisible.value = true
  hasChanges.value = true
}

// 关系相关
const toggleRelationshipMode = () => {
  relationshipMode.value = !relationshipMode.value
}

let tempRelationship: any = null

const startRelationship = (event: MouseEvent, table: Table) => {
  if (!relationshipMode.value) return
  
  tempRelationship = {
    sourceTable: table.id,
    sourceX: event.clientX,
    sourceY: event.clientY,
    targetX: event.clientX,
    targetY: event.clientY
  }
  
  document.addEventListener('mousemove', onDragRelationship)
  document.addEventListener('mouseup', stopRelationship)
}

const onDragRelationship = (event: MouseEvent) => {
  if (!tempRelationship) return
  tempRelationship.targetX = event.clientX
  tempRelationship.targetY = event.clientY
}

const stopRelationship = (event: MouseEvent) => {
  if (!tempRelationship) return
  
  // 查找目标表
  const targetTable = tables.value.find(table => {
    const rect = canvasArea.value?.getBoundingClientRect()
    if (!rect) return false
    
    const tableX = table.x + rect.left
    const tableY = table.y + rect.top
    const tableW = 220
    const tableH = 80 + table.fields.length * 30
    
    return event.clientX >= tableX && event.clientX <= tableX + tableW &&
           event.clientY >= tableY && event.clientY <= tableY + tableH
  })
  
  if (targetTable && targetTable.id !== tempRelationship.sourceTable) {
    editingRelationship.value = {
      id: Date.now().toString(),
      sourceTable: tempRelationship.sourceTable,
      targetTable: targetTable.id,
      sourceField: '',
      targetField: '',
      type: 'one-to-many',
      sourceX: tempRelationship.sourceX,
      sourceY: tempRelationship.sourceY,
      targetX: tempRelationship.targetX,
      targetY: tempRelationship.targetY
    }
    relationshipModalVisible.value = true
  }
  
  tempRelationship = null
  document.removeEventListener('mousemove', onDragRelationship)
  document.removeEventListener('mouseup', stopRelationship)
}

const editRelationship = (rel: Relationship) => {
  editingRelationship.value = { ...rel }
  relationshipModalVisible.value = true
}

const saveRelationship = () => {
  if (!editingRelationship.value.id) {
    relationships.value.push(editingRelationship.value as Relationship)
  } else {
    const index = relationships.value.findIndex(r => r.id === editingRelationship.value.id)
    if (index !== -1) {
      relationships.value[index] = { ...editingRelationship.value } as Relationship
    }
  }
  
  updateRelationshipPositions()
  hasChanges.value = true
  relationshipModalVisible.value = false
}

const deleteRelationship = () => {
  if (editingRelationship.value.id) {
    relationships.value = relationships.value.filter(r => r.id !== editingRelationship.value.id)
    hasChanges.value = true
  }
  relationshipModalVisible.value = false
}

const getTableFields = (tableId: string) => {
  return tables.value.find(t => t.id === tableId)?.fields || []
}

const updateRelationshipPositions = () => {
  relationships.value.forEach(rel => {
    const sourceTable = tables.value.find(t => t.id === rel.sourceTable)
    const targetTable = tables.value.find(t => t.id === rel.targetTable)
    
    if (sourceTable) {
      rel.sourceX = sourceTable.x + 110
      rel.sourceY = sourceTable.y + 60 + sourceTable.fields.length * 30
    }
    
    if (targetTable) {
      rel.targetX = targetTable.x + 110
      rel.targetY = targetTable.y
    }
  })
}

// 缩放控制
const zoomIn = () => {
  zoomLevel.value = Math.min(2, zoomLevel.value + 0.1)
}

const zoomOut = () => {
  zoomLevel.value = Math.max(0.3, zoomLevel.value - 0.1)
}

const fitToScreen = () => {
  if (tables.value.length === 0 || !canvasWrapper.value) {
    zoomLevel.value = 1
    return
  }
  
  const rect = canvasWrapper.value.getBoundingClientRect()
  let minX = Infinity, minY = Infinity, maxX = -Infinity, maxY = -Infinity
  
  tables.value.forEach(table => {
    minX = Math.min(minX, table.x)
    minY = Math.min(minY, table.y)
    maxX = Math.max(maxX, table.x + 220)
    maxY = Math.max(maxY, table.y + 100 + table.fields.length * 30)
  })
  
  const width = maxX - minX + 100
  const height = maxY - minY + 100
  
  const scaleX = (rect.width - 100) / width
  const scaleY = (rect.height - 100) / height
  
  zoomLevel.value = Math.min(1, Math.min(scaleX, scaleY))
}

// 数据库类型切换
const onDbTypeChange = () => {
  message.info(`已切换为 ${fieldTypeConfig[currentDbType.value]?.label || currentDbType.value}`)
}

// SQL操作
const copySQL = async () => {
  try {
    await navigator.clipboard.writeText(generatedSQL.value)
    message.success('SQL已复制到剪贴板')
  } catch {
    message.error('复制失败')
  }
}

const downloadSQL = () => {
  const blob = new Blob([generatedSQL.value], { type: 'text/plain' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `schema_${currentDbType.value}_${Date.now()}.sql`
  a.click()
  URL.revokeObjectURL(url)
  message.success('SQL文件下载成功')
}

const copySQLFromModal = async () => {
  await copySQL()
  message.success('已复制到剪剪板')
}

// 项目管理
const newProject = () => {
  if (tables.value.length > 0) {
    message.info('请先保存当前项目')
    return
  }
  message.success('新建项目成功')
}

const openProject = () => {
  message.info('打开项目功能')
}

const saveProject = () => {
  const project: Project = {
    name: '未命名项目',
    dbType: currentDbType.value,
    tables: tables.value,
    relationships: relationships.value,
    createTime: new Date().toISOString(),
    updateTime: new Date().toISOString()
  }
  
  const blob = new Blob([JSON.stringify(project, null, 2)], { type: 'application/json' })
  const url = URL.createObjectURL(blob)
  const a = document.createElement('a')
  a.href = url
  a.download = `db_design_${Date.now()}.json`
  a.click()
  URL.revokeObjectURL(url)
  message.success('项目保存成功')
  hasChanges.value = false
}

const exportSQL = () => {
  downloadSQL()
}

const importSQL = () => {
  message.info('导入SQL功能')
}

const onCanvasClick = () => {
  selectedTable.value = null
}

// 生命周期
onMounted(() => {
  document.addEventListener('mousemove', onDragTable)
  document.addEventListener('mouseup', stopDragTable)
})

onUnmounted(() => {
  document.removeEventListener('mousemove', onDragTable)
  document.removeEventListener('mouseup', stopDragTable)
})
</script>

<style scoped>
.db-designer {
  height: calc(100vh - 112px);
  display: flex;
  flex-direction: column;
  background: #f5f7fa;
}

.toolbar {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 24px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
}

.toolbar-left,
.toolbar-center,
.toolbar-right {
  display: flex;
  align-items: center;
  gap: 16px;
}

.main-content {
  flex: 1;
  display: flex;
  overflow: hidden;
}

.left-panel {
  background: white;
  border-right: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  position: relative;
}

.panel-tabs {
  padding: 8px 12px;
  border-bottom: 1px solid #e2e8f0;
}

.panel-content {
  flex: 1;
  overflow-y: auto;
  padding: 12px;
}

.tables-list {
  display: flex;
  flex-direction: column;
  gap: 4px;
}

.table-item {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 10px 12px;
  border-radius: 8px;
  cursor: pointer;
  transition: all 0.2s ease;
}

.table-item:hover {
  background: #f1f5f9;
}

.table-item.active {
  background: rgba(102, 126, 234, 0.1);
  color: #667eea;
}

.table-icon {
  color: #667eea;
}

.table-name {
  flex: 1;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.field-count {
  font-size: 12px;
}

.components-list {
  display: flex;
  flex-direction: column;
  gap: 8px;
}

.draggable-component {
  display: flex;
  align-items: center;
  gap: 10px;
  padding: 12px 16px;
  background: #f8fafc;
  border: 2px solid #e2e8f0;
  border-radius: 10px;
  cursor: grab;
  transition: all 0.2s ease;
}

.draggable-component:hover {
  border-color: #667eea;
  transform: translateX(4px);
}

.comp-icon {
  font-size: 18px;
}

.comp-name {
  font-size: 14px;
}

.panel-resizer {
  width: 6px;
  height: 100%;
  position: absolute;
  right: -3px;
  top: 0;
  cursor: col-resize;
  background: transparent;
  transition: background 0.2s ease;
}

.panel-resizer:hover {
  background: #667eea;
}

.canvas-wrapper {
  flex: 1;
  display: flex;
  flex-direction: column;
  overflow: hidden;
}

.canvas-toolbar {
  display: flex;
  justify-content: center;
  align-items: center;
  padding: 8px;
  background: white;
  border-bottom: 1px solid #e2e8f0;
}

.zoom-level {
  min-width: 50px;
  text-align: center;
  font-size: 14px;
  color: #64748b;
}

.canvas-area {
  flex: 1;
  position: relative;
  background:
    linear-gradient(90deg, #e2e8f0 1px, transparent 1px),
    linear-gradient(#e2e8f0 1px, transparent 1px);
  background-size: 20px 20px;
  overflow: auto;
  transform-origin: 0 0;
}

.relationships-layer {
  position: absolute;
  top: 0;
  left: 0;
  width: 100%;
  height: 100%;
  pointer-events: none;
}

.relationship-line {
  cursor: pointer;
  pointer-events: auto;
  transition: stroke-width 0.2s ease;
}

.relationship-line:hover {
  stroke-width: 3;
}

.relationship-label {
  font-size: 12px;
  fill: #667eea;
  cursor: pointer;
  pointer-events: auto;
  text-anchor: middle;
}

.table-card {
  position: absolute;
  width: 220px;
  background: white;
  border-radius: 10px;
  box-shadow: 0 4px 12px rgba(0, 0, 0, 0.1);
  border: 2px solid #e2e8f0;
  cursor: move;
  transition: border-color 0.2s ease, box-shadow 0.2s ease;
}

.table-card:hover {
  border-color: #667eea;
}

.table-card.selected {
  border-color: #667eea;
  box-shadow: 0 4px 16px rgba(102, 126, 234, 0.3);
}

.table-card-header {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 10px 12px;
  background: linear-gradient(135deg, #667eea, #764ba2);
  border-radius: 8px 8px 0 0;
  color: white;
}

.table-card-name {
  flex: 1;
  font-weight: 600;
  font-size: 14px;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.table-menu-btn {
  color: rgba(255, 255, 255, 0.8);
  padding: 2px;
}

.table-menu-btn:hover {
  color: white;
  background: rgba(255, 255, 255, 0.2);
}

.table-card-fields {
  padding: 8px;
}

.table-field-item {
  display: flex;
  align-items: center;
  gap: 8px;
  padding: 6px 4px;
  font-size: 13px;
  border-bottom: 1px solid #f1f5f9;
}

.table-field-item:last-child {
  border-bottom: none;
}

.table-field-item .field-name {
  flex: 1;
  overflow: hidden;
  text-overflow: ellipsis;
  white-space: nowrap;
}

.table-field-item .field-type {
  color: #64748b;
  font-size: 12px;
}

.primary-key-icon {
  color: #f59e0b;
  font-size: 12px;
}

.more-fields {
  padding: 8px;
  text-align: center;
  color: #94a3b8;
  font-size: 12px;
}

.connection-point {
  position: absolute;
  width: 20px;
  height: 20px;
  display: flex;
  align-items: center;
  justify-content: center;
  color: #667eea;
  cursor: crosshair;
  opacity: 0;
  transition: opacity 0.2s ease;
}

.table-card:hover .connection-point {
  opacity: 1;
}

.sql-panel {
  background: white;
  border-left: 1px solid #e2e8f0;
  display: flex;
  flex-direction: column;
  position: relative;
}

.sql-panel-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  border-bottom: 1px solid #e2e8f0;
}

.sql-panel-title {
  display: flex;
  align-items: center;
  gap: 8px;
  font-weight: 600;
}

.sql-panel-content {
  flex: 1;
  overflow: auto;
  padding: 16px;
}

.sql-code {
  margin: 0;
  padding: 16px;
  background: #1e1e1e;
  border-radius: 8px;
  color: #d4d4d4;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  white-space: pre-wrap;
  word-break: break-all;
}

.panel-resizer-horizontal {
  height: 6px;
  width: 100%;
  position: absolute;
  top: -3px;
  cursor: row-resize;
  background: transparent;
  transition: background 0.2s ease;
}

.panel-resizer-horizontal:hover {
  background: #667eea;
}

.table-editor {
  display: flex;
  flex-direction: column;
  gap: 24px;
}

.table-basic-info {
  display: flex;
  flex-wrap: wrap;
  gap: 16px;
}

.fields-editor {
  border: 1px solid #e2e8f0;
  border-radius: 8px;
  overflow: hidden;
}

.fields-header {
  display: flex;
  justify-content: space-between;
  align-items: center;
  padding: 12px 16px;
  background: #f8fafc;
  border-bottom: 1px solid #e2e8f0;
}

.fields-title {
  font-weight: 600;
}

.modal-footer {
  display: flex;
  justify-content: space-between;
  align-items: center;
  margin-top: 24px;
  padding-top: 16px;
  border-top: 1px solid #e2e8f0;
}

.sql-preview-modal {
  margin: 0;
  padding: 20px;
  background: #1e1e1e;
  border-radius: 8px;
  color: #d4d4d4;
  font-family: 'Fira Code', 'Consolas', monospace;
  font-size: 13px;
  line-height: 1.6;
  max-height: 400px;
  overflow: auto;
}

@media (max-width: 1200px) {
  .sql-panel {
    display: none;
  }
}
</style>
