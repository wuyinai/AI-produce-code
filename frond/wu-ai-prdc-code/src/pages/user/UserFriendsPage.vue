<template>
  <div class="user-friends-page">
    <div class="friends-container">
      <!-- 好友功能导航 -->
      <div class="friends-nav">
        <a-menu mode="horizontal" v-model:selectedKeys="selectedKeys">
          <a-menu-item key="list">
            <template #icon>
              <UserOutlined />
            </template>
            好友列表
          </a-menu-item>
          <a-menu-item key="requests">
            <template #icon>
              <MailOutlined />
            </template>
            好友请求
            <a-badge v-if="requestCount > 0" :count="requestCount" color="red" />
          </a-menu-item>
          <a-menu-item key="search">
            <template #icon>
              <SearchOutlined />
            </template>
            搜索用户
          </a-menu-item>
        </a-menu>
      </div>

      <!-- 好友功能内容 -->
      <div class="friends-content">
        <!-- 好友列表 -->
        <div v-if="selectedKeys[0] === 'list'" class="friends-list-section">
          <div class="section-header">
            <h2>我的好友</h2>
            <a-button type="primary" ghost @click="loadFriendList" class="refresh-button">
              <template #icon>
                <ReloadOutlined />
              </template>
              刷新
            </a-button>
          </div>
          <div v-if="loading" class="loading-container">
            <a-spin size="large" />
            <p>加载中...</p>
          </div>
          <div v-else-if="friendList.length === 0" class="empty-container">
            <UserOutlined class="empty-icon" />
            <p>暂无好友</p>
            <a-button type="primary" @click="selectedKeys = ['search']">
              <template #icon>
                <SearchOutlined />
              </template>
              去添加好友
            </a-button>
          </div>
          <div v-else class="friends-list">
            <a-card
              v-for="friend in friendList"
              :key="friend.id"
              class="friend-card"
            >
              <template #cover>
                <img
                  alt="好友头像"
                  :src="friend.userAvatar || defaultAvatar"
                  class="friend-avatar"
                />
              </template>
              <a-card-meta
                :title="friend.userName || '未设置昵称'"
                :description="friend.userProfile || '暂无简介'"
              >
              </a-card-meta>
              <div class="card-actions">
                <a-button type="primary" ghost @click="viewFriendDetail(friend)">
                  <template #icon>
                    <EyeOutlined />
                  </template>
                  查看详情
                </a-button>
                <a-popconfirm
                  title="确定要删除这个好友吗？"
                  @confirm="friend.id && deleteFriend(friend.id)"
                  ok-text="确定"
                  cancel-text="取消"
                >
                  <a-button danger>
                    <template #icon>
                      <DeleteOutlined />
                    </template>
                    删除好友
                  </a-button>
                </a-popconfirm>
              </div>
            </a-card>
          </div>
        </div>

        <!-- 好友请求 -->
        <div v-else-if="selectedKeys[0] === 'requests'" class="friend-requests-section">
          <div class="section-header">
            <h2>好友请求</h2>
            <a-button type="primary" ghost @click="loadReceivedRequests" class="refresh-button">
              <template #icon>
                <ReloadOutlined />
              </template>
              刷新
            </a-button>
          </div>
          <div v-if="loading" class="loading-container">
            <a-spin size="large" />
            <p>加载中...</p>
          </div>
          <div v-else-if="receivedRequests.length === 0" class="empty-container">
            <MailOutlined class="empty-icon" />
            <p>暂无好友请求</p>
          </div>
          <div v-else class="friend-requests-list">
            <a-card
              v-for="request in receivedRequests"
              :key="request.id"
              class="request-card"
            >
              <div class="request-content">
                <a-avatar
                  :src="request.fromUserAvatar || defaultAvatar"
                  class="request-avatar"
                />
                <div class="request-info">
                  <h3 class="request-name">{{ request.fromUserName || request.fromUserAccount }}</h3>
                  <p class="request-message">{{ request.message || '请求添加你为好友' }}</p>
                  <p class="request-time">{{ formatTime(request.createTime) }}</p>
                </div>
                <div class="request-actions">
                  <a-button type="primary" @click="request.id && acceptRequest(request.id)">
                    <template #icon>
                      <CheckCircleOutlined />
                    </template>
                    接受
                  </a-button>
                  <a-button danger @click="request.id && rejectRequest(request.id)">
                    <template #icon>
                      <CloseCircleOutlined />
                    </template>
                    拒绝
                  </a-button>
                </div>
              </div>
            </a-card>
          </div>
        </div>

        <!-- 搜索用户 -->
        <div v-else-if="selectedKeys[0] === 'search'" class="search-users-section">
          <div class="section-header">
            <h2>搜索用户</h2>
          </div>
          <div class="search-container">
            <a-input-search
              v-model:value="searchKeyword"
              placeholder="输入用户名或账号搜索"
              enter-button
              size="large"
              @search="handleSearch"
              class="search-input"
            >
              <template #addonBefore>
                <a-select v-model:value="searchType" style="width: 120px">
                  <a-select-option value="account">账号</a-select-option>
                  <a-select-option value="name">用户名</a-select-option>
                </a-select>
              </template>
            </a-input-search>
          </div>
          <div v-if="loading" class="loading-container">
            <a-spin size="large" />
            <p>搜索中...</p>
          </div>
          <div v-else-if="searchResults.length === 0 && hasSearched" class="empty-container">
            <SearchOutlined class="empty-icon" />
            <p>未找到匹配的用户</p>
          </div>
          <div v-else-if="searchResults.length > 0" class="search-results">
            <a-card
              v-for="user in searchResults"
              :key="user.id"
              class="search-result-card"
            >
              <template #cover>
                <img
                  alt="用户头像"
                  :src="user.userAvatar || defaultAvatar"
                  class="user-avatar"
                />
              </template>
              <a-card-meta
                :title="user.userName || user.userAccount"
                :description="user.userProfile || '暂无简介'"
              >
              </a-card-meta>
              <div class="card-actions">
                <a-button type="primary" @click="user.id && sendFriendRequest(user.id, (user.userName || user.userAccount || ''))">
                  <template #icon>
                    <UserAddOutlined />
                  </template>
                  添加好友
                </a-button>
              </div>
            </a-card>
          </div>
        </div>
      </div>
    </div>
  </div>

  <!-- 添加好友弹窗 -->
  <a-modal
    v-model:open="addFriendModalVisible"
    title="发送好友请求"
    @ok="confirmAddFriend"
    @cancel="addFriendModalVisible = false"
  >
    <div class="add-friend-modal">
      <div class="add-friend-info">
        <a-avatar :size="80" :src="selectedUserAvatar || defaultAvatar" />
        <div class="add-friend-name">{{ selectedUserName }}</div>
      </div>
      <div class="add-friend-message">
        <a-textarea
          v-model:value="friendRequestMessage"
          placeholder="请输入好友请求消息（可选）"
          :rows="4"
        />
      </div>
    </div>
  </a-modal>
