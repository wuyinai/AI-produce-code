<template>
  <div id="friendSelector">
    <a-modal
      :open="visibleState"
      title="添加协作者"
      @ok="handleOk"
      @cancel="handleCancel"
      @update:open="handleOpenChange"
      width="800px"
      :ok-text="'邀请'"
      :cancel-text="'取消'"
    >
      <div class="friend-selector-content">
        <!-- 搜索框 -->
        <a-input
          v-model:value="searchKeyword"
          placeholder="搜索好友昵称或账号"
          style="margin-bottom: 16px"
          @input="handleSearch"
        >
          <template #prefix>
            <SearchOutlined />
          </template>
        </a-input>

        <!-- 好友表格 -->
        <a-table
          v-if="filteredFriends.length > 0"
          :columns="columns"
          :data-source="filteredFriends"
          :row-selection="rowSelection"
          :pagination="false"
          :scroll="{ y: 400 }"
          row-key="id"
          bordered
        >
          <template #bodyCell="{ column, record }">
            <template v-if="column.key === 'avatar'">
              <a-avatar :src="record.userAvatar" size="large" />
            </template>
            <template v-else-if="column.key === 'onlineStatus'">
              <a-tag :color="record.onlineStatus === 'online' ? 'green' : 'red'">
                {{ record.onlineStatus === 'online' ? '在线' : '离线' }}
              </a-tag>
            </template>
            <template v-else-if="column.key === 'actions'">
              <a-button type="primary" size="small" @click="inviteSingleFriend(record.id)">
                邀请
              </a-button>
            </template>
          </template>
        </a-table>
        <a-empty v-else>
          <template #description> 没有找到匹配的好友 </template>
        </a-empty>
      </div>
    </a-modal>
  </div>
</template>

<script setup lang="ts">
import { ref, onMounted, watch } from 'vue'
import { message } from 'ant-design-vue'
import { SearchOutlined } from '@ant-design/icons-vue'
import request from '@/request'

// 定义好友类型
interface Friend {
  id: number
  userName: string
  userAccount: string
  userAvatar: string
  onlineStatus: string
}

// Props
const props = defineProps<{
  visible: boolean
  collaborationId: number | null
  appId: number | null
  appName: string | null
}>()

// Emits
const emit = defineEmits<{
  (e: 'update:visible', value: boolean): void
  (e: 'addCollaborators', friendIds: number[]): void
}>()

// 状态
const friends = ref<Friend[]>([])
const filteredFriends = ref<Friend[]>([])
const selectedFriends = ref<number[]>([])
const searchKeyword = ref('')
const loading = ref(false)
const visibleState = ref(props.visible)

// 表格列定义
const columns = [
  {
    title: '头像',
    key: 'avatar',
    width: 80,
    align: 'center',
  },
  {
    title: '昵称',
    dataIndex: 'userName',
    key: 'userName',
    ellipsis: true,
  },
  {
    title: '账号',
    dataIndex: 'userAccount',
    key: 'userAccount',
    ellipsis: true,
  },
  {
    title: '在线状态',
    key: 'onlineStatus',
    width: 100,
    align: 'center',
  },
  {
    title: '操作',
    key: 'actions',
    width: 100,
    align: 'center',
  },
]

// 行选择配置
const rowSelection = {
  selectedRowKeys: selectedFriends,
  onChange: (newSelectedRowKeys: number[]) => {
    selectedFriends.value = newSelectedRowKeys
  },
}

// 计算属性：根据搜索关键词过滤好友列表
const updateFilteredFriends = () => {
  if (!searchKeyword.value) {
    filteredFriends.value = friends.value
  } else {
    const keyword = searchKeyword.value.toLowerCase()
    filteredFriends.value = friends.value.filter(
      (friend) =>
        friend.userName.toLowerCase().includes(keyword) ||
        friend.userAccount.toLowerCase().includes(keyword),
    )
  }
}

// 监听搜索关键词变化
watch(searchKeyword, updateFilteredFriends)

// 监听props.visible变化，更新本地状态
watch(
  () => props.visible,
  (newVal) => {
    visibleState.value = newVal
    if (newVal) {
      getOnlineFriends()
    }
  },
)

// 处理modal状态变化
const handleOpenChange = (open: boolean) => {
  visibleState.value = open
  emit('update:visible', open)
}

// 获取在线好友列表
const getOnlineFriends = async () => {
  loading.value = true
  try {
    const res = await request.get('/collaboration/online-friends')
    if (res.data.code === 0) {
      friends.value = res.data.data
      filteredFriends.value = res.data.data
    } else {
      message.error('获取在线好友失败：' + res.data.message)
    }
  } catch (error) {
    console.error('获取在线好友失败：', error)
    message.error('获取在线好友失败，请重试')
  } finally {
    loading.value = false
  }
}

// 搜索好友
const handleSearch = () => {
  updateFilteredFriends()
}

// 单个邀请好友
const inviteSingleFriend = (friendId: number) => {
  emit('addCollaborators', [friendId])
  emit('update:visible', false)
  selectedFriends.value = []
}

// 确定添加协作者
const handleOk = () => {
  if (selectedFriends.value.length === 0) {
    message.warning('请至少选择一个好友')
    return
  }

  emit('addCollaborators', selectedFriends.value)
  emit('update:visible', false)
  selectedFriends.value = []
}

// 取消添加协作者
const handleCancel = () => {
  emit('update:visible', false)
  selectedFriends.value = []
}

// 初始化
onMounted(() => {
  if (props.visible) {
    getOnlineFriends()
  }
})
</script>

<style scoped>
#friendSelector {
  .friend-selector-content {
    max-height: 500px;
    overflow-y: auto;
  }

  .ant-table-wrapper {
    .ant-table {
      margin-bottom: 0;
    }

    .ant-table-thead > tr > th {
      background-color: #fafafa;
      font-weight: 600;
    }

    .ant-table-tbody > tr:hover > td {
      background-color: #f5f5f5;
    }

    .ant-table-tbody > tr.ant-table-row-selected > td {
      background-color: #e6f7ff;
    }
  }

  .ant-empty {
    margin: 40px 0;
  }
}
</style>
