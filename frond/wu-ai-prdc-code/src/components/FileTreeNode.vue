<template>
  <div class="file-tree-node">
    <div
      class="node-item"
      :class="{ 'is-dir': node.isDir, 'is-selected': selectedFile === node.path }"
      @click="handleClick"
    >
      <span class="node-expand-icon" @click.stop="toggleExpand">
        <FolderOpenOutlined v-if="node.isDir && expanded" />
        <FolderOutlined v-else-if="node.isDir && !expanded" />
        <FileTextOutlined v-else />
      </span>
      <span class="node-name">{{ node.name }}</span>
    </div>
    <div v-if="node.isDir && expanded && node.children" class="node-children">
      <FileTreeNode
        v-for="child in node.children"
        :key="child.path"
        :node="child"
        :selected-file="selectedFile"
        @select="$emit('select', $event)"
      />
    </div>
  </div>
</template>

<script setup lang="ts">
import { ref } from 'vue'
import { FolderOpenOutlined, FolderOutlined, FileTextOutlined } from '@ant-design/icons-vue'

interface Props {
  node: API.SourceCodeFileDTO
  selectedFile?: string
}

const props = defineProps<Props>()
const emit = defineEmits<{
  (e: 'select', file: API.SourceCodeFileDTO): void
}>()

const expanded = ref(false)

const handleClick = () => {
  if (props.node.isDir) {
    toggleExpand()
  } else {
    emit('select', props.node)
  }
}

const toggleExpand = () => {
  if (props.node.isDir) {
    expanded.value = !expanded.value
  }
}
</script>

<style scoped>
.file-tree-node {
  user-select: none;
}

.node-item {
  display: flex;
  align-items: center;
  padding: 6px 12px;
  cursor: pointer;
  transition: background-color 0.2s;
}

.node-item:hover {
  background-color: #f0f0f0;
}

.node-item.is-selected {
  background-color: #e6f7ff;
}

.node-item.is-dir {
  font-weight: 500;
}

.node-expand-icon {
  margin-right: 6px;
  color: #fa8c16;
}

.node-name {
  font-size: 13px;
  color: #333;
  word-break: break-all;
}

.node-children {
  padding-left: 16px;
}
</style>