</template>

<script setup lang="ts">
import { ref, onMounted, computed } from 'vue'
import { message } from 'ant-design-vue'
import {
  UserOutlined,
  MailOutlined,
  SearchOutlined,
  UserAddOutlined,
  CheckCircleOutlined,
  CloseCircleOutlined,
  EyeOutlined,
  DeleteOutlined,
  ReloadOutlined,
} from '@ant-design/icons-vue'
import {
  getFriendList,
  getReceivedFriendRequests,
  searchUsers,
  sendFriendRequest as sendFriendRequestApi,
  acceptFriendRequest,
  rejectFriendRequest,
  deleteFriend as deleteFriendApi,
} from '@/api/friendController'
import { formatTime } from '@/utils/time'

// 响应式数据
const selectedKeys = ref(['list'])
const friendList = ref<API.FriendVO[]>([])
const receivedRequests = ref<API.FriendRequestVO[]>([])
const searchResults = ref<API.FriendVO[]>([])
const searchKeyword = ref('')
const searchType = ref('account')
const loading = ref(false)
const hasSearched = ref(false)

// 添加好友弹窗相关
const addFriendModalVisible = ref(false)
const selectedUserId = ref<number>()
const selectedUserName = ref('')
const selectedUserAvatar = ref('')
const friendRequestMessage = ref('')

// 默认头像
const defaultAvatar = 'https://gw.alipayobjects.com/zos/rmsportal/BiazfanxmamNRoxxVxka.png'

// 计算属性
const requestCount = computed(() => receivedRequests.value.length)

// 生命周期钩子
onMounted(() => {
  loadFriendData()
})

// 加载好友数据
const loadFriendData = async () => {
  if (selectedKeys.value[0] === 'list') {
    await loadFriendList()
  } else if (selectedKeys.value[0] === 'requests') {
    await loadReceivedRequests()
  }
}

// 加载好友列表
const loadFriendList = async () => {
  loading.value = true
  try {
    const res = await getFriendList()
    if (res.data.code === 0) {
      friendList.value = res.data.data || []
    } else {
      message.error(res.data.message || '获取好友列表失败')
    }
  } catch (error) {
    message.error('获取好友列表失败')
  } finally {
    loading.value = false
  }
}

// 加载收到的好友请求
const loadReceivedRequests = async () => {
  loading.value = true
  try {
    const res = await getReceivedFriendRequests()
    if (res.data.code === 0) {
      receivedRequests.value = res.data.data || []
    } else {
      message.error(res.data.message || '获取好友请求失败')
    }
  } catch (error) {
    message.error('获取好友请求失败')
  } finally {
    loading.value = false
  }
}

// 搜索用户
const handleSearch = async () => {
  if (!searchKeyword.value.trim()) {
    message.warning('请输入搜索关键词')
    return
  }

  loading.value = true
  hasSearched.value = true
  try {
    const res = await searchUsers({ keyword: searchKeyword.value })
    if (res.data.code === 0) {
      searchResults.value = res.data.data || []
    } else {
      message.error(res.data.message || '搜索用户失败')
    }
  } catch (error) {
    message.error('搜索用户失败')
  } finally {
    loading.value = false
  }
}

// 发送好友请求
const sendFriendRequest = (userId: number, userName: string) => {
  selectedUserId.value = userId
  selectedUserName.value = userName
  addFriendModalVisible.value = true
}

// 确认发送好友请求
const confirmAddFriend = async () => {
  if (!selectedUserId.value) {
    return
  }

  try {
    const res = await sendFriendRequestApi({
      toUserId: selectedUserId.value,
      message: friendRequestMessage.value,
    })
    if (res.data.code === 0 && res.data.data) {
      message.success('好友请求发送成功')
      addFriendModalVisible.value = false
      resetAddFriendForm()
    } else {
      message.error(res.data.message || '发送好友请求失败')
    }
  } catch (error) {
    message.error('发送好友请求失败')
  }
}

// 重置添加好友表单
const resetAddFriendForm = () => {
  selectedUserId.value = undefined
  selectedUserName.value = ''
  selectedUserAvatar.value = ''
  friendRequestMessage.value = ''
}

// 接受好友请求
const acceptRequest = async (requestId: number) => {
  try {
    const res = await acceptFriendRequest({ requestId })
    if (res.data.code === 0 && res.data.data) {
      message.success('接受好友请求成功')
      await loadReceivedRequests()
      await loadFriendList()
    } else {
      message.error(res.data.message || '接受好友请求失败')
    }
  } catch (error) {
    message.error('接受好友请求失败')
  }
}

// 拒绝好友请求
const rejectRequest = async (requestId: number) => {
  try {
    const res = await rejectFriendRequest({ requestId })
    if (res.data.code === 0 && res.data.data) {
      message.success('拒绝好友请求成功')
      await loadReceivedRequests()
    } else {
      message.error(res.data.message || '拒绝好友请求失败')
    }
  } catch (error) {
    message.error('拒绝好友请求失败')
  }
}

// 删除好友
const deleteFriend = async (friendId: number) => {
  try {
    const res = await deleteFriendApi({ friendId })
    if (res.data.code === 0 && res.data.data) {
      message.success('删除好友成功')
      await loadFriendList()
    } else {
      message.error(res.data.message || '删除好友失败')
    }
  } catch (error) {
    message.error('删除好友失败')
  }
}

// 查看好友详情
const viewFriendDetail = (friend: API.FriendVO) => {
  // 这里可以跳转到好友详情页面
  message.info(`查看好友${friend.userName}的详情`)
}
</script>

<style scoped>
.user-friends-page {
  min-height: 100vh;
  background-color: #f5f5f5;
  padding: 20px;
}

.friends-container {
  max-width: 1200px;
  margin: 0 auto;
  background-color: white;
  border-radius: 8px;
  box-shadow: 0 2px 8px rgba(0, 0, 0, 0.1);
  overflow: hidden;
}

.friends-nav {
  border-bottom: 1px solid #e8e8e8;
}

.friends-content {
  padding: 20px;
}

.section-header {
  margin-bottom: 20px;
  display: flex;
  justify-content: space-between;
  align-items: center;
}

.section-header h2 {
  margin: 0;
  font-size: 20px;
  font-weight: 600;
}

.refresh-button {
  border-radius: 8px;
  font-weight: 500;
  transition: all 0.3s ease;
}

.loading-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
}

.empty-container {
  display: flex;
  flex-direction: column;
  align-items: center;
  justify-content: center;
  padding: 60px 20px;
  color: #999;
}

.empty-icon {
  font-size: 64px;
  margin-bottom: 16px;
}

/* 好友列表 */
.friends-list {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.friend-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.friend-card .ant-card-cover {
  height: 80px;
  width: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 16px;
}

.friend-avatar {
  width: 100%;
  height: 100%;
  object-fit: cover;
}

.card-actions {
  margin-top: 16px;
  display: flex;
  justify-content: space-between;
  gap: 16px;
}

.friend-card .ant-card-meta {
  text-align: center;
  margin-bottom: 16px;
}

.friend-card .ant-card-meta-title {
  margin-bottom: 8px !important;
  font-size: 16px;
  font-weight: 600;
}

.friend-card .ant-card-meta-description {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

/* 好友请求 */
.friend-requests-list {
  display: flex;
  flex-direction: column;
  gap: 16px;
}

.request-card {
  height: auto;
}

.request-content {
  display: flex;
  align-items: center;
  gap: 16px;
}

.request-avatar {
  width: 64px;
  height: 64px;
}

.request-info {
  flex: 1;
}

.request-name {
  font-size: 16px;
  font-weight: 600;
  margin-bottom: 8px;
}

.request-message {
  margin-bottom: 8px;
  color: #666;
}

.request-time {
  font-size: 12px;
  color: #999;
}

.request-actions {
  display: flex;
  gap: 8px;
}

/* 搜索用户 */
.search-container {
  margin-bottom: 20px;
}

.search-input {
  max-width: 600px;
}

.search-results {
  display: grid;
  grid-template-columns: repeat(auto-fill, minmax(300px, 1fr));
  gap: 20px;
}

.search-result-card {
  height: 100%;
  display: flex;
  flex-direction: column;
  padding: 16px;
}

.search-result-card .ant-card-cover {
  height: 80px;
  width: 80px;
  border-radius: 50%;
  overflow: hidden;
  margin: 0 auto 16px;
}

.search-result-card .ant-card-meta {
  text-align: center;
  margin-bottom: 16px;
}

.search-result-card .ant-card-meta-title {
  margin-bottom: 8px !important;
  font-size: 16px;
  font-weight: 600;
}

.search-result-card .ant-card-meta-description {
  color: #666;
  font-size: 14px;
  line-height: 1.4;
}

/* 添加好友弹窗 */
.add-friend-modal {
  padding: 20px 0;
}

.add-friend-info {
  display: flex;
  flex-direction: column;
  align-items: center;
  margin-bottom: 20px;
}

.add-friend-name {
  margin-top: 12px;
  font-size: 16px;
  font-weight: 600;
}

.add-friend-message {
  margin-top: 20px;
}
</style>
